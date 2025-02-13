package br.com.alura.orgs.model


import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import br.com.alura.orgs.model.mock.mockItems
import br.com.alura.orgs.model.repository.ItemRepository
import br.com.alura.orgs.model.source.ItemDAO
import br.com.alura.orgs.model.source.OrgRoomDatabase
import br.com.alura.orgs.utils.data.Response
import br.com.alura.orgs.utils.exception.ItemException
import br.com.alura.orgs.utils.tools.collectUntil
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ItemRepositoryTest {
    private lateinit var repository: ItemRepository
    private lateinit var dao: ItemDAO
    private lateinit var db: OrgRoomDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room
            .inMemoryDatabaseBuilder(context, OrgRoomDatabase::class.java)
            .build()
        dao = db.itemDao()
        repository = ItemRepository(dao)
    }

    @Before
    fun setupTestData() = runTest { db.clearAllTables() }

    @After
    fun tearDown() { db.close() }

    @Test
    fun whenInsertItemIsSuccessful() = runTest {
        repository.insertItem(mockItems[0])
            .collectUntil{ response -> response is Response.Success }
            .collect { response ->
                when (response) {
                    is Response.Success -> {
                        val item = dao.getItemById(1)!!
                        assertEquals(item.itemName, mockItems[0].itemName)
                    }

                    is Response.Loading -> assert(true)
                    is Response.Failure -> assert(false)
                }
            }
    }

    @Test
    fun whenInsertDuplicatedItemIsUnsuccessful() = runTest {
        dao.insert(mockItems[0])
        val item = dao.getItemById(1)!!
        repository.insertItem(item)
            .collectUntil{ response -> response is Response.Success }
            .collect { result ->
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
    fun whenGetAllItemsIsSuccessful() = runTest {
        mockItems.forEach { dao.insert(it) }
        repository.getAllItems()
            .collectUntil{ response -> response is Response.Success }
            .collect { response ->
                when (response) {
                    is Response.Success -> {
                        assertEquals(mockItems.size, response.result.size)
                        assert(
                            response.result
                                .map { it.copy(id = 0) }
                                .containsAll(mockItems)
                        )
                    }

                    is Response.Loading -> assert(true)
                    is Response.Failure -> assert(false)
                }
            }
    }

    @Test
    fun whenDeleteItemRemovesProperly() = runTest {
        val item = mockItems[0]
        dao.insert(item)
        val itemForDelete = dao.getItemById(1)!!

        repository.deleteItem(itemForDelete)
            .collectUntil{ response -> response is Response.Success }
            .collect { response ->
                when (response) {
                    is Response.Success -> {
                        assert(dao.getItems().first().isEmpty())
                    }

                    is Response.Loading -> {}
                    is Response.Failure -> assert(false)
                }
            }
    }

    @Test
    fun whenGetItemByIdIsSuccessful() = runTest {
        dao.insert(mockItems[0])
        repository.getItemById(1)
            .collectUntil{ response -> response is Response.Success }
            .collect { response ->
                when (response) {
                    is Response.Success -> {
                        val item = response.result.copy(id = 0)
                        assertEquals(mockItems[0],item)
                    }
                    is Response.Loading -> assert(true)
                    is Response.Failure -> assert(false)
                }
            }
    }

    @Test
    fun whenGetItemByIdSearchByWrongIdIsUnsuccessful() = runTest {
        repository.getItemById(1)
            .collectUntil{ response -> response is Response.Success }
            .collect { response ->
                when (response) {
                    is Response.Success -> assert(false)

                    is Response.Loading -> {}
                    is Response.Failure -> {
                        assert(response.exception is ItemException.ItemNotFoundException)
                    }
                }
            }
    }

    @Test
    fun whenUpdateItemUpdatesIsSuccessful() = runTest {
        dao.insert(mockItems[0])
        val itemBeforeUpdate = dao.getItems().first().first().copy(
            itemName = mockItems[1].itemName,
            itemDescription = mockItems[1].itemDescription,
            itemValue = mockItems[1].itemValue,
            quantityInStock = mockItems[1].quantityInStock
        )

        repository.updateItem(itemBeforeUpdate)
            .collectUntil{ response -> response is Response.Success }
            .collect {
                when (it) {
                    is Response.Success -> {
                        val itemAfterUpdate = dao.getItemById(1)
                        assertEquals(itemBeforeUpdate, itemAfterUpdate)
                    }

                    is Response.Loading -> assert(true)
                    is Response.Failure -> assert(false)
                }
            }
    }

    @Test
    fun whenUpdateItemWithSameDataDoesNotChangeItem() = runTest {
        dao.insert(mockItems[2])
        val sameItem = dao.getItems().first().first()
        repository.updateItem(sameItem)
            .collectUntil{ response -> response is Response.Success }
            .collect {
                when (it) {
                    is Response.Success -> assert(true)
                    is Response.Loading -> assert(true)
                    is Response.Failure -> assert(false)
                }
            }
    }

}