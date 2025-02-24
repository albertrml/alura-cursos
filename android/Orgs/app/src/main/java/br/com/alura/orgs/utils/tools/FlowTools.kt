package br.com.alura.orgs.utils.tools

import br.com.alura.orgs.utils.tools.FlowToolsException.CollectUntilPredicateException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion

sealed class FlowToolsException(override val message: String): Exception() {
    class CollectUntilPredicateException : FlowToolsException(
        "Predicate was collected before completion"
    )
}

fun <T> Flow<T>.until(predicate: (T) -> Boolean): Flow<T> =
    flow {
        try {
            collect { value ->
                emit(value)
                if (predicate(value)) {
                    throw CollectUntilPredicateException()
                }
            }
        } catch (e: CollectUntilPredicateException) {
        }
    }.onCompletion {
        if (it !is CollectUntilPredicateException) {
            if (it != null) {
                throw it
            }
        }
    }