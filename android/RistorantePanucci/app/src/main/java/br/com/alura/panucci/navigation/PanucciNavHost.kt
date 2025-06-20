package br.com.alura.panucci.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import br.com.alura.panucci.navigation.destionation.checkoutScreen
import br.com.alura.panucci.navigation.destionation.productDetailsScreen
import br.com.alura.panucci.navigation.graphs.homeGraph
import br.com.alura.panucci.navigation.graphs.homeGraphRoute

@Composable
fun PanucciNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = homeGraphRoute
    ){
        homeGraph(navController)
        productDetailsScreen(navController)
        checkoutScreen(navController)
    }
}