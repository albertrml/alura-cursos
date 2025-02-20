package br.com.alura.orgs.viewmodel

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import br.com.alura.orgs.domain.UpdateUseCase
import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.model.mock.mockItems
import br.com.alura.orgs.model.repository.AccountRepository
import br.com.alura.orgs.model.repository.ItemRepository
import br.com.alura.orgs.model.source.AccountDAO
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
    private lateinit var updateViewModel: UpdateViewModel
    private lateinit var updateUseCase: UpdateUseCase
    private lateinit var accountRepository: AccountRepository
    private lateinit var accountDAO: AccountDAO
    private lateinit var itemRepository: ItemRepository
    private lateinit var itemDAO: ItemDAO
    private lateinit var db: OrgRoomDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room
            .inMemoryDatabaseBuilder(context, OrgRoomDatabase::class.java)
            .build()
        itemDAO = db.itemDao()
        itemRepository = ItemRepository(itemDAO)
        accountDAO = db.accountDao()
        accountRepository = AccountRepository(accountDAO)
        updateUseCase = UpdateUseCase(accountRepository,itemRepository)
        updateViewModel = UpdateViewModel(updateUseCase)
    }

    @Before
    fun setupTestData() = runTest { db.clearAllTables() }

    @After
    fun tearDown() { db.close() }

    @Test
    fun whenOnFetchItemByIdChangesSuccessfullyUiState() = runTest {
        val item = mockItems[0]
        itemDAO.insert(item)
        updateViewModel.onEvent(UpdateUiEvent.OnFetchItemUiById(1))
        updateViewModel.uiState
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
        updateViewModel.onEvent(UpdateUiEvent.OnFetchItemUiById(1))
        updateViewModel.uiState
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
        itemDAO.insert(item)
        val itemBeforeUpdate = ItemUi.fromItem(
            itemDAO.getItemById(1)!!.copy(
                itemName = mockItems[1].itemName,
                itemDescription = mockItems[1].itemDescription,
                itemValue = mockItems[1].itemValue,
                quantityInStock = mockItems[1].quantityInStock
            )
        )
        updateViewModel.onEvent(UpdateUiEvent.OnUpdate(itemBeforeUpdate))
        updateViewModel.uiState
            .until { uiState -> uiState.updateState is Response.Success }
            .collect { uiState ->
            when (uiState.updateState) {
                is Response.Success -> {
                    val itemAfterUpdate = ItemUi.fromItem(itemDAO.getItemById(1)!!)
                    assertEquals(itemBeforeUpdate, itemAfterUpdate)
                }
                is Response.Loading -> assert(true)
                is Response.Failure -> assert(false)
            }
        }
    }
}