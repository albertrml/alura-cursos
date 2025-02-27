package br.com.alura.orgs.model.source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.alura.orgs.model.entity.Item
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDAO {
    @Insert (onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(item: Item)

    @Update
    suspend fun update(item: Item)

    @Delete
    suspend fun delete(item: Item)

    @Query("Select * from item where id = :id")
    suspend fun getItemById(id: Int): Item?

    @Query("SELECT * FROM item WHERE user_owner = :userOwner")
    fun getItemsByUserOwner(userOwner: String): Flow<List<Item>>

    @Query("Select * from item Order by name ASC")
    fun getItems(): Flow<List<Item>>

}