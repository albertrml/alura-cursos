package br.com.alura.orgs.view.viemodel

import br.com.alura.orgs.model.entity.Item
import br.com.alura.orgs.utils.Response

data class ItemUiState(
    val items: List<Item> = emptyList(),
    val fetchAllItemsState: Response<Unit> = Response.Loading,
    val fetchItemByIdState: Response<Item> = Response.Loading,
    val deleteState: Response<Unit> = Response.Loading,
    val insertState: Response<Unit> = Response.Loading,
    val updateState: Response<Unit> = Response.Loading,
)