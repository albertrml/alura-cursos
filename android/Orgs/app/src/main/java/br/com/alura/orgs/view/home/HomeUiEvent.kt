package br.com.alura.orgs.view.home

import br.com.alura.orgs.model.entity.ItemUi

sealed class HomeUiEvent {
    data object OnFetchAllItems : HomeUiEvent()
    data class OnDelete(val itemUi: ItemUi) : HomeUiEvent()
}