package br.com.alura.orgs.view.viemodel

import br.com.alura.orgs.model.entity.Item

sealed class ItemUiEvent {
    data object OnFetchAllItems : ItemUiEvent()
    data class OnFetchItemById(val itemId: Int) : ItemUiEvent()
    data class OnInsertItem(
        val itemName: String,
        val itemDescription: String,
        val itemValue: String,
        val quantityInStock: String) : ItemUiEvent()
    data class OnDeleteItem(val item: Item) : ItemUiEvent()
    data class OnUpdateItem(val item: Item) : ItemUiEvent()
    data class OnIncreaseQuantity(val item: Item) : ItemUiEvent()
    data class OnDecreaseQuantity(val item: Item) : ItemUiEvent()
}