package br.com.alura.orgs.domain

import br.com.alura.orgs.model.entity.Account
import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.model.repository.AccountRepository
import br.com.alura.orgs.model.repository.ItemRepository
import br.com.alura.orgs.utils.data.Authenticate
import br.com.alura.orgs.utils.data.mapTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class UpdateUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val itemRepository: ItemRepository
) {

    fun fetchItemUiById(itemUiId: Int) = itemRepository.getItemById(itemUiId).map { response ->
        response.mapTo { item -> ItemUi.fromItem(item) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun updateItemUi(itemUi: ItemUi) = accountRepository.auth
        .filterIsInstance<Authenticate.Login<Account>>()
        .flatMapConcat {
            itemRepository.updateItem(itemUi.toItem())
        }

}