package br.com.alura.panucci.navigation

import androidx.navigation.NavController
import androidx.navigation.navOptions
import br.com.alura.panucci.navigation.destionation.drinksRoute
import br.com.alura.panucci.navigation.destionation.highlightListRoute
import br.com.alura.panucci.navigation.destionation.menuRoute
import br.com.alura.panucci.navigation.destionation.navigateToDrinks
import br.com.alura.panucci.navigation.destionation.navigateToHighlight
import br.com.alura.panucci.navigation.destionation.navigateToMenu
import br.com.alura.panucci.navigation.graphs.homeGraphRoute
import br.com.alura.panucci.ui.components.BottomAppBarItem

fun NavController.bottomAppNavigateTo(
    item: BottomAppBarItem
) {
    val (route, navigate) = when (item) {
        BottomAppBarItem.Highlight -> {
            highlightListRoute to ::navigateToHighlight
        }
        BottomAppBarItem.Menu -> {
            menuRoute to ::navigateToMenu
        }
        BottomAppBarItem.Drinks -> {
            drinksRoute to ::navigateToDrinks
        }
    }

    val navOptions = navOptions {
        launchSingleTop = true
        popUpTo(route)
    }
    navigate(navOptions)
}

fun NavController.navigateToHomeGraph(){
    navigate(homeGraphRoute)
}