package br.com.alura.orgs.viewmodel.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.alura.orgs.domain.HomeItemUiUseCase
import br.com.alura.orgs.model.entity.Account
import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.model.repository.AccountRepository
import br.com.alura.orgs.utils.data.Authenticate
import br.com.alura.orgs.utils.data.SortedItem
import br.com.alura.orgs.utils.data.update
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val homeItemUiUseCase: HomeItemUiUseCase
): ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            accountRepository.auth.collectLatest { auth ->
                when(auth){
                    is Authenticate.Login -> {
                        onEvent(HomeUiEvent.OnFetchAllItemsByIdAscending)
                    }
                    is Authenticate.Logoff -> {
                        _uiState.update { HomeUiState() }
                    }
                }
            }
        }
    }

    fun onEvent(event: HomeUiEvent){
        when(event){
            is HomeUiEvent.OnDelete -> deleteItem(event.itemUi)
            is HomeUiEvent.OnFetchAllItemsByIdAscending ->
                fetchAllItemsByAccount(sortBy = SortedItem.ByIdAscending)
            is HomeUiEvent.OnFetchAllItemsByNameAscending ->
                fetchAllItemsByAccount(sortBy = SortedItem.ByNameAscending)
            is HomeUiEvent.OnFetchAllItemsByPriceAscending ->
                fetchAllItemsByAccount(sortBy = SortedItem.ByPriceAscending)
            is HomeUiEvent.OnFetchAllItemsByQuantityDescending ->
                fetchAllItemsByAccount(sortBy = SortedItem.ByQuantityDescending)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun deleteItem(itemUi: ItemUi){
        viewModelScope.launch {
            accountRepository.auth
                .filterIsInstance<Authenticate.Login<Account>>()
                .map { it.account.username }
                .flatMapLatest { username ->
                    homeItemUiUseCase.deleteItem(username,itemUi)
                }
                .collect { response ->
                    response.update(_uiState) { state, res ->
                        state.copy(deleteState = res)
                    }
                }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun fetchAllItemsByAccount(sortBy: SortedItem){
        viewModelScope.launch {
            accountRepository.auth
                .filterIsInstance<Authenticate.Login<Account>>()
                .map { it.account.username }
                .flatMapLatest { username ->
                    homeItemUiUseCase.fetchAllItemUis(username, sortBy)
                }
                .collect { response ->
                    response.update(_uiState) { state, res ->
                        state.copy(fetchAllItemsState = res)
                    }
                }
        }
    }

}
