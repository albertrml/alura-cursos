package br.com.alura.orgs.viemodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.alura.orgs.model.entity.Item
import br.com.alura.orgs.model.repository.ItemRepository
import br.com.alura.orgs.utils.handleResponse
import br.com.alura.orgs.view.udf.UiEvent
import br.com.alura.orgs.view.udf.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrgViewModel @Inject constructor(private val repository: ItemRepository): ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    init {
        onEvent(UiEvent.OnFetchAllItems)
    }

    fun onEvent(event: UiEvent){
        when(event){
            is UiEvent.OnDecreaseQuantity -> decreaseQuantity(event.item)
            is UiEvent.OnDelete -> deleteItem(event.item)
            is UiEvent.OnFetchAllItems -> fetchAllItems()
            is UiEvent.OnFetchById -> fetchItemById(event.itemId)
            is UiEvent.OnIncreaseQuantity -> increaseQuantity(event.item)
            is UiEvent.OnInsert -> {
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
            is UiEvent.OnUpdate -> updateItem(event.item)
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
