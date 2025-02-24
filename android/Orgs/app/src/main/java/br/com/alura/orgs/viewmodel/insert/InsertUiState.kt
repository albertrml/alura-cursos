package br.com.alura.orgs.viewmodel.insert

import br.com.alura.orgs.utils.data.Response

data class InsertUiState (
    val urlImage: String = "",
    val insertState: Response<Unit> = Response.Loading
)