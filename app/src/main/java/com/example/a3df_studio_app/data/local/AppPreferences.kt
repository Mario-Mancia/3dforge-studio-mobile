package com.example.a3df_studio_app.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.forgeDataStore by preferencesDataStore(name = "forge_prefs")

@Serializable
data class SesionLocal(
    val usuarioId: Long,
    val clienteId: Long,
    val correo: String,
    val nombres: String,
    val apellidos: String,
    val expiraEn: String? = null
)

@Serializable
data class ItemCarritoLocal(
    val productoId: Long,
    val nombre: String,
    val precio: String,
    val imagenUrl: String? = null,
    val stock: Int,
    val cantidad: Int
)

class AppPreferences(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    private object Claves {
        val TOKEN = stringPreferencesKey("token_cifrado")
        val SESION = stringPreferencesKey("sesion")
        val CARRITO = stringPreferencesKey("carrito")
        val ULTIMO_CORREO = stringPreferencesKey("ultimo_correo")
    }

    suspend fun leerToken(): String? =
        context.forgeDataStore.data.first()[Claves.TOKEN]?.let { TokenCipher.descifrar(it) }

    suspend fun leerSesion(): SesionLocal? =
        context.forgeDataStore.data.first()[Claves.SESION]?.let { texto ->
            runCatching { json.decodeFromString<SesionLocal>(texto) }.getOrNull()
        }

    suspend fun guardarSesion(token: String, sesion: SesionLocal) {
        context.forgeDataStore.edit { preferencias ->
            preferencias[Claves.TOKEN] = TokenCipher.cifrar(token)
            preferencias[Claves.SESION] = json.encodeToString(sesion)
        }
    }

    suspend fun limpiarSesion() {
        context.forgeDataStore.edit { preferencias ->
            preferencias.remove(Claves.TOKEN)
            preferencias.remove(Claves.SESION)
        }
    }

    private fun decodificarCarrito(preferencias: Preferences): List<ItemCarritoLocal> =
        preferencias[Claves.CARRITO]?.let { texto ->
            runCatching { json.decodeFromString<List<ItemCarritoLocal>>(texto) }.getOrNull()
        } ?: emptyList()

    val carrito: Flow<List<ItemCarritoLocal>> = context.forgeDataStore.data.map { decodificarCarrito(it) }

    suspend fun guardarCarrito(items: List<ItemCarritoLocal>) {
        context.forgeDataStore.edit { preferencias -> preferencias[Claves.CARRITO] = json.encodeToString(items) }
    }

    suspend fun leerUltimoCorreo(): String? = context.forgeDataStore.data.first()[Claves.ULTIMO_CORREO]

    suspend fun guardarUltimoCorreo(correo: String) {
        context.forgeDataStore.edit { preferencias -> preferencias[Claves.ULTIMO_CORREO] = correo }
    }
}
