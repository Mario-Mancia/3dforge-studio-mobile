package com.example.a3df_studio_app.domain.utils

import kotlin.coroutines.cancellation.CancellationException

suspend fun <T> resultOf(bloque: suspend () -> T): Result<T> = try {
    Result.success(bloque())
} catch (e: CancellationException) {
    throw e
} catch (e: Throwable) {
    Result.failure(e)
}

fun Throwable.mensajeParaUsuario(): String = message?.takeIf { it.isNotBlank() } ?: "Ocurrió un error. Intenta de nuevo."
