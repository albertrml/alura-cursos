package br.com.alura.orgs.viewmodel.home

import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.utils.data.SortType

sealed class HomeUiEvent {
    data class OnFetchAllItems(val sortBy: SortType = SortType.ByIdAscending) : HomeUiEvent()
    data class OnDelete(val itemUi: ItemUi) : HomeUiEvent()
}