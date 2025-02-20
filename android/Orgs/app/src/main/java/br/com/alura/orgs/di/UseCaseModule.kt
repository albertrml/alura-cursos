package br.com.alura.orgs.di

import br.com.alura.orgs.domain.DetailsItemUiUseCase
import br.com.alura.orgs.model.repository.ItemRepository
import br.com.alura.orgs.domain.HomeUseCase
import br.com.alura.orgs.domain.InsertUseCase
import br.com.alura.orgs.domain.UpdateUseCase
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
    ): InsertUseCase {
        return InsertUseCase(accountRepository,itemRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateItemUiUseCase(
        accountRepository: AccountRepository,
        itemRepository: ItemRepository
    ): UpdateUseCase {
        return UpdateUseCase(accountRepository,itemRepository)
    }

    @Provides
    @Singleton
    fun provideHomeItemUiUseCase(
        accountRepository: AccountRepository,
        itemRepository: ItemRepository
    ): HomeUseCase {
        return HomeUseCase(accountRepository, itemRepository)
    }

    @Provides
    @Singleton
    fun provideDetailsItemUiUseCase(itemRepository: ItemRepository): DetailsItemUiUseCase {
        return DetailsItemUiUseCase(itemRepository)
    }

}