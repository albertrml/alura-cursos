package br.com.alura.orgs.viewmodel

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import br.com.alura.orgs.domain.DetailsItemUiUseCase
import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.model.mock.mockItems
import br.com.alura.orgs.model.repository.ItemRepository
import br.com.alura.orgs.model.source.ItemDAO
import br.com.alura.orgs.model.source.OrgsRoomDatabase
import br.com.alura.orgs.utils.data.Response
import br.com.alura.orgs.utils.exception.ItemException
import br.com.alura.orgs.utils.tools.collectUntil
import br.com.alura.orgs.viewmodel.details.DetailsUiEvent
import br.com.alura.orgs.viewmodel.details.DetailsViewModel
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DetailsViewModelTest {

    private lateinit var viewModel: DetailsViewModel
    private lateinit var useCase: DetailsItemUiUseCase
    private lateinit var repository: ItemRepository
    private lateinit var dao: ItemDAO
    private lateinit var db: OrgsRoomDatabase

    @Before
    fun setup(){
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room
            .inMemoryDatabaseBuilder(context, OrgsRoomDatabase::class.java)
            .build()

        dao = db.itemDao()
        repository = ItemRepository(dao)
        useCase = DetailsItemUiUseCase(repository)
        viewModel = DetailsViewModel(useCase)
    }

    @Before
    fun setupTestData() = runTest { db.clearAllTables() }

    @After
    fun closeDatabase() { db.close() }

    @Test
    fun whenOnFetchItemUiByIdIsSuccessful() = runTest {
        dao.insert(mockItems.first())
        viewModel.onEvent(DetailsUiEvent.OnFetchItemById(1))
        viewModel.uiState
            .collectUntil { uiState -> uiState.fetchItemByIdState is Response.Success }
            .collect { uiState ->
                when (uiState.fetchItemByIdState) {
                    is Response.Success -> {
                        val expectedItemUi = ItemUi.fromItem(dao.getItemById(1)!!)
                        val itemUi = (uiState.fetchItemByIdState as Response.Success<ItemUi>).result
                        assertEquals(expectedItemUi, itemUi)
                    }
                    is Response.Loading -> assert(true)
                    is Response.Failure -> assert(false)
                }
            }
    }

    @Test
    fun whenFetchItemUiByIdIsUnsuccessful() = runTest {
        viewModel.onEvent(DetailsUiEvent.OnFetchItemById(1))
        viewModel.uiState
            .collectUntil { uiState -> uiState.fetchItemByIdState is Response.Failure }
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

}