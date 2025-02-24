package br.com.alura.orgs.viewmodel

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import br.com.alura.orgs.domain.InsertUseCase
import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.model.mock.mockAccounts
import br.com.alura.orgs.model.mock.mockItems
import br.com.alura.orgs.model.repository.AccountRepository
import br.com.alura.orgs.model.repository.ItemRepository
import br.com.alura.orgs.model.source.AccountDAO
import br.com.alura.orgs.model.source.ItemDAO
import br.com.alura.orgs.model.source.OrgRoomDatabase
import br.com.alura.orgs.utils.data.Authenticate
import br.com.alura.orgs.utils.data.Response.*
import br.com.alura.orgs.utils.tools.until
import br.com.alura.orgs.viewmodel.insert.InsertUiEvent
import br.com.alura.orgs.viewmodel.insert.InsertViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test


class InsertViewModelTest {

    private lateinit var viewModel: InsertViewModel
    private lateinit var insertUseCase: InsertUseCase
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
        insertUseCase = InsertUseCase(accountRepository,itemRepository)
        viewModel = InsertViewModel(insertUseCase)
    }

    @Before
    fun setupTestData() = runTest { db.clearAllTables() }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun whenOnInsertItemIsSuccessful() = runTest {
        val account = mockAccounts.first
        val expectedItem = mockItems.first{ it.userOwner == account.username }
        val itemUiToInsert = ItemUi.fromItem(expectedItem)

        accountDAO.insert(account)

        combine(
            accountRepository.authenticate(account.username, account.password),
            accountRepository.auth
        ) { _, auth ->
            if (auth is Authenticate.Login){
                viewModel.onEvent(InsertUiEvent.OnInsert(itemUiToInsert))
            }
            auth
        }
            .until { it is Authenticate.Login }
            .collect()

        viewModel.uiState
            .map { uiState -> uiState.insertState }
            .until { insertState -> insertState is Success }
            .collect{ insertState ->
                when(insertState){
                    is Success -> {
                        val itemFromDB = itemDAO.getItemById(1)!!.copy(
                            id = mockItems[0].id
                        )
                        assertEquals(expectedItem, itemFromDB)
                    }
                    is Loading -> {
                        assertTrue(true)
                    }
                    is Failure -> {
                        assertFalse(true)
                    }
                }
            }

    }

    @Test
    fun whenInsertDuplicateItemButDifferentIdsIsSuccessful() = runTest {
        val account = mockAccounts.first
        val item = mockItems.first{ it.userOwner == account.username }
        val itemUiMock = ItemUi.fromItem(item)

        accountDAO.insert(account)

        combine(
            accountRepository.authenticate(account.username, account.password),
            accountRepository.auth
        ) { _, auth ->
            if (auth is Authenticate.Login){
                viewModel.onEvent(InsertUiEvent.OnInsert(itemUiMock))
                viewModel.onEvent(InsertUiEvent.OnInsert(itemUiMock))
            }
            auth
        }
            .until { it is Authenticate.Login }
            .collect()


        viewModel.uiState
            .map { uiState -> uiState.insertState }
            .until { insertState -> insertState is Success }
            .collect { insertState ->
                when (insertState) {
                    is Success -> {
                        val itemFromViewModel = itemDAO.getItems().first()
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
        val account = mockAccounts.first
        val item = mockItems.first{ it.userOwner == account.username }
        accountDAO.insert(account)
        itemDAO.insert(item)

        val itemFromDatabase = ItemUi.fromItem(itemDAO.getItemById(1)!!)

        combine(
            accountRepository.authenticate(account.username, account.password),
            accountRepository.auth
        ) { _, auth ->
            if (auth is Authenticate.Login){
                viewModel.onEvent(InsertUiEvent.OnInsert(itemFromDatabase))
            }
            auth
        }
            .until { it is Authenticate.Login }
            .collect()

        viewModel.uiState
            .until { uiState -> uiState.insertState is Failure }
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