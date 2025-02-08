package br.com.alura.orgs.model.repository

import br.com.alura.orgs.model.entity.Account
import br.com.alura.orgs.model.source.AccountDAO
import br.com.alura.orgs.utils.data.Authenticate
import br.com.alura.orgs.utils.exception.AccountException
import br.com.alura.orgs.utils.data.Response
import br.com.alura.orgs.utils.tools.isPasswordValid
import br.com.alura.orgs.utils.tools.isUsernameValid
import br.com.alura.orgs.utils.tools.performDatabaseOperation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import javax.inject.Inject


class AccountRepository @Inject constructor(private val accountDao: AccountDAO) {

    private val _auth = MutableStateFlow<Authenticate<Account>>(Authenticate.Logoff)
    val auth = _auth.asStateFlow()

    fun createAccount(username: String, password: String): Flow<Response<Unit>> = flow {
        emit(Response.Loading)
        emit(
            performDatabaseOperation {
                if(!username.isUsernameValid()) throw AccountException.InvalidUsername()
                if(!password.isPasswordValid()) throw AccountException.InvalidPassword()
                if(accountDao.isUsernameExist(username))
                    throw AccountException.UsernameAlreadyExists()

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
            _auth.value.takeIf { auth -> auth is Authenticate.Login }
                ?.let { auth -> auth as Authenticate.Login }
                ?.takeIf { auth -> auth.account.username == account.username }
                ?.let { _auth.update { Authenticate.Login(account) } }
            Unit
        })
    }

    fun updatePassword(newPassword: String): Flow<Response<Unit>> = flow {
        emit(Response.Loading)
        emit(
            performDatabaseOperation {
                if(!newPassword.isPasswordValid())
                    throw AccountException.InvalidPassword()
                when(_auth.value){
                    is Authenticate.Logoff -> throw AccountException.AccountIsNotAuthenticated()
                    is Authenticate.Login -> {
                        val account = _auth.value as Authenticate.Login
                        accountDao.update(account.account.copy(password = newPassword))
                    }
                }
            }
        )
    }

    fun deleteAccount(account: Account): Flow<Response<Unit>> = flow {
        emit(Response.Loading)
        emit(
            performDatabaseOperation {
                accountDao.delete(account)
                _auth.value.takeIf { auth -> auth is Authenticate.Login }
                    ?.let { auth -> auth as Authenticate.Login }
                    ?.takeIf { auth -> auth.account == account }
                    ?.let { _auth.update { Authenticate.Logoff } }
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
                    _auth.update { Authenticate.Login(account) }
                } ?:  throw AccountException.InvalidCredentials()
            }
        )
    }

    fun isUsernameExists(username: String): Flow<Response<Boolean>> = flow {
        emit(Response.Loading)
        emit(performDatabaseOperation { accountDao.isUsernameExist(username) })
    }

    fun logoff(){
        _auth.update { Authenticate.Logoff }
    }

}