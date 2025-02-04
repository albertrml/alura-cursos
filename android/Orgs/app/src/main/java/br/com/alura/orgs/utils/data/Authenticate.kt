package br.com.alura.orgs.utils.data

import br.com.alura.orgs.model.entity.Account

sealed class Authenticate <out T> {
    data class Login <out T>(val account: Account): Authenticate<T>()
    data object Logoff : Authenticate<Nothing>()
}