package br.com.alura.panucci.navigation.destionation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import br.com.alura.panucci.ui.screens.ProductDetailsScreen
import br.com.alura.panucci.ui.viewmodels.ProductDetailsViewModel

private const val productDetailsRoute = "productDetails"
internal const val productIdArgument = "productId"

fun NavGraphBuilder.productDetailsScreen(
    navController: NavHostController
) {
    composable(
        "${productDetailsRoute}/{$productIdArgument}"
    ) { backStackEntry ->
        backStackEntry.arguments?.getString(productIdArgument)?.let { id ->
            val viewModel = viewModel<ProductDetailsViewModel>(
                factory = ProductDetailsViewModel.Factory
            )
            val uiState by viewModel.uiState.collectAsState()
            ProductDetailsScreen(
                uiState = uiState,
                onNavigateToCheckout = {
                    navController.navigateToCheckout()
                },
                onNavigateToBack = {
                    navController.navigateUp()
                }
            )
        } ?: LaunchedEffect(Unit) {
            navController.navigateUp()
        }
    }
}

fun NavController.navigateToProductDetails(id: String) {
    navigate("${productDetailsRoute}/$id")
}