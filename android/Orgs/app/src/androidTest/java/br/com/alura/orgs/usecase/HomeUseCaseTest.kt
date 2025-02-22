package br.com.alura.orgs.usecase

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import br.com.alura.orgs.domain.HomeUseCase
import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.model.mock.mockAccounts
import br.com.alura.orgs.model.mock.mockItems
import br.com.alura.orgs.model.repository.AccountRepository
import br.com.alura.orgs.model.repository.ItemRepository
import br.com.alura.orgs.model.source.AccountDAO
import br.com.alura.orgs.model.source.ItemDAO
import br.com.alura.orgs.model.source.OrgRoomDatabase
import br.com.alura.orgs.utils.data.Response
import br.com.alura.orgs.utils.data.SortedItem
import br.com.alura.orgs.utils.exception.AccountException.AccountIsNotAuthenticated
import br.com.alura.orgs.utils.exception.ItemException.ItemBelongsToAnotherAccountException
import br.com.alura.orgs.utils.tools.until
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HomeUseCaseTest {

    private lateinit var homeUseCase: HomeUseCase
    private lateinit var accountDao: AccountDAO
    private lateinit var accountRepository: AccountRepository
    private lateinit var itemRepository: ItemRepository
    private lateinit var itemDao: ItemDAO
    private lateinit var db: OrgRoomDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room
            .inMemoryDatabaseBuilder(context, OrgRoomDatabase::class.java)
            .build()
        itemDao = db.itemDao()
        itemRepository = ItemRepository(itemDao)
        accountDao = db.accountDao()
        accountRepository = AccountRepository(accountDao)
        homeUseCase = HomeUseCase(accountRepository,itemRepository)
    }

    @Before
    fun setupTestData() = runTest {
        db.clearAllTables()
        mockAccounts.forEach{ accountDao.insert(it) }
        mockItems.forEach { itemDao.insert(it) }
    }

    @After
    fun tearDown() { db.close() }

    @Test
    fun whenDeleteItemIsSuccessful() = runTest {
        val account = mockAccounts.first()

        accountRepository.authenticate(account.username,account.password).collect()
        val outdatedItemList = itemDao.getItemsByUserOwner(account.username).first()
        val itemForDelete = ItemUi.fromItem(outdatedItemList.first())

        homeUseCase.deleteItem(itemForDelete)
            .until { response -> response is Response.Success  }
            .collect{ response ->
                when(response){
                    is Response.Success -> {
                        val updatedItemList = itemDao.getItemsByUserOwner(account.username).first()
                        assert(outdatedItemList.size > updatedItemList.size)
                        assertFalse(updatedItemList.contains(itemForDelete.toItem()))
                    }
                    is Response.Loading -> assert(true)
                    is Response.Failure -> assert(false)
                }
            }
    }

    @Test
    fun whenDeleteItemIsUnsuccessfulDueAccountIsNotAuthenticated() = runTest {
        val account = mockAccounts.first()

        val outdatedItemList = itemDao.getItemsByUserOwner(account.username).first()
        val itemForDelete = ItemUi.fromItem(outdatedItemList.first())

        homeUseCase.deleteItem(itemForDelete)
            .until { response -> response is Response.Failure  }
            .collect{ response ->
                when(response){
                    is Response.Success -> assert(false)
                    is Response.Loading -> assert(true)
                    is Response.Failure ->
                        assertTrue(response.exception is AccountIsNotAuthenticated)
                }
            }
    }

    @Test
    fun whenDeleteItemIsUnsuccessfulDueItemBelongsToAnotherAccountException() = runTest {
        val account = mockAccounts.first()
        val otherAccount = mockAccounts[1]

        accountRepository.authenticate(otherAccount.username,otherAccount.password).collect()

        val outdatedItemList = itemDao.getItemsByUserOwner(account.username).first()
        val itemForDelete = ItemUi.fromItem(outdatedItemList.first())

        homeUseCase.deleteItem(itemForDelete)
            .until { response -> response is Response.Failure  }
            .collect{ response ->
                when(response){
                    is Response.Success -> assert(false)
                    is Response.Loading -> assert(true)
                    is Response.Failure ->
                        assertTrue(response.exception is ItemBelongsToAnotherAccountException)
                }
            }
    }


    @Test
    fun whenFetchAllItemsIsSuccessful() = runTest {
        val account = mockAccounts.first()
        val expectedItems = mockItems
            .filter { it.userOwner == account.username }
            .map { item -> ItemUi.fromItem(item) }
            .toList()

        combine(
            accountRepository.authenticate(account.username,account.password),
            homeUseCase.fetchAllItemUis(SortedItem.ByIdAscending)
        ){ auth, fetch ->
            if (auth is Response.Success){
                when(fetch){
                    is Response.Success -> {
                        val fetchResults = fetch.result.map { it.copy(id = 0) }
                        assertEquals(expectedItems.size,fetchResults.size)
                        assertTrue(expectedItems.containsAll(fetchResults))
                    }
                    is Response.Loading -> assert(true)
                    is Response.Failure -> assert(false)
                }
            }
            fetch
        }
            .until { response -> response is Response.Success  }
            .collect()
    }
}