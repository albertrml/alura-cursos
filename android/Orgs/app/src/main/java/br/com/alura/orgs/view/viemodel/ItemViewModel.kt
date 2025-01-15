package br.com.alura.orgs.view.viemodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.alura.orgs.model.entity.Item
import br.com.alura.orgs.model.repository.ItemRepository
import br.com.alura.orgs.utils.handleResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ItemViewModel @Inject constructor(private val repository: ItemRepository): ViewModel() {
    private val _uiState = MutableStateFlow(ItemUiState())
    val uiState = _uiState.asStateFlow()

    fun onEvent(event: ItemUiEvent){
        when(event){
            is ItemUiEvent.OnDecreaseQuantity -> decreaseQuantity(event.item)
            is ItemUiEvent.OnDeleteItem -> deleteItem(event.item)
            is ItemUiEvent.OnFetchAllItems -> fetchAllItems()
            is ItemUiEvent.OnFetchItemById -> fetchItemById(event.itemId)
            is ItemUiEvent.OnIncreaseQuantity -> increaseQuantity(event.item)
            is ItemUiEvent.OnInsertItem -> {
                val item  = event.run {
                    Item(
                        itemName = itemName,
                        itemDescription = itemDescription,
                        itemValue = itemValue.toDouble(),
                        quantityInStock = quantityInStock.toInt()
                    )
                }
                insertItem(item)
            }
            is ItemUiEvent.OnUpdateItem -> updateItem(event.item)
        }
    }

    private fun decreaseQuantity(item: Item){
        val newItem = item.copy(quantityInStock = item.quantityInStock - 1)
        updateItem(newItem)
    }

    private fun increaseQuantity(item: Item){
        val newItem = item.copy(quantityInStock = item.quantityInStock + 1)
        updateItem(newItem)
    }

    private fun deleteItem(item: Item){
        viewModelScope.launch {
            repository.deleteItem(item).collect { response ->
                response.handleResponse(_uiState) { state, res ->
                    state.copy(deleteState = res)
                }
            }
        }
    }

    private fun fetchAllItems() {
        viewModelScope.launch {
            repository.getAllItems().collect { response ->
                response.handleResponse(_uiState) { state, res ->
                    state.copy(
                        fetchAllItemsState = res
                    )
                }
            }
        }
    }


    private fun fetchItemById(id: Int) {
        viewModelScope.launch {
            repository.getItemById(id).collect { response ->
                response.handleResponse(_uiState) { state, res ->
                    state.copy(fetchItemByIdState = res)
                }
            }
        }
    }

    private fun insertItem(item: Item) {
        viewModelScope.launch {
            repository.insertItem(item).collect { response ->
                response.handleResponse(_uiState) { state, res ->
                    state.copy(insertState = res)
                }
            }
        }
    }

    private fun updateItem(item: Item) {
        viewModelScope.launch {
            repository.updateItem(item).collect { response ->
                response.handleResponse(_uiState) { state, res ->
                    state.copy(updateState = res)
                }
            }
        }
    }
}
