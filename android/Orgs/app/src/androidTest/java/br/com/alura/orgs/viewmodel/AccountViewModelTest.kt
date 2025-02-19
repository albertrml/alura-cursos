package br.com.alura.orgs.viewmodel

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import br.com.alura.orgs.model.mock.mockAccounts
import br.com.alura.orgs.model.repository.AccountRepository
import br.com.alura.orgs.model.source.AccountDAO
import br.com.alura.orgs.model.source.OrgRoomDatabase
import br.com.alura.orgs.utils.data.Authenticate
import br.com.alura.orgs.utils.data.Response
import br.com.alura.orgs.utils.data.SortedAccount
import br.com.alura.orgs.utils.exception.AccountException
import br.com.alura.orgs.utils.tools.until
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
    private lateinit var db: OrgRoomDatabase
    private val invalidUsername = "Test#"
    private val invalidPassword = "Test"
    private val wrongPassword = "A12345bcd"


    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context,
            OrgRoomDatabase::class.java
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
            .until { uiState -> uiState.authenticateState is Response.Success }
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
        val account = mockAccounts.first
        dao.insert(account)
        viewModel.onEvent(AccountUiEvent.OnAuthenticate(account.username, wrongPassword))
        viewModel.uiState
            .until { uiState -> uiState.authenticateState is Response.Failure }
            .collect{ uiState ->
                Log.i("AuthenticateException",uiState.authenticateState.toString())
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
            .until { uiState -> uiState.createAccountState is Response.Success }
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
        viewModel.onEvent(AccountUiEvent.OnCreateAccount(invalidUsername, account.password))
        viewModel.uiState
            .until { uiState -> uiState.createAccountState is Response.Failure }
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
        viewModel.onEvent(AccountUiEvent.OnCreateAccount(account.username, invalidPassword))
        viewModel.uiState
            .until { uiState -> uiState.createAccountState is Response.Failure }
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
            .until { uiState -> uiState.createAccountState is Response.Failure }
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
            .until { uiState -> uiState.deleteAccountState is Response.Success }
            .collect { uiState ->
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
            .until { uiState -> uiState.deleteAccountState is Response.Failure }
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
    @Test
    fun whenGetAccountsSortedAscendingIsSuccessful() = runTest {
        mockAccounts.forEach { dao.insert(it) }
        viewModel.onEvent(AccountUiEvent.OnGetAccounts())
        viewModel.uiState
            .until { uiState -> uiState.getAccountsState is Response.Success }
            .collect{ uiState ->
                when(val response = uiState.getAccountsState){
                    is Response.Success -> {
                        val usernamesFromDatabase = response.result
                        val usernamesFromMock = mockAccounts.map { it.username }.sorted()
                        assertEquals(usernamesFromMock, usernamesFromDatabase)
                    }
                    is Response.Loading -> assert(true)
                    is Response.Failure -> assert(false)
                }
            }
    }

    @Test
    fun whenGetAccountsSortedDescendingIsSuccessful() = runTest {
        mockAccounts.forEach { dao.insert(it) }
        viewModel.onEvent(AccountUiEvent.OnGetAccounts(SortedAccount.ByUsernameDescending))
        viewModel.uiState
            .until { uiState -> uiState.getAccountsState is Response.Success }
            .collect{ uiState ->
                when(val response = uiState.getAccountsState){
                    is Response.Success -> {
                        val usernamesFromDatabase = response.result
                        val usernamesFromMock = mockAccounts.map { it.username }.sortedDescending()
                        assertEquals(usernamesFromMock, usernamesFromDatabase)
                    }
                    is Response.Loading -> assert(true)
                    is Response.Failure -> assert(false)
                }
            }
    }

    //isUsernameExists
    @Test
    fun whenIsUsernameExistsReturnsTrue() = runTest {
        val expectedAccount = mockAccounts[0]
        mockAccounts.forEach { dao.insert(it) }
        viewModel.onEvent(AccountUiEvent.OnIsUsernameExists(expectedAccount.username))
        viewModel.uiState
            .until { uiState -> uiState.isUsernameExistsState is Response.Success }
            .collect{ uiState ->
                when(val response = uiState.isUsernameExistsState){
                    is Response.Success -> {
                        assertTrue(response.result)
                    }
                    is Response.Loading -> assert(true)
                    is Response.Failure -> assert(false)
                }
            }
    }

    @Test
    fun whenIsUsernameDoesNotExistIsReturnsFalse() = runTest {
        mockAccounts.forEach { dao.insert(it) }
        viewModel.onEvent(AccountUiEvent.OnIsUsernameExists(invalidUsername))
        viewModel.uiState
            .until { uiState -> uiState.isUsernameExistsState is Response.Success }
            .collect{ uiState ->
                when(val response = uiState.isUsernameExistsState){
                    is Response.Success -> {
                        assertFalse(response.result)
                    }
                    is Response.Loading -> assert(true)
                    is Response.Failure -> assert(false)
                }
            }
    }

    //logout
    @Test
    fun whenLogoutIsSuccessful() = runTest {
        val account = mockAccounts[0]
        dao.insert(account)
        viewModel.onEvent(AccountUiEvent.OnAuthenticate(account.username,account.password))
        viewModel.uiState.until { uiState -> uiState.authenticateState is Response.Success }
            .collect{ uiState ->
                if(uiState.authenticateState is Response.Success){
                    assertTrue(repository.auth.value is Authenticate.Login)
                    viewModel.onEvent(AccountUiEvent.OnLogout{
                        assertTrue(repository.auth.value is Authenticate.Logoff)
                    })
                }
            }
    }

    //updatePassword
    /*@Test
    fun whenUpdatePasswordIsSuccessful() = runTest{
        val passwordBeforeUpdate = mockAccounts[0].password
        val account = mockAccounts[0]
        dao.insert(account)
        repository.authenticate(account.username,account.password).collect{ response ->
            if(response is Response.Success)
                viewModel.onEvent(AccountUiEvent.OnUpdatePassword(newPassword))
        }

        viewModel.uiState
            .collectUntil { uiState -> uiState.updateAccountState is Response.Success }
            .collect{ uiState ->
                when(uiState.updateAccountState){
                    is Response.Success -> {
                        val passwordAfterUpdate =
                            (repository.auth.value as Authenticate.Login).account.password
                        assertNotEquals(passwordBeforeUpdate, passwordAfterUpdate)
                    }
                    is Response.Loading -> assert(true)
                    is Response.Failure -> assert(false)
                }
            }
    }*/

}