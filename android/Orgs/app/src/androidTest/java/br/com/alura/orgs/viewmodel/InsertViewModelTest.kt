package br.com.alura.orgs.viewmodel

import android.content.Context
import android.database.sqlite.SQLiteConstraintException

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import br.com.alura.orgs.model.mock.mockItems
import br.com.alura.orgs.model.repository.ItemRepository
import br.com.alura.orgs.model.source.ItemDAO
import br.com.alura.orgs.model.source.ItemRoomDatabase
import br.com.alura.orgs.utils.Response.*
import br.com.alura.orgs.view.insert.InsertUiEvent
import br.com.alura.orgs.view.insert.InsertViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test

class InsertViewModelTest {
    private lateinit var viewModel: InsertViewModel
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
        viewModel = InsertViewModel(repository)
    }

    @Before
    fun setupTestData() = runTest { db.clearAllTables() }

    @After
    fun tearDown() { db.close() }

    @Test
    fun onInsertItemTriggersSuccess() = runTest {
        viewModel.onEvent(InsertUiEvent.OnInsert(mockItems[0]))
        viewModel.uiState.take(2).collect { uiState ->
            when (uiState.insertState) {
                is Success -> {
                    val itemFromViewModel = itemDao.getItemById(1).copy(
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
        viewModel.onEvent(InsertUiEvent.OnInsert(mockItems[0]))
        val item = itemDao.getItemById(1)
        viewModel.onEvent(InsertUiEvent.OnInsert(item.copy(0)))
        viewModel.uiState.take(2).collect { uiState ->
            when (uiState.insertState) {
                is Success -> {
                    val itemFromViewModel = itemDao.getItems().first()
                    assertEquals(itemFromViewModel.size,2)
                    assertEquals(item, itemFromViewModel[0])
                    assertNotEquals(item, itemFromViewModel[1])
                }
                is Loading -> assert(true)
                is Failure -> assert(false)
            }
        }
    }

    @Test
    fun onInsertDuplicateItemFails() = runTest {
        viewModel.onEvent(InsertUiEvent.OnInsert(mockItems[0]))
        val item = itemDao.getItemById(1)
        viewModel.onEvent(InsertUiEvent.OnInsert(item))
        viewModel.uiState.take(2).collect { uiState ->
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