package br.com.alura.orgs.di

import android.content.Context
import br.com.alura.orgs.model.repository.ItemRepository
import br.com.alura.orgs.model.source.ItemDAO
import br.com.alura.orgs.model.source.ItemRoomDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideItemDatabase(@ApplicationContext context: Context): ItemRoomDatabase {
        return ItemRoomDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideItemDao(database: ItemRoomDatabase): ItemDAO {
        return database.itemDao()
    }

    @Provides
    @Singleton
    fun provideItemRepository(itemDao: ItemDAO): ItemRepository {
        return ItemRepository(itemDao)
    }
}