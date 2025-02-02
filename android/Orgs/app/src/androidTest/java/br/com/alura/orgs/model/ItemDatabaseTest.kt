package br.com.alura.orgs.model

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import br.com.alura.orgs.model.mock.mockItems
import br.com.alura.orgs.model.source.ItemDAO
import br.com.alura.orgs.model.source.ItemRoomDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test


class ItemDatabaseTest {
    private lateinit var itemDAO: ItemDAO
    private lateinit var db: ItemRoomDatabase

    @Before
    fun createDb(){
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            ctx,
            ItemRoomDatabase::class.java
        ).build()
        itemDAO = db.itemDao()
    }

    @Before
    fun setupTestData() = runTest {  db.clearAllTables() }

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
    fun whenGetItemByIdDoesNotFindItem() = runTest {
        val item = itemDAO.getItemById(1)
        assertNull(item)
    }

    @Test
    fun whenInsertDuplicateItemThrowsException() = runTest {
        itemDAO.insert(mockItems[0])
        val item = itemDAO.getItemById(1)!!
        assertThrows(SQLiteConstraintException::class.java) {
            runBlocking { itemDAO.insert(item) }
        }
    }

    @Test
    fun whenUpdateItemUpdatesSuccessfully() = runTest {
        itemDAO.insert(mockItems[2])
        val itemBeforeUpdate = itemDAO.getItemById(1)!!.copy(
            itemName = mockItems[1].itemName,
            itemDescription = mockItems[1].itemDescription,
            itemValue = mockItems[1].itemValue,
            quantityInStock = mockItems[1].quantityInStock
        )
        itemDAO.update(itemBeforeUpdate)
        val itemAfterUpdate = itemDAO.getItemById(1)

        assert(itemDAO.getItems().first().size == 1)
        assertEquals(itemBeforeUpdate, itemAfterUpdate)
    }

    @Test
    fun whenDeleteItemRemovesSuccessfully() = runTest {
        itemDAO.insert(mockItems[0])
        val itemForDelete = itemDAO.getItemById(1)!!
        assertEquals(1, itemDAO.getItems().first().size)
        itemDAO.delete(itemForDelete)
        assert(itemDAO.getItems().first().isEmpty())
    }

    @Test
    fun whenGetItemsRetrievesProperlyAllItems() = runTest {
        mockItems.forEach { itemDAO.insert(it) }
        val allItems = itemDAO.getItems().first().map { it.copy(id = 0) }
        assert(allItems.size == mockItems.size)
        assert(allItems.containsAll(mockItems))
    }

}
