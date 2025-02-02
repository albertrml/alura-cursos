package br.com.alura.orgs.domain

import br.com.alura.orgs.model.entity.Item
import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.model.repository.ItemRepository
import br.com.alura.orgs.utils.data.mapTo
import br.com.alura.orgs.utils.data.SortType
import br.com.alura.orgs.utils.data.SortType.ByIdAscending
import br.com.alura.orgs.utils.data.SortType.ByNameAscending
import br.com.alura.orgs.utils.data.SortType.ByPriceAscending
import br.com.alura.orgs.utils.data.SortType.ByQuantityDescending
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HomeItemUiUseCase @Inject constructor(private val repository: ItemRepository)  {

    fun deleteItem(itemUi: ItemUi) = repository.deleteItem(itemUi.toItem())

    fun fetchAllItemUis(sortBy: SortType) = repository.getAllItems().map { response ->
        response.mapTo { items ->
            items.sortedByType(sortBy)
                .map { item ->
                    ItemUi.fromItem(item)
                        .copy(
                            itemValue = item.itemValue.toString(),
                            quantityInStock = item.quantityInStock.toString()
                        )
                }
        }
    }


    private fun List<Item>.sortedByType(sortMethod: SortType): List<Item> {
        return when (sortMethod) {
            is ByIdAscending -> this.sortedBy { it.id }
            is ByNameAscending -> this.sortedBy { it.itemName }
            is ByPriceAscending -> this.sortedBy { it.itemValue }
            is ByQuantityDescending -> this.sortedByDescending { it.quantityInStock }
        }
    }

}