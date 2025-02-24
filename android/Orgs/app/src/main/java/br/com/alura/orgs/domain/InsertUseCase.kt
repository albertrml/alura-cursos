package br.com.alura.orgs.domain

import android.util.Log
import br.com.alura.orgs.model.entity.Account
import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.model.repository.AccountRepository
import br.com.alura.orgs.model.repository.ItemRepository
import br.com.alura.orgs.utils.data.Authenticate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapConcat
import javax.inject.Inject

class InsertUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val itemRepository: ItemRepository
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    fun insertItemUi(itemUi: ItemUi) = accountRepository.auth
        .filterIsInstance<Authenticate.Login<Account>>()
        .flatMapConcat { auth ->
            Log.i("InsertUseCase", "insertItemUi: ${auth.account}")
            val newItem = itemUi.copy(userOwner = auth.account.username).toItem()
            itemRepository.insertItem(newItem)
        }

}