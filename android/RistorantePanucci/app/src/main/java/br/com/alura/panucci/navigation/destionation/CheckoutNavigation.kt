package br.com.alura.panucci.navigation.destionation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import br.com.alura.panucci.ui.screens.CheckoutScreen
import br.com.alura.panucci.ui.viewmodels.CheckoutViewModel

private const val checkoutRoute = "checkout"

fun NavGraphBuilder.checkoutScreen(
    navController: NavHostController
){
    composable(checkoutRoute) {
        val viewModel = viewModel<CheckoutViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        CheckoutScreen(
            onPopBackStack = {
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set(
                        key = "order_done",
                        value = "Pedido realizado com sucesso"
                    )
                navController.navigateUp()
            },
            uiState = uiState,
        )
    }
}

fun NavController.navigateToCheckout() {
    navigate(checkoutRoute)
}