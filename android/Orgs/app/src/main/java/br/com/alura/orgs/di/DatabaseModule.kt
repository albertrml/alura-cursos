package br.com.alura.orgs.di

import android.content.Context
import br.com.alura.orgs.model.source.AccountDAO
import br.com.alura.orgs.model.source.ItemDAO
import br.com.alura.orgs.model.source.OrgsRoomDatabase
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
    fun provideItemDatabase(@ApplicationContext context: Context): OrgsRoomDatabase {
        return OrgsRoomDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideItemDao(database: OrgsRoomDatabase): ItemDAO {
        return database.itemDao()
    }


    @Provides
    @Singleton
    fun provideAccountDao(database: OrgsRoomDatabase): AccountDAO {
        return database.accountDao()
    }

}