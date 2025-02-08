package br.com.alura.orgs.viewmodel

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import br.com.alura.orgs.model.mock.mockAccounts
import br.com.alura.orgs.model.repository.AccountRepository
import br.com.alura.orgs.model.source.AccountDAO
import br.com.alura.orgs.model.source.OrgsRoomDatabase
import br.com.alura.orgs.utils.data.Authenticate
import br.com.alura.orgs.utils.data.Response
import br.com.alura.orgs.utils.exception.AccountException
import br.com.alura.orgs.utils.tools.collectUntil
import br.com.alura.orgs.viewmodel.account.AccountUiEvent
import br.com.alura.orgs.viewmodel.account.AccountViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AccountViewModelTest {

    private lateinit var viewModel: AccountViewModel
    private lateinit var repository: AccountRepository
    private lateinit var dao: AccountDAO
    private lateinit var db: OrgsRoomDatabase

    private val wrongUsername = "Test#"
    private val wrongPassword = "Test"
    private val newPassword = "65432A"

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context,
            OrgsRoomDatabase::class.java
        ).build()

        dao = db.accountDao()
        repository = AccountRepository(dao)
        viewModel = AccountViewModel(repository)

    }

    @After
    fun closeDb(){ db.close() }

    //authenticate
    @Test
    fun whenAuthenticateIsSuccessful() = runTest {
        val expectedAccount = mockAccounts[0]
        dao.insert(expectedAccount)
        viewModel.onEvent(
            AccountUiEvent.OnAuthenticate(expectedAccount.username, expectedAccount.password)
        )
        viewModel.uiState
            .collectUntil { uiState -> uiState.authenticateState is Response.Success }
            .collect{ uiState ->
                when(uiState.authenticateState){
                    is Response.Success -> {
                        when (val state = repository.auth.value) {
                            is Authenticate.Login -> {
                                val accountFromDatabase = state.account
                                assertEquals(expectedAccount, accountFromDatabase)
                            }
                            is Authenticate.Logoff -> assertFalse(
                                "Expected Authenticate.Login",
                                true
                            )
                        }
                    }
                    is Response.Loading -> assertTrue(
                        "Waiting for conclusion",
                        true
                    )
                    is Response.Failure -> assertFalse(
                        "Expected Response.Success",
                        true
                    )
                }
            }
    }

    @Test
    fun whenAuthenticateIsUnsuccessful() = runTest {
        val account = mockAccounts[0]
        dao.insert(account)
        viewModel.onEvent(AccountUiEvent.OnAuthenticate(wrongUsername, wrongPassword))
        viewModel.uiState
            .collectUntil { uiState -> uiState.authenticateState is Response.Failure }
            .collect{ uiState ->
                when(uiState.authenticateState){
                    is Response.Success -> assertFalse(
                        "Expected Response.Failure",
                        true
                    )
                    is Response.Loading -> assertTrue(
                        "Waiting for conclusion",
                        true
                    )
                    is Response.Failure -> {
                        val failure = uiState.authenticateState as Response.Failure
                        assertTrue(
                            "Expected AccountException.InvalidCredentials",
                            failure.exception is AccountException.InvalidCredentials
                        )
                    }
                }
            }
    }

    //createAccount
    @Test
    fun whenCreateAccountIsSuccessful() = runTest {
        val expectedAccount = mockAccounts[0]
        viewModel.onEvent(
            AccountUiEvent.OnCreateAccount(expectedAccount.username, expectedAccount.password)
        )
        viewModel.uiState
            .collectUntil { uiState -> uiState.createAccountState is Response.Success }
            .collect{ uiState ->
                when(uiState.createAccountState){
                    is Response.Success -> {
                        val accountFromDatabase = dao
                            .authenticate(expectedAccount.username,expectedAccount.password)
                        assertNotNull(accountFromDatabase)
                        assertEquals(expectedAccount, accountFromDatabase)
                    }
                    is Response.Loading -> assertTrue(
                        "Waiting for conclusion",
                        true
                    )
                    is Response.Failure -> assertFalse(
                        "Expected Response.Success",
                        true
                    )
                }
            }
    }

    @Test
    fun whenCreateAccountWithInvalidUsernameIsUnsuccessful() = runTest {
        val account = mockAccounts[0]
        viewModel.onEvent(AccountUiEvent.OnCreateAccount(wrongUsername, account.password))
        viewModel.uiState
            .collectUntil { uiState -> uiState.createAccountState is Response.Failure }
            .collect{ uiState ->
                when(uiState.createAccountState){
                    is Response.Success -> assertFalse(
                        "Expected Response.Failure",
                        true
                    )
                    is Response.Loading -> assertTrue(
                        "Waiting for conclusion",
                        true
                    )
                    is Response.Failure -> {
                        val failure = uiState.createAccountState as Response.Failure
                        assert(failure.exception is AccountException.InvalidUsername)
                    }
                }
            }
    }

    @Test
    fun whenCreateAccountWithInvalidPasswordIsUnsuccessful() = runTest {
        val account = mockAccounts[0]
        viewModel.onEvent(AccountUiEvent.OnCreateAccount(account.username, wrongPassword))
        viewModel.uiState
            .collectUntil { uiState -> uiState.createAccountState is Response.Failure }
            .collect{ uiState ->
                when(uiState.createAccountState){
                    is Response.Success -> assertFalse(
                        "Expected Response.Failure",
                        true
                    )
                    is Response.Loading -> assertTrue(
                        "Waiting for conclusion",
                        true
                    )
                    is Response.Failure -> {
                        val failure = uiState.createAccountState as Response.Failure
                        assert(failure.exception is AccountException.InvalidPassword)
                    }
                }
            }
    }

    @Test
    fun whenCreateAccountWithDuplicateUsernameIsUnsuccessful() = runTest {
        val account = mockAccounts[0]
        dao.insert(account)
        viewModel.onEvent(AccountUiEvent.OnCreateAccount(account.username, account.password))
        viewModel.uiState
            .collectUntil { uiState -> uiState.createAccountState is Response.Failure }
            .collect{ uiState ->
                when(uiState.createAccountState){
                    is Response.Success -> assertFalse(
                        "Expected Response.Failure",
                        true
                    )
                    is Response.Loading -> assertTrue(
                        "Waiting for conclusion",
                        true
                    )
                    is Response.Failure -> {
                        val failure = uiState.createAccountState as Response.Failure
                        assert(failure.exception is AccountException.UsernameAlreadyExists)
                    }
                }
            }
    }

    //deleteAccount
    @Test
    fun whenDeleteAccountThatIsAuthenticatedIsSuccessful() = runTest{
        val accountForDelete = mockAccounts[0]
        dao.insert(accountForDelete)

        repository.authenticate(accountForDelete.username, accountForDelete.password)
            .collectLatest { response ->
                if (response is Response.Success){
                    viewModel.onEvent(AccountUiEvent.OnDeleteAccount)
                }
            }

        viewModel.uiState
            .collectUntil { uiState -> uiState.deleteAccountState is Response.Success }
            .collectLatest { uiState ->
                when (val deleteAccountState = uiState.deleteAccountState) {
                    is Response.Success -> {
                        assertTrue(
                            "Account was deleted",
                            dao.getAllUsernames().isEmpty()
                        )
                        assertTrue(
                            "Expected Authenticate.Logoff",
                            repository.auth.value is Authenticate.Logoff
                        )
                    }
                    else -> {
                        assertFalse(
                            "Expected Response.Success",
                            deleteAccountState is Response.Failure
                        )
                    }
                }
            }
    }

    @Test
    fun whenDeleteAccountThatIsNotAuthenticatedIsUnsuccessful() = runTest{
        val accountForDelete = mockAccounts[0]
        dao.insert(accountForDelete)

        viewModel.onEvent(AccountUiEvent.OnDeleteAccount)
        viewModel.uiState
            .collectUntil { uiState -> uiState.deleteAccountState is Response.Failure }
            .collect { uiState ->
                when(uiState.deleteAccountState){
                    is Response.Success -> assert(false)
                    is Response.Loading -> assert(true)
                    is Response.Failure -> {
                        val failure = uiState.deleteAccountState as Response.Failure
                        assert(failure.exception is AccountException.AccountIsNotAuthenticated)
                    }
                }
            }

    }

    //getAccounts

    //isUsernameExists

    //logout

    //updatePassword

}