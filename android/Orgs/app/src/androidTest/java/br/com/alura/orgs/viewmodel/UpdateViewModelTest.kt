package br.com.alura.orgs.viewmodel

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import br.com.alura.orgs.domain.UpdateItemUiUseCase
import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.model.mock.mockItems
import br.com.alura.orgs.model.repository.ItemRepository
import br.com.alura.orgs.model.source.ItemDAO
import br.com.alura.orgs.model.source.OrgRoomDatabase
import br.com.alura.orgs.utils.data.Response
import br.com.alura.orgs.utils.exception.ItemException
import br.com.alura.orgs.utils.tools.until
import br.com.alura.orgs.viewmodel.update.UpdateUiEvent
import br.com.alura.orgs.viewmodel.update.UpdateViewModel
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UpdateViewModelTest {
    private lateinit var viewModel: UpdateViewModel
    private lateinit var useCase: UpdateItemUiUseCase
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
        useCase = UpdateItemUiUseCase(repository)
        viewModel = UpdateViewModel(useCase)
    }

    @Before
    fun setupTestData() = runTest { db.clearAllTables() }

    @After
    fun tearDown() { db.close() }

    @Test
    fun whenOnFetchItemByIdChangesSuccessfullyUiState() = runTest {
        val item = mockItems[0]
        dao.insert(item)
        viewModel.onEvent(UpdateUiEvent.OnFetchItemUiById(1))
        viewModel.uiState
            .until { uiState -> uiState.fetchItemByIdState is Response.Success }
            .collect { uiState ->
                when (uiState.fetchItemByIdState) {
                    is Response.Success -> {
                        val itemFromViewModel =
                            (uiState.fetchItemByIdState as Response.Success<ItemUi>)
                                .result.copy(id = item.id)
                        assertEquals(ItemUi.fromItem(item), itemFromViewModel)
                    }

                    is Response.Loading -> assert(true)
                    is Response.Failure -> assert(false)
                }
            }
    }

    @Test
    fun whenOnFetchItemByIdIsUnsuccessful() = runTest {
        viewModel.onEvent(UpdateUiEvent.OnFetchItemUiById(1))
        viewModel.uiState
            .until { uiState -> uiState.fetchItemByIdState is Response.Failure }
            .collect { uiState ->
                when (uiState.fetchItemByIdState) {
                    is Response.Success -> assert(false)
                    is Response.Loading -> assert(true)
                    is Response.Failure -> {
                        assert(
                                (uiState.fetchItemByIdState as Response.Failure)
                                    .exception is ItemException.ItemNotFoundException
                        )
                    }
                }
            }
    }

    @Test
    fun whenOnUpdateItemUiIsSuccessful() = runTest {
        val item = mockItems[0]
        dao.insert(item)
        val itemBeforeUpdate = ItemUi.fromItem(
            dao.getItemById(1)!!.copy(
                itemName = mockItems[1].itemName,
                itemDescription = mockItems[1].itemDescription,
                itemValue = mockItems[1].itemValue,
                quantityInStock = mockItems[1].quantityInStock
            )
        )
        viewModel.onEvent(UpdateUiEvent.OnUpdate(itemBeforeUpdate))
        viewModel.uiState
            .until { uiState -> uiState.updateState is Response.Success }
            .collect { uiState ->
            when (uiState.updateState) {
                is Response.Success -> {
                    val itemAfterUpdate = ItemUi.fromItem(dao.getItemById(1)!!)
                    assertEquals(itemBeforeUpdate, itemAfterUpdate)
                }
                is Response.Loading -> assert(true)
                is Response.Failure -> assert(false)
            }
        }
    }
}