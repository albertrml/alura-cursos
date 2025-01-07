package br.com.alura.orgs.model.repository

import br.com.alura.orgs.model.entity.Item
import br.com.alura.orgs.model.source.ItemDAO
import br.com.alura.orgs.utils.Response
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ItemRepository @Inject constructor(private val itemDao: ItemDAO) {
    private suspend fun <T> performDatabaseOperation(
        databaseOperation: suspend () -> T
    ): Response<T> =
        try { Response.Success(databaseOperation()) }
        catch (e: Exception) { Response.Failure(e) }

    fun getAllItems(): Flow<Response<List<Item>>> = flow {
        emit(Response.Loading)
        val initialData = performDatabaseOperation { itemDao.getItems().first() }
        emit(initialData) // Emit initial data
        if (initialData is Response.Success) {
            itemDao.getItems().collect { emit(Response.Success(it)) } // Emit updates
        }
    }

    fun insertItem(item: Item): Flow<Response<Unit>> = flow {
        emit(Response.Loading)
        emit(performDatabaseOperation { itemDao.insert(item) })
    }

    fun updateItem(item: Item): Flow<Response<Unit>> = flow {
        emit(Response.Loading)
        emit(performDatabaseOperation { itemDao.update(item) })
    }

    fun deleteItem(item: Item): Flow<Response<Unit>> = flow {
        emit(Response.Loading)
        emit(performDatabaseOperation { itemDao.delete(item) })
    }

    fun getItemById(id: Int): Flow<Response<Item>> = flow {
        emit(Response.Loading)
        emit(performDatabaseOperation { itemDao.getItemById(id).first() })
    }

}