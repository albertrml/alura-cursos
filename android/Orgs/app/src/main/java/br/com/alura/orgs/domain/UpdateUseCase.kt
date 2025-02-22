package br.com.alura.orgs.domain

import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.model.repository.AccountRepository
import br.com.alura.orgs.model.repository.ItemRepository
import br.com.alura.orgs.utils.data.Authenticate
import br.com.alura.orgs.utils.data.Response
import br.com.alura.orgs.utils.data.mapTo
import br.com.alura.orgs.utils.exception.AccountException
import br.com.alura.orgs.utils.exception.ItemException
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class UpdateUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val itemRepository: ItemRepository
) {

    fun fetchItemUiById(itemUiId: Int) = itemRepository.getItemById(itemUiId).map { response ->
        response.mapTo { item -> ItemUi.fromItem(item) }
    }

    fun updateItemUi(itemUi: ItemUi) = combine(
        accountRepository.auth,
        itemRepository.updateItem(itemUi.toItem())
    ) { auth, update ->
        when(auth){
            is Authenticate.Login -> {
                if(auth.account.username == itemUi.userOwner)
                    update
                else
                    Response.Failure(ItemException.ItemBelongsToAnotherAccountException())
            }
            is Authenticate.Logoff ->
                Response.Failure(AccountException.AccountIsNotAuthenticated())
        }
    }

}