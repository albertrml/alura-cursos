package br.com.alura.orgs.model.entity

import kotlinx.coroutines.flow.MutableStateFlow

data class ItemUi(
    val id: Int = 0,
    val itemName: MutableStateFlow<String> = MutableStateFlow(""),
    val itemDescription: MutableStateFlow<String> = MutableStateFlow(""),
    val itemValue: MutableStateFlow<String> = MutableStateFlow(""),
    val quantityInStock: MutableStateFlow<String> = MutableStateFlow("")
) {
    fun toItem() = Item(
        id = id,
        itemName = itemName.value,
        itemDescription = itemDescription.value,
        itemValue = itemValue.value.toDoubleOrNull() ?: 0.0,
        quantityInStock = quantityInStock.value.toIntOrNull() ?: 0
    )

    companion object {
        fun fromItem(item: Item) = ItemUi(
            id = item.id,
            itemName = MutableStateFlow(item.itemName),
            itemDescription = MutableStateFlow(item.itemDescription),
            itemValue = MutableStateFlow(item.itemValue.toString()),
            quantityInStock = MutableStateFlow(item.quantityInStock.toString())
        )
    }
}
