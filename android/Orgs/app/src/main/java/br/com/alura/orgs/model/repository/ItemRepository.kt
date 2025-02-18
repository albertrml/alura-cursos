package br.com.alura.orgs.model.repository

import br.com.alura.orgs.model.entity.Item
import br.com.alura.orgs.model.source.ItemDAO
import br.com.alura.orgs.utils.exception.ItemException
import br.com.alura.orgs.utils.data.Response
import br.com.alura.orgs.utils.tools.performDatabaseOperation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ItemRepository @Inject constructor(private val itemDao: ItemDAO) {

    fun getAllItems(): Flow<Response<List<Item>>> = flow {
        emit(Response.Loading)
        try {
            emitAll(
                itemDao.getItems().map {
                    Response.Success(it)
                }
            )
        }catch (e: Exception){
            Response.Failure(e)
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

    fun deleteItem(userOwner: String, item: Item): Flow<Response<Unit>> = flow {
        emit(Response.Loading)
        emit(
            performDatabaseOperation {
                if (userOwner != item.userOwner) throw ItemException.ItemIsNotOwnerException()
                itemDao.delete(item)
            }
        )
    }

    fun getItemById(id: Int): Flow<Response<Item>> = flow {
        emit(Response.Loading)
        emit(
            performDatabaseOperation {
                itemDao.getItemById(id) ?: throw ItemException.ItemNotFoundException()
            }
        )
    }

    fun getItemsByUserOwner(userOwner: String): Flow<Response<List<Item>>> = flow {
        emit(Response.Loading)
        try {
            emitAll(
                itemDao.getItemsByUserOwner(userOwner).map {
                    Response.Success(it)
                }
            )
        }catch (e: Exception){
            Response.Failure(e)
        }
    }

}