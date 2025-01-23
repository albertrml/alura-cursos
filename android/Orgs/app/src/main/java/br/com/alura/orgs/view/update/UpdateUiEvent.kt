package br.com.alura.orgs.view.update

import br.com.alura.orgs.model.entity.Item

sealed class UpdateUiEvent {
    data class OnUpdate(val item: Item) : UpdateUiEvent()
    data class OnFetchItemById(val itemId: Int) : UpdateUiEvent()
}