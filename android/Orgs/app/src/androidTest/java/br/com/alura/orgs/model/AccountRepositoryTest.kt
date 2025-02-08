package br.com.alura.orgs.model

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import br.com.alura.orgs.model.mock.mockAccounts
import br.com.alura.orgs.model.repository.AccountRepository
import br.com.alura.orgs.model.source.AccountDAO
import br.com.alura.orgs.model.source.OrgsRoomDatabase
import br.com.alura.orgs.utils.data.Authenticate
import br.com.alura.orgs.utils.data.Response
import br.com.alura.orgs.utils.exception.AccountException
import br.com.alura.orgs.utils.tools.collectUntil
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class  AccountRepositoryTest {

    private lateinit var repository: AccountRepository
    private lateinit var dao: AccountDAO
    private lateinit var db: OrgsRoomDatabase
    private val wrongUsername = "Test#"
    private val wrongPassword = "Test"
    private val newPassword = "65432A"

    @Before
    fun setup(){
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context,
            OrgsRoomDatabase::class.java
        ).build()

        dao = db.accountDao()
        repository = AccountRepository(dao)
    }

    @Before
    fun setupTestData() = runTest {  db.clearAllTables() }

    @After
    fun closeDb(){ db.close() }

    @Test
    fun whenCreateAccountAndRetrieveAllUsernameIsSuccessful() = runTest {
        repository.createAccount(mockAccounts[0].username, mockAccounts[0].password)
            .collectUntil { response -> response is Response.Success }
            .collect{ response ->
                when(response){
                    is Response.Success -> {
                        val accountsFromDatabase = dao.getAllUsernames().first()
                        assert(mockAccounts.map { it.username }.contains(accountsFromDatabase))
                    }
                    is Response.Loading -> assert(true)
                    is Response.Failure -> assert(false)
                }
            }
    }

    @Test
    fun whenCreateAccountWithWrongUsernameIsFailure() = runTest {
        val account = mockAccounts.first()
        repository.createAccount(wrongUsername, account.password)
            .collectUntil { response -> response is Response.Failure }
            .collect {
                when (it) {
                    is Response.Success -> assert(false)
                    is Response.Loading -> assert(true)
                    is Response.Failure -> {
                        assert(it.exception is AccountException.InvalidUsername)
                    }
                }
            }
    }

    @Test
    fun whenCreateAccountWithWrongPasswordIsFailure() = runTest {
        val account = mockAccounts.first()
        repository.createAccount(account.username, wrongPassword)
            .collectUntil { response -> response is Response.Failure }
            .collect {
                when (it) {
                    is Response.Success -> assert(false)
                    is Response.Loading -> assert(true)
                    is Response.Failure -> {
                        assert(it.exception is AccountException.InvalidPassword)
                    }
                }
            }
    }

    @Test
    fun whenCreateAccountWithDuplicateUsernameIsFailure() = runTest {
        val account = mockAccounts.first()
        dao.insert(account)
        repository.createAccount(account.username, account.password)
            .collectUntil { response -> response is Response.Failure }
            .collect {
                when (it) {
                    is Response.Success -> assert(false)
                    is Response.Loading -> assert(true)
                    is Response.Failure -> {
                        assert(it.exception is AccountException.UsernameAlreadyExists)
                    }
                }
            }
    }

    @Test
    fun whenGetAllUsernamesIsSuccessful() = runTest{
        mockAccounts.forEach { dao.insert(it) }
        repository.getAllUsernames()
            .collectUntil { response -> response is Response.Success }
            .collect{ response ->
                when(response){
                    is Response.Success -> {
                        val usernamesFromDatabase = dao.getAllUsernames()
                        val usernamesFromMock = mockAccounts.map { it.username }.sorted()
                        assertEquals(usernamesFromMock, usernamesFromDatabase)
                    }
                    is Response.Loading -> assert(true)
                    is Response.Failure -> assert(false)
                }
            }
    }

    @Test
    fun whenAuthenticateAccountIsSuccessful() = runTest {
        mockAccounts.forEach { dao.insert(it) }
        repository.authenticate(mockAccounts[0].username, mockAccounts[0].password)
            .collectLatest { response ->
                when (response) {
                    is Response.Success -> assert(true)
                    is Response.Loading -> assert(true)
                    is Response.Failure -> assert(false)
                }
            }

        repository.auth
            .collectUntil { auth -> auth is Authenticate.Login }
            .collectLatest { auth ->
                when (auth) {
                    is Authenticate.Login -> assertEquals(mockAccounts[0], auth.account)
                    is Authenticate.Logoff -> assert(false)
                }
            }
    }

    @Test
    fun whenAuthenticateAccountIsFailure() = runTest {
        mockAccounts.forEach { dao.insert(it) }
        repository.authenticate(wrongUsername, wrongPassword)
            .collectUntil { response -> response is Response.Failure }
            .collect{ response ->
                when(response){
                    is Response.Success -> assert(false)
                    is Response.Loading -> assert(true)
                    is Response.Failure -> {
                        assert(response.exception is AccountException.InvalidCredentials)
                    }
                }
            }
    }

    @Test
    fun whenSearchForAccountIsSuccessful() = runTest {
        mockAccounts.forEach { dao.insert(it) }
        repository.isUsernameExists(mockAccounts[0].username)
            .collectUntil { response -> response is Response.Success }
            .collect{ response ->
                when(response){
                    is Response.Success -> {
                        val doesItExist = response.result
                        assert(doesItExist)
                    }
                    is Response.Loading -> assert(true)
                    is Response.Failure -> assert(false)

                }
            }
    }

    @Test
    fun whenSearchForAccountIsUnsuccessful() = runTest {
        mockAccounts.forEach { dao.insert(it) }
        repository.isUsernameExists(wrongUsername)
            .collectUntil { response -> response is Response.Success }
            .collect{ response ->
                when(response){
                    is Response.Success -> {
                        val doesItNotExist = response.result
                        assertFalse(doesItNotExist)
                    }
                    is Response.Loading -> assert(true)
                    is Response.Failure -> assert(false)
                }
            }
    }

    @Test
    fun whenUpdatePasswordFromAccountIsSuccessful() = runTest {
        val expectedAccount = mockAccounts.first()
        dao.insert(expectedAccount)

        repository.authenticate(expectedAccount.username,expectedAccount.password).collect()

        repository.updatePassword(newPassword = newPassword)
            .collectUntil { response -> response is Response.Success }
            .collect { response ->
                when(response){
                    is Response.Success -> {
                        val authenticatedAccount = dao
                            .authenticate(expectedAccount.username,newPassword)
                        assertNotNull(authenticatedAccount)
                        assertEquals(expectedAccount.username,authenticatedAccount!!.username)
                        assertEquals(newPassword, authenticatedAccount.password)
                    }
                    is Response.Loading -> assert(true)
                    is Response.Failure -> assert(false)
                }
            }
    }

    @Test
    fun whenUpdatePasswordIsNotValid() = runTest {
        val account = mockAccounts.first()
        dao.insert(account)
        repository.authenticate(account.username, account.password).collect()
        repository.updatePassword(newPassword = wrongPassword)
            .collectUntil { response -> response is Response.Failure }
            .collect{ response ->
                when(response){
                    is Response.Success -> assert(false)
                    is Response.Loading -> assert(true)
                    is Response.Failure -> {
                        assert(response.exception is AccountException.InvalidPassword)
                    }
                }
            }
    }


    @Test
    fun whenUpdatePasswordButAccountIsNotAuthenticatedIsFailure() = runTest {
        val account = mockAccounts.first()
        dao.insert(account)

        if (repository.auth.value is Authenticate.Logoff){
            repository.updatePassword(newPassword = newPassword)
                .collectUntil { response -> response is Response.Failure }
                .collect { response ->
                    when (response) {
                        is Response.Success -> {}
                        is Response.Loading -> {}
                        is Response.Failure -> {
                            assert(response.exception is AccountException.AccountIsNotAuthenticated)
                        }
                    }
                }
        }

    }

    @Test
    fun whenUpdatePasswordButAccountDoesNotExistIsSuccessWithoutResult() = runTest{
        repository.updateAccount(mockAccounts.first().copy(password = newPassword))
            .collectUntil { response -> response is Response.Success }
            .collect{ response ->
                when(response) {
                    is Response.Success -> {
                        val authenticatedAccount = dao
                            .authenticate(mockAccounts[0].username,newPassword)
                        assertNull(authenticatedAccount)
                    }
                    is Response.Loading -> {}
                    is Response.Failure -> assert(false)
                }
            }
    }

    @Test
    fun whenDeleteUnauthenticatedAccountIsSuccessful() = runTest {
        dao.insert(mockAccounts.first())
        repository.deleteAccount(mockAccounts.first())
            .collectUntil { response -> response is Response.Success }
            .collect{ response ->
                when(response){
                    is Response.Success -> {
                        val authenticatedAccount = dao
                            .authenticate(mockAccounts[0].username,mockAccounts[0].password)
                        assertNull(authenticatedAccount)
                    }
                    is Response.Loading -> assert(true)
                    is Response.Failure -> assert(false)
                }
            }
    }

    @Test
    fun whenDeleteAuthenticatedAccountIsSuccessful() = runTest {
        val accountForDelete = mockAccounts.first()
        dao.insert(accountForDelete)
        repository.authenticate(accountForDelete.username, accountForDelete.password).collect()
        repository.deleteAccount(accountForDelete)
            .collectUntil { response -> response is Response.Success }
            .collect{ response ->
                when(response){
                    is Response.Success -> {
                        val authenticatedAccount =
                            dao.authenticate(accountForDelete.username,accountForDelete.password)
                        assertNull(authenticatedAccount)
                    }
                    is Response.Loading -> assert(true)
                    is Response.Failure -> assert(false)
                }
            }
    }

}