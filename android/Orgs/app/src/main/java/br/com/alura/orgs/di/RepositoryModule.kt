package br.com.alura.orgs.di

import br.com.alura.orgs.model.repository.AccountRepository
import br.com.alura.orgs.model.repository.ItemRepository
import br.com.alura.orgs.model.source.AccountDAO
import br.com.alura.orgs.model.source.ItemDAO
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideItemRepository(itemDao: ItemDAO): ItemRepository {
        return ItemRepository(itemDao)
    }

    @Provides
    @Singleton
    fun provideAccountRepository(accountDao: AccountDAO): AccountRepository {
        return AccountRepository(accountDao)
    }

}