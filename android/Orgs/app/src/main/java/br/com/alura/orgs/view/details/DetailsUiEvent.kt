package br.com.alura.orgs.view.details

sealed class DetailsUiEvent {
    data class OnFetchItemById(val itemId: Int): DetailsUiEvent()
}