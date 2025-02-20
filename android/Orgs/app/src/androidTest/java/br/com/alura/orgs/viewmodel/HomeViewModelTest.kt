package br.com.alura.orgs.viewmodel

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import br.com.alura.orgs.domain.HomeUseCase
import br.com.alura.orgs.model.entity.Item
import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.model.mock.mockAccounts
import br.com.alura.orgs.model.mock.mockItems
import br.com.alura.orgs.model.repository.AccountRepository
import br.com.alura.orgs.model.repository.ItemRepository
import br.com.alura.orgs.model.source.AccountDAO
import br.com.alura.orgs.model.source.ItemDAO
import br.com.alura.orgs.model.source.OrgRoomDatabase
import br.com.alura.orgs.utils.data.Response
import br.com.alura.orgs.utils.data.Response.Failure
import br.com.alura.orgs.utils.data.Response.Loading
import br.com.alura.orgs.utils.data.Response.Success
import br.com.alura.orgs.utils.tools.until
import br.com.alura.orgs.viewmodel.home.HomeUiEvent
import br.com.alura.orgs.viewmodel.home.HomeUiState
import br.com.alura.orgs.viewmodel.home.HomeViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

class HomeViewModelTest {
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var homeUseCase: HomeUseCase
    private lateinit var accountRepository: AccountRepository
    private lateinit var itemRepository: ItemRepository
    private lateinit var accountDAO: AccountDAO
    private lateinit var itemDAO: ItemDAO
    private lateinit var db: OrgRoomDatabase
    private val accounts = mockAccounts

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room
            .inMemoryDatabaseBuilder(context, OrgRoomDatabase::class.java)
            .build()
        itemDAO = db.itemDao()
        accountDAO = db.accountDao()
        itemRepository = ItemRepository(itemDAO)
        accountRepository = AccountRepository(accountDAO)
        homeUseCase = HomeUseCase(accountRepository,itemRepository)
        homeViewModel = HomeViewModel(accountRepository,homeUseCase)
    }

    @Before
    fun setupTestData() = runTest {
        db.clearAllTables()
        accounts.forEach{ accountDAO.insert(it) }
        mockItems.forEach{ itemDAO.insert(it) }
    }

    @After
    fun tearDown() { db.close() }


    @Test
    fun whenOnDeleteItemUiDeletesSuccessfully() = runTest {
        val account = accounts.first
        val expectedItems = itemDAO.getItems().first()
            .filter { it.userOwner == account.username }
            .sortedBy { it.id }
        val itemUiForDelete = ItemUi.fromItem(expectedItems.first())

        combine(
            accountRepository.authenticate(account.username, account.password),
            homeViewModel.uiState
        ){ auth, uiState ->
            if (auth is Success){
                homeViewModel.onEvent(HomeUiEvent.OnDelete(itemUiForDelete))
            }
            uiState.deleteState
        }
            .until { response -> response is Success }
            .collect{ response ->
                when(response){
                    is Success -> {
                        val itemsFromDB = itemDAO.getItemsByUserOwner(account.username)
                            .first().sortedBy { it.id }
                        assertFalse(itemsFromDB.contains(itemUiForDelete.toItem()))
                        assertEquals(expectedItems.size, itemsFromDB.size + 1)
                    }
                    is Loading -> assert(true)
                    is Failure -> assert(false)
                }
            }
    }

    @Test
    fun whenOnFetchAllItemsChangesSuccessfullyUiState() = runTest {
        val account = accounts.first()
        val expectedItems = itemDAO.getItems().first()
            .filter { it.userOwner == account.username }
            .sortedBy { it.id }
            .map { ItemUi.fromItem(it) }

        combine(
            accountRepository.authenticate(account.username, account.password),
            homeViewModel.uiState
        ){ auth, uiState ->
            if (auth is Success){
                homeViewModel.onEvent(HomeUiEvent.OnFetchAllItemsByIdAscending)
            }
            uiState.fetchAllItemsState
        }
            .until { response -> response is Success }
            .collect{ response ->
                when(response){
                    is Success -> {
                        val itemsFromViewModel = response.result.sortedBy { it.id }
                        Log.i("outdatedList", expectedItems.toString())
                        Log.i("updatedList", itemsFromViewModel.toString())
                        assertEquals(expectedItems.size, itemsFromViewModel.size)
                        assertEquals(expectedItems, itemsFromViewModel)
                    }
                    is Loading -> assert(true)
                    is Failure -> assert(false)
                }
            }
    }

}