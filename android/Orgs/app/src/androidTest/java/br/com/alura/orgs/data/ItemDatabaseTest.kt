package br.com.alura.orgs.data

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import br.com.alura.orgs.model.entity.Item
import br.com.alura.orgs.model.mock.mockItems
import br.com.alura.orgs.model.source.ItemDAO
import br.com.alura.orgs.model.source.ItemRoomDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import java.io.IOException


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
    fun setupTestData() = runTest {
        db.clearAllTables()
    }

    @After
    @Throws(IOException::class)
    fun closeDb(){
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun testInsertItemAndRetrieveSuccessfully() = runTest {
        assertEquals(emptyList<Item>(), itemDAO.getItems().first())
        val item = mockItems[0]
        itemDAO.insert(item)
        val firsItem = itemDAO.getItems().first().first()
        val expectedItem = item.copy(id = firsItem.id)
        assertEquals(expectedItem,firsItem)
    }

    @Test
    @Throws(Exception::class)
    fun testInsertDuplicateItemThrowsException() = runTest {
        itemDAO.insert(mockItems[0])
        val item = itemDAO.getItemById(1)
        assertThrows(SQLiteConstraintException::class.java) {
            runBlocking { itemDAO.insert(item) }
        }
    }

    @Test
    @Throws(Exception::class)
    fun testGetItemByIdReturnsCorrectItem() = runTest {
        mockItems.forEach { itemDAO.insert(it) }

        val secondItem = itemDAO.getItemById(2)

        assert(secondItem.id == 2)
        assert(
            compareValuesBy(secondItem, mockItems[1],
                {it.itemName},
                {it.itemDescription},
                {it.itemValue},
                {it.quantityInStock}
            ) == 0
        )
    }

    @Test
    @Throws(Exception::class)
    fun testUpdateItemCorrectly() = runTest {
        itemDAO.insert(mockItems[2])
        val itemBeforeUpdate = itemDAO.getItemById(1).copy(
            itemName = mockItems[1].itemName,
            itemDescription = mockItems[1].itemDescription,
            itemValue = mockItems[1].itemValue,
            quantityInStock = mockItems[1].quantityInStock
        )
        itemDAO.update(itemBeforeUpdate)
        val itemAfterUpdate = itemDAO.getItemById(1)

        assert(itemDAO.getItems().first().size == 1)
        assert(
            compareValuesBy(
                itemAfterUpdate, itemBeforeUpdate,
                {it.itemName},
                {it.itemDescription},
                {it.itemValue},
                {it.quantityInStock}
            ) == 0
        )
    }

    @Test
    @Throws(Exception::class)
    fun testDeleteItemRemovesCorrectly() = runTest {
        val item = mockItems[0]
        itemDAO.insert(item)
        val itemForDelete = itemDAO.getItemById(1)
        assertEquals(1, itemDAO.getItems().first().size)
        itemDAO.delete(itemForDelete)
        assert(itemDAO.getItems().first().isEmpty())
    }


    @Test
    @Throws(Exception::class)
    fun testGetItemsReturnsCorrectlyAllItems() = runTest {
        mockItems.forEach { itemDAO.insert(it) }
        val allItems = itemDAO.getItems().first().map {
            Item(
                itemName = it.itemName,
                itemDescription = it.itemDescription,
                itemValue = it.itemValue,
                quantityInStock = it.quantityInStock
            )
        }
        assert(allItems.size == mockItems.size)
        assert(allItems.containsAll(mockItems))
    }

}
