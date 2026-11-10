package com.example.a3df_studio_app.data.remote

import com.example.a3df_studio_app.data.config.AppConfig
import com.example.a3df_studio_app.data.remote.dto.ErrorBody
import com.example.a3df_studio_app.domain.models.AppError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

/** Cliente HTTP de la API de DForge (Django). Todas las rutas son relativas a API_BASE_URL y terminan en "/". */
class ApiClient {

    @Volatile
    var token: String? = null

    var alExpirar: (() -> Unit)? = null

    val json: Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    private val http: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .addInterceptor(
            Interceptor { cadena ->
                val actual = token
                val pedido = if (actual != null) {
                    cadena.request().newBuilder().header("Authorization", "Bearer $actual").build()
                } else {
                    cadena.request()
                }
                cadena.proceed(pedido)
            }
        )
        .build()

    // ---- Construcción de peticiones (públicas porque las usan las funciones inline) ----

    fun urlDe(ruta: String, consulta: Map<String, String?> = emptyMap()): HttpUrl {
        val constructor = (AppConfig.apiBaseUrl + ruta.trimStart('/')).toHttpUrl().newBuilder()
        consulta.forEach { (clave, valor) -> if (valor != null) constructor.addQueryParameter(clave, valor) }
        return constructor.build()
    }

    fun pedidoGet(ruta: String, consulta: Map<String, String?> = emptyMap()): Request =
        Request.Builder().url(urlDe(ruta, consulta)).get().build()

    fun pedidoJson(ruta: String, metodo: String, cuerpo: String): Request =
        Request.Builder().url(urlDe(ruta)).method(metodo, cuerpo.toRequestBody(TIPO_JSON)).build()

    fun pedidoVacio(ruta: String): Request =
        Request.Builder().url(urlDe(ruta)).post(ByteArray(0).toRequestBody(null)).build()

    fun pedidoMultipart(ruta: String, cuerpo: MultipartBody): Request =
        Request.Builder().url(urlDe(ruta)).post(cuerpo).build()

    // ---- Ejecución ----

    suspend fun ejecutar(pedido: Request): String = withContext(Dispatchers.IO) {
        try {
            http.newCall(pedido).execute().use { respuesta ->
                val texto = respuesta.body?.string().orEmpty()
                if (respuesta.isSuccessful) texto else throw errorDe(respuesta.code, texto)
            }
        } catch (e: AppError) {
            if (e is AppError.SesionExpirada) alExpirar?.invoke()
            throw e
        } catch (e: IOException) {
            throw AppError.SinConexion
        }
    }

    suspend inline fun <reified T> get(ruta: String, consulta: Map<String, String?> = emptyMap()): T =
        json.decodeFromString<T>(ejecutar(pedidoGet(ruta, consulta)))

    suspend inline fun <reified B, reified T> post(ruta: String, cuerpo: B): T =
        json.decodeFromString<T>(ejecutar(pedidoJson(ruta, "POST", json.encodeToString(cuerpo))))

    suspend inline fun <reified B, reified T> patch(ruta: String, cuerpo: B): T =
        json.decodeFromString<T>(ejecutar(pedidoJson(ruta, "PATCH", json.encodeToString(cuerpo))))

    suspend inline fun <reified T> postVacio(ruta: String): T =
        json.decodeFromString<T>(ejecutar(pedidoVacio(ruta)))

    suspend inline fun <reified T> postMultipart(ruta: String, cuerpo: MultipartBody): T =
        json.decodeFromString<T>(ejecutar(pedidoMultipart(ruta, cuerpo)))

    /** Descarga un archivo autenticado (Bearer) a [destino]. */
    suspend fun descargar(ruta: String, destino: File) {
        withContext(Dispatchers.IO) {
            try {
                http.newCall(pedidoGet(ruta)).execute().use { respuesta ->
                    if (!respuesta.isSuccessful) throw errorDe(respuesta.code, respuesta.body?.string().orEmpty())
                    destino.parentFile?.mkdirs()
                    val cuerpo = respuesta.body ?: throw AppError.Local("El servidor no envió el archivo.")
                    cuerpo.byteStream().use { entrada ->
                        destino.outputStream().use { salida -> entrada.copyTo(salida) }
                    }
                }
            } catch (e: AppError) {
                if (e is AppError.SesionExpirada) alExpirar?.invoke()
                throw e
            } catch (e: IOException) {
                throw AppError.SinConexion
            }
        }
    }

    // ---- Traducción de errores (documento de la API, sección 5.10) ----

    private fun errorDe(codigoHttp: Int, texto: String): AppError {
        val info = runCatching { json.decodeFromString<ErrorBody>(texto).error }.getOrNull()
        val codigo = info?.codigo.orEmpty()
        val mensaje = info?.mensaje?.takeIf { it.isNotBlank() }
        return when {
            codigoHttp == 401 && codigo == "no_autenticado" -> AppError.SesionExpirada
            codigoHttp == 403 && codigo == "correo_no_confirmado" ->
                AppError.CorreoNoConfirmado(mensaje ?: "Confirma tu correo para poder ingresar.")
            codigoHttp == 403 -> AppError.CuentaNoHabilitada(mensaje ?: "Tu cuenta no tiene acceso a la app de clientes.")
            codigoHttp == 429 -> AppError.Bloqueado(mensaje ?: "Demasiados intentos. Intenta más tarde.")
            codigoHttp == 400 && info != null && info.campos.isNotEmpty() ->
                AppError.DatosInvalidos(mensaje ?: "Revisa los datos indicados.", info.campos)
            info != null -> AppError.ErrorApi(codigoHttp, codigo, mensaje ?: "Ocurrió un error. Intenta de nuevo.")
            else -> AppError.ErrorApi(codigoHttp, "", "Ocurrió un error. Intenta de nuevo.")
        }
    }

    companion object {
        private val TIPO_JSON = "application/json; charset=utf-8".toMediaType()
    }
}
