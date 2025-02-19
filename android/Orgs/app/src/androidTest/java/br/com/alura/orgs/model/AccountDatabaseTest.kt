package br.com.alura.orgs.model

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import br.com.alura.orgs.model.mock.mockAccounts
import br.com.alura.orgs.model.mock.mockItems
import br.com.alura.orgs.model.source.AccountDAO
import br.com.alura.orgs.model.source.ItemDAO
import br.com.alura.orgs.model.source.OrgRoomDatabase
import br.com.alura.orgs.utils.tools.collectUntil
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AccountDatabaseTest {

    private lateinit var itemDao: ItemDAO
    private lateinit var dao: AccountDAO
    private lateinit var db: OrgRoomDatabase
    private val wrongUsername = "Test"
    private val wrongPassword = "Test123"
    private val newPassword = "65432A"
    private val expectedAccounts = mockAccounts


    @Before
    fun setup(){
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context = context,
            klass = OrgRoomDatabase::class.java
        ).build()
        dao = db.accountDao()
        itemDao = db.itemDao()
    }

    @Before
    fun setupTestData() = runTest {  db.clearAllTables() }

    @After
    fun closeDb(){ db.close() }

    /*** Insert and Check If Account Exists ***/

    @Test
    fun whenInsertAndCheckIfAccountExists() = runTest {
        val expectedAccount = expectedAccounts.first
        dao.insert(expectedAccount)
        assertTrue(
            dao.isUsernameExist(expectedAccount.username)
        )
        assertFalse(
            dao.isUsernameExist(wrongUsername)
        )
    }

    @Test
    fun whenInsertDuplicateAccount() = runTest {
        val expectedAccount = expectedAccounts.first
        val duplicateAccount = expectedAccount.copy()
        dao.insert(expectedAccount)
        assertThrows(SQLiteConstraintException::class.java){
            runBlocking { dao.insert(duplicateAccount) }
        }
    }

    /*** Authentication Account ***/
    @Test
    fun whenAuthenticateWithCorrectCredentials() = runTest {
        val expectedAccount = expectedAccounts.first
        dao.insert(expectedAccount)

        val authenticatedAccount = expectedAccount?.run {
            dao.authenticate(username, password)
        } ?: assert(false)

        assertEquals(expectedAccount, authenticatedAccount)
    }

    @Test
    fun whenAuthenticateWithWrongCredentials() = runTest {
        val expectedAccount = expectedAccounts.first
        dao.insert(expectedAccount)

        assertNull(
            dao.authenticate(wrongUsername, wrongPassword)
        )

        assertNull(
            dao.authenticate(expectedAccount.username, wrongPassword)
        )

        assertNull(
            dao.authenticate(wrongUsername, expectedAccount.password)
        )
    }

    /*** Get All Usernames ***/
    @Test
    fun whenGetAllUsernamesIsSuccessful() = runTest {
        expectedAccounts.forEach { dao.insert(it) }
        val usernames = dao.getAllUsernames()
        assertEquals(usernames.size, expectedAccounts.size)
        assert(
            usernames.containsAll(expectedAccounts.map { it.username })
        )
    }

    /*** Update Account ***/
    @Test
    fun whenUpdateAccountARegisteredAccountIsSuccessful() = runTest{
        val account = expectedAccounts.first
        val updatedAccount = account.copy(password = newPassword)
        dao.insert(account)
        dao.update(updatedAccount)
        val accountFromDatabase = updatedAccount.run {
            dao.authenticate(username, password)
        }
        assertEquals(updatedAccount, accountFromDatabase)
        assertNotEquals(account, accountFromDatabase)
    }

    @Test
    fun whenUpdateAccountARegisteredAccountWithTheSameCredentialsIsSuccessful() = runTest{
        val expectedAccount = expectedAccounts.first
        dao.insert(expectedAccount)
        dao.update(expectedAccount)
        val accountFromDatabase = expectedAccount.run {
            dao.authenticate(username, password)
        }
        assertEquals(expectedAccount, accountFromDatabase)
    }

    @Test
    fun whenUpdateAccountAUnregisteredAccountThrowsException() = runTest {
        val unregisteredAccount = expectedAccounts.first
        dao.update(unregisteredAccount)

        unregisteredAccount.run {
            assertNull(dao.authenticate(username, password))
            assertFalse(dao.isUsernameExist(username))
        }

        dao.insert(unregisteredAccount)
        val otherUnregisteredAccount = unregisteredAccount.copy(username = wrongUsername)

        otherUnregisteredAccount.let {
            dao.update(it)
            assertNull(dao.authenticate(it.username, it.password))
            assertFalse(dao.isUsernameExist(it.username))
        }
    }

    /*** Delete Account ***/
    @Test
    fun whenDeleteAccountIsSuccessful() = runTest {
        val expectedAccount = expectedAccounts.first
        dao.insert(expectedAccount)

        val itemsToInsert = mockItems.filter { it.userOwner == expectedAccount.username }
        itemsToInsert.forEach { itemDao.insert(it) }

        itemDao.getItemsByUserOwner(expectedAccount.username)
            .collectUntil { items -> items.size == itemsToInsert.size  }
            .collectLatest { items ->
                val itemsFromDatabase = items.map { it.copy(id = 0) }
                assertEquals(itemsToInsert.size, items.size)
                assert(itemsFromDatabase.containsAll(itemsToInsert))
            }

        dao.delete(expectedAccount)

        expectedAccount.run {
            assertNull(dao.authenticate(username, password))
            assertFalse(dao.isUsernameExist(username))

            itemDao.getItemsByUserOwner(username)
                .collectUntil { items -> items.isEmpty()  }
                .collectLatest { items ->
                    assertTrue(items.isEmpty())
                }
        }
    }

}