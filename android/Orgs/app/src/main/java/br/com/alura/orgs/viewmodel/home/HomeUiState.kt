package br.com.alura.orgs.viewmodel.home

import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.utils.data.Response

data class HomeUiState(
    val fetchAllItemsState: Response<List<ItemUi>> = Response.Loading,
    val deleteState: Response<Unit> = Response.Loading
)