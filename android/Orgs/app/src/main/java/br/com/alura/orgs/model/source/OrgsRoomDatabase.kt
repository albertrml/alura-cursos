package br.com.alura.orgs.model.source

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import br.com.alura.orgs.model.entity.Account
import br.com.alura.orgs.model.entity.Item

@Database(entities = [Item::class, Account::class], version = 2, exportSchema = true)
abstract class OrgsRoomDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDAO
    abstract fun accountDao(): AccountDAO

    companion object {
        @Volatile
        private var INSTANCE: OrgsRoomDatabase? = null

        fun getDatabase(ctx: Context): OrgsRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    ctx.applicationContext,
                    OrgsRoomDatabase::class.java,
                    "item_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}