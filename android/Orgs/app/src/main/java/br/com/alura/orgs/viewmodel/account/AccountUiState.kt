package br.com.alura.orgs.viewmodel.account

import br.com.alura.orgs.utils.data.Response

data class AccountUiState(
    val authenticateState: Response<Unit> = Response.Loading,
    val createAccountState: Response<Unit> = Response.Loading,
    val deleteAccountState: Response<Unit> = Response.Loading,
    val isUsernameExistsState: Response<Boolean> = Response.Loading,
    val readAccountsState: Response<List<String>> = Response.Loading,
    val updateAccountState: Response<Unit> = Response.Loading
)
