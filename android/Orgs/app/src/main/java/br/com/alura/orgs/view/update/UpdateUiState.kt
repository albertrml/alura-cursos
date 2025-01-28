package br.com.alura.orgs.view.update

import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.utils.Response

data class UpdateUiState (
    val urlImage: String = "",
    val updateState: Response<Unit> = Response.Loading,
    val fetchItemByIdState: Response<ItemUi> = Response.Loading
)