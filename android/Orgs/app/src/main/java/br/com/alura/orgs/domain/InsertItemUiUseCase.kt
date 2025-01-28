package br.com.alura.orgs.domain

import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.model.repository.ItemRepository
import javax.inject.Inject

class InsertItemUiUseCase @Inject constructor(private val repository: ItemRepository) {

    fun insertItemUi(itemUi: ItemUi) = repository.insertItem(itemUi.toItem())

}