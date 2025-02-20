package br.com.alura.orgs.model.repository

import br.com.alura.orgs.model.entity.Account
import br.com.alura.orgs.model.source.AccountDAO
import br.com.alura.orgs.utils.data.Authenticate
import br.com.alura.orgs.utils.exception.AccountException
import br.com.alura.orgs.utils.data.Response
import br.com.alura.orgs.utils.data.SortedAccount
import br.com.alura.orgs.utils.data.sortAccountsBy
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

    fun createAccount(username: String, password: String): Flow<Response<Unit>> = flow {
        emit(Response.Loading)
        emit(
            performDatabaseOperation {
                if(accountDao.isUsernameExist(username))
                    throw AccountException.UsernameAlreadyExists()

                accountDao.insert(Account(username, password))
            }
        )
    }

    fun deleteAccount(account: Account): Flow<Response<Unit>> = flow {
        emit(Response.Loading)
        emit(
            performDatabaseOperation {
                auth.value.let { auth ->
                    if (auth is Authenticate.Logoff)
                        throw AccountException.AccountIsNotAuthenticated()
                    if (auth is Authenticate.Login && auth.account.username != account.username)
                        throw AccountException.AccountBelongsToAnotherUser()
                }
                accountDao.delete(account)
                _auth.update { Authenticate.Logoff }
            }
        )
    }

    fun getAllUsernames(sortedBy: SortedAccount): Flow<Response<List<String>>> = flow {
        emit(Response.Loading)
        emit(performDatabaseOperation {
            val accounts = accountDao.getAllUsernames()
            accounts.sortAccountsBy(sortedBy)
        })
    }

    fun isUsernameExists(username: String): Flow<Response<Boolean>> = flow {
        emit(Response.Loading)
        emit(performDatabaseOperation { accountDao.isUsernameExist(username) })
    }

    fun logout(){
        _auth.update { Authenticate.Logoff }
    }

    fun updateAccount(account: Account): Flow<Response<Unit>> = flow {
        emit(Response.Loading)
        emit(
            performDatabaseOperation {
                auth.value.let { auth ->
                    if (auth is Authenticate.Logoff)
                        throw AccountException.AccountIsNotAuthenticated()
                    if (auth is Authenticate.Login && auth.account.username != account.username)
                        throw AccountException.AccountBelongsToAnotherUser()
                    if (auth is Authenticate.Login && auth.account == account)
                        throw AccountException.AccountIsTheSame()
                }
                accountDao.update(account)
                _auth.update { Authenticate.Login(account) }
            }
        )
    }

}