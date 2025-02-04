package br.com.alura.orgs.model.repository

import br.com.alura.orgs.model.entity.Account
import br.com.alura.orgs.model.source.AccountDAO
import br.com.alura.orgs.utils.data.Authenticate
import br.com.alura.orgs.utils.exception.AccountException
import br.com.alura.orgs.utils.data.Response
import br.com.alura.orgs.utils.tools.performDatabaseOperation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import javax.inject.Inject


class AccountRepository @Inject constructor(private val accountDao: AccountDAO) {

    private val _account = MutableStateFlow<Authenticate<Account>>(Authenticate.Logoff)
    val account = _account.asStateFlow()

    fun createAccount(username: String, password: String): Flow<Response<Unit>> = flow {
        emit(Response.Loading)
        emit(
            performDatabaseOperation {
                accountDao.insert(Account(username, password))
            }
        )
    }

    fun getAllUsernames(): Flow<Response<List<String>>> = flow {
        emit(Response.Loading)
        emit(performDatabaseOperation { accountDao.getAllUsernames() })
    }

    fun updateAccount(account: Account): Flow<Response<Unit>> = flow {
        emit(Response.Loading)
        emit(performDatabaseOperation {
            accountDao.update(account)
            _account.value.takeIf { auth -> auth is Authenticate.Login }
                ?.let { auth -> auth as Authenticate.Login }
                ?.takeIf { auth -> auth.account.username == account.username }
                ?.let { _account.update { Authenticate.Login(account) } }
            Unit
        })
    }

    fun deleteAccount(account: Account): Flow<Response<Unit>> = flow {
        emit(Response.Loading)
        emit(
            performDatabaseOperation {
                accountDao.delete(account)
                _account.value.takeIf { auth -> auth is Authenticate.Login }
                    ?.let { auth -> auth as Authenticate.Login }
                    ?.takeIf { auth -> auth.account == account }
                    ?.let { _account.update { Authenticate.Logoff } }
                Unit
            }
        )
    }

    fun authenticate(username: String, password: String): Flow<Response<Unit>> = flow {
        emit(Response.Loading)
        emit(
            performDatabaseOperation {
                if(!accountDao.isUsernameExist(username))
                    throw AccountException.InvalidCredentials()

                accountDao.authenticate(username, password)?.let { account ->
                    _account.update { Authenticate.Login(account) }
                } ?:  throw AccountException.InvalidCredentials()
            }
        )
    }

    fun isUsernameExists(username: String): Flow<Response<Boolean>> = flow {
        emit(Response.Loading)
        emit(performDatabaseOperation { accountDao.isUsernameExist(username) })
    }

    fun logoff(){
        _account.update { Authenticate.Logoff }
    }

}