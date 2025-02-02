package br.com.alura.orgs.viewmodel

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import br.com.alura.orgs.domain.InsertItemUiUseCase
import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.model.mock.mockItems
import br.com.alura.orgs.model.repository.ItemRepository
import br.com.alura.orgs.model.source.ItemDAO
import br.com.alura.orgs.model.source.ItemRoomDatabase
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
    private lateinit var insertItemUiUseCase: InsertItemUiUseCase
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
        insertItemUiUseCase = InsertItemUiUseCase(repository)
        viewModel = InsertViewModel(insertItemUiUseCase)
    }

    @Before
    fun setupTestData() = runTest { db.clearAllTables() }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun onInsertItemTriggersSuccess() = runTest {
        val itemUiMock = ItemUi.fromItem(mockItems[0])

        viewModel.onEvent(InsertUiEvent.OnInsert(itemUiMock))
        viewModel.uiState
            .collectUntil { uiState -> uiState.insertState is Success }
            .collect { uiState ->
                when (uiState.insertState) {
                    is Success -> {
                        val itemFromViewModel = itemDao.getItemById(1)!!.copy(
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
    fun onInsertDuplicateItemButDifferentIdSucceeds() = runTest {

        val itemUiMock = ItemUi.fromItem(mockItems[0])
        viewModel.onEvent(InsertUiEvent.OnInsert(itemUiMock))
        viewModel.onEvent(InsertUiEvent.OnInsert(itemUiMock))

        viewModel.uiState
            .collectUntil { uiState -> uiState.insertState is Success }
            .collect { uiState ->
                when (uiState.insertState) {
                    is Success -> {
                        val itemFromViewModel = itemDao.getItems().first()
                        assertEquals(itemFromViewModel.size, 2)
                        assertNotEquals(itemFromViewModel[0], itemFromViewModel[1])
                    }

                    is Loading -> assert(true)
                    is Failure -> assert(false)
                }
            }
    }

    @Test
    fun onInsertDuplicateItemFails() = runTest {
        val itemUiMock = ItemUi.fromItem(mockItems[0])
        viewModel.onEvent(InsertUiEvent.OnInsert(itemUiMock))

        viewModel.uiState
            .collectUntil { uiState -> uiState.insertState is Success }
            .collect { uiState ->
                if (uiState.insertState is Success) {

                    val itemFromViewModel = ItemUi.fromItem(itemDao.getItemById(1)!!)
                    viewModel.onEvent(InsertUiEvent.OnInsert(itemFromViewModel))

                }
            }

        viewModel.uiState
            .collectUntil { uiState2 -> uiState2.insertState is Failure }
            .collect { uiState2 ->
                Log.i("Response", uiState2.insertState.toString())
                when (uiState2.insertState) {
                    is Success -> assert(false)
                    is Loading -> assert(true)
                    is Failure -> assert(
                        (uiState2.insertState as Failure).exception is SQLiteConstraintException
                    )
                }
            }

    }

}