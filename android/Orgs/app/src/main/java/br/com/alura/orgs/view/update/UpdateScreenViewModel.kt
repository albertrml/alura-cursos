package br.com.alura.orgs.view.update

import androidx.lifecycle.ViewModel
import br.com.alura.orgs.model.entity.Item
import br.com.alura.orgs.model.entity.ItemUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UpdateScreenViewModel : ViewModel() {
    private val _itemUi = MutableStateFlow(ItemUi())
    val itemUi: StateFlow<ItemUi> get() = _itemUi

    fun fromItem(item: Item) {
        _itemUi.value = ItemUi.fromItem(item)
    }

    fun toItem(): Item = _itemUi.value.toItem()
}