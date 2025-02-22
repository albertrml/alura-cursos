package br.com.alura.orgs.usecase

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import br.com.alura.orgs.domain.UpdateUseCase
import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.model.mock.mockAccounts
import br.com.alura.orgs.model.mock.mockItems
import br.com.alura.orgs.model.repository.AccountRepository
import br.com.alura.orgs.model.repository.ItemRepository
import br.com.alura.orgs.model.source.AccountDAO
import br.com.alura.orgs.model.source.ItemDAO
import br.com.alura.orgs.model.source.OrgRoomDatabase
import br.com.alura.orgs.utils.data.Authenticate
import br.com.alura.orgs.utils.data.Response
import br.com.alura.orgs.utils.exception.AccountException
import br.com.alura.orgs.utils.exception.ItemException
import br.com.alura.orgs.utils.tools.until
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

class UpdateUseCaseTest {

    private lateinit var updateUseCase: UpdateUseCase
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

        itemDAO = db.itemDao()
        itemRepository = ItemRepository(itemDAO)
        accountDAO = db.accountDao()
        accountRepository = AccountRepository(accountDAO)
        updateUseCase = UpdateUseCase(accountRepository,itemRepository)
    }

    @Before
    fun setupTestData() = runTest {
        db.clearAllTables()
        mockAccounts.forEach { accountDAO.insert(it) }
    }

    @After
    fun closeDatabase() { db.close() }

    @Test
    fun whenFetchItemByIdIsSuccessful() = runTest {
        mockItems.forEach { itemDAO.insert(it) }

        updateUseCase.fetchItemUiById(1)
            .until { response -> response is Response.Success  }
            .collect{ response ->
                when(response){
                    is Response.Success -> {
                        val expectedItemUi = ItemUi.fromItem(itemDAO.getItemById(1)!!)
                        val itemUi = response.result
                        assertEquals(1, itemUi.id)
                        assertEquals(expectedItemUi, itemUi)
                    }
                    is Response.Loading -> assert(true)
                    is Response.Failure -> assert(false)
                }
            }
    }

    @Test
    fun whenFetchItemByIdIsUnsuccessful() = runTest{
        updateUseCase.fetchItemUiById(1)
            .until { response -> response is Response.Failure  }
            .collect { response ->
                when(response){
                    is Response.Success -> assert(false)
                    is Response.Loading -> assert(true)
                    is Response.Failure -> {
                        assert(response.exception is ItemException.ItemNotFoundException)
                    }
                }
            }
    }

    @Test
    fun whenUpdateItemUiIsSuccessful() = runTest {
        itemDAO.insert(mockItems.first())
        val expectedItemUi = ItemUi.fromItem(itemDAO.getItemById(1)!!).copy(
            itemName = mockItems[1].itemName,
            itemDescription = mockItems[1].itemDescription,
            itemValue = mockItems[1].itemValue.toString(),
            quantityInStock = mockItems[1].quantityInStock.toString(),
            itemUrl = mockItems[1].itemUrl
        )

        val account = mockAccounts.first { it.username == expectedItemUi.userOwner }
        accountRepository.authenticate(account.username, account.password).collect{}

        updateUseCase.updateItemUi(expectedItemUi)
            .until { response -> response is Response.Success  }
            .collect { response ->
                when(response){
                    is Response.Success -> {
                        val currentItemUi = ItemUi.fromItem(itemDAO.getItemById(1)!!)
                        assertEquals(expectedItemUi, currentItemUi)
                    }
                    is Response.Loading -> assert(true)
                    is Response.Failure -> assert(false)
                }
            }
    }

    @Test
    fun whenUpdateItemUiFailsDueAccountIsNotAuthenticated() = runTest{
        val item = mockItems.first()
        itemDAO.insert(item)
        val itemForUpdate = ItemUi.fromItem(itemDAO.getItemById(1)!!).copy(
            itemName = mockItems[1].itemName
        )

        updateUseCase.updateItemUi(itemForUpdate)
            .until { response -> response is Response.Failure }
            .collect{ response ->
                when(response){
                    is Response.Success -> assertFalse(
                        "Expected Response.Failure",
                        true
                    )
                    is Response.Loading -> assert(true)
                    is Response.Failure -> {
                        assert(response.exception is AccountException.AccountIsNotAuthenticated)
                    }
                }
            }
    }

    @Test
    fun whenUpdateItemUiFailsDueItemBelongsToAnotherAccount() = runTest{
        val account = mockAccounts.first()
        val item = mockItems.first{ it.userOwner != account.username }
        itemDAO.insert(item)
        val itemForUpdate = ItemUi.fromItem(itemDAO.getItemById(1)!!).copy(
            itemName = "Teste"
        )

        accountRepository.authenticate(account.username, account.password).collect()
        combine(
            accountRepository.auth,
            updateUseCase.updateItemUi(itemForUpdate)
        ) { auth, update ->
            Log.i("UpdateUseCaseTest", "$auth and $update")
            if (auth is Authenticate.Login){
                 when(update){
                     is Response.Success -> assertFalse(
                         "Expected Response.Failure",
                         true
                     )
                     is Response.Loading -> {}
                     is Response.Failure -> {
                         assert(update.exception is ItemException.ItemBelongsToAnotherAccountException)
                     }
                 }
             }
            update
        }
            .until { update -> update is Response.Failure }
            .collectLatest {  }
    }
}