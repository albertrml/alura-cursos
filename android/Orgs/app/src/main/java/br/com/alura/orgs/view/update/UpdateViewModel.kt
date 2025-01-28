package br.com.alura.orgs.view.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.alura.orgs.domain.UpdateItemUiUseCase
import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.utils.Response
import br.com.alura.orgs.utils.handleResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val repository: UpdateItemUiUseCase
): ViewModel() {

    private val _uiState = MutableStateFlow(UpdateUiState())
    val uiState = _uiState.asStateFlow()

    fun onEvent(event: UpdateUiEvent){
        when(event) {
            is UpdateUiEvent.OnFetchItemById -> fetchItemById(event.itemId)
            is UpdateUiEvent.OnSaveUrlImage -> saveUrlImage(event.url)
            is UpdateUiEvent.OnUpdate -> updateItem(event.itemUi)
        }
    }

    private fun fetchItemById(itemUiId: Int) {
        viewModelScope.launch {
            repository.fetchItemUiById(itemUiId).collect{ response ->
                response.handleResponse(_uiState){ state, res ->
                    state.copy(fetchItemByIdState = res)
                }
                if(response is Response.Success){ saveUrlImage(url = response.result.itemUrl) }
            }
        }
    }

    private fun updateItem(itemUi: ItemUi) {
        viewModelScope.launch {
            repository.updateItemUi(itemUi).collect{ response ->
                response.handleResponse(_uiState){ state, res ->
                    state.copy(updateState = res)
                }
            }
        }
    }

    private fun saveUrlImage(url: String) {
        _uiState.value = _uiState.value.copy(urlImage = url)
    }

}