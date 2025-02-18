package br.com.alura.orgs.viewmodel.home

import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.utils.data.SortedItem

sealed class HomeUiEvent {
    data object OnFetchAllItemsByIdAscending : HomeUiEvent()
    data object OnFetchAllItemsByNameAscending : HomeUiEvent()
    data object OnFetchAllItemsByPriceAscending : HomeUiEvent()
    data object OnFetchAllItemsByQuantityDescending : HomeUiEvent()
    data class OnDelete(val itemUi: ItemUi) : HomeUiEvent()
}