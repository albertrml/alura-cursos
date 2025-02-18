package br.com.alura.orgs.domain

import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.model.repository.ItemRepository
import br.com.alura.orgs.utils.data.Response
import br.com.alura.orgs.utils.data.mapTo
import br.com.alura.orgs.utils.data.SortedItem
import br.com.alura.orgs.utils.data.sortItemsBy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HomeItemUiUseCase @Inject constructor(private val repository: ItemRepository)  {

    fun deleteItem(username: String, itemUi: ItemUi) =
        repository.deleteItem(username, itemUi.toItem())

    fun fetchAllItemUis(
        username: String,
        sortBy: SortedItem
    ): Flow<Response<List<ItemUi>>> {
        return repository.getItemsByUserOwner(username).map { response ->
            response.mapTo { items ->
                items.sortItemsBy(sortBy).map { item -> ItemUi.fromItem(item) }
            }
        }
    }

}