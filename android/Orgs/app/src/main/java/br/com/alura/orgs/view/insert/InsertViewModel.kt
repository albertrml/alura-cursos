package br.com.alura.orgs.view.insert

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
class InsertViewModel @Inject constructor(private val repository: ItemRepository): ViewModel() {
    private val _uiState = MutableStateFlow(InsertUiState())
    val uiState = _uiState.asStateFlow()

    fun onEvent(event: InsertUiEvent){
        when(event) {
            is InsertUiEvent.OnInsert -> onInsert(event.item)
        }
    }

    private fun onInsert(item: Item) {
        viewModelScope.launch {
            repository.insertItem(item).collect{ response ->
                response.handleResponse(_uiState){ state, res ->
                    state.copy(insertState = res)
                }
            }
        }
    }

}