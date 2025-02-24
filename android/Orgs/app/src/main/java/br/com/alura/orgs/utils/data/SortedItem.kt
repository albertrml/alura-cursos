package br.com.alura.orgs.utils.data

import br.com.alura.orgs.model.entity.Item
import br.com.alura.orgs.utils.data.SortedItem.ByIdAscending
import br.com.alura.orgs.utils.data.SortedItem.ByPriceAscending
import br.com.alura.orgs.utils.data.SortedItem.ByQuantityDescending

sealed class SortedItem {
    data object ByIdAscending : SortedItem()
    data object ByNameAscending : SortedItem()
    data object ByPriceAscending : SortedItem()
    data object ByQuantityDescending : SortedItem()
}

fun List<Item>.sortItemsBy(sortMethod: SortedItem): List<Item> {
    return when (sortMethod) {
        is ByIdAscending -> this.sortedBy { it.id }
        is ByPriceAscending -> this.sortedBy { it.itemValue }
        is ByQuantityDescending -> this.sortedByDescending { it.quantityInStock }
        else -> this.sortedBy { it.itemName }
    }
}
