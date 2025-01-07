package br.com.alura.orgs

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.com.alura.orgs.model.entity.mockItems
import br.com.alura.orgs.model.source.ItemDAO
import br.com.alura.orgs.model.source.ItemRoomDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class TestItemDatabase {
    private lateinit var itemDAO: ItemDAO
    private lateinit var db: ItemRoomDatabase

    @Before
    fun createDb(){
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            ctx,
            ItemRoomDatabase::class.java
        ).build()
        itemDAO = db.itemDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb(){
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun allOperations() = runBlocking {
        mockItems.forEach { itemDAO.insert(it) }
        val allItems = itemDAO.getItems().first()
        assert(allItems.size == mockItems.size)

        val item = itemDAO.getItemById(1).first()
        itemDAO.delete(item)
        val allItemsAfterDelete = itemDAO.getItems().first()
        assert(allItemsAfterDelete.size == mockItems.size - 1)

        val itemBeforeUpdate = itemDAO.getItemById(2).first()
        val itemAfterUpdate = itemBeforeUpdate.copy(
            itemName = "Pinaple",
            itemValue = 10.0,
            quantityInStock = 10
        )
        Log.e("itemBeforeUpdate", itemBeforeUpdate.toString())
        Log.e("itemAfterUpdate", itemAfterUpdate.toString())
        itemDAO.update(itemAfterUpdate)
        val allItemsAfterUpdate = itemDAO.getItems().first()
        assert(allItemsAfterUpdate.find {it.itemName == "Pinaple"} != null)
    }
}