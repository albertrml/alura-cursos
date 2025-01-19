package br.com.alura.orgs.view.udf

import br.com.alura.orgs.model.entity.Item
import br.com.alura.orgs.utils.Response

data class UiState(
    val fetchAllItemsState: Response<List<Item>> = Response.Loading,
    val fetchItemByIdState: Response<Item> = Response.Loading,
    val deleteState: Response<Unit> = Response.Loading,
    val insertState: Response<Unit> = Response.Loading,
    val updateState: Response<Unit> = Response.Loading,
)