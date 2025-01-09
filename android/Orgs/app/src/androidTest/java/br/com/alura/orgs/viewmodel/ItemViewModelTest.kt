package br.com.alura.orgs.viewmodel

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import br.com.alura.orgs.model.entity.Item
import br.com.alura.orgs.model.mock.mockItems
import br.com.alura.orgs.model.repository.ItemRepository
import br.com.alura.orgs.model.source.ItemDAO
import br.com.alura.orgs.model.source.ItemRoomDatabase
import br.com.alura.orgs.utils.Response
import br.com.alura.orgs.view.viemodel.ItemUiEvent
import br.com.alura.orgs.view.viemodel.ItemViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ItemViewModelTest {

    private lateinit var viewModel: ItemViewModel
    private lateinit var repository: ItemRepository
    private lateinit var itemDao: ItemDAO
    private lateinit var db: ItemRoomDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room
            .inMemoryDatabaseBuilder(context, ItemRoomDatabase::class.java)
            .build()
        itemDao = db.itemDao()
        repository = ItemRepository(itemDao)
        viewModel = ItemViewModel(repository)
    }

    @Before
    fun setupTestData() = runTest {
        db.clearAllTables()
    }


    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun onDecreaseQuantityUpdatesItemQuantity() = runTest {
        val item = mockItems[0]
        itemDao.insert(item)

        val itemBeforeDecrease = itemDao.getItemById(1)
        val itemQuantityBeforeDecrease = itemBeforeDecrease.quantityInStock

        viewModel.onEvent(ItemUiEvent.OnDecreaseQuantity(itemBeforeDecrease))
        viewModel.uiState.take(2).collect{ uiState ->
            when(uiState.updateState){
                is Response.Success -> {
                    val itemQuantityAfterDecrease = itemDao
                        .getItemById(1)
                        .quantityInStock

                    assertEquals(
                        itemQuantityBeforeDecrease - 1,
                        itemQuantityAfterDecrease,
                    )
                }
                is Response.Loading -> assert(true)
                is Response.Failure -> assert(false)
            }
        }
    }

    @Test
    fun onIncreaseQuantityUpdatesItemQuantity() = runTest {
        val item = mockItems[0]
        itemDao.insert(item)

        val itemBeforeIncrease = itemDao.getItemById(1)
        val itemQuantityBeforeIncrease = itemBeforeIncrease.quantityInStock

        viewModel.onEvent(ItemUiEvent.OnIncreaseQuantity(itemBeforeIncrease))
        viewModel.uiState.take(2).collect{ uiState ->
            when(uiState.updateState){
                is Response.Success -> {
                    val itemQuantityAfterIncrease = itemDao
                        .getItemById(1)
                        .quantityInStock

                    assertEquals(
                        itemQuantityBeforeIncrease + 1,
                        itemQuantityAfterIncrease,
                    )
                }
                is Response.Loading -> assert(true)
                is Response.Failure -> assert(false)
            }
        }
    }

    @Test
    fun onInsertItemTriggersSuccess() = runTest {}

    @Test
    fun onInsertDuplicateItemTriggersFailure() = runTest {}

    @Test
    fun onDeleteItemDeletesItem() = runTest {}

    @Test
    fun fetchAllItemsUpdatesItemsInState() = runTest {
        mockItems.forEach { itemDao.insert(it) }
        assertEquals(viewModel.uiState.value.items, emptyList<Item>())
        assertEquals(viewModel.uiState.value.fetchAllItemsState, Response.Loading)

        viewModel.onEvent(ItemUiEvent.OnFetchAllItems)
        viewModel.uiState.take(2).collect { uiState ->
            when (uiState.fetchAllItemsState) {
                is Response.Success -> {
                    val itemsFromViewModel = uiState.items.map {
                        Item(
                            itemName = it.itemName,
                            itemDescription = it.itemDescription,
                            itemValue = it.itemValue,
                            quantityInStock = it.quantityInStock
                        )
                    }
                    assertEquals(mockItems, itemsFromViewModel)
                }
                is Response.Loading -> assert(true)
                is Response.Failure -> assert(false)
            }
        }
    }

    @Test
    fun onFetchItemByIdUpdatesFetchByIdStateInState() = runTest {
        val item = mockItems[0]
        itemDao.insert(item)
        viewModel.onEvent(ItemUiEvent.OnFetchItemById(1))
        viewModel.uiState.take(2).collect{ uiState ->
            when(uiState.fetchItemByIdState) {
                is Response.Success -> {
                    val itemFromViewModel = (
                            uiState.fetchItemByIdState as Response.Success<Item>
                        )
                        .result
                        .copy( id = item.id)
                    assertEquals(item, itemFromViewModel)
                }
                is Response.Loading -> assert(true)
                is Response.Failure -> assert(false)
            }
        }
    }

    @Test
    fun onUpdateItemUpdatesItem() = runTest {
        val item = mockItems[0]
        itemDao.insert(item)
        val itemBeforeUpdate = itemDao.getItems().first().first().copy(
            itemName = mockItems[1].itemName,
            itemDescription = mockItems[1].itemDescription,
            itemValue = mockItems[1].itemValue,
            quantityInStock = mockItems[1].quantityInStock
        )
        viewModel.onEvent(ItemUiEvent.OnUpdateItem(itemBeforeUpdate))
        viewModel.uiState.take(2).collect { uiState ->
            when (uiState.updateState) {
                is Response.Success -> {
                    val itemAfterUpdate = itemDao.getItemById(1)
                    assertEquals(itemBeforeUpdate, itemAfterUpdate)
                }
                is Response.Loading -> assert(true)
                is Response.Failure -> assert(false)
            }
        }
    }
}