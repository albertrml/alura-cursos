package br.com.alura.orgs.model


import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import br.com.alura.orgs.model.mock.mockAccounts
import br.com.alura.orgs.model.mock.mockItems
import br.com.alura.orgs.model.repository.ItemRepository
import br.com.alura.orgs.model.source.AccountDAO
import br.com.alura.orgs.model.source.ItemDAO
import br.com.alura.orgs.model.source.OrgRoomDatabase
import br.com.alura.orgs.utils.data.Response
import br.com.alura.orgs.utils.exception.ItemException
import br.com.alura.orgs.utils.tools.until
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ItemRepositoryTest {
    private lateinit var accountDAO: AccountDAO
    private lateinit var repository: ItemRepository
    private lateinit var dao: ItemDAO
    private lateinit var db: OrgRoomDatabase
    private val expectedItems = mockItems
    private val nonExistentUserOwner = "TESTETESTE"

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room
            .inMemoryDatabaseBuilder(context, OrgRoomDatabase::class.java)
            .build()
        dao = db.itemDao()
        accountDAO = db.accountDao()
        repository = ItemRepository(dao)
    }

    @Before
    fun setupTestData() = runTest {
        db.clearAllTables()
        mockAccounts.forEach{ accountDAO.insert(it) }
    }

    @After
    fun tearDown() { db.close() }

    /***  delete ***/
    @Test
    fun whenDeleteItemRemovesProperly() = runTest {
        val item = expectedItems[0]
        dao.insert(item)
        val itemForDelete = dao.getItemById(1)!!

        repository.deleteItem(itemForDelete)
            .until{ response -> response is Response.Success }
            .collect { response ->
                when (response) {
                    is Response.Success -> {
                        assert(dao.getItems().first().isEmpty())
                    }
                    is Response.Loading -> assert(true)
                    is Response.Failure -> assert(false)
                }
            }
    }

    /***  getAllItems ***/
    @Test
    fun whenGetAllItemsIsSuccessful() = runTest {
        expectedItems.forEach { dao.insert(it) }
        repository.getAllItems()
            .until{ response -> response is Response.Success }
            .collect { response ->
                when (response) {
                    is Response.Success -> {
                        val allItems = response.result.map { it.copy(id = 0) }
                        assertEquals(expectedItems.size, allItems.size)
                        assert(allItems.containsAll(expectedItems))
                    }
                    is Response.Loading -> assert(true)
                    is Response.Failure -> assert(false)
                }
            }
    }

    /*** getItemById ***/
    @Test
    fun whenGetItemByIdIsSuccessful() = runTest {
        dao.insert(expectedItems[0])
        repository.getItemById(1)
            .until{ response -> response is Response.Success }
            .collect { response ->
                when (response) {
                    is Response.Success -> {
                        val item = response.result.copy(id = 0)
                        assertEquals(expectedItems[0],item)
                    }
                    is Response.Loading -> assert(true)
                    is Response.Failure -> assert(false)
                }
            }
    }

    @Test
    fun whenGetItemByIdSearchByWrongIdIsUnsuccessful() = runTest {
        repository.getItemById(1)
            .until{ response -> response is Response.Success }
            .collect { response ->
                when (response) {
                    is Response.Success -> assert(false)
                    is Response.Loading -> assert(true)
                    is Response.Failure -> {
                        assert(response.exception is ItemException.ItemNotFoundException)
                    }
                }
            }
    }

    /*** getItemsByUserOwner ***/
    @Test
    fun whenGetItemsByUserOwnerIsSuccessful() = runTest {
        expectedItems.forEach { dao.insert(it) }
        val expectedRichardItems = expectedItems.filter { it.userOwner == "richard" }
        repository.getItemsByUserOwner("richard")
            .until { response -> response is Response.Success }
            .collect{ response ->
                when(response){
                    is Response.Success -> {
                        val richardItems = response.result.map { it.copy(id = 0) }
                        assertEquals(expectedRichardItems.size, richardItems.size)
                        assert(richardItems.containsAll(expectedRichardItems))
                    }
                    is Response.Loading -> assert(true)
                    is Response.Failure -> assert(false)
                }
            }
    }

    @Test
    fun whenGetItemsByNonExistentUserOwnerOrDonNotHaveItemsIsEmpty() = runTest {
        expectedItems.forEach { dao.insert(it) }
        repository.getItemsByUserOwner(nonExistentUserOwner)
            .until { response -> response is Response.Success }
            .collect{ response ->
                when(response){
                    is Response.Success -> assert(response.result.isEmpty())
                    is Response.Loading -> assert(true)
                    is Response.Failure -> assert(false)
                }
            }
    }

    /*** InsertItem ***/
    @Test
    fun whenInsertItemIsSuccessful() = runTest {
        repository.insertItem(expectedItems[0])
            .until{ response -> response is Response.Success }
            .collect { response ->
                when (response) {
                    is Response.Success -> {
                        val item = dao.getItemById(1)!!.copy(id = 0)
                        assertEquals(expectedItems[0], item)
                    }
                    is Response.Loading -> assert(true)
                    is Response.Failure -> assert(false)
                }
            }
    }

    @Test
    fun whenInsertDuplicatedItemIsUnsuccessful() = runTest {
        dao.insert(expectedItems[0])
        val duplicateItem = dao.getItemById(1)!!
        repository.insertItem(duplicateItem)
            .until{ response -> response is Response.Success }
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
    fun whenInsertWithNonExistentUsernameIsUnsuccessful() = runTest {
        repository.insertItem(expectedItems[0].copy(userOwner = nonExistentUserOwner))
            .until{ response -> response is Response.Success }
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

    /*** InsertItem ***/
    @Test
    fun whenUpdateItemUpdatesIsSuccessful() = runTest {
        dao.insert(expectedItems[0])
        val itemBeforeUpdate = dao.getItems().first().first().copy(
            itemName = expectedItems[1].itemName,
            itemDescription = expectedItems[1].itemDescription,
            itemValue = expectedItems[1].itemValue,
            quantityInStock = expectedItems[1].quantityInStock
        )

        repository.updateItem(itemBeforeUpdate)
            .until{ response -> response is Response.Success }
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
        dao.insert(expectedItems[2])
        val sameItem = dao.getItems().first().first()
        repository.updateItem(sameItem)
            .until{ response -> response is Response.Success }
            .collect { response ->
                when (response) {
                    is Response.Success -> assert(true)
                    is Response.Loading -> assert(true)
                    is Response.Failure -> assert(false)
                }
            }
    }

}