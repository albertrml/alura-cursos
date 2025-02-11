package br.com.alura.orgs.viewmodel.home

import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.utils.data.SortedItem

sealed class HomeUiEvent {
    data class OnFetchAllItems(val sortBy: SortedItem = SortedItem.ByIdAscending) : HomeUiEvent()
    data class OnDelete(val itemUi: ItemUi) : HomeUiEvent()
}