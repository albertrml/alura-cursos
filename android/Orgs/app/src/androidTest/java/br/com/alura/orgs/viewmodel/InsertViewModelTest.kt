package br.com.alura.orgs.viewmodel

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import br.com.alura.orgs.domain.InsertItemUiUseCase
import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.model.mock.mockItems
import br.com.alura.orgs.model.repository.ItemRepository
import br.com.alura.orgs.model.source.ItemDAO
import br.com.alura.orgs.model.source.OrgsRoomDatabase
import br.com.alura.orgs.utils.data.Response.*
import br.com.alura.orgs.utils.tools.collectUntil
import br.com.alura.orgs.viewmodel.insert.InsertUiEvent
import br.com.alura.orgs.viewmodel.insert.InsertViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test


class InsertViewModelTest {


    private lateinit var viewModel: InsertViewModel
    private lateinit var useCase: InsertItemUiUseCase
    private lateinit var repository: ItemRepository
    private lateinit var dao: ItemDAO
    private lateinit var db: OrgsRoomDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room
            .inMemoryDatabaseBuilder(context, OrgsRoomDatabase::class.java)
            .build()

        dao = db.itemDao()
        repository = ItemRepository(dao)
        useCase = InsertItemUiUseCase(repository)
        viewModel = InsertViewModel(useCase)
    }

    @Before
    fun setupTestData() = runTest { db.clearAllTables() }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun whenOnInsertItemIsSuccessful() = runTest {
        val itemUiMock = ItemUi.fromItem(mockItems[0])

        viewModel.onEvent(InsertUiEvent.OnInsert(itemUiMock))
        viewModel.uiState
            .collectUntil { uiState -> uiState.insertState is Success }
            .collect { uiState ->
                when (uiState.insertState) {
                    is Success -> {
                        val itemFromViewModel = dao.getItemById(1)!!.copy(
                            id = mockItems[0].id
                        )
                        assertEquals(mockItems[0], itemFromViewModel)
                    }

                    is Loading -> assert(true)
                    is Failure -> assert(false)
                }
            }
    }

    @Test
    fun whenInsertDuplicateItemButDifferentIdsIsSuccessful() = runTest {

        val itemUiMock = ItemUi.fromItem(mockItems[0])
        viewModel.onEvent(InsertUiEvent.OnInsert(itemUiMock))
        viewModel.onEvent(InsertUiEvent.OnInsert(itemUiMock))

        viewModel.uiState
            .collectUntil { uiState -> uiState.insertState is Success }
            .collect { uiState ->
                when (uiState.insertState) {
                    is Success -> {
                        val itemFromViewModel = dao.getItems().first()
                        assertEquals(itemFromViewModel.size, 2)
                        assertNotEquals(itemFromViewModel[0], itemFromViewModel[1])
                    }

                    is Loading -> assert(true)
                    is Failure -> assert(false)
                }
            }
    }

    @Test
    fun whenInsertDuplicateItemIsUnsuccessful() = runTest {
        dao.insert(mockItems.first())
        val itemFromDatabase = ItemUi.fromItem(dao.getItemById(1)!!)
        viewModel.onEvent(InsertUiEvent.OnInsert(itemFromDatabase))
        viewModel.uiState
            .collectUntil { uiState -> uiState.insertState is Failure }
            .collect { uiState ->
                when (uiState.insertState) {
                    is Success -> assert(false)
                    is Loading -> assert(true)
                    is Failure -> assert(
                        (uiState.insertState as Failure).exception is SQLiteConstraintException
                    )
                }
            }

    }

}