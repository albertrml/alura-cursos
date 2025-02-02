package br.com.alura.orgs.viewmodel.update

import br.com.alura.orgs.model.entity.ItemUi

sealed class UpdateUiEvent {
    data class OnFetchItemById(val itemId: Int) : UpdateUiEvent()
    data class OnSaveUrlImage(val url: String) : UpdateUiEvent()
    data class OnUpdate(val itemUi: ItemUi) : UpdateUiEvent()
}