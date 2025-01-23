package br.com.alura.orgs.view.update

import br.com.alura.orgs.model.entity.Item
import br.com.alura.orgs.utils.Response

data class UpdateUiState (
    val updateState: Response<Unit> = Response.Loading,
    val fetchItemByIdState: Response<Item> = Response.Loading
)