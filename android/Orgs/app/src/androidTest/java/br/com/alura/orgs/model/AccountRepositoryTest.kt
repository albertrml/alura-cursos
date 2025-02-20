package br.com.alura.orgs.model

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import br.com.alura.orgs.model.mock.mockAccounts
import br.com.alura.orgs.model.repository.AccountRepository
import br.com.alura.orgs.model.source.AccountDAO
import br.com.alura.orgs.model.source.OrgRoomDatabase
import br.com.alura.orgs.utils.data.Authenticate
import br.com.alura.orgs.utils.data.Response
import br.com.alura.orgs.utils.data.SortedAccount.ByUsernameAscending
import br.com.alura.orgs.utils.data.SortedAccount.ByUsernameDescending
import br.com.alura.orgs.utils.exception.AccountException
import br.com.alura.orgs.utils.tools.until
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class  AccountRepositoryTest {

    private lateinit var repository: AccountRepository
    private lateinit var dao: AccountDAO
    private lateinit var db: OrgRoomDatabase
    private val wrongPassword = "A123bcd"
    private val wrongUsername = "Test"
    private val newPassword = "65432A"
    private val expectedAccounts = mockAccounts

    @Before
    fun setup(){
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context,
            OrgRoomDatabase::class.java
        ).build()

        dao = db.accountDao()
        repository = AccountRepository(dao)
    }

    @Before
    fun setupTestData() = runTest {  db.clearAllTables() }

    @After
    fun closeDb(){ db.close() }

    /*** authenticate an account ***/
    @Test
    fun whenAuthenticateAccountIsSuccessful() = runTest {
        val expectedAccount = expectedAccounts.first
        dao.insert(expectedAccount)

        repository.authenticate(expectedAccount.username, expectedAccount.password)
            .until { response -> response is Response.Success }
            .collect{ response ->
                when(response){
                    is Response.Success -> {
                        val returnedAccount = (repository.auth.value as Authenticate.Login).account
                        assertEquals(expectedAccount, returnedAccount)
                    }
                    is Response.Loading -> {
                        assertTrue(repository.auth.value is Authenticate.Logoff)
                    }
                    is Response.Failure -> assert(false)
                }
            }
    }

    @Test
    fun whenAuthenticateWithInvalidCredentialsIsUnsuccessful() = runTest {
        val expectedAccount = expectedAccounts.first
        dao.insert(expectedAccount)
        repository.authenticate(expectedAccount.username, wrongPassword)
            .until { response -> response is Response.Failure }
            .collect{ response ->
                when(response){
                    is Response.Success -> assert(false)
                    is Response.Loading -> {
                        assertTrue(repository.auth.value is Authenticate.Logoff)
                    }
                    is Response.Failure -> {
                        assert(response.exception is AccountException.InvalidCredentials)
                    }
                }
            }

        repository.authenticate(wrongUsername, expectedAccount.password)
            .until { response -> response is Response.Failure }
            .collect{ response ->
                when(response){
                    is Response.Success -> assert(false)
                    is Response.Loading -> {
                        assertTrue(repository.auth.value is Authenticate.Logoff)
                    }
                    is Response.Failure -> {
                        assert(response.exception is AccountException.InvalidCredentials)
                    }
                }
            }
    }

    /*** create an account ***/
    @Test
    fun whenCreateAccountIsSuccessful() = runTest {
        val expectedAccount = expectedAccounts.first
        repository.createAccount(expectedAccount.username, expectedAccount.password)
            .until { response -> response is Response.Success }
            .collect{ response ->
                when(response){
                    is Response.Success -> {
                        assertTrue(dao.isUsernameExist(expectedAccount.username))
                    }
                    is Response.Loading -> assert(true)
                    is Response.Failure -> assert(false)
                }
            }
    }

    @Test
    fun whenCreateAccountWithDuplicateUsernameIsFailure() = runTest {
        val expectedAccount = expectedAccounts.first()
        dao.insert(expectedAccount)
        repository.createAccount(expectedAccount.username, expectedAccount.password)
            .until { response -> response is Response.Failure }
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

    /*** delete an account ***/
    @Test
    fun whenDeleteUnauthenticatedAccountIsUnsuccessful() = runTest {
        val account = expectedAccounts.first()
        dao.insert(account)
        repository.deleteAccount(account)
            .until { response -> response is Response.Failure }
            .collect{ response ->
                when(response){
                    is Response.Success -> assert(false)
                    is Response.Loading -> assert(true)
                    is Response.Failure -> {
                        assert(response.exception is AccountException.AccountIsNotAuthenticated)
                    }
                }
            }
    }

    @Test
    fun whenDeleteWhichBelongsToAnotherUserIsUnsuccessful() = runTest {
        val account = expectedAccounts.first()
        val otherAccount = expectedAccounts[1]

        dao.insert(account)
        repository.authenticate(account.username, account.password).collect()

        combine(
            repository.auth,
            repository.deleteAccount(otherAccount)
        ){ auth, delete ->
            if (auth is Authenticate.Login){
                when(delete){
                    is Response.Success -> assert(false)
                    is Response.Loading -> assert(true)
                    is Response.Failure -> {
                        assert(delete.exception is AccountException.AccountBelongsToAnotherUser)
                    }
                }
            }
            delete
        }
            .until { response -> response is Response.Failure }
            .collect()
    }

    @Test
    fun whenDeleteAuthenticatedAccountIsSuccessful() = runTest {
        val account = mockAccounts.first()

        dao.insert(account)
        repository.authenticate(account.username, account.password).collect()

        combine(
            repository.auth,
            repository.deleteAccount(account)
        ){ auth, delete ->
            if (auth is Authenticate.Logoff){
                when(delete){
                    is Response.Success -> {
                        val isUsernameExist = dao.isUsernameExist(account.username)
                        assertFalse(isUsernameExist)

                        val usernames = dao.getAllUsernames()
                        assertEquals(0,usernames.size)
                    }
                    is Response.Loading -> assert(true)
                    is Response.Failure -> assert(false)
                }
            }
            delete
        }
            .until { response -> response is Response.Success }
            .collect()
    }

    /*** get all registered usernames ***/
    @Test
    fun whenGetAllUsernamesInAscendingSortIsSuccessful() = runTest{
        val ascendingSortedAccount = expectedAccounts
            .sortedBy { it.username }
            .map { it.username }

        expectedAccounts.forEach{ dao.insert(it) }

        repository.getAllUsernames(ByUsernameAscending)
            .until { response -> response is Response.Success }
            .collect{ response ->
                when(response){
                    is Response.Success -> {
                        val usernamesFromDatabase = dao.getAllUsernames()
                        assertEquals(ascendingSortedAccount, usernamesFromDatabase)
                    }
                    is Response.Loading -> assert(true)
                    is Response.Failure -> assert(false)
                }
            }
    }

    @Test
    fun whenGetAllUsernamesInDescendingSortIsSuccessful() = runTest{
        val descendingSortedAccount = expectedAccounts
            .sortedByDescending { it.username }
            .map { it.username }

        expectedAccounts.forEach{ dao.insert(it) }

        repository.getAllUsernames(ByUsernameDescending)
            .until { response -> response is Response.Success }
            .collect{ response ->
                when(response){
                    is Response.Success -> {
                        val usernamesFromDatabase = response.result
                        assertEquals(descendingSortedAccount, usernamesFromDatabase)
                    }
                    is Response.Loading -> assert(true)
                    is Response.Failure -> assert(false)
                }
            }
    }

    /*** is username exists? ***/
    @Test
    fun whenUsernameIsExist() = runTest {
        val registeredUsername = expectedAccounts.first().username
        expectedAccounts.forEach { dao.insert(it) }
        repository.isUsernameExists(registeredUsername)
            .until { response -> response is Response.Success }
            .collect{ response ->
                when(response){
                    is Response.Success -> {
                        val doesItExist = response.result
                        assertTrue(doesItExist)
                    }
                    is Response.Loading -> assert(true)
                    is Response.Failure -> assert(false)

                }
            }
    }

    @Test
    fun whenUsernameIsNotExist() = runTest {
        val unregisteredUsername = wrongUsername
        expectedAccounts.forEach { dao.insert(it) }
        repository.isUsernameExists(unregisteredUsername)
            .until { response -> response is Response.Success }
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
    fun whenLogoutIsSuccessful() = runTest {
        val account = expectedAccounts.first()
        dao.insert(account)

        combine(
            repository.auth,
            repository.authenticate(account.username, account.password)
        ){ authState, authenticate ->
            if (authenticate is Response.Success){
                repository.logout()
            }
            authState to authenticate
        }
            .until {
                val authState = it.first
                val authenticate = it.second
                authState is Authenticate.Logoff && authenticate is Response.Success
            }
            .collectLatest{
                val authState = it.first
                val authenticate = it.second
                if (authState is Authenticate.Logoff && authenticate is Response.Success)
                    assertTrue(true)
            }
    }

    /*** update an account ***/
    @Test
    fun whenUpdateAccountIsSuccessful() = runTest {
        val outdatedAccount = expectedAccounts.first()
        val expectedAccount = expectedAccounts.first().copy(password = newPassword)

        dao.insert(outdatedAccount)
        repository.authenticate(outdatedAccount.username,outdatedAccount.password).collect()

        combine(
            repository.auth,
            repository.updateAccount(expectedAccount)
        ){ auth, updateResponse ->
            if (auth is Authenticate.Login){
                when(updateResponse){
                    is Response.Success -> {
                        assertEquals(expectedAccount, auth.account)
                        assertNotEquals(outdatedAccount, auth.account)
                    }
                    is Response.Loading -> assert(true)
                    is Response.Failure -> assert(false)
                }
            }
            updateResponse
        }
            .until { response -> response is Response.Success }
            .collect()
    }

    @Test
    fun whenUpdateUnauthenticatedAccountIsUnsuccessful() = runTest {
        val outdatedAccount = expectedAccounts.first()
        val expectedAccount = expectedAccounts.first().copy(password = newPassword)

        dao.insert(outdatedAccount)

        repository.updateAccount(expectedAccount)
            .until { response -> response is Response.Failure }
            .collect{ response ->
                when(response){
                    is Response.Success -> assert(false)
                    is Response.Loading -> {
                        assertTrue(repository.auth.value is Authenticate.Logoff)
                    }
                    is Response.Failure -> {
                        assert(response.exception is AccountException.AccountIsNotAuthenticated)
                    }
                }
            }
    }

    @Test
    fun whenUpdateWhichBelongsToAnotherUserIsUnsuccessful() = runTest {
        val myAccount = expectedAccounts.first()
        val otherAccount = expectedAccounts[1]

        dao.insert(myAccount)

        repository.authenticate(myAccount.username,myAccount.password).collect()

        combine(
            repository.auth,
            repository.updateAccount(otherAccount)
        ){ auth, update ->
            if (auth is Authenticate.Login){
                when(update){
                    is Response.Success -> assert(false)
                    is Response.Loading -> assert(true)
                    is Response.Failure -> {
                        assert(update.exception is AccountException.AccountBelongsToAnotherUser)
                    }
                }
            }
            update
        }
            .until { response -> response is Response.Failure }
            .collect()
    }

    @Test
    fun whenUpdateAccountWithTheSameDataIsUnsuccessful() = runTest {
        val outdatedAccount = expectedAccounts.first()
        val updatedAccount = outdatedAccount

        dao.insert(outdatedAccount)
        repository.authenticate(outdatedAccount.username,outdatedAccount.password).collect()

        combine(
            repository.auth,
            repository.updateAccount(updatedAccount)
        ){ auth, update ->
            if (auth is Authenticate.Login){
                when(update){
                    is Response.Success -> assert(false)
                    is Response.Loading -> assert(true)
                    is Response.Failure -> {
                        assert(update.exception is AccountException.AccountIsTheSame)
                    }
                }
            }
            update
        }
            .until { response -> response is Response.Failure }
            .collect()
    }
}