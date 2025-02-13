package br.com.alura.orgs.model.repository

import br.com.alura.orgs.model.entity.Account
import br.com.alura.orgs.model.source.AccountDAO
import br.com.alura.orgs.utils.data.Authenticate
import br.com.alura.orgs.utils.exception.AccountException
import br.com.alura.orgs.utils.data.Response
import br.com.alura.orgs.utils.data.SortedAccount
import br.com.alura.orgs.utils.data.sortAccountsBy
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

    fun getAllUsernames(sortedBy: SortedAccount): Flow<Response<List<String>>> = flow {
        emit(Response.Loading)
        emit(performDatabaseOperation {
            val accounts = accountDao.getAllUsernames()
            accounts.sortAccountsBy(sortedBy)
        })
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
                when(_auth.value){
                    is Authenticate.Logoff -> throw AccountException.AccountIsNotAuthenticated()
                    is Authenticate.Login -> {
                        val currentAuth = _auth.value as Authenticate.Login

                        if(!newPassword.isPasswordValid())
                            throw AccountException.InvalidPassword()

                        if(newPassword == currentAuth.account.password)
                            throw AccountException.PasswordIsTheSame()

                        val newAccount = currentAuth.account.copy(password = newPassword)
                        accountDao.update(newAccount)
                        accountDao.authenticate(newAccount.username,newAccount.password)
                            ?.let { newCredentials ->
                                _auth.update { Authenticate.Login(newCredentials) }
                            }
                            ?: throw AccountException.UpdatedPasswordCantBeDone()
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
                if (!username.isUsernameValid())
                    throw AccountException.InvalidUsername()

                if (!password.isPasswordValid())
                    throw AccountException.InvalidPassword()

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