package com.sivemore.mobile.data.network

import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject
import retrofit2.HttpException

class MobileErrorMapper @Inject constructor() {
    fun toMessage(throwable: Throwable): String = when (throwable) {
        is SocketTimeoutException -> "La conexión tardó demasiado. Intenta nuevamente."
        is IOException -> "No fue posible conectar con el backend."
        is HttpException -> when (throwable.code()) {
            400 -> throwable.message()
            401 -> "La sesión expiró. Inicia sesión nuevamente."
            403 -> "No tienes permisos para realizar esta acción."
            404 -> "No se encontró la información solicitada."
            else -> "Ocurrió un error del servidor. Intenta nuevamente."
        }
        else -> throwable.message ?: "Ocurrió un error inesperado."
    }
}
