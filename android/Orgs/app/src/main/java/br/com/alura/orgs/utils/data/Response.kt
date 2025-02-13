package br.com.alura.orgs.utils.data

import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlin.Exception

sealed class Response <out T>{
    data class Success <out T>(val result: T): Response<T>()
    data class Failure (val exception: Exception): Response<Nothing>()
    data object Loading: Response<Nothing>()
}

fun <T, S> Response<T>.mapTo(transform: (T) -> S): Response<S> {
    return when (this) {
        is Response.Success -> Response.Success(transform(this.result))
        is Response.Failure -> Response.Failure(this.exception)
        is Response.Loading -> Response.Loading
    }
}

inline fun <T, S> Response<T>.update(
    uiState: MutableStateFlow<S>,
    updateState: (S, Response<T>) -> S
) {
    uiState.update { state ->
        updateState(state, this)
    }
}

fun <T> Response<T>.showResults(
    successViewGroup: ViewGroup,
    loadingViewGroup: ViewGroup,
    failureViewGroup: ViewGroup,
    actionOnSuccess: (T) -> Unit = {},
    actionOnFailure: (Exception) -> Unit = {},
    delay: Long = 500
) {
    when (this) {
        is Response.Success -> {
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    successViewGroup.visibility = ViewGroup.VISIBLE
                    loadingViewGroup.visibility = ViewGroup.GONE
                    failureViewGroup.visibility = ViewGroup.GONE
                    actionOnSuccess(this.result)
                },
                delay
            )
        }

        is Response.Loading -> {
            successViewGroup.visibility = ViewGroup.GONE
            loadingViewGroup.visibility = ViewGroup.VISIBLE
            failureViewGroup.visibility = ViewGroup.GONE
            Handler(Looper.getMainLooper()).postDelayed({}, delay)
        }

        is Response.Failure -> {
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    successViewGroup.visibility = ViewGroup.GONE
                    loadingViewGroup.visibility = ViewGroup.GONE
                    failureViewGroup.visibility = ViewGroup.VISIBLE
                    actionOnFailure(this.exception)
                }, delay
            )
        }
    }
}

