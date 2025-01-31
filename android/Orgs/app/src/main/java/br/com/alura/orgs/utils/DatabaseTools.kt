package br.com.alura.orgs.utils

suspend fun <T> performDatabaseOperation(
    databaseOperation: suspend () -> T
): Response<T> =
    try { Response.Success(databaseOperation()) }
    catch (e: Exception) { Response.Failure(e) }