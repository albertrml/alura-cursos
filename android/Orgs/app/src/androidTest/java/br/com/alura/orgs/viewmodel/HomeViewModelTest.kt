package br.com.alura.orgs.viewmodel

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import br.com.alura.orgs.domain.HomeItemUiUseCase
import br.com.alura.orgs.model.entity.Item
import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.model.mock.mockItems
import br.com.alura.orgs.model.repository.ItemRepository
import br.com.alura.orgs.model.source.ItemDAO
import br.com.alura.orgs.model.source.ItemRoomDatabase
import br.com.alura.orgs.utils.data.Response.Failure
import br.com.alura.orgs.utils.data.Response.Loading
import br.com.alura.orgs.utils.data.Response.Success
import br.com.alura.orgs.utils.data.SortType
import br.com.alura.orgs.utils.tools.collectUntil
import br.com.alura.orgs.viewmodel.home.HomeUiEvent
import br.com.alura.orgs.viewmodel.home.HomeViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class HomeViewModelTest {
    private lateinit var viewModel: HomeViewModel
    private lateinit var homeItemUiUseCase: HomeItemUiUseCase
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
        homeItemUiUseCase = HomeItemUiUseCase(repository)
        viewModel = HomeViewModel(homeItemUiUseCase)
    }

    @Before
    fun setupTestData() = runTest { db.clearAllTables() }

    @After
    fun tearDown() { db.close() }


    @Test
    fun onDeleteItemDeletesItem() = runTest {
        itemDao.insert(mockItems[0])
        val itemUi = ItemUi.fromItem(itemDao.getItemById(1)!!)

        viewModel.onEvent(HomeUiEvent.OnDelete(itemUi))
        viewModel.uiState
            .collectUntil { uiState -> uiState.deleteState is Success }
            .collect { uiState ->
                when (uiState.deleteState) {
                    is Success -> {
                        val itemsFromViewModel = itemDao.getItems().first()
                        assertEquals(emptyList<Item>(), itemsFromViewModel)
                    }

                    is Loading -> assert(true)
                    is Failure -> assert(false)
                }
            }
    }

    @Test
    fun fetchAllItemsUpdatesItemsInState() = runTest {
        mockItems.forEach { itemDao.insert(it) }

        viewModel.onEvent(HomeUiEvent.OnFetchAllItems(SortType.ByIdAscending))
        viewModel.uiState
            .collectUntil { uiState -> uiState.fetchAllItemsState is Success }
            .collect { uiState ->
                when (uiState.fetchAllItemsState) {
                    is Success -> {
                        val itemsFromViewModel =
                            (uiState.fetchAllItemsState as Success<List<ItemUi>>).result
                                .map { it.copy(id = 0) }

                        val expectedItems = mockItems.map { ItemUi.fromItem(it) }
                        assertEquals(expectedItems.size, itemsFromViewModel.size)
                        assertEquals(expectedItems, itemsFromViewModel)
                    }

                    is Loading -> assert(true)
                    is Failure -> assert(false)
                }
            }
    }

}