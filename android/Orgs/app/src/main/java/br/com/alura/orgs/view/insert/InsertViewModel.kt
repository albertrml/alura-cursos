package br.com.alura.orgs.view.insert

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.alura.orgs.domain.InsertItemUiUseCase
import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.utils.handleResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InsertViewModel @Inject constructor(
    private val repository: InsertItemUiUseCase
): ViewModel() {
    private val _uiState = MutableStateFlow(InsertUiState())
    val uiState = _uiState.asStateFlow()

    fun onEvent(event: InsertUiEvent){
        when(event) {
            is InsertUiEvent.OnInsert -> onInsert(event.item)
            is InsertUiEvent.OnSaveUrlImage -> saveUrlImage(event.url)
        }
    }

    private fun onInsert(itemUi: ItemUi) {
        viewModelScope.launch {
            repository.insertItemUi(itemUi).collect{ response ->
                response.handleResponse(_uiState){ state, res ->
                    state.copy(insertState = res)
                }
            }
        }
    }

    private fun saveUrlImage(url: String) {
        _uiState.value = _uiState.value.copy(urlImage = url)
    }

}