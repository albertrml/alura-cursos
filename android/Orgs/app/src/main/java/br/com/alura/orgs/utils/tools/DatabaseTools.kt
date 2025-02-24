package br.com.alura.orgs.utils.tools

import br.com.alura.orgs.utils.data.Response

suspend fun <T> performDatabaseOperation(
    databaseOperation: suspend () -> T
): Response<T> =
    try {
        Response.Success(databaseOperation())
    }
    catch (e: Exception) {
        Response.Failure(e)
    }