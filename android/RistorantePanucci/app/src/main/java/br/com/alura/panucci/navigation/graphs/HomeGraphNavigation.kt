package br.com.alura.panucci.navigation.graphs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.navigation
import br.com.alura.panucci.navigation.destionation.drinksListScreen
import br.com.alura.panucci.navigation.destionation.highlightListRoute
import br.com.alura.panucci.navigation.destionation.highlightListScreen
import br.com.alura.panucci.navigation.destionation.menuListScreen

const val homeGraphRoute = "home"

fun NavGraphBuilder.homeGraph(navController: NavHostController){
    navigation(
        startDestination = highlightListRoute,
        route = homeGraphRoute
    ){
        highlightListScreen(navController)
        menuListScreen(navController)
        drinksListScreen(navController)
    }
}