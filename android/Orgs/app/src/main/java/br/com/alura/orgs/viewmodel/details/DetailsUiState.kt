package br.com.alura.orgs.viewmodel.details

import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.utils.data.Response

data class DetailsUiState (
    val fetchItemByIdState: Response<ItemUi> = Response.Loading
)