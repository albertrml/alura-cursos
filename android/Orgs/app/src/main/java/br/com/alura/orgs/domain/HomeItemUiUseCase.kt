package br.com.alura.orgs.domain

import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.model.repository.ItemRepository
import br.com.alura.orgs.utils.data.mapTo
import br.com.alura.orgs.utils.data.SortedItem
import br.com.alura.orgs.utils.data.sortItemsBy
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HomeItemUiUseCase @Inject constructor(private val repository: ItemRepository)  {

    fun deleteItem(itemUi: ItemUi) = repository.deleteItem(itemUi.toItem())

    fun fetchAllItemUis(sortBy: SortedItem) = repository.getAllItems().map { response ->
        response.mapTo { items ->
            items.sortItemsBy(sortBy)
                .map { item ->
                    ItemUi.fromItem(item)
                        .copy(
                            itemValue = item.itemValue.toString(),
                            quantityInStock = item.quantityInStock.toString()
                        )
                }
        }
    }

}