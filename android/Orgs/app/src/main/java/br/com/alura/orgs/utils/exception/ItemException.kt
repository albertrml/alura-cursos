package br.com.alura.orgs.utils.exception

sealed class ItemException(override val message: String) : Exception() {
    class ItemNotFoundException : ItemException(
        message = "Item not found"
    )
}