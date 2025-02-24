package br.com.alura.orgs.utils.exception

sealed class ItemException(override val message: String) : Exception() {
    class ItemNotFoundException : ItemException(
        message = "Item not found"
    )
    class InvalidNameException : ItemException(
        message = "Invalid name"
    )
    class InvalidDescriptionException : ItemException(
        message = "Invalid description"
    )
    class InvalidValueException : ItemException(
        message = "Invalid value"
    )
    class InvalidQuantityException : ItemException(
        message = "Invalid quantity"
    )
    class InvalidUserOwnerException : ItemException(
        message = "Invalid user owner"
    )

    class ItemBelongsToAnotherAccountException: ItemException(
        message = "Item is not owner"
    )

}