package br.com.alura.orgs.domain

import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.model.repository.ItemRepository
import br.com.alura.orgs.utils.data.mapTo
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DetailsItemUiUseCase @Inject constructor(private val repository: ItemRepository) {
    fun fetchItemUiById(itemId: Int) = repository.getItemById(itemId).map { response ->
        response.mapTo { item ->
            ItemUi.fromItem(item).copy(itemValue = item.itemValue.toString())
        }
    }
}