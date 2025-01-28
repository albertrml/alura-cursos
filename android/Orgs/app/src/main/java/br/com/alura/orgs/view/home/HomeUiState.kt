package br.com.alura.orgs.view.home

import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.utils.Response

data class HomeUiState(
    val fetchAllItemsState: Response<List<ItemUi>> = Response.Loading,
    val deleteState: Response<Unit> = Response.Loading
)