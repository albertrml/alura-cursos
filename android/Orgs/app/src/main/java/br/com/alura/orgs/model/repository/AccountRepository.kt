package br.com.alura.orgs.model.repository

import br.com.alura.orgs.model.entity.Account
import br.com.alura.orgs.model.source.AccountDAO
import br.com.alura.orgs.utils.exception.AccountException
import br.com.alura.orgs.utils.data.Response
import br.com.alura.orgs.utils.tools.performDatabaseOperation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AccountRepository @Inject constructor(private val accountDao: AccountDAO) {

    fun createAccount(username: String, password: String): Flow<Response<Unit>> = flow {
        emit(Response.Loading)
        emit(
            performDatabaseOperation {
                accountDao.create(Account(username, password))
            }
        )
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

    fun isUsernameExists(username: String): Flow<Response<Boolean>> = flow {
        emit(Response.Loading)
        emit(performDatabaseOperation { accountDao.usernameExists(username) })
    }

}