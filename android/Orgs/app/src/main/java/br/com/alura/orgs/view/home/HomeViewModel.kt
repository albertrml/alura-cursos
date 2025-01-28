package br.com.alura.orgs.view.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.alura.orgs.domain.HomeItemUiUseCase
import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.utils.SortType
import br.com.alura.orgs.utils.handleResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeItemUiUseCase
): ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        onEvent(HomeUiEvent.OnFetchAllItems())
    }

    fun onEvent(event: HomeUiEvent){
        when(event){
            is HomeUiEvent.OnDelete -> deleteItem(event.itemUi)
            is HomeUiEvent.OnFetchAllItems -> fetchAllItems(event.sortBy)
        }
    }

    private fun deleteItem(itemUi: ItemUi){
        viewModelScope.launch {
            repository.deleteItem(itemUi).collect { response ->
                response.handleResponse(_uiState) { state, res ->
                    state.copy(deleteState = res)
                }
            }
        }
    }

    private fun fetchAllItems(sortBy: SortType) {
        viewModelScope.launch {
            repository.fetchAllItemUis(sortBy).collect { response ->
                response.handleResponse(_uiState) { state, res ->
                    state.copy(fetchAllItemsState = res)
                }
            }
        }
    }

}
