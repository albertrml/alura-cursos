package br.com.alura.orgs.view.insert

import br.com.alura.orgs.utils.Response

data class InsertUiState (
    val urlImage: String = "",
    val insertState: Response<Unit> = Response.Loading
)