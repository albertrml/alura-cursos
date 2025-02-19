package br.com.alura.orgs.utils.exception

sealed class AccountException(override val message: String) : Exception() {
    class InvalidCredentials : AccountException(
        message = "Account doesn't exist or invalid credentials"
    )
    class UsernameAlreadyExists : AccountException(
        message = "Username already exists"
    )
    class InvalidPassword : AccountException(
        message = "Password must have between 6 and 12 alphanumeric characters " +
                  "with one digit and one uppercase letter"
    )

    class AccountIsNotAuthenticated : AccountException(
        message = "Account is not authenticated"
    )

    class InvalidUsername : AccountException(
        message = "Username must have at least 3 characters and must start with a letter. " +
                  "Only letters and numbers are allowed"
    )

    class PasswordIsTheSame : AccountException(
        message = "Password is the same as the current one"
    )

    class UpdatedPasswordCantBeDone : AccountException(
        message = "Updated password can't be done"
    )

    class AccountBelongsToAnotherUser : AccountException(
        message = "Account belongs to other user"
    )

    class AccountIsTheSame : AccountException(
        message = "Account is the same"
    )
}
