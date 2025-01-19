package br.com.alura.orgs.view.udf

import br.com.alura.orgs.model.entity.Item

sealed class UiEvent {
    data object OnFetchAllItems : UiEvent()
    data class OnFetchById(val itemId: Int) : UiEvent()
    data class OnInsert(
        val itemName: String,
        val itemDescription: String,
        val itemValue: String,
        val quantityInStock: String) : UiEvent()
    data class OnDelete(val item: Item) : UiEvent()
    data class OnUpdate(val item: Item) : UiEvent()
    data class OnIncreaseQuantity(val item: Item) : UiEvent()
    data class OnDecreaseQuantity(val item: Item) : UiEvent()
}