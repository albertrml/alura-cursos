package br.com.alura.orgs.view.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.alura.orgs.model.entity.Item
import br.com.alura.orgs.model.entity.emptyItem
import br.com.alura.orgs.model.repository.ItemRepository
import br.com.alura.orgs.utils.handleResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpdateViewModel @Inject constructor(private val repository: ItemRepository): ViewModel() {
    private val _uiState = MutableStateFlow(UpdateUiState())
    val uiState = _uiState.asStateFlow()

    fun onEvent(event: UpdateUiEvent){
        when(event) {
            is UpdateUiEvent.OnFetchItemById -> fetchItemById(event.itemId)
            is UpdateUiEvent.OnUpdate -> updateItem(event.item)
        }
    }

    private fun fetchItemById(itemId: Int) {
        viewModelScope.launch {
            repository.getItemById(itemId).collect{ response ->
                response.handleResponse(_uiState){ state, res ->
                    state.copy(fetchItemByIdState = res)
                }
            }
        }
    }

    private fun updateItem(item: Item) {
        viewModelScope.launch {
            repository.updateItem(item).collect{ response ->
                response.handleResponse(_uiState){ state, res ->
                    state.copy(updateState = res)
                }
            }
        }
    }

}