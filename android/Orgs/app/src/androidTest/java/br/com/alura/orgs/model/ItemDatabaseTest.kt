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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test


class ItemDatabaseTest {
    private lateinit var accountDao: AccountDAO
    private lateinit var itemDAO: ItemDAO
    private lateinit var db: OrgRoomDatabase
    private val expectedItems = mockItems
    private val nonExistentUserOwner = "TESTETESTE"

    @Before
    fun createDb(){
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            ctx,
            OrgRoomDatabase::class.java
        ).build()
        itemDAO = db.itemDao()
        accountDao = db.accountDao()
    }

    @Before
    fun setupTestData() = runTest {
        db.clearAllTables()
        mockAccounts.forEach{ accountDao.insert(it) }
    }

    @After
    fun closeDb(){ db.close() }

    @Test
    fun whenInsertItemIsSuccessful() = runTest {
        itemDAO.insert(mockItems[0])

        val itemFromDatabase = itemDAO.getItemById(1)
        assertNotNull(itemFromDatabase)

        val expectedItem = mockItems[0].copy(id = itemFromDatabase!!.id)
        assertEquals(expectedItem,itemFromDatabase)
    }

    @Test
    fun whenGetItemByIdFindItem() = runTest {
        itemDAO.insert(expectedItems[0])
        val item = itemDAO.getItemById(1)
        assertNotNull(item)
    }

    @Test
    fun whenGetItemByIdDoesNotFindItem() = runTest {
        val item = itemDAO.getItemById(1)
        assertNull(item)
    }

    @Test
    fun whenGetItemsRetrievesProperlyAllItems() = runTest {
        expectedItems.forEach { itemDAO.insert(it) }
        val allItems = itemDAO.getItems().first().map { it.copy(id = 0) }
        assert(allItems.size == expectedItems.size)
        assert(allItems.containsAll(expectedItems))
    }

    @Test
    fun whenGetItemsByUserOwnerRetrievesProperlyAllItems() = runTest {
        val expectedRichardItems = expectedItems.filter { it.userOwner == "richard" }
        expectedItems.forEach { itemDAO.insert(it) }
        val richardItems = itemDAO
            .getItemsByUserOwner("richard")
            .first().map { it.copy(id = 0) }
        assertEquals(expectedRichardItems.size, richardItems.size)
        assert(richardItems.containsAll(expectedRichardItems))
    }

    @Test
    fun whenGetItemsByWrongUserOwnerRetrievesEmpty() = runTest {
        expectedItems.forEach { itemDAO.insert(it) }
        val items = itemDAO
            .getItemsByUserOwner(nonExistentUserOwner)
            .first().map { it.copy(id = 0) }
        assert(items.isEmpty())
    }

    @Test
    fun whenInsertDuplicateItemThrowsException() = runTest {
        itemDAO.insert(expectedItems[0])
        val item = itemDAO.getItemById(1)!!
        assertThrows(SQLiteConstraintException::class.java) {
            runBlocking { itemDAO.insert(item) }
        }
    }

    @Test
    fun whenInsertItemWithNonExistentUserOwnerThrowsException() = runTest {
        assertThrows(SQLiteConstraintException::class.java) {
            runBlocking { itemDAO.insert(expectedItems[0].copy(userOwner = nonExistentUserOwner)) }
        }
    }

    @Test
    fun whenUpdateItemUpdatesSuccessfully() = runTest {
        itemDAO.insert(expectedItems[2])
        val itemBeforeUpdate = itemDAO.getItemById(1)!!.copy(
            itemName = expectedItems[1].itemName,
            itemDescription = expectedItems[1].itemDescription,
            itemValue = expectedItems[1].itemValue,
            quantityInStock = expectedItems[1].quantityInStock
        )
        itemDAO.update(itemBeforeUpdate)
        val itemAfterUpdate = itemDAO.getItemById(1)

        assert(itemDAO.getItems().first().size == 1)
        assertEquals(itemBeforeUpdate, itemAfterUpdate)
        assertNotEquals(expectedItems[2], itemAfterUpdate!!.copy(id = 0))
    }

    @Test
    fun whenDeleteItemRemovesSuccessfully() = runTest {
        itemDAO.insert(expectedItems[0])
        val itemForDelete = itemDAO.getItemById(1)!!
        assertEquals(1, itemDAO.getItems().first().size)
        itemDAO.delete(itemForDelete)
        assert(itemDAO.getItems().first().isEmpty())
    }

}
