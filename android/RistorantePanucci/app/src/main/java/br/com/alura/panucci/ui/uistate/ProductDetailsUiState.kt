package br.com.alura.panucci.ui.uistate

import androidx.compose.runtime.Composable
import br.com.alura.panucci.model.Product

sealed class ProductDetailsUiState{
    object Loading: ProductDetailsUiState()
    data class Success(val product: Product): ProductDetailsUiState()
    object Failure: ProductDetailsUiState()


    @Composable
    fun ShowComposableByResult(
        onLoading: @Composable () -> Unit,
        onSuccess: @Composable (Product) -> Unit,
        onFailure: @Composable () -> Unit
    ){
        when(this){
            is Loading -> onLoading()
            is Success -> onSuccess(product)
            is Failure -> onFailure()
        }
    }
}