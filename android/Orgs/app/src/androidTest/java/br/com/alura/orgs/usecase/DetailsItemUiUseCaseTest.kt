package br.com.alura.orgs.usecase

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import br.com.alura.orgs.domain.DetailsItemUiUseCase
import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.model.mock.mockAccounts
import br.com.alura.orgs.model.mock.mockItems
import br.com.alura.orgs.model.repository.ItemRepository
import br.com.alura.orgs.model.source.AccountDAO
import br.com.alura.orgs.model.source.ItemDAO
import br.com.alura.orgs.model.source.OrgRoomDatabase
import br.com.alura.orgs.utils.data.Response
import br.com.alura.orgs.utils.exception.ItemException
import br.com.alura.orgs.utils.tools.until
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DetailsItemUiUseCaseTest {

    private lateinit var useCase: DetailsItemUiUseCase
    private lateinit var repository: ItemRepository
    private lateinit var dao: ItemDAO
    private lateinit var accountDao: AccountDAO
    private lateinit var db: OrgRoomDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room
            .inMemoryDatabaseBuilder(context, OrgRoomDatabase::class.java)
            .build()

        dao = db.itemDao()
        accountDao = db.accountDao()
        repository = ItemRepository(dao)
        useCase = DetailsItemUiUseCase(repository)
    }

    @Before
    fun setupTestData() = runTest {
        db.clearAllTables()
        mockAccounts.forEach { accountDao.insert(it) }
    }

    @After
    fun closeDatabase() { db.close() }

    @Test
    fun whenFetchItemUiByIdIsSuccessful() = runTest {
        mockItems.forEach { dao.insert(it) }
        useCase.fetchItemUiById(1)
            .until { response -> response is Response.Success  }
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
    fun whenFetchItemUiByIdIsUnsuccessful() = runTest{
        useCase.fetchItemUiById(1)
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


}