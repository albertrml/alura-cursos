package br.com.alura.orgs.view.insert

import br.com.alura.orgs.model.entity.Item
import br.com.alura.orgs.model.entity.emptyItem
import br.com.alura.orgs.utils.Response

data class InsertUiState (
    val insertState: Response<Unit> = Response.Loading
)