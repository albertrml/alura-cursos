package br.com.alura.orgs.model.repository

import br.com.alura.orgs.model.entity.Account
import br.com.alura.orgs.model.source.AccountDAO
import br.com.alura.orgs.utils.AccountException
import br.com.alura.orgs.utils.Response
import br.com.alura.orgs.utils.performDatabaseOperation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AccountRepository @Inject constructor(private val accountDao: AccountDAO) {

    fun createAccount(account: Account): Flow<Response<Unit>> = flow {
        emit(Response.Loading)
        emit(performDatabaseOperation { accountDao.create(account) })
    }

    fun readAccounts(): Flow<Response<List<Account>>> = flow {
        emit(Response.Loading)
        emit(performDatabaseOperation { accountDao.read() })
    }

    fun updateAccount(account: Account): Flow<Response<Unit>> = flow {
        emit(Response.Loading)
        emit(performDatabaseOperation { accountDao.update(account) })
    }

    fun deleteAccount(account: Account): Flow<Response<Unit>> = flow {
        emit(Response.Loading)
        emit(performDatabaseOperation { accountDao.delete(account) })
    }

    fun authenticate(username: String, password: String): Flow<Response<Account>> = flow {
        emit(Response.Loading)
        emit(
            performDatabaseOperation {
                if(!accountDao.usernameExists(username))
                    throw AccountException.InvalidCredentials()

                accountDao.authenticate(username, password)
                    ?: throw AccountException.InvalidCredentials()
            }
        )
    }

}