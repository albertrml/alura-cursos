package br.com.alura.orgs.domain

import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.model.repository.ItemRepository
import br.com.alura.orgs.utils.currencyFormat
import br.com.alura.orgs.utils.mapTo
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HomeItemUiUseCase @Inject constructor(private val repository: ItemRepository)  {

    fun deleteItem(itemUi: ItemUi) = repository.deleteItem(itemUi.toItem())

    fun fetchAllItemUis() = repository.getAllItems().map { response ->
        response.mapTo { items ->
            items.map { item ->
                ItemUi.fromItem(item)
                    .copy(
                        itemValue = currencyFormat(item.itemValue),
                        quantityInStock = "${item.quantityInStock} pct"
                    )
            }
        }
    }

}