package br.com.alura.orgs.usecase

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import br.com.alura.orgs.domain.InsertItemUiUseCase
import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.model.mock.mockItems
import br.com.alura.orgs.model.repository.ItemRepository
import br.com.alura.orgs.model.source.ItemDAO
import br.com.alura.orgs.model.source.OrgsRoomDatabase
import br.com.alura.orgs.utils.data.Response
import br.com.alura.orgs.utils.tools.collectUntil
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class InsertItemUiUseCaseTest {

    private lateinit var useCase: InsertItemUiUseCase
    private lateinit var repository: ItemRepository
    private lateinit var dao: ItemDAO
    private lateinit var db: OrgsRoomDatabase

    @Before
    fun setup(){
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room
            .inMemoryDatabaseBuilder(context, OrgsRoomDatabase::class.java)
            .build()

        dao = db.itemDao()
        repository = ItemRepository(dao)
        useCase = InsertItemUiUseCase(repository)
    }

    @Before
    fun setupTestData() = runTest { db.clearAllTables() }

    @After
    fun closeDatabase() { db.close() }

    @Test
    fun whenInsertItemIsSuccessful() = runTest {
        val expectedItemUi = ItemUi.fromItem(mockItems.first())
        useCase.insertItemUi(expectedItemUi)
            .collectUntil { response -> response is Response.Success }
            .collect{ response ->
                when(response){
                    is Response.Success -> {
                        val itemUiFromDatabase = ItemUi.fromItem(dao.getItemById(1)!!)
                        assertEquals(1, dao.getItems().first().size)
                        assert(expectedItemUi.id < itemUiFromDatabase.id)
                        assertEquals(expectedItemUi, itemUiFromDatabase.copy(id = 0))
                    }
                    is Response.Loading -> assert(true)
                    is Response.Failure -> assert(false)
                }
            }
    }

    @Test
    fun whenInsertItemIsUnsuccessful() = runTest {
        dao.insert(mockItems.first())
        val itemUiFromDatabase = ItemUi.fromItem(dao.getItemById(1)!!)
        useCase.insertItemUi(itemUiFromDatabase)
            .collectUntil { response -> response is Response.Failure }
            .collect{ response ->
                when(response){
                    is Response.Success -> assert(false)
                    is Response.Loading -> assert(true)
                    is Response.Failure -> {
                        assert( response.exception is SQLiteConstraintException)
                    }
                }
            }
    }

}