package br.com.alura.orgs.viewmodel.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.alura.orgs.model.repository.AccountRepository
import br.com.alura.orgs.utils.data.Authenticate
import br.com.alura.orgs.utils.data.Response
import br.com.alura.orgs.utils.data.handleResponse
import br.com.alura.orgs.utils.exception.AccountException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val repository: AccountRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<AccountUiState> = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    fun onEvent(event: AccountUiEvent) {
        when (event) {
            is AccountUiEvent.OnAuthenticate -> authenticate(event.username, event.password)
            is AccountUiEvent.OnCreateAccount -> createAccount(event.username, event.password)
            is AccountUiEvent.OnDeleteAccount -> deleteAccount()
            is AccountUiEvent.OnGetAccounts -> getAccounts()
            is AccountUiEvent.OnIsUsernameExists -> isUsernameExists(event.username)
            is AccountUiEvent.OnLogout -> logout(event.onLogout)
            is AccountUiEvent.OnUpdatePassword -> updatePassword(event.password)
        }
    }

    private fun authenticate(username: String, password: String) {
        viewModelScope.launch {
            repository.authenticate(username, password).collect { response ->
                response.handleResponse(_uiState){ state, res ->
                    state.copy(authenticateState = res)
                }
            }
        }
    }

    private fun createAccount(username: String, password: String) {
        viewModelScope.launch {
            repository.createAccount(username, password).collect { response ->
                response.handleResponse(_uiState){ state, res ->
                    state.copy(createAccountState = res)
                }
            }
        }
    }

    private fun deleteAccount() {
        viewModelScope.launch {
            val auth = repository.auth.first()
            if (auth is Authenticate.Login) {
                repository.deleteAccount(auth.account).collect { response ->
                    response.handleResponse(_uiState) { state, res ->
                        state.copy(deleteAccountState = res)
                    }
                }
            }
            else {
                _uiState.update { state ->
                    state.copy(
                        deleteAccountState = Response.Failure(
                            AccountException.AccountIsNotAuthenticated()
                        )
                    )
                }
            }
        }
    }

    private fun getAccounts() {
        viewModelScope.launch {
            repository.getAllUsernames().collect { response ->
                response.handleResponse(_uiState){ state, res ->
                    state.copy(readAccountsState = res)
                }
            }
        }
    }

    private fun isUsernameExists(username: String) {
        viewModelScope.launch {
            repository.isUsernameExists(username).collect { response ->
                response.handleResponse(_uiState){ state, res ->
                    state.copy(isUsernameExistsState = res)
                }
            }
        }
    }

    private fun logout(onLogout: () -> Unit) {
        viewModelScope.launch {
            _uiState.emit(AccountUiState())
            onLogout()
        }
    }

    private fun updatePassword(password: String) {
        viewModelScope.launch {
            repository.updatePassword(password).collect { response ->
                response.handleResponse(_uiState){ state, res ->
                    state.copy(updateAccountState = res)
                }
            }
        }
    }

}