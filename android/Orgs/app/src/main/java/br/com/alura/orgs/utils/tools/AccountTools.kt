package br.com.alura.orgs.utils.tools

fun String.isPasswordValid(): Boolean {
    return length >= 6 &&
           any { it.isDigit() } &&
           any { it.isUpperCase() }
}