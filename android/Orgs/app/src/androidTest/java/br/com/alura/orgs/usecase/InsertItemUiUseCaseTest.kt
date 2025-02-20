package br.com.alura.orgs.usecase

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import br.com.alura.orgs.domain.InsertItemUiUseCase
import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.model.mock.mockAccounts
import br.com.alura.orgs.model.mock.mockItems
import br.com.alura.orgs.model.repository.AccountRepository
import br.com.alura.orgs.model.repository.ItemRepository
import br.com.alura.orgs.model.source.AccountDAO
import br.com.alura.orgs.model.source.ItemDAO
import br.com.alura.orgs.model.source.OrgRoomDatabase
import br.com.alura.orgs.utils.data.Response
import br.com.alura.orgs.utils.tools.until
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class InsertItemUiUseCaseTest {

    private lateinit var insertUseCase: InsertItemUiUseCase
    private lateinit var accountRepository: AccountRepository
    private lateinit var accountDAO: AccountDAO
    private lateinit var itemRepository: ItemRepository
    private lateinit var itemDAO: ItemDAO
    private lateinit var db: OrgRoomDatabase

    @Before
    fun setup(){
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room
            .inMemoryDatabaseBuilder(context, OrgRoomDatabase::class.java)
            .build()

        accountDAO = db.accountDao()
        accountRepository = AccountRepository(accountDAO)
        itemDAO = db.itemDao()
        itemRepository = ItemRepository(itemDAO)
        insertUseCase = InsertItemUiUseCase(accountRepository,itemRepository)
    }

    @Before
    fun setupTestData() = runTest {
        db.clearAllTables()
    }

    @After
    fun closeDatabase() { db.close() }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun whenInsertItemIsSuccessful() = runTest {
        val account = mockAccounts.first()
        val expectedItem = mockItems.first { it.userOwner == account.username }
        val itemUi = ItemUi.fromItem(expectedItem.copy(userOwner = ""))

        accountDAO.insert(account)
        accountRepository.authenticate(account.username,account.password)
            .filterIsInstance<Response.Success<Unit>>()
            .flatMapConcat { _ ->
                insertUseCase.insertItemUi(itemUi)
            }
            .until { response -> response is Response.Success }
            .collect { response ->
                when (response) {
                    is Response.Success -> {
                        val itemsFromDatabase = itemDAO.getItems().first()
                        assertEquals(1, itemsFromDatabase.size)
                        assertEquals(expectedItem, itemsFromDatabase.first().copy(id = 0))
                    }
                    is Response.Loading -> assert(true)
                    is Response.Failure -> assert(false)
                }
            }
    }

}