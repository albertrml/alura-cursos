package br.com.alura.orgs.view.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.alura.orgs.model.entity.Item
import br.com.alura.orgs.model.repository.ItemRepository
import br.com.alura.orgs.utils.handleResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val repository: ItemRepository): ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        onEvent(HomeUiEvent.OnFetchAllItems)
    }

    fun onEvent(event: HomeUiEvent){
        when(event){
            is HomeUiEvent.OnDelete -> deleteItem(event.item)
            is HomeUiEvent.OnFetchAllItems -> fetchAllItems()
        }
    }

    private fun deleteItem(item: Item){
        viewModelScope.launch {
            repository.deleteItem(item).collect { response ->
                response.handleResponse(_uiState) { state, res ->
                    state.copy(deleteState = res)
                }
            }
        }
    }

    private fun fetchAllItems() {
        viewModelScope.launch {
            repository.getAllItems().collect { response ->
                response.handleResponse(_uiState) { state, res ->
                    state.copy(fetchAllItemsState = res)
                }
            }
        }
    }

}
