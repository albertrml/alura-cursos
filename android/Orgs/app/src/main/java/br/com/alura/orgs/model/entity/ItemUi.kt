package br.com.alura.orgs.model.entity

data class ItemUi(
    val id: Int = 0,
    val itemName: String = "",
    val itemDescription: String = "",
    val itemValue: String = "",
    val quantityInStock: String = "",
    val itemUrl: String = ""
){

    fun toItem() = Item(
        id = id,
        itemName = itemName,
        itemDescription = itemDescription,
        itemValue = itemValue.toDoubleOrNull() ?: 0.0,
        quantityInStock = quantityInStock.toIntOrNull() ?: 0,
        itemUrl = itemUrl
    )

    companion object {
        fun fromItem(item: Item) = ItemUi(
            id = item.id,
            itemName = item.itemName,
            itemDescription = item.itemDescription,
            itemValue = item.itemValue.toString(),
            quantityInStock = item.quantityInStock.toString(),
            itemUrl = item.itemUrl
        )
    }

}



