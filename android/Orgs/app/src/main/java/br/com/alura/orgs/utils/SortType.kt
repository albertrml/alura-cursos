package br.com.alura.orgs.utils

sealed class SortType {
    data object ByIdAscending : SortType()
    data object ByNameAscending : SortType()
    data object ByPriceAscending : SortType()
    data object ByQuantityDescending : SortType()
}

