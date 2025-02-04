package br.com.alura.orgs.model

import android.content.Context
import android.util.Log
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
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AccountRepositoryTest {

    private lateinit var repository: AccountRepository
    private lateinit var dao: AccountDAO
    private lateinit var db: OrgsRoomDatabase
    private val wrongUsername = "Test"
    private val wrongPassword = "Test123"

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
    fun whenInsertAccountAndRetrieveAllUsernameIsSuccessful() = runTest {
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
            .collectUntil { response -> response is Response.Success }
            .collect{ response ->
                when(response){
                    is Response.Success -> assert(true)
                    is Response.Loading -> assert(true)
                    is Response.Failure -> assert(false)
                }
            }

        repository.account.collectUntil { auth -> auth is Authenticate.Login }
            .collect{ auth ->
                when(auth) {
                    is Authenticate.Login -> {
                        assertEquals(mockAccounts[0], auth.account)
                    }
                    else -> {}
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
        dao.insert(mockAccounts.first())
        repository.updateAccount(mockAccounts.first().copy(password = "65432A"))
            .collectUntil { response -> response is Response.Success }
            .collect{ response ->
                when(response) {
                    is Response.Success -> {
                        val authenticatedAccount = dao
                            .authenticate(mockAccounts[0].username,"65432A")
                        assertNotNull(authenticatedAccount)
                        assertEquals(mockAccounts[0].username,authenticatedAccount!!.username)
                        assertNotEquals(mockAccounts[0].password, authenticatedAccount.password)
                    }
                    is Response.Loading -> assert(true)
                    is Response.Failure -> assert(false)
                }
            }
    }

    @Test
    fun whenUpdatePasswordButAccountDoesNotExistIsSuccessWithoutResult() = runTest{
        repository.updateAccount(mockAccounts.first().copy(password = "65432A"))
            .collectUntil { response -> response is Response.Success }
            .collect{ response ->
                when(response) {
                    is Response.Success -> {
                        val authenticatedAccount = dao
                            .authenticate(mockAccounts[0].username,"65432A")
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
        dao.insert(mockAccounts.first())
        repository.account.take(3)
            .collect{ auth ->
                Log.i("authAccount", auth.toString())
                when(auth){
                    is Authenticate.Login -> {
                        repository.deleteAccount(mockAccounts.first())
                            .collectUntil { response -> response is Response.Success }
                            .collect{ response ->
                                when(response){
                                    is Response.Success -> {
                                        with(mockAccounts[0]){
                                            val authenticatedAccount =
                                                dao.authenticate(username,password)
                                            assertNull(authenticatedAccount)
                                        }
                                    }
                                    is Response.Loading -> assert(true)
                                    is Response.Failure -> assert(false)
                                }
                            }
                    }
                    is Authenticate.Logoff -> {
                        with(mockAccounts.first()){
                            repository.authenticate(username,password)
                                .collectUntil { response -> response is Response.Success }
                                .collect{}
                        }
                    }
                }
            }
    }

}