package br.com.alura.panucci.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.com.alura.panucci.model.dao.ProductDao
import br.com.alura.panucci.navigation.destionation.productIdArgument
import br.com.alura.panucci.ui.uistate.ProductDetailsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProductDetailsViewModel(
    private val dao: ProductDao = ProductDao(),
    saveStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductDetailsUiState>(ProductDetailsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            saveStateHandle.getStateFlow<String?>(productIdArgument, null)
                .filterNotNull()
                .collect { id -> findProductById(id) }
        }
    }

    fun findProductById(id: String) {
        viewModelScope.launch {
            _uiState.update { ProductDetailsUiState.Loading }
            dao.findById(id)?.let { product ->
                _uiState.update { ProductDetailsUiState.Success(product) }
            } ?: run { _uiState.update { ProductDetailsUiState.Failure } }
        }
    }

    companion object{
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer{
                val savedStateHandle = createSavedStateHandle()
                ProductDetailsViewModel(
                    dao = ProductDao(),
                    saveStateHandle = savedStateHandle
                )
            }
        }
    }

}