package br.com.alura.orgs.view.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.alura.orgs.model.entity.Item
import br.com.alura.orgs.model.repository.ItemRepository
import br.com.alura.orgs.utils.Response
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

class ItemViewModel @Inject constructor(private val repository: ItemRepository): ViewModel() {
    val allItems = repository.getAllItems().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = Response.Loading
    )
    fun insertItem(item: Item) = repository.insertItem(item)
    fun updateItem(item: Item) = repository.updateItem(item)
    fun deleteItem(item: Item) = repository.deleteItem(item)
    fun getItemById(id: Int) = repository.getItemById(id)
}
