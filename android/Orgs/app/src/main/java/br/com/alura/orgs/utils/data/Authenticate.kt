package br.com.alura.orgs.utils.data

sealed class Authenticate <out T> {
    data class Login <out T>(val account: T): Authenticate<T>()
    data object Logoff : Authenticate<Nothing>()
}