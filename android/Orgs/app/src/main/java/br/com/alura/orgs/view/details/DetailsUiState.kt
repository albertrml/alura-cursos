package br.com.alura.orgs.view.details

import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.utils.Response

data class DetailsUiState (
    val fetchItemByIdState: Response<ItemUi> = Response.Loading
)