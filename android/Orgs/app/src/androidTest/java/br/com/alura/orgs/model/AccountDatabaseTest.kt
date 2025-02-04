package br.com.alura.orgs.model

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import br.com.alura.orgs.model.entity.Account
import br.com.alura.orgs.model.mock.mockAccounts
import br.com.alura.orgs.model.source.AccountDAO
import br.com.alura.orgs.model.source.OrgsRoomDatabase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AccountDatabaseTest {

    private lateinit var dao: AccountDAO
    private lateinit var db: OrgsRoomDatabase
    private val wrongUsername = "Test"
    private val wrongPassword = "Test123"
    private val newPassword = "65432A"

    @Before
    fun setup(){
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context = context,
            klass = OrgsRoomDatabase::class.java
        ).build()
        dao = db.accountDao()
    }

    @Before
    fun setupTestData() = runTest {  db.clearAllTables() }

    @After
    fun closeDb(){ db.close() }

    @Test
    fun whenInsertAccountAndRetrieveAllUsernameIsSuccessful() = runTest {
        mockAccounts.forEach { dao.insert(it) }
        val accountsFromDatabase = dao.getAllUsernames()
        assert(accountsFromDatabase.size == mockAccounts.size)
        accountsFromDatabase.forEach { account ->
            assert(mockAccounts.map { it.username }.contains(account))
        }
    }

    @Test
    fun whenSearchForUsernameAndDoesExist() = runTest{
        mockAccounts.forEach { dao.insert(it) }
        assert(dao.isUsernameExist(mockAccounts[0].username))
    }

    @Test
    fun whenSearchForUsernameAndDoesNotExist() = runTest{
        mockAccounts.forEach { dao.insert(it) }
        assert(!dao.isUsernameExist(wrongUsername))
    }

    @Test
    fun whenAuthenticateWithCorrectPassword() = runTest{
        mockAccounts.forEach { dao.insert(it) }
        val authenticatedAccount = dao
            .authenticate(mockAccounts[0].username, mockAccounts[0].password)
        assertEquals(authenticatedAccount, mockAccounts[0])
    }

    @Test
    fun whenAuthenticateWithWrongPassword() = runTest{
        mockAccounts.forEach { dao.insert(it) }
        val authenticatedAccount = dao.authenticate(mockAccounts[0].username, wrongPassword)
        assertNull(authenticatedAccount)
    }

    @Test
    fun whenUpdateAccountIsSuccessful() = runTest{
        mockAccounts.forEach { dao.insert(it) }
        val accountForUpdate = mockAccounts[0].copy(password = newPassword)
        dao.update(accountForUpdate)
        with(accountForUpdate){
            val updatedAccount = dao.authenticate(username, newPassword)
            assertEquals(updatedAccount, accountForUpdate)
            assertNotEquals(updatedAccount, mockAccounts[0])
        }
    }

    @Test
    fun whenDeleteAccountIsSuccessful() = runTest {
        mockAccounts.forEach { dao.insert(it) }
        val accountForDelete = mockAccounts[0]
        assert(dao.isUsernameExist(accountForDelete.username))
        dao.delete(accountForDelete)
        assertNull(dao.authenticate(accountForDelete.username, accountForDelete.password))
    }

    @Test
    fun whenDeleteAccountIsUnsuccessful() = runTest {
        mockAccounts.forEach { dao.insert(it) }
        val accountForDelete = Account(username = wrongUsername, password = wrongPassword)
        dao.delete(accountForDelete)
        assertEquals(dao.getAllUsernames().size, mockAccounts.size)
    }

}