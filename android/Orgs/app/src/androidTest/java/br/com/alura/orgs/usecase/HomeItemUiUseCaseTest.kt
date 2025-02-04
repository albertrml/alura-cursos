package br.com.alura.orgs.usecase

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import br.com.alura.orgs.domain.HomeItemUiUseCase
import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.model.mock.mockItems
import br.com.alura.orgs.model.repository.ItemRepository
import br.com.alura.orgs.model.source.ItemDAO
import br.com.alura.orgs.model.source.OrgsRoomDatabase
import br.com.alura.orgs.utils.data.Response
import br.com.alura.orgs.utils.data.SortType
import br.com.alura.orgs.utils.tools.collectUntil
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class HomeItemUiUseCaseTest {

    private lateinit var homeItemUiUseCase: HomeItemUiUseCase
    private lateinit var repository: ItemRepository
    private lateinit var itemDao: ItemDAO
    private lateinit var db: OrgsRoomDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room
            .inMemoryDatabaseBuilder(context, OrgsRoomDatabase::class.java)
            .build()
        itemDao = db.itemDao()
        repository = ItemRepository(itemDao)
        homeItemUiUseCase = HomeItemUiUseCase(repository)
    }

    @Before
    fun setupTestData() = runTest { db.clearAllTables() }

    @After
    fun tearDown() { db.close() }

    @Test
    fun whenFetchAllItemsIsSuccessful() = runTest {
        mockItems.forEach { itemDao.insert(it) }

        homeItemUiUseCase.fetchAllItemUis(SortType.ByIdAscending)
            .collectUntil { response -> response is Response.Success  }
            .collect{ response ->
                when(response){
                    is Response.Success -> {
                        val items = response.result.map { it.toItem().copy(id = 0) }
                        assertEquals(mockItems.size, items.size)
                        assertEquals(mockItems, items)
                    }
                    is Response.Loading -> assert(true)
                    is Response.Failure -> assert(false)
                }
            }
    }

    @Test
    fun whenDeleteItemIsSuccessful() = runTest {
        itemDao.insert(mockItems.first())
        val itemForDelete = ItemUi.fromItem(itemDao.getItemById(1)!!)
        homeItemUiUseCase.deleteItem(itemForDelete)
            .collectUntil { response -> response is Response.Success  }
            .collect{ response ->
                when(response){
                    is Response.Success -> {
                        assertEquals(0, itemDao.getItems().first().size)
                    }

                    is Response.Loading -> assert(true)
                    is Response.Failure -> assert(false)
                }
            }
    }

}