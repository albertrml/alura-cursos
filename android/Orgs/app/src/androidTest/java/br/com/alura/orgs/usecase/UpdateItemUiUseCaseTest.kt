package br.com.alura.orgs.usecase

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import br.com.alura.orgs.domain.UpdateItemUiUseCase
import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.model.mock.mockItems
import br.com.alura.orgs.model.repository.ItemRepository
import br.com.alura.orgs.model.source.ItemDAO
import br.com.alura.orgs.model.source.ItemRoomDatabase
import br.com.alura.orgs.utils.data.Response
import br.com.alura.orgs.utils.exception.ItemException
import br.com.alura.orgs.utils.tools.collectUntil
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UpdateItemUiUseCaseTest {

    private lateinit var useCase: UpdateItemUiUseCase
    private lateinit var repository: ItemRepository
    private lateinit var dao: ItemDAO
    private lateinit var db: ItemRoomDatabase

    @Before
    fun setup(){
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room
            .inMemoryDatabaseBuilder(context, ItemRoomDatabase::class.java)
            .build()

        dao = db.itemDao()
        repository = ItemRepository(dao)
        useCase = UpdateItemUiUseCase(repository)
    }

    @Before
    fun setupTestData() = runTest { db.clearAllTables() }

    @After
    fun closeDatabase() { db.close() }

    @Test
    fun whenFetchItemByIdIsSuccessful() = runTest {
        mockItems.forEach { dao.insert(it) }

        useCase.fetchItemUiById(1)
            .collectUntil { response -> response is Response.Success  }
            .collect{ response ->
                when(response){
                    is Response.Success -> {
                        val expectedItemUi = ItemUi.fromItem(dao.getItemById(1)!!)
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
        useCase.fetchItemUiById(1)
            .collectUntil { response -> response is Response.Failure  }
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
        dao.insert(mockItems.first())
        val expectedItemUi = ItemUi.fromItem(dao.getItemById(1)!!).copy(
            itemName = mockItems[1].itemName,
            itemDescription = mockItems[1].itemDescription,
            itemValue = mockItems[1].itemValue.toString(),
            quantityInStock = mockItems[1].quantityInStock.toString(),
            itemUrl = mockItems[1].itemUrl
        )

        useCase.updateItemUi(expectedItemUi)
            .collectUntil { response -> response is Response.Success  }
            .collect { response ->
                when(response){
                    is Response.Success -> {
                        val currentItemUi = ItemUi.fromItem(dao.getItemById(1)!!)
                        assertEquals(expectedItemUi, currentItemUi)
                    }
                    is Response.Loading -> assert(true)
                    is Response.Failure -> assert(false)
                }
            }
    }
}