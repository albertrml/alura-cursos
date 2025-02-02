package br.com.alura.orgs.viewmodel.insert

import br.com.alura.orgs.model.entity.ItemUi

sealed class InsertUiEvent {
    data class OnInsert(val item: ItemUi) : InsertUiEvent()
    data class OnSaveUrlImage(val url: String) : InsertUiEvent()
}