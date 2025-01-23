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
import br.com.alura.orgs.utils.Response.Failure
import br.com.alura.orgs.utils.Response.Loading
import br.com.alura.orgs.utils.Response.Success
import br.com.alura.orgs.view.home.HomeUiEvent
import br.com.alura.orgs.view.home.HomeViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test

class HomeViewModelTest {
    private lateinit var viewModel: HomeViewModel
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
        viewModel = HomeViewModel(repository)
    }

    @Before
    fun setupTestData() = runTest { db.clearAllTables() }

    @After
    fun tearDown() { db.close() }



    @Test
    fun onDeleteItemDeletesItem() = runTest {
        itemDao.insert(mockItems[0])
        val item = itemDao.getItemById(1)
        viewModel.onEvent(HomeUiEvent.OnDelete(item))

        viewModel.uiState.take(2).collect{ uiState ->
            when(uiState.deleteState){
                is Success -> {
                    val itemFromViewModel = itemDao.getItems().first()
                    assertEquals(emptyList<Item>(), itemFromViewModel)
                }
                is Loading -> assert(true)
                is Failure -> assert(false)
            }
        }
    }

    @Test
    fun fetchAllItemsUpdatesItemsInState() = runTest {
        mockItems.forEach { itemDao.insert(it) }
        viewModel.onEvent(HomeUiEvent.OnFetchAllItems)
        viewModel.uiState.take(2).collect { uiState ->
            when (uiState.fetchAllItemsState) {
                is Success -> {
                    val itemsFromViewModel = (
                            uiState.fetchAllItemsState as Success<List<Item>>
                        ).result
                        .sortedBy { it.id }
                        .map {
                            Item(
                                itemName = it.itemName,
                                itemDescription = it.itemDescription,
                                itemValue = it.itemValue,
                                quantityInStock = it.quantityInStock
                            )
                        }
                    assertEquals(mockItems, itemsFromViewModel)
                }
                is Loading -> assert(true)
                is Failure -> assert(false)
            }
        }
    }

}