package br.com.alura.orgs.utils

sealed class AccountException(override val message: String) : Exception() {
    class InvalidCredentials : AccountException(
        message = "Account doesn't exist or invalid credentials"
    )
}
