package br.com.alura.orgs.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.lang.Exception

sealed class Response <out T>{
    data class Success <out T>(val result: T): Response<T>()
    data class Failure (val exception: Exception): Response<Nothing>()
    data object Loading: Response<Nothing>()
}

fun <T, S> Response<T>.handleResponse(
    uiState: MutableStateFlow<S>,
    updateState: (S, Response<T>) -> S
) {
    uiState.update { state ->
        updateState(state, this)
    }
}

fun <T,S> Response<T>.mapTo(data: S) = when(this){
    is Response.Success -> Response.Success(data)
    is Response.Failure -> Response.Failure(this.exception)
    is Response.Loading -> Response.Loading
}