package br.com.alura.orgs.viewmodel.details

sealed class DetailsUiEvent {
    data class OnFetchItemById(val itemId: Int): DetailsUiEvent()
}