package br.com.alura.orgs.di

import br.com.alura.orgs.domain.DetailsItemUiUseCase
import br.com.alura.orgs.model.repository.ItemRepository
import br.com.alura.orgs.domain.HomeItemUiUseCase
import br.com.alura.orgs.domain.InsertItemUiUseCase
import br.com.alura.orgs.domain.UpdateItemUiUseCase
import br.com.alura.orgs.model.repository.AccountRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideInsertItemUiUseCase(
        accountRepository: AccountRepository,
        itemRepository: ItemRepository
    ): InsertItemUiUseCase {
        return InsertItemUiUseCase(accountRepository,itemRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateItemUiUseCase(itemRepository: ItemRepository): UpdateItemUiUseCase {
        return UpdateItemUiUseCase(itemRepository)
    }

    @Provides
    @Singleton
    fun provideHomeItemUiUseCase(
        accountRepository: AccountRepository,
        itemRepository: ItemRepository
    ): HomeItemUiUseCase {
        return HomeItemUiUseCase(accountRepository, itemRepository)
    }

    @Provides
    @Singleton
    fun provideDetailsItemUiUseCase(itemRepository: ItemRepository): DetailsItemUiUseCase {
        return DetailsItemUiUseCase(itemRepository)
    }

}