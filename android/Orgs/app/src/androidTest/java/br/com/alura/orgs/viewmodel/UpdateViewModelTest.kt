package br.com.alura.orgs.viewmodel

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import br.com.alura.orgs.domain.UpdateItemUiUseCase
import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.model.mock.mockItems
import br.com.alura.orgs.model.repository.ItemRepository
import br.com.alura.orgs.model.source.ItemDAO
import br.com.alura.orgs.model.source.ItemRoomDatabase
import br.com.alura.orgs.utils.data.Response
import br.com.alura.orgs.utils.tools.collectUntil
import br.com.alura.orgs.viewmodel.update.UpdateUiEvent
import br.com.alura.orgs.viewmodel.update.UpdateViewModel
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UpdateViewModelTest {
    private lateinit var viewModel: UpdateViewModel
    private lateinit var updateItemUiUseCase: UpdateItemUiUseCase
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
        updateItemUiUseCase = UpdateItemUiUseCase(repository)
        viewModel = UpdateViewModel(updateItemUiUseCase)
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
                            uiState.fetchItemByIdState as Response.Success<ItemUi>
                            )
                        .result
                        .copy( id = item.id)
                    assertEquals(ItemUi.fromItem(item), itemFromViewModel)
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
        val itemBeforeUpdate = ItemUi.fromItem(
            itemDao.getItemById(1)!!.copy(
                itemName = mockItems[1].itemName,
                itemDescription = mockItems[1].itemDescription,
                itemValue = mockItems[1].itemValue,
                quantityInStock = mockItems[1].quantityInStock
            )
        )
        viewModel.onEvent(UpdateUiEvent.OnUpdate(itemBeforeUpdate))
        viewModel.uiState
            .collectUntil { uiState -> uiState.updateState is Response.Success }
            .collect { uiState ->
            when (uiState.updateState) {
                is Response.Success -> {
                    val itemAfterUpdate = ItemUi.fromItem(itemDao.getItemById(1)!!)
                    assertEquals(itemBeforeUpdate, itemAfterUpdate)
                }
                is Response.Loading -> assert(true)
                is Response.Failure -> assert(false)
            }
        }
    }
}