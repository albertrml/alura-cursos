package br.com.alura.orgs.view.home

import br.com.alura.orgs.model.entity.Item

sealed class HomeUiEvent {
    data object OnFetchAllItems : HomeUiEvent()
    data class OnDelete(val item: Item) : HomeUiEvent()
}