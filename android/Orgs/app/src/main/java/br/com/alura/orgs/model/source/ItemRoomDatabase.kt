package br.com.alura.orgs.model.source

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import br.com.alura.orgs.model.entity.Account
import br.com.alura.orgs.model.entity.Item

@Database(entities = [Item::class, Account::class], version = 3, exportSchema = true)
abstract class ItemRoomDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDAO
    abstract fun accountDao(): AccountDAO

    companion object {
        @Volatile
        private var INSTANCE: ItemRoomDatabase? = null

        fun getDatabase(ctx: Context): ItemRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    ctx.applicationContext,
                    ItemRoomDatabase::class.java,
                    "item_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}