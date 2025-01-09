package br.com.alura.orgs.view.viemodel

import br.com.alura.orgs.model.entity.Item
import br.com.alura.orgs.utils.Response

data class ItemUiState(
    val items: List<Item> = emptyList(),
    val decreaseState: Response<Unit> = Response.Loading,
    val deleteState: Response<Unit> = Response.Loading,
    val fetchState: Response<Unit> = Response.Loading,
    val fetchByIdState: Response<Item> = Response.Loading,
    val increaseState: Response<Unit> = Response.Loading,
    val insertState: Response<Unit> = Response.Loading,
    val updateState: Response<Unit> = Response.Loading,
)