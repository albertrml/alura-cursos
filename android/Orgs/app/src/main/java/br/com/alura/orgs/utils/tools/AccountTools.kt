package br.com.alura.orgs.utils.tools

fun String.isPasswordValid(): Boolean {
    return length in 6..8 &&
           any { it.isDigit() } &&
           any { it.isUpperCase() }
}

fun String.isUsernameValid(): Boolean {
    val usernameRegex = Regex("^[a-zA-Z][a-zA-Z0-9]{2,19}$")
    return usernameRegex.matches(this.lowercase())
}

fun String.getUserName(): String { return this.lowercase() }