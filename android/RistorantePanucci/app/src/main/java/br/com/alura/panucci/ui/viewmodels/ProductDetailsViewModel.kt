package br.com.alura.panucci.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.alura.panucci.model.dao.ProductDao
import br.com.alura.panucci.ui.uistate.ProductDetailsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProductDetailsViewModel(
    private val dao: ProductDao = ProductDao()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductDetailsUiState>(ProductDetailsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun findProductById(id: String) {
        viewModelScope.launch {
            _uiState.update { ProductDetailsUiState.Loading }
            dao.findById(id)?.let { product ->
                _uiState.update {ProductDetailsUiState.Success(product) }
            } ?: run { _uiState.update { ProductDetailsUiState.Failure } }
        }
    }

}