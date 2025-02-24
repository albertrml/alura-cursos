package br.com.alura.orgs.utils.data

import br.com.alura.orgs.utils.data.SortedAccount.ByUsernameAscending
import br.com.alura.orgs.utils.data.SortedAccount.ByUsernameDescending

sealed class SortedAccount {
    data object ByUsernameAscending : SortedAccount()
    data object ByUsernameDescending : SortedAccount()
}

fun List<String>.sortAccountsBy(sortedBy: SortedAccount): List<String>{
    return when(sortedBy){
        is ByUsernameAscending -> this.sortedBy { it }
        is ByUsernameDescending -> this.sortedByDescending { it }
    }
}