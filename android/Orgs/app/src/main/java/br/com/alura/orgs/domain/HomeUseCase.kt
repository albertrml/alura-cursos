package br.com.alura.orgs.domain

import br.com.alura.orgs.model.entity.Account
import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.model.repository.AccountRepository
import br.com.alura.orgs.model.repository.ItemRepository
import br.com.alura.orgs.utils.data.Authenticate
import br.com.alura.orgs.utils.data.Response
import br.com.alura.orgs.utils.data.mapTo
import br.com.alura.orgs.utils.data.SortedItem
import br.com.alura.orgs.utils.data.sortItemsBy
import br.com.alura.orgs.utils.exception.AccountException
import br.com.alura.orgs.utils.exception.ItemException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HomeUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val itemRepository: ItemRepository
)  {

    fun deleteItem(itemUi: ItemUi) = combine(
        accountRepository.auth,
        itemRepository.deleteItem(itemUi.toItem())
    ){ auth, delete ->
        when(auth){
            is Authenticate.Login -> {
                if (auth.account.username != itemUi.userOwner)
                    Response.Failure(ItemException.ItemBelongsToAnotherAccountException())
                else
                    delete
            }
            is Authenticate.Logoff -> Response.Failure(AccountException.AccountIsNotAuthenticated())
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun fetchAllItemUis(
        sortBy: SortedItem
    ) = accountRepository.auth
        .filterIsInstance<Authenticate.Login<Account>>()
        .flatMapConcat { auth ->
            itemRepository.getItemsByUserOwner(auth.account.username).map { response ->
                response.mapTo { items ->
                    items.sortItemsBy(sortBy).map { item -> ItemUi.fromItem(item) }
                }
            }
        }

    fun logout() = accountRepository.logout()

    fun onAuthChange(
        onLogin: () -> Unit,
        onLogoff: () -> Unit
    ) = accountRepository.auth.map {
        when(it){
            is Authenticate.Login -> onLogin()
            is Authenticate.Logoff -> onLogoff()
        }
    }
}