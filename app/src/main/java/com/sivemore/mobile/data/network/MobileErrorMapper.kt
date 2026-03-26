package com.sivemore.mobile.data.network

import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.net.ssl.SSLException
import org.json.JSONObject
import retrofit2.HttpException

class MobileErrorMapper @Inject constructor() {
    fun toMessage(throwable: Throwable): String = when (throwable) {
        is SocketTimeoutException -> "La conexion tardo demasiado. Intenta nuevamente."
        is SSLException -> "La conexion segura fallo. Revisa la URL o el certificado local."
        is IOException -> "No fue posible conectar con el backend."
        is HttpException -> when (throwable.code()) {
            400 -> throwable.extractBackendMessage() ?: "La solicitud no es valida."
            401 -> throwable.extractBackendMessage() ?: "La sesion expiro. Inicia sesion nuevamente."
            403 -> throwable.extractBackendMessage() ?: "No tienes permisos para realizar esta accion."
            404 -> throwable.extractBackendMessage() ?: "No se encontro la informacion solicitada."
            else -> throwable.extractBackendMessage() ?: "Ocurrio un error del servidor. Intenta nuevamente."
        }
        else -> throwable.message ?: "Ocurrio un error inesperado."
    }

    private fun HttpException.extractBackendMessage(): String? {
        val rawBody = response()?.errorBody()?.string()?.trim().orEmpty()
        if (rawBody.isBlank()) {
            return message().takeIf { it.isNotBlank() && it != "Response.error()" }
        }

        return runCatching {
            val json = JSONObject(rawBody)
            buildList {
                json.optString("message").takeIf { it.isNotBlank() }?.let(::add)
                val details = json.optJSONObject("details")
                if (details != null) {
                    val keys = details.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        details.optString(key).takeIf { it.isNotBlank() }?.let(::add)
                    }
                }
            }.joinToString(separator = "\n").ifBlank { null }
        }.getOrNull() ?: rawBody
    }
}
