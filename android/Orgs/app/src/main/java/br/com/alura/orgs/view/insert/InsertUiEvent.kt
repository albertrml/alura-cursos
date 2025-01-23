package br.com.alura.orgs.view.insert

import br.com.alura.orgs.model.entity.Item

sealed class InsertUiEvent {
    data class OnInsert(val item: Item) : InsertUiEvent()
}