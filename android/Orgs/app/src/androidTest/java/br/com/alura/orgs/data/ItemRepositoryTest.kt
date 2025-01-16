package br.com.alura.orgs.data


import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import br.com.alura.orgs.model.entity.Item
import br.com.alura.orgs.model.mock.mockItems
import br.com.alura.orgs.model.repository.ItemRepository
import br.com.alura.orgs.model.source.ItemDAO
import br.com.alura.orgs.model.source.ItemRoomDatabase
import br.com.alura.orgs.utils.Response
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ItemRepositoryTest {
    private lateinit var itemRepository: ItemRepository
    private lateinit var itemDao: ItemDAO
    private lateinit var db: ItemRoomDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room
            .inMemoryDatabaseBuilder(context, ItemRoomDatabase::class.java)
            .build()
        itemDao = db.itemDao()
        itemRepository = ItemRepository(itemDao)
    }

    @Before
    fun setupTestData() = runTest { db.clearAllTables() }

    @After
    fun tearDown() { db.close() }

    @Test
    fun testInsertItemSucceeds() = runTest {
        itemRepository.insertItem(mockItems[0]).collect { result ->
            when (result) {
                is Response.Success -> {
                    val item = itemDao.getItemById(1)
                    assertEquals(item.itemName, mockItems[0].itemName)
                }
                is Response.Loading -> assert(true)
                is Response.Failure -> assert(false)
            }
        }
    }

    @Test
    fun testInsertItemWithNegativeQuantityFail() = runTest {
        val item = mockItems[0].copy(quantityInStock = -1)
        itemRepository.insertItem(item).collect{
            when (it) {
                is Response.Success -> assert(false)
                is Response.Loading -> assert(true)
                is Response.Failure -> assert(true)
            }
        }
    }

    @Test
    fun testInsertItemWithNegativeValueFail() = runTest {
        val item = mockItems[0].copy(itemValue = -1.0)
        itemRepository.insertItem(item).collect{
            when (it) {
                is Response.Success -> assert(false)
                is Response.Loading -> assert(true)
                is Response.Failure -> assert(true)
            }
        }
    }

    @Test
    fun testInsertItemWhenAlreadyExistsThrowsError() = runTest {
        itemDao.insert(mockItems[0])
        val item = itemDao.getItemById(1)
        itemRepository.insertItem(item).collect { result ->
            when (result) {
                is Response.Success -> assert(false)
                is Response.Loading -> assert(true)
                is Response.Failure -> assert(
                    result.exception is SQLiteConstraintException
                )
            }
        }
    }

    @Test
    fun testGetAllItemsReturnsCorrectItems() = runTest {
        mockItems.forEach { itemDao.insert(it) }
        itemRepository.getAllItems().take(2).collect { result ->
            when (result) {
                is Response.Success -> assert(
                    result.result.map {
                        Item(
                            itemName = it.itemName,
                            itemDescription = it.itemDescription,
                            itemValue = it.itemValue,
                            quantityInStock = it.quantityInStock
                        )
                    }.containsAll(mockItems)
                )
                is Response.Loading -> assert(true)
                is Response.Failure -> assert(false)
            }
        }
    }

    @Test
    fun testDeleteItemRemovesItemCorrectly() = runTest {
        val item = mockItems[0]
        itemDao.insert(item)
        val itemForDelete = itemDao.getItemById(1)


        itemRepository.deleteItem(itemForDelete).collect {
            when (it) {
                is Response.Success -> assert(
                    itemDao.getItems().first().isEmpty()
                )

                is Response.Loading -> assert(true)
                is Response.Failure -> assert(false)
            }
        }
    }

    @Test
    fun testUpdateItemUpdatesItemCorrectly() = runTest {
        itemDao.insert(mockItems[0])
        val itemBeforeUpdate = itemDao.getItems().first().first().copy(
            itemName = mockItems[1].itemName,
            itemDescription = mockItems[1].itemDescription,
            itemValue = mockItems[1].itemValue,
            quantityInStock = mockItems[1].quantityInStock
        )
        itemRepository.updateItem(itemBeforeUpdate).collect {
            when (it) {
                is Response.Success -> {
                    val itemAfterUpdate = itemDao.getItemById(1)
                    assertEquals(itemBeforeUpdate, itemAfterUpdate)
                }

                is Response.Loading -> assert(true)
                is Response.Failure -> assert(false)
            }
        }
    }

    @Test
    fun testUpdateItemWithSameDataDoesNotChangeItem() = runTest {
        itemDao.insert(mockItems[2])
        val sameItem = itemDao.getItems().first().first()
        itemRepository.updateItem(sameItem).collect {
            when (it) {
                is Response.Success -> assert(true)
                is Response.Loading -> assert(true)
                is Response.Failure -> assert(false)
            }
        }
    }

    @Test
    fun testUpdateItemWithNegativeValueFails() = runTest {
        itemDao.insert(mockItems[2])
        val updateItemWithNegativeValue = itemDao
            .getItems().first().first().copy(itemValue = -1.0)
        itemRepository.updateItem(updateItemWithNegativeValue).collect {
            when (it) {
                is Response.Success -> assert(false)
                is Response.Loading -> assert(true)
                is Response.Failure -> assert(true)
            }
        }
    }

    @Test
    fun testUpdateItemWithNegativeQuantityFails() = runTest {
        itemDao.insert(mockItems[2])
        val updateItemWithNegativeValue = itemDao
            .getItems().first().first().copy(quantityInStock = -1)
        itemRepository.updateItem(updateItemWithNegativeValue).collect {
            when (it) {
                is Response.Success -> assert(false)
                is Response.Loading -> assert(true)
                is Response.Failure -> assert(true)
            }
        }
    }
}