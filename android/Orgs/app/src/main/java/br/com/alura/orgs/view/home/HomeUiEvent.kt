package br.com.alura.orgs.view.home

import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.utils.SortType

sealed class HomeUiEvent {
    data class OnFetchAllItems(val sortBy: SortType = SortType.ByIdAscending) : HomeUiEvent()
    data class OnDelete(val itemUi: ItemUi) : HomeUiEvent()
}