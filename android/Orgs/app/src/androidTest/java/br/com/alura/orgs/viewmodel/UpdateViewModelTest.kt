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
import br.com.alura.orgs.view.home.HomeUiEvent
import br.com.alura.orgs.view.home.HomeViewModel
import br.com.alura.orgs.view.update.UpdateUiEvent
import br.com.alura.orgs.view.update.UpdateViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UpdateViewModelTest {
    private lateinit var viewModel: UpdateViewModel
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
        viewModel = UpdateViewModel(repository)
    }

    @Before
    fun setupTestData() = runTest { db.clearAllTables() }

    @After
    fun tearDown() { db.close() }

    @Test
    fun onFetchItemByIdUpdatesFetchByIdStateInState() = runTest {
        val item = mockItems[0]
        itemDao.insert(item)
        viewModel.onEvent(UpdateUiEvent.OnFetchItemById(1))
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
    fun onUpdateItemSuccess() = runTest {
        val item = mockItems[0]
        itemDao.insert(item)
        val itemBeforeUpdate = itemDao.getItems().first().first().copy(
            itemName = mockItems[1].itemName,
            itemDescription = mockItems[1].itemDescription,
            itemValue = mockItems[1].itemValue,
            quantityInStock = mockItems[1].quantityInStock
        )
        viewModel.onEvent(UpdateUiEvent.OnUpdate(itemBeforeUpdate))
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