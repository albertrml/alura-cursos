package br.com.alura.orgs.viewmodel.account

import br.com.alura.orgs.utils.data.SortedAccount

sealed class AccountUiEvent {
    data class OnAuthenticate(val username: String, val password: String) : AccountUiEvent()
    data class OnCreateAccount(val username: String, val password: String) : AccountUiEvent()
    data class OnIsUsernameExists(val username: String) : AccountUiEvent()
    data class OnLogout(val onLogout: () -> Unit) : AccountUiEvent()
    data class OnUpdatePassword(val password: String) : AccountUiEvent()
    data object OnDeleteAccount : AccountUiEvent()
    data class OnGetAccounts(
        val sortedBy: SortedAccount = SortedAccount.ByUsernameAscending
    ) : AccountUiEvent()
}