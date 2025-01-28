package br.com.alura.orgs.view.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.alura.orgs.domain.DetailsItemUiUseCase
import br.com.alura.orgs.utils.handleResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val repository: DetailsItemUiUseCase
): ViewModel(){

    private val _uiState = MutableStateFlow(DetailsUiState())
    val uiState = _uiState.asStateFlow()

    fun onEvent(event: DetailsUiEvent){
        when(event) {
            is DetailsUiEvent.OnFetchItemById -> fetchItemById(event.itemId)
        }
    }

    private fun fetchItemById(itemId: Int) {
        viewModelScope.launch {
            repository.fetchItemById(itemId).collect{ response ->
                response.handleResponse(_uiState){ state, res ->
                    state.copy( fetchItemByIdState = res)
                }
            }
        }
    }

}