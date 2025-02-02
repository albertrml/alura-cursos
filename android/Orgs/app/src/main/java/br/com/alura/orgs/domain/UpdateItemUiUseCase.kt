package br.com.alura.orgs.domain

import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.model.repository.ItemRepository
import br.com.alura.orgs.utils.data.mapTo
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class UpdateItemUiUseCase @Inject constructor(private val repository: ItemRepository) {

    fun fetchItemUiById(itemUiId: Int) = repository.getItemById(itemUiId).map { response ->
        response.mapTo { item -> ItemUi.fromItem(item) }
    }

    fun updateItemUi(itemUi: ItemUi) = repository.updateItem(itemUi.toItem())

}