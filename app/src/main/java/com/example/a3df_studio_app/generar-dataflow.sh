#!/usr/bin/env bash
set -Eeuo pipefail

readonly VERDE='\033[0;32m'
readonly AMARILLO='\033[0;33m'
readonly ROJO='\033[0;31m'
readonly AZUL='\033[0;34m'
readonly RESET='\033[0m'

info()  { printf "${AZUL}[INFO]${RESET} %s\n" "$*"; }
ok()    { printf "${VERDE}[OK]${RESET} %s\n" "$*"; }
aviso() { printf "${AMARILLO}[AVISO]${RESET} %s\n" "$*"; }
error() { printf "${ROJO}[ERROR]${RESET} %s\n" "$*" >&2; exit 1; }

mostrar_ayuda() {
    cat <<'EOF'
GENERADOR DE CAPA DE DATOS - 3DForge Studio (Kotlin + Jetpack Compose + Supabase)

Uso:
  ./generar-dataflow.sh [RUTA_PROYECTO_ANDROID]

Ejecutar sobre un proyecto creado en Android Studio con la plantilla
"Empty Activity" (Compose, Kotlin DSL). Por defecto usa el directorio actual.

Variables opcionales:
  PACKAGE_NAME=com.ejemplo.app     Paquete del código (por defecto: namespace del módulo app)
  KOTLIN_VERSION=2.3.21            Versión de Kotlin (por defecto: libs.versions.toml)
  SUPABASE_VERSION=3.9.0           Fuerza la versión de supabase-kt
  KTOR_VERSION=3.6.0               Fuerza la versión de Ktor

Genera:
  data/    -> Supabase (Database.kt), DTOs, repositorios, DataStore, Google, WhatsApp, DI
  domain/  -> modelos, mappers, validaciones y casos de uso
  MainActivity.kt (inicializa la capa de datos)
  supabase/app_movil.sql y supabase/datos_ejemplo.sql
EOF
}

[[ "${1:-}" == "-h" || "${1:-}" == "--help" ]] && { mostrar_ayuda; exit 0; }

ROOT="$(cd "${1:-.}" && pwd)"
APP_GRADLE="$ROOT/app/build.gradle.kts"
ROOT_GRADLE="$ROOT/build.gradle.kts"
TOML="$ROOT/gradle/libs.versions.toml"
MANIFEST="$ROOT/app/src/main/AndroidManifest.xml"
LOCAL_PROPS="$ROOT/local.properties"

[[ -f "$ROOT/settings.gradle.kts" ]] || error "No se encontró settings.gradle.kts en $ROOT"
[[ -f "$APP_GRADLE" ]] || error "No se encontró app/build.gradle.kts (solo se soporta Kotlin DSL)"
[[ -f "$MANIFEST" ]] || error "No se encontró app/src/main/AndroidManifest.xml"

NAMESPACE="$(grep -E '^\s*namespace\s*=' "$APP_GRADLE" | head -1 | sed -E 's/.*"([^"]+)".*/\1/')"
[[ -n "$NAMESPACE" ]] || error "No se pudo detectar el namespace en app/build.gradle.kts"
PKG="${PACKAGE_NAME:-$NAMESPACE}"
PKG_PATH="${PKG//./\/}"

if [[ -d "$ROOT/app/src/main/kotlin/$PKG_PATH" ]]; then
    D="$ROOT/app/src/main/kotlin/$PKG_PATH"
else
    D="$ROOT/app/src/main/java/$PKG_PATH"
fi
mkdir -p "$D"

version_ge() { [[ "$(printf '%s\n%s\n' "$2" "$1" | sort -V | head -1)" == "$2" ]]; }

if [[ -z "${KOTLIN_VERSION:-}" && -f "$TOML" ]]; then
    KOTLIN_VERSION="$(grep -E '^\s*kotlin\s*=\s*"' "$TOML" | head -1 | sed -E 's/.*"([^"]+)".*/\1/')"
fi
KOTLIN_VERSION="${KOTLIN_VERSION:-2.2.10}"

if version_ge "$KOTLIN_VERSION" "2.4.0"; then
    DEF_SUPABASE="3.9.0"; DEF_KTOR="3.6.0"
elif version_ge "$KOTLIN_VERSION" "2.3.0"; then
    DEF_SUPABASE="3.6.0"; DEF_KTOR="3.4.3"
else
    DEF_SUPABASE="3.4.1"; DEF_KTOR="3.4.0"
    version_ge "$KOTLIN_VERSION" "2.2.0" || aviso "Kotlin $KOTLIN_VERSION es antiguo; se recomienda 2.2.10 o superior."
fi
SUPABASE_VERSION="${SUPABASE_VERSION:-$DEF_SUPABASE}"
KTOR_VERSION="${KTOR_VERSION:-$DEF_KTOR}"
readonly CREDENTIALS_VERSION="1.5.0"
readonly GOOGLEID_VERSION="1.1.1"
readonly DATASTORE_VERSION="1.1.7"

info "Proyecto:   $ROOT"
info "Paquete:    $PKG (namespace $NAMESPACE)"
info "Código en:  ${D#"$ROOT"/}"
info "Kotlin $KOTLIN_VERSION | supabase-kt $SUPABASE_VERSION | Ktor $KTOR_VERSION"

RESPALDO="$ROOT/.respaldos/dataflow-$(date +%Y%m%d-%H%M%S)"
mkdir -p "$RESPALDO"
respaldar() {
    local origen="$1"
    [[ -e "$origen" ]] || return 0
    local relativo="${origen#"$ROOT"/}"
    mkdir -p "$RESPALDO/$(dirname "$relativo")"
    cp -r "$origen" "$RESPALDO/$relativo"
}

for archivo in "$APP_GRADLE" "$ROOT_GRADLE" "$MANIFEST" "$LOCAL_PROPS" "$D/MainActivity.kt"; do
    respaldar "$archivo"
done
for dir in "$D/data" "$D/domain"; do
    if [[ -d "$dir" ]]; then
        respaldar "$dir"
        rm -rf "$dir"
    fi
done
if [[ -f "$ROOT/.gitignore" ]] && ! grep -qx '.respaldos/' "$ROOT/.gitignore"; then
    printf '\n.respaldos/\n' >> "$ROOT/.gitignore"
fi
ok "Respaldo creado en ${RESPALDO#"$ROOT"/}"

ARCHIVOS_CREADOS=0
crear() {
    local destino="$1"
    mkdir -p "$(dirname "$destino")"
    sed -e "s/__PKG__/${PKG}/g" -e "s/__NS__/${NAMESPACE}/g" > "$destino"
    ARCHIVOS_CREADOS=$((ARCHIVOS_CREADOS + 1))
    printf "  ${VERDE}+${RESET} %s\n" "${destino#"$ROOT"/}"
}

# ============================================================
# DATA / CONFIG
# ============================================================
info "Generando capa data/ ..."

crear "$D/data/config/AppConfig.kt" <<'KT'
package __PKG__.data.config

import __NS__.BuildConfig

object AppConfig {
    val supabaseUrl: String = BuildConfig.SUPABASE_URL.trim()
    val supabaseAnonKey: String = BuildConfig.SUPABASE_ANON_KEY.trim()
    val googleWebClientId: String = BuildConfig.GOOGLE_WEB_CLIENT_ID.trim()
    val whatsappFallbackNumber: String = BuildConfig.WHATSAPP_NUMBER.trim()

    const val AUTH_SCHEME = "forgestudio"
    const val AUTH_HOST = "auth-callback"
    const val NOMBRE_NEGOCIO = "3DForge Studio"

    val isSupabaseConfigured: Boolean
        get() = supabaseUrl.startsWith("https://") && supabaseAnonKey.isNotBlank()

    val isGoogleConfigured: Boolean
        get() = googleWebClientId.isNotBlank()
}
KT

# ============================================================
# DATA / REMOTE / SUPABASE
# ============================================================
crear "$D/data/remote/supabase/Database.kt" <<'KT'
package __PKG__.data.remote.supabase

import __PKG__.data.config.AppConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json

object Database {

    val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
        encodeDefaults = true
    }

    val isConfigured: Boolean
        get() = AppConfig.isSupabaseConfigured

    val client: SupabaseClient by lazy {
        check(isConfigured) { NOT_CONFIGURED_MESSAGE }
        createSupabaseClient(
            supabaseUrl = AppConfig.supabaseUrl,
            supabaseKey = AppConfig.supabaseAnonKey
        ) {
            defaultSerializer = KotlinXSerializer(json)
            install(Auth) {
                scheme = AppConfig.AUTH_SCHEME
                host = AppConfig.AUTH_HOST
            }
            install(Postgrest)
        }
    }

    val auth: Auth
        get() = client.auth

    val postgrest: Postgrest
        get() = client.postgrest

    const val NOT_CONFIGURED_MESSAGE =
        "Supabase no está configurado. Define SUPABASE_URL y SUPABASE_ANON_KEY en local.properties."
}
KT

crear "$D/data/remote/supabase/Tables.kt" <<'KT'
package __PKG__.data.remote.supabase

object Tables {
    const val CLIENTES = "clientes"
    const val CATEGORIAS_SERVICIOS = "categorias_servicios"
    const val SERVICIOS = "servicios"
    const val PRODUCTOS = "productos"
    const val COTIZACIONES = "cotizaciones"
    const val ENCARGOS = "encargos"
    const val FACTURAS = "facturas"
    const val PAGOS = "pagos"
    const val PROMOCIONES = "promociones"
    const val RESENAS = "resenas"
    const val SOLICITUDES_SOPORTE = "solicitudes_soporte"
    const val CONFIGURACION_NEGOCIO = "configuracion_negocio"
}

object Rpc {
    const val ASEGURAR_CLIENTE = "app_asegurar_cliente"
    const val CREAR_COTIZACION = "app_crear_cotizacion"
    const val CREAR_PEDIDO = "app_crear_pedido"
    const val REGISTRAR_PAGO_SIMULADO = "app_registrar_pago_simulado"
    const val REGISTRAR_INTERACCION = "app_registrar_interaccion"
}
KT

crear "$D/data/remote/supabase/SupabaseCall.kt" <<'KT'
package __PKG__.data.remote.supabase

import __PKG__.domain.models.AppException
import __PKG__.domain.models.AuthRequiredException
import __PKG__.domain.models.NotConfiguredException
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import kotlinx.serialization.SerializationException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

internal suspend fun <T> supabaseCall(block: suspend () -> T): T {
    if (!Database.isConfigured) throw NotConfiguredException()
    return try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        throw e.toAppException()
    }
}

internal fun Throwable.toAppException(): AppException = when (this) {
    is AppException -> this
    is AuthRestException -> AppException(authMessage(), this)
    is PostgrestRestException -> postgrestException()
    is IOException -> AppException("No hay conexión a internet o el servidor no está disponible.", this)
    is SerializationException -> AppException("La respuesta del servidor no tiene el formato esperado.", this)
    else -> AppException(message ?: "Ocurrió un error inesperado.", this)
}

private fun AuthRestException.authMessage(): String = when (errorCode) {
    AuthErrorCode.InvalidCredentials -> "Correo o contraseña incorrectos."
    AuthErrorCode.EmailNotConfirmed -> "Debes verificar tu correo antes de iniciar sesión."
    AuthErrorCode.UserAlreadyExists, AuthErrorCode.EmailExists -> "Ya existe una cuenta registrada con este correo."
    AuthErrorCode.OtpExpired -> "El código expiró o no es válido. Solicita uno nuevo."
    AuthErrorCode.WeakPassword -> "La contraseña es demasiado débil."
    AuthErrorCode.OverEmailSendRateLimit -> "Se enviaron demasiados correos. Espera unos minutos e inténtalo de nuevo."
    else -> errorDescription.ifBlank { "No se pudo completar la autenticación." }
}

private fun PostgrestRestException.postgrestException(): AppException {
    val raw = error
    return when {
        raw.contains("AUTH_REQUIRED") || code == "PGRST301" -> AuthRequiredException()
        raw.contains("CLIENTE_NO_REGISTRADO") -> AppException("Tu perfil de cliente aún no existe. Vuelve a iniciar sesión.")
        raw.contains("SIN_ITEMS") -> AppException("Agrega al menos un elemento a la solicitud.")
        raw.contains("CANTIDAD_INVALIDA") -> AppException("Las cantidades deben ser mayores a cero.")
        raw.contains("SERVICIO_NO_DISPONIBLE") -> AppException("Uno de los servicios seleccionados ya no está disponible.")
        raw.contains("PRODUCTO_NO_DISPONIBLE") -> AppException("Uno de los productos seleccionados ya no está disponible.")
        raw.contains("STOCK_INSUFICIENTE") -> AppException("No hay stock suficiente de ${raw.substringAfter("STOCK_INSUFICIENTE:", "un producto").trim()}.")
        raw.contains("FACTURA_NO_ENCONTRADA") -> AppException("No se encontró la factura seleccionada.")
        raw.contains("FACTURA_NO_PAGABLE") -> AppException("Esta factura ya no admite pagos.")
        raw.contains("MONTO_INVALIDO") -> AppException("El monto no es válido para esta factura.")
        raw.contains("TARJETA_INVALIDA") -> AppException("Los datos de la tarjeta no son válidos.")
        raw.contains("TIPO_INVALIDO") -> AppException("Tipo de solicitud no válido.")
        raw.contains("usuario del sistema") -> AppException("El negocio aún no tiene un usuario responsable configurado.")
        code == "42501" -> AppException("No tienes permisos para realizar esta acción.")
        code == "PGRST202" -> AppException("Falta ejecutar el script supabase/app_movil.sql en la base de datos.")
        else -> AppException("Error del servidor: $raw", this)
    }
}
KT

# ============================================================
# DATA / REMOTE / DTO
# ============================================================
crear "$D/data/remote/dto/ClienteDto.kt" <<'KT'
package __PKG__.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClienteDto(
    val id: Long,
    @SerialName("auth_uid") val authUid: String? = null,
    @SerialName("tipo_cliente") val tipoCliente: String = "PERSONA",
    val nombres: String? = null,
    val apellidos: String? = null,
    @SerialName("nombre_empresa") val nombreEmpresa: String? = null,
    val correo: String = "",
    val telefono: String? = null,
    @SerialName("identificacion_fiscal") val identificacionFiscal: String? = null,
    val direccion: String? = null,
    @SerialName("foto_url") val fotoUrl: String? = null,
    @SerialName("creado_en") val creadoEn: String? = null,
    @SerialName("actualizado_en") val actualizadoEn: String? = null
)
KT

crear "$D/data/remote/dto/CatalogoDto.kt" <<'KT'
package __PKG__.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NombreRefDto(
    val nombre: String = ""
)

@Serializable
data class CategoriaServicioDto(
    val id: Long,
    val nombre: String,
    val descripcion: String? = null,
    val activo: Boolean = true
)

@Serializable
data class ServicioDto(
    val id: Long,
    @SerialName("id_categoria") val idCategoria: Long,
    val nombre: String,
    val descripcion: String = "",
    @SerialName("precio_base") val precioBase: Double = 0.0,
    @SerialName("requiere_archivo") val requiereArchivo: Boolean = false,
    val activo: Boolean = true,
    @SerialName("categorias_servicios") val categoria: NombreRefDto? = null
)

@Serializable
data class ProductoDto(
    val id: Long,
    val sku: String,
    val nombre: String,
    val descripcion: String? = null,
    val precio: Double = 0.0,
    val stock: Int = 0,
    @SerialName("stock_minimo") val stockMinimo: Int = 0,
    val imagen: String? = null,
    val activo: Boolean = true
)

@Serializable
data class PromocionDto(
    val id: Long,
    val nombre: String,
    val descripcion: String? = null,
    @SerialName("tipo_descuento") val tipoDescuento: String,
    @SerialName("valor_descuento") val valorDescuento: Double,
    @SerialName("fecha_inicio") val fechaInicio: String? = null,
    @SerialName("fecha_fin") val fechaFin: String? = null,
    val activo: Boolean = true
)

@Serializable
data class ConfiguracionNegocioDto(
    val id: Long,
    @SerialName("nombre_negocio") val nombreNegocio: String,
    val correo: String? = null,
    val telefono: String? = null,
    val direccion: String? = null,
    @SerialName("sitio_web") val sitioWeb: String? = null,
    val logo: String? = null
)
KT

crear "$D/data/remote/dto/CotizacionDto.kt" <<'KT'
package __PKG__.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CotizacionDto(
    val id: Long,
    val codigo: String,
    @SerialName("fecha_emision") val fechaEmision: String? = null,
    @SerialName("fecha_vencimiento") val fechaVencimiento: String? = null,
    val subtotal: Double = 0.0,
    val descuento: Double = 0.0,
    val impuesto: Double = 0.0,
    val total: Double = 0.0,
    val estado: String,
    val observaciones: String? = null,
    @SerialName("cotizaciones_detalles") val detalles: List<CotizacionDetalleDto> = emptyList()
)

@Serializable
data class CotizacionDetalleDto(
    val id: Long,
    @SerialName("id_servicio") val idServicio: Long,
    val descripcion: String,
    val cantidad: Double,
    @SerialName("precio_unitario") val precioUnitario: Double,
    val descuento: Double = 0.0,
    val impuesto: Double = 0.0,
    val subtotal: Double
)
KT

crear "$D/data/remote/dto/EncargoDto.kt" <<'KT'
package __PKG__.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EncargoDto(
    val id: Long,
    val codigo: String,
    @SerialName("fecha_solicitud") val fechaSolicitud: String? = null,
    @SerialName("fecha_entrega_estimada") val fechaEntregaEstimada: String? = null,
    @SerialName("fecha_entrega_real") val fechaEntregaReal: String? = null,
    val descripcion: String = "",
    val subtotal: Double = 0.0,
    val descuento: Double = 0.0,
    val impuesto: Double = 0.0,
    val total: Double = 0.0,
    val estado: String,
    val observaciones: String? = null,
    @SerialName("encargos_detalles") val detalles: List<EncargoDetalleDto> = emptyList(),
    @SerialName("historial_encargos") val historial: List<HistorialEncargoDto> = emptyList()
)

@Serializable
data class EncargoDetalleDto(
    val id: Long,
    @SerialName("id_servicio") val idServicio: Long? = null,
    @SerialName("id_producto") val idProducto: Long? = null,
    val descripcion: String,
    val cantidad: Double,
    @SerialName("precio_unitario") val precioUnitario: Double,
    val descuento: Double = 0.0,
    val impuesto: Double = 0.0,
    val subtotal: Double
)

@Serializable
data class HistorialEncargoDto(
    val id: Long,
    @SerialName("estado_anterior") val estadoAnterior: String? = null,
    @SerialName("estado_nuevo") val estadoNuevo: String,
    val comentario: String? = null,
    @SerialName("fecha_cambio") val fechaCambio: String? = null
)
KT

crear "$D/data/remote/dto/FacturacionDto.kt" <<'KT'
package __PKG__.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FacturaDto(
    val id: Long,
    @SerialName("id_encargo") val idEncargo: Long? = null,
    @SerialName("numero_factura") val numeroFactura: String,
    @SerialName("fecha_emision") val fechaEmision: String? = null,
    @SerialName("fecha_vencimiento") val fechaVencimiento: String? = null,
    @SerialName("nombre_facturacion") val nombreFacturacion: String = "",
    val subtotal: Double = 0.0,
    val descuento: Double = 0.0,
    val impuesto: Double = 0.0,
    val total: Double = 0.0,
    @SerialName("saldo_pendiente") val saldoPendiente: Double = 0.0,
    val estado: String,
    val observaciones: String? = null,
    @SerialName("facturas_detalles") val detalles: List<FacturaDetalleDto> = emptyList()
)

@Serializable
data class FacturaDetalleDto(
    val id: Long,
    val descripcion: String,
    val cantidad: Double,
    @SerialName("precio_unitario") val precioUnitario: Double,
    val subtotal: Double
)

@Serializable
data class PagoDto(
    val id: Long,
    @SerialName("id_factura") val idFactura: Long,
    val referencia: String? = null,
    val monto: Double,
    @SerialName("fecha_pago") val fechaPago: String? = null,
    val estado: String,
    val observaciones: String? = null,
    @SerialName("metodos_pago") val metodo: NombreRefDto? = null
)
KT

crear "$D/data/remote/dto/FeedbackDto.kt" <<'KT'
package __PKG__.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResenaDto(
    val id: Long,
    @SerialName("id_servicio") val idServicio: Long? = null,
    @SerialName("id_encargo") val idEncargo: Long? = null,
    val puntuacion: Int,
    val comentario: String? = null,
    @SerialName("fecha_creacion") val fechaCreacion: String? = null
)

@Serializable
data class ResenaInsertDto(
    @SerialName("id_cliente") val idCliente: Long,
    @SerialName("id_servicio") val idServicio: Long? = null,
    @SerialName("id_encargo") val idEncargo: Long? = null,
    val puntuacion: Int,
    val comentario: String? = null
)

@Serializable
data class SolicitudSoporteDto(
    val id: Long,
    val asunto: String,
    val descripcion: String,
    val prioridad: String,
    val estado: String,
    @SerialName("creado_en") val creadoEn: String? = null
)

@Serializable
data class SolicitudSoporteInsertDto(
    @SerialName("id_cliente") val idCliente: Long,
    val asunto: String,
    val descripcion: String,
    val prioridad: String,
    val estado: String = "ABIERTA"
)
KT

crear "$D/data/remote/dto/RpcDto.kt" <<'KT'
package __PKG__.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AsegurarClienteParams(
    @SerialName("p_nombres") val nombres: String? = null,
    @SerialName("p_apellidos") val apellidos: String? = null,
    @SerialName("p_telefono") val telefono: String? = null,
    @SerialName("p_foto_url") val fotoUrl: String? = null
)

@Serializable
data class CotizacionItemParam(
    @SerialName("id_servicio") val idServicio: Long,
    val cantidad: Double,
    val descripcion: String? = null
)

@Serializable
data class CrearCotizacionParams(
    @SerialName("p_items") val items: List<CotizacionItemParam>,
    @SerialName("p_observaciones") val observaciones: String? = null
)

@Serializable
data class CotizacionCreadaDto(
    val id: Long,
    val codigo: String,
    val total: Double
)

@Serializable
data class PedidoItemParam(
    @SerialName("id_producto") val idProducto: Long,
    val cantidad: Int
)

@Serializable
data class CrearPedidoParams(
    @SerialName("p_items") val items: List<PedidoItemParam>,
    @SerialName("p_observaciones") val observaciones: String? = null
)

@Serializable
data class PedidoCreadoDto(
    @SerialName("id_encargo") val idEncargo: Long,
    val codigo: String,
    @SerialName("id_factura") val idFactura: Long,
    @SerialName("numero_factura") val numeroFactura: String,
    val total: Double
)

@Serializable
data class PagoSimuladoParams(
    @SerialName("p_id_factura") val idFactura: Long,
    @SerialName("p_monto") val monto: Double,
    @SerialName("p_marca") val marca: String,
    @SerialName("p_ultimos4") val ultimos4: String,
    @SerialName("p_titular") val titular: String? = null
)

@Serializable
data class PagoRegistradoDto(
    @SerialName("id_pago") val idPago: Long,
    val referencia: String,
    val estado: String,
    @SerialName("saldo_pendiente") val saldoPendiente: Double,
    @SerialName("estado_factura") val estadoFactura: String
)

@Serializable
data class InteraccionParams(
    @SerialName("p_tipo") val tipo: String,
    @SerialName("p_asunto") val asunto: String,
    @SerialName("p_descripcion") val descripcion: String? = null
)
KT

# ============================================================
# DATA / REMOTE / AUTH (GOOGLE)
# ============================================================
crear "$D/data/remote/auth/GoogleCredentialProvider.kt" <<'KT'
package __PKG__.data.remote.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import __PKG__.data.config.AppConfig
import __PKG__.domain.models.AppException
import __PKG__.domain.models.GoogleSignInCancelledException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import java.security.MessageDigest
import java.util.UUID

class GoogleCredentialProvider {

    data class GoogleToken(
        val idToken: String,
        val rawNonce: String
    )

    suspend fun requestIdToken(activityContext: Context): GoogleToken {
        if (!AppConfig.isGoogleConfigured) {
            throw AppException("Configura GOOGLE_WEB_CLIENT_ID en local.properties para usar Google.")
        }

        val rawNonce = UUID.randomUUID().toString()
        val hashedNonce = MessageDigest.getInstance("SHA-256")
            .digest(rawNonce.toByteArray())
            .joinToString("") { "%02x".format(it) }

        val googleOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(AppConfig.googleWebClientId)
            .setNonce(hashedNonce)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleOption)
            .build()

        val response = try {
            CredentialManager.create(activityContext).getCredential(activityContext, request)
        } catch (e: GetCredentialCancellationException) {
            throw GoogleSignInCancelledException()
        } catch (e: NoCredentialException) {
            throw AppException("No se encontró una cuenta de Google en este dispositivo.", e)
        } catch (e: GetCredentialException) {
            throw AppException("No se pudo iniciar sesión con Google: ${e.message}", e)
        }

        val credential = response.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
            return GoogleToken(idToken = googleCredential.idToken, rawNonce = rawNonce)
        }
        throw AppException("La credencial recibida de Google no es válida.")
    }
}
KT

# ============================================================
# DATA / LOCAL / DATASTORE
# ============================================================
crear "$D/data/local/datastore/PreferenceKeys.kt" <<'KT'
package __PKG__.data.local.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferenceKeys {
    val ONBOARDING_VISTO = booleanPreferencesKey("onboarding_visto")
    val ULTIMO_CORREO = stringPreferencesKey("ultimo_correo")
    val CORREO_PENDIENTE_VERIFICACION = stringPreferencesKey("correo_pendiente_verificacion")
}
KT

# ============================================================
# DATA / EXTERNAL
# ============================================================
crear "$D/data/external/WhatsAppLauncher.kt" <<'KT'
package __PKG__.data.external

import android.content.Intent
import android.net.Uri
import __PKG__.domain.models.AppException

class WhatsAppLauncher {

    fun crearIntent(telefono: String, mensaje: String): Intent {
        val digitos = telefono.filter { it.isDigit() }
        if (digitos.length < 8) throw AppException("El número de WhatsApp del negocio no es válido.")
        val uri = Uri.parse("https://wa.me/$digitos?text=${Uri.encode(mensaje)}")
        return Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}
KT

# ============================================================
# DATA / REPOSITORIES
# ============================================================
crear "$D/data/repositories/AuthRepository.kt" <<'KT'
package __PKG__.data.repositories

import android.content.Context
import __PKG__.data.remote.auth.GoogleCredentialProvider
import __PKG__.data.remote.supabase.Database
import __PKG__.data.remote.supabase.supabaseCall
import __PKG__.domain.mappers.toAuthUser
import __PKG__.domain.models.AppException
import __PKG__.domain.models.AuthRequiredException
import __PKG__.domain.models.AuthState
import __PKG__.domain.models.AuthUser
import __PKG__.domain.models.EmailNotVerifiedException
import __PKG__.domain.models.NotConfiguredException
import __PKG__.domain.models.SignUpResult
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthRepository(
    private val googleCredentialProvider: GoogleCredentialProvider
) {

    val authState: Flow<AuthState> = flow {
        if (!Database.isConfigured) {
            emit(AuthState.NotConfigured)
            return@flow
        }
        emitAll(Database.auth.sessionStatus.map { it.toAuthState() })
    }.distinctUntilChanged()

    fun usuarioActual(): AuthUser? {
        if (!Database.isConfigured) return null
        return Database.auth.currentUserOrNull()?.toAuthUser()
    }

    fun requireUser(): AuthUser {
        if (!Database.isConfigured) throw NotConfiguredException()
        return usuarioActual() ?: throw AuthRequiredException()
    }

    suspend fun registrar(
        correo: String,
        clave: String,
        nombres: String,
        apellidos: String
    ): SignUpResult = supabaseCall {
        val resultado = Database.auth.signUpWith(Email) {
            email = correo
            password = clave
            data = buildJsonObject {
                put("nombres", nombres)
                put("apellidos", apellidos)
                put("full_name", "$nombres $apellidos")
            }
        }
        val sesion = Database.auth.currentUserOrNull()
        if (resultado == null && sesion != null) {
            SignUpResult.SignedIn(sesion.toAuthUser())
        } else {
            SignUpResult.VerificationRequired(correo)
        }
    }

    suspend fun verificarCodigoRegistro(correo: String, codigo: String): AuthUser = supabaseCall {
        Database.auth.verifyEmailOtp(type = OtpType.Email.SIGNUP, email = correo, token = codigo)
        Database.auth.currentUserOrNull()?.toAuthUser()
            ?: throw AppException("No se pudo iniciar la sesión después de verificar el correo.")
    }

    suspend fun reenviarCodigoRegistro(correo: String) = supabaseCall {
        Database.auth.resendEmail(OtpType.Email.SIGNUP, correo)
    }

    suspend fun iniciarSesion(correo: String, clave: String): AuthUser = supabaseCall {
        try {
            Database.auth.signInWith(Email) {
                email = correo
                password = clave
            }
        } catch (e: AuthRestException) {
            if (e.errorCode == AuthErrorCode.EmailNotConfirmed) throw EmailNotVerifiedException(correo)
            throw e
        }
        Database.auth.currentUserOrNull()?.toAuthUser()
            ?: throw AppException("No se pudo recuperar la sesión del usuario.")
    }

    suspend fun iniciarSesionConGoogle(activityContext: Context): AuthUser = supabaseCall {
        val token = googleCredentialProvider.requestIdToken(activityContext)
        Database.auth.signInWith(IDToken) {
            idToken = token.idToken
            provider = Google
            nonce = token.rawNonce
        }
        Database.auth.currentUserOrNull()?.toAuthUser()
            ?: throw AppException("No se pudo recuperar la sesión de Google.")
    }

    suspend fun enviarRecuperacionClave(correo: String) = supabaseCall {
        Database.auth.resetPasswordForEmail(correo)
    }

    suspend fun cerrarSesion() = supabaseCall {
        Database.auth.signOut()
    }

    private fun SessionStatus.toAuthState(): AuthState = when (this) {
        is SessionStatus.Authenticated ->
            session.user?.let { AuthState.Authenticated(it.toAuthUser()) } ?: AuthState.Loading
        is SessionStatus.NotAuthenticated -> AuthState.Guest
        is SessionStatus.Initializing -> AuthState.Loading
        is SessionStatus.RefreshFailure ->
            Database.auth.currentUserOrNull()?.let { AuthState.Authenticated(it.toAuthUser()) } ?: AuthState.Guest
    }
}
KT

crear "$D/data/repositories/ClienteRepository.kt" <<'KT'
package __PKG__.data.repositories

import __PKG__.data.remote.dto.AsegurarClienteParams
import __PKG__.data.remote.dto.ClienteDto
import __PKG__.data.remote.supabase.Database
import __PKG__.data.remote.supabase.Rpc
import __PKG__.data.remote.supabase.Tables
import __PKG__.data.remote.supabase.supabaseCall
import __PKG__.domain.mappers.toDomain
import __PKG__.domain.models.AuthRequiredException
import __PKG__.domain.models.Cliente
import __PKG__.domain.models.ClienteUpdate
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.Instant

class ClienteRepository {

    private val _clienteActual = MutableStateFlow<Cliente?>(null)
    val clienteActual: StateFlow<Cliente?> = _clienteActual.asStateFlow()

    suspend fun asegurarCliente(
        nombres: String?,
        apellidos: String?,
        telefono: String?,
        fotoUrl: String?
    ): Cliente = supabaseCall {
        val params = AsegurarClienteParams(
            nombres = nombres?.trim()?.takeIf { it.isNotEmpty() },
            apellidos = apellidos?.trim()?.takeIf { it.isNotEmpty() },
            telefono = telefono?.trim()?.takeIf { it.isNotEmpty() },
            fotoUrl = fotoUrl?.takeIf { it.isNotBlank() }
        )
        Database.postgrest.rpc(Rpc.ASEGURAR_CLIENTE, params)
            .decodeAs<ClienteDto>()
            .toDomain()
            .also { _clienteActual.value = it }
    }

    suspend fun obtenerMiCliente(): Cliente? = supabaseCall {
        val uid = Database.auth.currentUserOrNull()?.id ?: throw AuthRequiredException()
        Database.postgrest.from(Tables.CLIENTES)
            .select { filter { eq("auth_uid", uid) } }
            .decodeSingleOrNull<ClienteDto>()
            ?.toDomain()
            ?.also { _clienteActual.value = it }
    }

    suspend fun requireClienteId(): Long =
        _clienteActual.value?.id
            ?: obtenerMiCliente()?.id
            ?: asegurarCliente(null, null, null, null).id

    suspend fun actualizar(idCliente: Long, cambios: ClienteUpdate): Cliente = supabaseCall {
        val cuerpo = buildJsonObject {
            put("tipo_cliente", cambios.tipo.name)
            put("nombres", cambios.nombres.limpio())
            put("apellidos", cambios.apellidos.limpio())
            put("nombre_empresa", cambios.nombreEmpresa.limpio())
            put("telefono", cambios.telefono.limpio())
            put("identificacion_fiscal", cambios.identificacionFiscal.limpio())
            put("direccion", cambios.direccion.limpio())
            put("actualizado_en", Instant.now().toString())
        }
        Database.postgrest.from(Tables.CLIENTES)
            .update(cuerpo) {
                select()
                filter { eq("id", idCliente) }
            }
            .decodeSingle<ClienteDto>()
            .toDomain()
            .also { _clienteActual.value = it }
    }

    fun limpiarCache() {
        _clienteActual.value = null
    }

    private fun String?.limpio(): String? = this?.trim()?.takeIf { it.isNotEmpty() }
}
KT

crear "$D/data/repositories/CatalogRepository.kt" <<'KT'
package __PKG__.data.repositories

import __PKG__.data.remote.dto.CategoriaServicioDto
import __PKG__.data.remote.dto.ProductoDto
import __PKG__.data.remote.dto.PromocionDto
import __PKG__.data.remote.dto.ServicioDto
import __PKG__.data.remote.supabase.Database
import __PKG__.data.remote.supabase.Tables
import __PKG__.data.remote.supabase.supabaseCall
import __PKG__.domain.mappers.toDomain
import __PKG__.domain.models.CategoriaServicio
import __PKG__.domain.models.Producto
import __PKG__.domain.models.Promocion
import __PKG__.domain.models.Servicio
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import java.time.Instant

class CatalogRepository {

    private val columnasServicio = Columns.raw("*, categorias_servicios(nombre)")

    suspend fun obtenerProductos(busqueda: String? = null): List<Producto> = supabaseCall {
        val termino = busqueda?.trim()?.takeIf { it.isNotEmpty() }
        Database.postgrest.from(Tables.PRODUCTOS)
            .select {
                filter {
                    eq("activo", true)
                    if (termino != null) ilike("nombre", "%$termino%")
                }
                order("nombre", Order.ASCENDING)
            }
            .decodeList<ProductoDto>()
            .map { it.toDomain() }
    }

    suspend fun obtenerProducto(id: Long): Producto? = supabaseCall {
        Database.postgrest.from(Tables.PRODUCTOS)
            .select { filter { eq("id", id) } }
            .decodeSingleOrNull<ProductoDto>()
            ?.toDomain()
    }

    suspend fun obtenerCategorias(): List<CategoriaServicio> = supabaseCall {
        Database.postgrest.from(Tables.CATEGORIAS_SERVICIOS)
            .select {
                filter { eq("activo", true) }
                order("nombre", Order.ASCENDING)
            }
            .decodeList<CategoriaServicioDto>()
            .map { it.toDomain() }
    }

    suspend fun obtenerServicios(idCategoria: Long? = null): List<Servicio> = supabaseCall {
        Database.postgrest.from(Tables.SERVICIOS)
            .select(columnasServicio) {
                filter {
                    eq("activo", true)
                    if (idCategoria != null) eq("id_categoria", idCategoria)
                }
                order("nombre", Order.ASCENDING)
            }
            .decodeList<ServicioDto>()
            .map { it.toDomain() }
    }

    suspend fun obtenerServicio(id: Long): Servicio? = supabaseCall {
        Database.postgrest.from(Tables.SERVICIOS)
            .select(columnasServicio) { filter { eq("id", id) } }
            .decodeSingleOrNull<ServicioDto>()
            ?.toDomain()
    }

    suspend fun obtenerPromocionesActivas(): List<Promocion> = supabaseCall {
        val ahora = Instant.now().toString()
        Database.postgrest.from(Tables.PROMOCIONES)
            .select {
                filter {
                    eq("activo", true)
                    lte("fecha_inicio", ahora)
                    gte("fecha_fin", ahora)
                }
                order("fecha_fin", Order.ASCENDING)
            }
            .decodeList<PromocionDto>()
            .map { it.toDomain() }
    }
}
KT

crear "$D/data/repositories/NegocioRepository.kt" <<'KT'
package __PKG__.data.repositories

import __PKG__.data.config.AppConfig
import __PKG__.data.remote.dto.ConfiguracionNegocioDto
import __PKG__.data.remote.supabase.Database
import __PKG__.data.remote.supabase.Tables
import __PKG__.data.remote.supabase.supabaseCall
import __PKG__.domain.mappers.toDomain
import __PKG__.domain.models.AppException
import __PKG__.domain.models.NegocioInfo
import __PKG__.domain.utils.valorONull
import io.github.jan.supabase.postgrest.query.Order

class NegocioRepository {

    @Volatile
    private var cache: NegocioInfo? = null

    suspend fun obtenerNegocio(forzar: Boolean = false): NegocioInfo? {
        if (!forzar) cache?.let { return it }
        return supabaseCall {
            Database.postgrest.from(Tables.CONFIGURACION_NEGOCIO)
                .select {
                    order("id", Order.ASCENDING)
                    limit(1)
                }
                .decodeSingleOrNull<ConfiguracionNegocioDto>()
                ?.toDomain()
        }.also { cache = it }
    }

    suspend fun obtenerTelefonoWhatsApp(): String {
        val remoto = valorONull { obtenerNegocio()?.telefono }
        return listOfNotNull(remoto, AppConfig.whatsappFallbackNumber)
            .map { numero -> numero.filter { it.isDigit() } }
            .firstOrNull { it.length >= 8 }
            ?: throw AppException("No hay un número de WhatsApp configurado para el negocio.")
    }
}
KT

crear "$D/data/repositories/CotizacionRepository.kt" <<'KT'
package __PKG__.data.repositories

import __PKG__.data.remote.dto.CotizacionCreadaDto
import __PKG__.data.remote.dto.CotizacionDto
import __PKG__.data.remote.dto.CotizacionItemParam
import __PKG__.data.remote.dto.CrearCotizacionParams
import __PKG__.data.remote.supabase.Database
import __PKG__.data.remote.supabase.Rpc
import __PKG__.data.remote.supabase.Tables
import __PKG__.data.remote.supabase.supabaseCall
import __PKG__.domain.mappers.toDomain
import __PKG__.domain.models.Cotizacion
import __PKG__.domain.models.CotizacionCreada
import __PKG__.domain.models.ItemCotizacion
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc

class CotizacionRepository {

    suspend fun crearCotizacion(items: List<ItemCotizacion>, observaciones: String?): CotizacionCreada = supabaseCall {
        val params = CrearCotizacionParams(
            items = items.map { CotizacionItemParam(it.servicio.id, it.cantidad, it.detalle) },
            observaciones = observaciones
        )
        Database.postgrest.rpc(Rpc.CREAR_COTIZACION, params)
            .decodeAs<CotizacionCreadaDto>()
            .toDomain()
    }

    suspend fun obtenerMisCotizaciones(): List<Cotizacion> = supabaseCall {
        Database.postgrest.from(Tables.COTIZACIONES)
            .select(Columns.raw("*, cotizaciones_detalles(*)")) {
                order("fecha_emision", Order.DESCENDING)
            }
            .decodeList<CotizacionDto>()
            .map { it.toDomain() }
    }
}
KT

crear "$D/data/repositories/EncargoRepository.kt" <<'KT'
package __PKG__.data.repositories

import __PKG__.data.remote.dto.CrearPedidoParams
import __PKG__.data.remote.dto.EncargoDto
import __PKG__.data.remote.dto.PedidoCreadoDto
import __PKG__.data.remote.dto.PedidoItemParam
import __PKG__.data.remote.supabase.Database
import __PKG__.data.remote.supabase.Rpc
import __PKG__.data.remote.supabase.Tables
import __PKG__.data.remote.supabase.supabaseCall
import __PKG__.domain.mappers.toDomain
import __PKG__.domain.models.Encargo
import __PKG__.domain.models.ItemPedido
import __PKG__.domain.models.PedidoCreado
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc

class EncargoRepository {

    private val columnas = Columns.raw("*, encargos_detalles(*), historial_encargos(*)")

    suspend fun crearPedido(items: List<ItemPedido>, observaciones: String?): PedidoCreado = supabaseCall {
        val params = CrearPedidoParams(
            items = items.map { PedidoItemParam(it.producto.id, it.cantidad) },
            observaciones = observaciones
        )
        Database.postgrest.rpc(Rpc.CREAR_PEDIDO, params)
            .decodeAs<PedidoCreadoDto>()
            .toDomain()
    }

    suspend fun obtenerMisEncargos(): List<Encargo> = supabaseCall {
        Database.postgrest.from(Tables.ENCARGOS)
            .select(columnas) { order("fecha_solicitud", Order.DESCENDING) }
            .decodeList<EncargoDto>()
            .map { it.toDomain() }
    }

    suspend fun obtenerEncargo(id: Long): Encargo? = supabaseCall {
        Database.postgrest.from(Tables.ENCARGOS)
            .select(columnas) { filter { eq("id", id) } }
            .decodeSingleOrNull<EncargoDto>()
            ?.toDomain()
    }
}
KT

crear "$D/data/repositories/PagoRepository.kt" <<'KT'
package __PKG__.data.repositories

import __PKG__.data.remote.dto.FacturaDto
import __PKG__.data.remote.dto.PagoDto
import __PKG__.data.remote.dto.PagoRegistradoDto
import __PKG__.data.remote.dto.PagoSimuladoParams
import __PKG__.data.remote.supabase.Database
import __PKG__.data.remote.supabase.Rpc
import __PKG__.data.remote.supabase.Tables
import __PKG__.data.remote.supabase.supabaseCall
import __PKG__.domain.mappers.toDomain
import __PKG__.domain.models.EstadoFactura
import __PKG__.domain.models.Factura
import __PKG__.domain.models.Pago
import __PKG__.domain.models.PagoRegistrado
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc

class PagoRepository {

    suspend fun obtenerMisFacturas(soloPendientes: Boolean = false): List<Factura> = supabaseCall {
        val pendientes = EstadoFactura.entries.filter { it.pagable }.map { it.name }
        Database.postgrest.from(Tables.FACTURAS)
            .select(Columns.raw("*, facturas_detalles(*)")) {
                if (soloPendientes) filter { isIn("estado", pendientes) }
                order("fecha_emision", Order.DESCENDING)
            }
            .decodeList<FacturaDto>()
            .map { it.toDomain() }
    }

    suspend fun obtenerMisPagos(): List<Pago> = supabaseCall {
        Database.postgrest.from(Tables.PAGOS)
            .select(Columns.raw("*, metodos_pago(nombre)")) {
                order("fecha_pago", Order.DESCENDING)
            }
            .decodeList<PagoDto>()
            .map { it.toDomain() }
    }

    suspend fun registrarPagoSimulado(
        idFactura: Long,
        monto: Double,
        marca: String,
        ultimos4: String,
        titular: String?
    ): PagoRegistrado = supabaseCall {
        val params = PagoSimuladoParams(idFactura, monto, marca, ultimos4, titular)
        Database.postgrest.rpc(Rpc.REGISTRAR_PAGO_SIMULADO, params)
            .decodeAs<PagoRegistradoDto>()
            .toDomain()
    }
}
KT

crear "$D/data/repositories/FeedbackRepository.kt" <<'KT'
package __PKG__.data.repositories

import __PKG__.data.remote.dto.ResenaDto
import __PKG__.data.remote.dto.ResenaInsertDto
import __PKG__.data.remote.dto.SolicitudSoporteDto
import __PKG__.data.remote.dto.SolicitudSoporteInsertDto
import __PKG__.data.remote.supabase.Database
import __PKG__.data.remote.supabase.Tables
import __PKG__.data.remote.supabase.supabaseCall
import __PKG__.domain.mappers.toDomain
import __PKG__.domain.models.PrioridadSoporte
import __PKG__.domain.models.Resena
import __PKG__.domain.models.SolicitudSoporte
import io.github.jan.supabase.postgrest.query.Order

class FeedbackRepository {

    suspend fun enviarResena(
        idCliente: Long,
        puntuacion: Int,
        comentario: String?,
        idServicio: Long?,
        idEncargo: Long?
    ): Resena = supabaseCall {
        val dto = ResenaInsertDto(idCliente, idServicio, idEncargo, puntuacion, comentario)
        Database.postgrest.from(Tables.RESENAS)
            .insert(dto) { select() }
            .decodeSingle<ResenaDto>()
            .toDomain()
    }

    suspend fun obtenerMisResenas(): List<Resena> = supabaseCall {
        Database.postgrest.from(Tables.RESENAS)
            .select { order("fecha_creacion", Order.DESCENDING) }
            .decodeList<ResenaDto>()
            .map { it.toDomain() }
    }

    suspend fun enviarSolicitudSoporte(
        idCliente: Long,
        asunto: String,
        descripcion: String,
        prioridad: PrioridadSoporte
    ): SolicitudSoporte = supabaseCall {
        val dto = SolicitudSoporteInsertDto(idCliente, asunto, descripcion, prioridad.name)
        Database.postgrest.from(Tables.SOLICITUDES_SOPORTE)
            .insert(dto) { select() }
            .decodeSingle<SolicitudSoporteDto>()
            .toDomain()
    }

    suspend fun obtenerMisSolicitudes(): List<SolicitudSoporte> = supabaseCall {
        Database.postgrest.from(Tables.SOLICITUDES_SOPORTE)
            .select { order("creado_en", Order.DESCENDING) }
            .decodeList<SolicitudSoporteDto>()
            .map { it.toDomain() }
    }
}
KT

crear "$D/data/repositories/CrmRepository.kt" <<'KT'
package __PKG__.data.repositories

import __PKG__.data.remote.dto.InteraccionParams
import __PKG__.data.remote.supabase.Database
import __PKG__.data.remote.supabase.Rpc
import __PKG__.data.remote.supabase.supabaseCall
import __PKG__.domain.models.TipoInteraccion
import io.github.jan.supabase.postgrest.rpc

class CrmRepository {

    suspend fun registrarInteraccion(
        tipo: TipoInteraccion,
        asunto: String,
        descripcion: String?
    ): Long = supabaseCall {
        val params = InteraccionParams(tipo.name, asunto.take(150), descripcion)
        Database.postgrest.rpc(Rpc.REGISTRAR_INTERACCION, params).decodeAs<Long>()
    }
}
KT

crear "$D/data/repositories/PreferencesRepository.kt" <<'KT'
package __PKG__.data.repositories

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import __PKG__.data.local.datastore.PreferenceKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.forgeDataStore: DataStore<Preferences> by preferencesDataStore(name = "forge_preferences")

class PreferencesRepository(private val context: Context) {

    val onboardingVisto: Flow<Boolean> = context.forgeDataStore.data
        .map { it[PreferenceKeys.ONBOARDING_VISTO] ?: false }

    val ultimoCorreo: Flow<String?> = context.forgeDataStore.data
        .map { it[PreferenceKeys.ULTIMO_CORREO] }

    val correoPendienteVerificacion: Flow<String?> = context.forgeDataStore.data
        .map { it[PreferenceKeys.CORREO_PENDIENTE_VERIFICACION] }

    suspend fun marcarOnboardingVisto() {
        context.forgeDataStore.edit { it[PreferenceKeys.ONBOARDING_VISTO] = true }
    }

    suspend fun guardarUltimoCorreo(correo: String) {
        context.forgeDataStore.edit { it[PreferenceKeys.ULTIMO_CORREO] = correo }
    }

    suspend fun guardarCorreoPendiente(correo: String?) {
        context.forgeDataStore.edit { prefs ->
            if (correo == null) {
                prefs.remove(PreferenceKeys.CORREO_PENDIENTE_VERIFICACION)
            } else {
                prefs[PreferenceKeys.CORREO_PENDIENTE_VERIFICACION] = correo
            }
        }
    }
}
KT

# ============================================================
# DOMAIN / MODELS
# ============================================================
info "Generando capa domain/ ..."

crear "$D/domain/models/AppExceptions.kt" <<'KT'
package __PKG__.domain.models

open class AppException(message: String, cause: Throwable? = null) : Exception(message, cause)

class NotConfiguredException :
    AppException("Supabase no está configurado. Define SUPABASE_URL y SUPABASE_ANON_KEY en local.properties.")

class AuthRequiredException :
    AppException("Debes iniciar sesión o registrarte para continuar.")

class EmailNotVerifiedException(val correo: String) :
    AppException("Debes verificar tu correo antes de continuar.")

class GoogleSignInCancelledException :
    AppException("Inicio de sesión con Google cancelado.")

class ValidationException(message: String) : AppException(message)

class PaymentDeclinedException(message: String) : AppException(message)
KT

crear "$D/domain/models/AuthUser.kt" <<'KT'
package __PKG__.domain.models

data class AuthUser(
    val id: String,
    val email: String,
    val nombreCompleto: String?,
    val nombres: String?,
    val apellidos: String?,
    val avatarUrl: String?,
    val emailVerificado: Boolean,
    val proveedor: String
) {
    val nombreVisible: String
        get() = nombreCompleto?.takeIf { it.isNotBlank() } ?: email.substringBefore("@")

    val primerNombre: String
        get() = nombreVisible.substringBefore(" ")

    val iniciales: String
        get() = nombreVisible.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifBlank { "?" }
}
KT

crear "$D/domain/models/AuthState.kt" <<'KT'
package __PKG__.domain.models

sealed interface AuthState {
    data object Loading : AuthState
    data object NotConfigured : AuthState
    data object Guest : AuthState
    data class Authenticated(val user: AuthUser) : AuthState
}

sealed interface SignUpResult {
    data class VerificationRequired(val correo: String) : SignUpResult
    data class SignedIn(val user: AuthUser) : SignUpResult
}
KT

crear "$D/domain/models/Estados.kt" <<'KT'
package __PKG__.domain.models

enum class TipoCliente(val etiqueta: String) {
    PERSONA("Persona"),
    EMPRESA("Empresa");

    companion object {
        fun desde(valor: String?): TipoCliente = entries.firstOrNull { it.name == valor } ?: PERSONA
    }
}

enum class EstadoCotizacion(val etiqueta: String) {
    BORRADOR("En revisión"),
    ENVIADA("Enviada"),
    ACEPTADA("Aceptada"),
    RECHAZADA("Rechazada"),
    VENCIDA("Vencida"),
    CANCELADA("Cancelada");

    companion object {
        fun desde(valor: String?): EstadoCotizacion = entries.firstOrNull { it.name == valor } ?: BORRADOR
    }
}

enum class EstadoEncargo(val etiqueta: String, val paso: Int) {
    PENDIENTE("Pendiente", 0),
    EN_REVISION("En revisión", 1),
    APROBADO("Aprobado", 2),
    EN_PRODUCCION("En producción", 3),
    EN_ESPERA("En espera", 3),
    FINALIZADO("Finalizado", 4),
    ENTREGADO("Entregado", 5),
    CANCELADO("Cancelado", -1);

    val finalizado: Boolean
        get() = this == ENTREGADO || this == CANCELADO

    companion object {
        const val TOTAL_PASOS = 5

        fun desde(valor: String?): EstadoEncargo = entries.firstOrNull { it.name == valor } ?: PENDIENTE
    }
}

enum class EstadoFactura(val etiqueta: String, val pagable: Boolean) {
    PENDIENTE("Pendiente", true),
    PARCIALMENTE_PAGADA("Pago parcial", true),
    PAGADA("Pagada", false),
    ANULADA("Anulada", false),
    VENCIDA("Vencida", true);

    companion object {
        fun desde(valor: String?): EstadoFactura = entries.firstOrNull { it.name == valor } ?: PENDIENTE
    }
}

enum class EstadoPago(val etiqueta: String) {
    PENDIENTE("Pendiente"),
    CONFIRMADO("Confirmado"),
    RECHAZADO("Rechazado"),
    REEMBOLSADO("Reembolsado"),
    CANCELADO("Cancelado");

    companion object {
        fun desde(valor: String?): EstadoPago = entries.firstOrNull { it.name == valor } ?: PENDIENTE
    }
}

enum class PrioridadSoporte(val etiqueta: String) {
    BAJA("Baja"),
    MEDIA("Media"),
    ALTA("Alta"),
    URGENTE("Urgente");

    companion object {
        fun desde(valor: String?): PrioridadSoporte = entries.firstOrNull { it.name == valor } ?: MEDIA
    }
}

enum class EstadoSoporte(val etiqueta: String) {
    ABIERTA("Abierta"),
    EN_PROCESO("En proceso"),
    RESUELTA("Resuelta"),
    CERRADA("Cerrada");

    companion object {
        fun desde(valor: String?): EstadoSoporte = entries.firstOrNull { it.name == valor } ?: ABIERTA
    }
}

enum class TipoInteraccion {
    LLAMADA,
    CORREO,
    ASESORIA,
    REUNION,
    SEGUIMIENTO,
    RECLAMO
}

enum class TipoDescuento {
    PORCENTAJE,
    MONTO_FIJO;

    companion object {
        fun desde(valor: String?): TipoDescuento = entries.firstOrNull { it.name == valor } ?: PORCENTAJE
    }
}

enum class MarcaTarjeta(val etiqueta: String) {
    VISA("Visa"),
    MASTERCARD("Mastercard"),
    AMEX("American Express"),
    DESCONOCIDA("Tarjeta")
}
KT

crear "$D/domain/models/Cliente.kt" <<'KT'
package __PKG__.domain.models

data class Cliente(
    val id: Long,
    val tipo: TipoCliente,
    val nombres: String?,
    val apellidos: String?,
    val nombreEmpresa: String?,
    val correo: String,
    val telefono: String?,
    val identificacionFiscal: String?,
    val direccion: String?,
    val fotoUrl: String?
) {
    val nombreVisible: String
        get() = when {
            tipo == TipoCliente.EMPRESA && !nombreEmpresa.isNullOrBlank() -> nombreEmpresa
            else -> listOfNotNull(nombres, apellidos).joinToString(" ").ifBlank { correo }
        }

    val perfilCompleto: Boolean
        get() = !telefono.isNullOrBlank() && !direccion.isNullOrBlank() &&
            (tipo == TipoCliente.PERSONA && !nombres.isNullOrBlank() ||
                tipo == TipoCliente.EMPRESA && !nombreEmpresa.isNullOrBlank())
}

data class ClienteUpdate(
    val tipo: TipoCliente,
    val nombres: String?,
    val apellidos: String?,
    val nombreEmpresa: String?,
    val telefono: String?,
    val identificacionFiscal: String?,
    val direccion: String?
)
KT

crear "$D/domain/models/Catalogo.kt" <<'KT'
package __PKG__.domain.models

import java.time.OffsetDateTime

data class Producto(
    val id: Long,
    val sku: String,
    val nombre: String,
    val descripcion: String?,
    val precio: Double,
    val stock: Int,
    val imagenUrl: String?
) {
    val disponible: Boolean
        get() = stock > 0
}

data class CategoriaServicio(
    val id: Long,
    val nombre: String,
    val descripcion: String?
)

data class Servicio(
    val id: Long,
    val idCategoria: Long,
    val categoriaNombre: String?,
    val nombre: String,
    val descripcion: String,
    val precioBase: Double,
    val requiereArchivo: Boolean
)

data class Promocion(
    val id: Long,
    val nombre: String,
    val descripcion: String?,
    val tipoDescuento: TipoDescuento,
    val valorDescuento: Double,
    val fechaFin: OffsetDateTime?
)

data class NegocioInfo(
    val nombre: String,
    val correo: String?,
    val telefono: String?,
    val direccion: String?,
    val sitioWeb: String?,
    val logoUrl: String?
)
KT

crear "$D/domain/models/Cotizacion.kt" <<'KT'
package __PKG__.domain.models

import java.time.OffsetDateTime

data class Cotizacion(
    val id: Long,
    val codigo: String,
    val fechaEmision: OffsetDateTime?,
    val fechaVencimiento: OffsetDateTime?,
    val subtotal: Double,
    val descuento: Double,
    val impuesto: Double,
    val total: Double,
    val estado: EstadoCotizacion,
    val observaciones: String?,
    val detalles: List<CotizacionDetalle>
)

data class CotizacionDetalle(
    val id: Long,
    val idServicio: Long,
    val descripcion: String,
    val cantidad: Double,
    val precioUnitario: Double,
    val subtotal: Double
)

data class CotizacionCreada(
    val id: Long,
    val codigo: String,
    val total: Double
)

data class ItemCotizacion(
    val servicio: Servicio,
    val cantidad: Double = 1.0,
    val detalle: String? = null
) {
    val subtotalEstimado: Double
        get() = servicio.precioBase * cantidad
}
KT

crear "$D/domain/models/Encargo.kt" <<'KT'
package __PKG__.domain.models

import java.time.OffsetDateTime

data class Encargo(
    val id: Long,
    val codigo: String,
    val fechaSolicitud: OffsetDateTime?,
    val fechaEntregaEstimada: OffsetDateTime?,
    val fechaEntregaReal: OffsetDateTime?,
    val descripcion: String,
    val subtotal: Double,
    val descuento: Double,
    val impuesto: Double,
    val total: Double,
    val estado: EstadoEncargo,
    val observaciones: String?,
    val detalles: List<EncargoDetalle>,
    val historial: List<HistorialEncargo>
)

data class EncargoDetalle(
    val id: Long,
    val idServicio: Long?,
    val idProducto: Long?,
    val descripcion: String,
    val cantidad: Double,
    val precioUnitario: Double,
    val subtotal: Double
)

data class HistorialEncargo(
    val id: Long,
    val estadoAnterior: EstadoEncargo?,
    val estadoNuevo: EstadoEncargo,
    val comentario: String?,
    val fecha: OffsetDateTime?
)

data class ItemPedido(
    val producto: Producto,
    val cantidad: Int = 1
) {
    val subtotal: Double
        get() = producto.precio * cantidad
}

data class PedidoCreado(
    val idEncargo: Long,
    val codigo: String,
    val idFactura: Long,
    val numeroFactura: String,
    val total: Double
)
KT

crear "$D/domain/models/Facturacion.kt" <<'KT'
package __PKG__.domain.models

import java.time.OffsetDateTime

data class Factura(
    val id: Long,
    val idEncargo: Long?,
    val numero: String,
    val fechaEmision: OffsetDateTime?,
    val fechaVencimiento: OffsetDateTime?,
    val nombreFacturacion: String,
    val subtotal: Double,
    val descuento: Double,
    val impuesto: Double,
    val total: Double,
    val saldoPendiente: Double,
    val estado: EstadoFactura,
    val detalles: List<FacturaDetalle>
) {
    val pagable: Boolean
        get() = estado.pagable && saldoPendiente > 0.0
}

data class FacturaDetalle(
    val id: Long,
    val descripcion: String,
    val cantidad: Double,
    val precioUnitario: Double,
    val subtotal: Double
)

data class Pago(
    val id: Long,
    val idFactura: Long,
    val referencia: String?,
    val monto: Double,
    val fecha: OffsetDateTime?,
    val estado: EstadoPago,
    val metodo: String?,
    val observaciones: String?
)

data class PagoRegistrado(
    val idPago: Long,
    val referencia: String,
    val estado: EstadoPago,
    val saldoPendiente: Double,
    val estadoFactura: EstadoFactura
)

data class TarjetaPago(
    val numero: String,
    val titular: String,
    val mesExpiracion: Int,
    val anioExpiracion: Int,
    val cvv: String
)
KT

crear "$D/domain/models/Feedback.kt" <<'KT'
package __PKG__.domain.models

import java.time.OffsetDateTime

data class Resena(
    val id: Long,
    val idServicio: Long?,
    val idEncargo: Long?,
    val puntuacion: Int,
    val comentario: String?,
    val fecha: OffsetDateTime?
)

data class SolicitudSoporte(
    val id: Long,
    val asunto: String,
    val descripcion: String,
    val prioridad: PrioridadSoporte,
    val estado: EstadoSoporte,
    val fecha: OffsetDateTime?
)
KT

crear "$D/domain/models/WhatsAppRequest.kt" <<'KT'
package __PKG__.domain.models

import android.content.Intent

data class WhatsAppRequest(
    val intent: Intent,
    val codigoReferencia: String?
)
KT

# ============================================================
# DOMAIN / MAPPERS
# ============================================================
crear "$D/domain/mappers/DateMapper.kt" <<'KT'
package __PKG__.domain.mappers

import java.time.OffsetDateTime

internal fun String?.toFecha(): OffsetDateTime? =
    this?.let { runCatching { OffsetDateTime.parse(it) }.getOrNull() }
KT

crear "$D/domain/mappers/AuthMapper.kt" <<'KT'
package __PKG__.domain.mappers

import __PKG__.domain.models.AuthUser
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

private fun JsonObject?.texto(clave: String): String? =
    (this?.get(clave) as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotBlank() }

fun UserInfo.toAuthUser(): AuthUser {
    val metadata = userMetadata
    val completo = metadata.texto("full_name") ?: metadata.texto("name")
    val nombres = metadata.texto("nombres") ?: completo?.substringBefore(" ")
    val apellidos = metadata.texto("apellidos")
        ?: completo?.substringAfter(" ", "")?.takeIf { it.isNotBlank() }
    val proveedor = appMetadata.texto("provider") ?: "email"
    return AuthUser(
        id = id,
        email = email.orEmpty(),
        nombreCompleto = completo ?: listOfNotNull(nombres, apellidos).joinToString(" ").ifBlank { null },
        nombres = nombres,
        apellidos = apellidos,
        avatarUrl = metadata.texto("avatar_url") ?: metadata.texto("picture"),
        emailVerificado = emailConfirmedAt != null || proveedor != "email",
        proveedor = proveedor
    )
}
KT

crear "$D/domain/mappers/ClienteMapper.kt" <<'KT'
package __PKG__.domain.mappers

import __PKG__.data.remote.dto.ClienteDto
import __PKG__.domain.models.Cliente
import __PKG__.domain.models.TipoCliente

fun ClienteDto.toDomain(): Cliente = Cliente(
    id = id,
    tipo = TipoCliente.desde(tipoCliente),
    nombres = nombres,
    apellidos = apellidos,
    nombreEmpresa = nombreEmpresa,
    correo = correo,
    telefono = telefono,
    identificacionFiscal = identificacionFiscal,
    direccion = direccion,
    fotoUrl = fotoUrl
)
KT

crear "$D/domain/mappers/CatalogoMapper.kt" <<'KT'
package __PKG__.domain.mappers

import __PKG__.data.remote.dto.CategoriaServicioDto
import __PKG__.data.remote.dto.ConfiguracionNegocioDto
import __PKG__.data.remote.dto.ProductoDto
import __PKG__.data.remote.dto.PromocionDto
import __PKG__.data.remote.dto.ServicioDto
import __PKG__.domain.models.CategoriaServicio
import __PKG__.domain.models.NegocioInfo
import __PKG__.domain.models.Producto
import __PKG__.domain.models.Promocion
import __PKG__.domain.models.Servicio
import __PKG__.domain.models.TipoDescuento

fun ProductoDto.toDomain(): Producto = Producto(
    id = id,
    sku = sku,
    nombre = nombre,
    descripcion = descripcion,
    precio = precio,
    stock = stock,
    imagenUrl = imagen?.takeIf { it.isNotBlank() }
)

fun CategoriaServicioDto.toDomain(): CategoriaServicio = CategoriaServicio(
    id = id,
    nombre = nombre,
    descripcion = descripcion
)

fun ServicioDto.toDomain(): Servicio = Servicio(
    id = id,
    idCategoria = idCategoria,
    categoriaNombre = categoria?.nombre,
    nombre = nombre,
    descripcion = descripcion,
    precioBase = precioBase,
    requiereArchivo = requiereArchivo
)

fun PromocionDto.toDomain(): Promocion = Promocion(
    id = id,
    nombre = nombre,
    descripcion = descripcion,
    tipoDescuento = TipoDescuento.desde(tipoDescuento),
    valorDescuento = valorDescuento,
    fechaFin = fechaFin.toFecha()
)

fun ConfiguracionNegocioDto.toDomain(): NegocioInfo = NegocioInfo(
    nombre = nombreNegocio,
    correo = correo,
    telefono = telefono,
    direccion = direccion,
    sitioWeb = sitioWeb,
    logoUrl = logo
)
KT

crear "$D/domain/mappers/CotizacionMapper.kt" <<'KT'
package __PKG__.domain.mappers

import __PKG__.data.remote.dto.CotizacionCreadaDto
import __PKG__.data.remote.dto.CotizacionDetalleDto
import __PKG__.data.remote.dto.CotizacionDto
import __PKG__.domain.models.Cotizacion
import __PKG__.domain.models.CotizacionCreada
import __PKG__.domain.models.CotizacionDetalle
import __PKG__.domain.models.EstadoCotizacion

fun CotizacionDto.toDomain(): Cotizacion = Cotizacion(
    id = id,
    codigo = codigo,
    fechaEmision = fechaEmision.toFecha(),
    fechaVencimiento = fechaVencimiento.toFecha(),
    subtotal = subtotal,
    descuento = descuento,
    impuesto = impuesto,
    total = total,
    estado = EstadoCotizacion.desde(estado),
    observaciones = observaciones,
    detalles = detalles.map { it.toDomain() }
)

fun CotizacionDetalleDto.toDomain(): CotizacionDetalle = CotizacionDetalle(
    id = id,
    idServicio = idServicio,
    descripcion = descripcion,
    cantidad = cantidad,
    precioUnitario = precioUnitario,
    subtotal = subtotal
)

fun CotizacionCreadaDto.toDomain(): CotizacionCreada = CotizacionCreada(
    id = id,
    codigo = codigo,
    total = total
)
KT

crear "$D/domain/mappers/EncargoMapper.kt" <<'KT'
package __PKG__.domain.mappers

import __PKG__.data.remote.dto.EncargoDetalleDto
import __PKG__.data.remote.dto.EncargoDto
import __PKG__.data.remote.dto.HistorialEncargoDto
import __PKG__.data.remote.dto.PedidoCreadoDto
import __PKG__.domain.models.Encargo
import __PKG__.domain.models.EncargoDetalle
import __PKG__.domain.models.EstadoEncargo
import __PKG__.domain.models.HistorialEncargo
import __PKG__.domain.models.PedidoCreado

fun EncargoDto.toDomain(): Encargo = Encargo(
    id = id,
    codigo = codigo,
    fechaSolicitud = fechaSolicitud.toFecha(),
    fechaEntregaEstimada = fechaEntregaEstimada.toFecha(),
    fechaEntregaReal = fechaEntregaReal.toFecha(),
    descripcion = descripcion,
    subtotal = subtotal,
    descuento = descuento,
    impuesto = impuesto,
    total = total,
    estado = EstadoEncargo.desde(estado),
    observaciones = observaciones,
    detalles = detalles.map { it.toDomain() },
    historial = historial.map { it.toDomain() }.sortedBy { it.fecha }
)

fun EncargoDetalleDto.toDomain(): EncargoDetalle = EncargoDetalle(
    id = id,
    idServicio = idServicio,
    idProducto = idProducto,
    descripcion = descripcion,
    cantidad = cantidad,
    precioUnitario = precioUnitario,
    subtotal = subtotal
)

fun HistorialEncargoDto.toDomain(): HistorialEncargo = HistorialEncargo(
    id = id,
    estadoAnterior = estadoAnterior?.let { EstadoEncargo.desde(it) },
    estadoNuevo = EstadoEncargo.desde(estadoNuevo),
    comentario = comentario,
    fecha = fechaCambio.toFecha()
)

fun PedidoCreadoDto.toDomain(): PedidoCreado = PedidoCreado(
    idEncargo = idEncargo,
    codigo = codigo,
    idFactura = idFactura,
    numeroFactura = numeroFactura,
    total = total
)
KT

crear "$D/domain/mappers/FacturacionMapper.kt" <<'KT'
package __PKG__.domain.mappers

import __PKG__.data.remote.dto.FacturaDetalleDto
import __PKG__.data.remote.dto.FacturaDto
import __PKG__.data.remote.dto.PagoDto
import __PKG__.data.remote.dto.PagoRegistradoDto
import __PKG__.domain.models.EstadoFactura
import __PKG__.domain.models.EstadoPago
import __PKG__.domain.models.Factura
import __PKG__.domain.models.FacturaDetalle
import __PKG__.domain.models.Pago
import __PKG__.domain.models.PagoRegistrado

fun FacturaDto.toDomain(): Factura = Factura(
    id = id,
    idEncargo = idEncargo,
    numero = numeroFactura,
    fechaEmision = fechaEmision.toFecha(),
    fechaVencimiento = fechaVencimiento.toFecha(),
    nombreFacturacion = nombreFacturacion,
    subtotal = subtotal,
    descuento = descuento,
    impuesto = impuesto,
    total = total,
    saldoPendiente = saldoPendiente,
    estado = EstadoFactura.desde(estado),
    detalles = detalles.map { it.toDomain() }
)

fun FacturaDetalleDto.toDomain(): FacturaDetalle = FacturaDetalle(
    id = id,
    descripcion = descripcion,
    cantidad = cantidad,
    precioUnitario = precioUnitario,
    subtotal = subtotal
)

fun PagoDto.toDomain(): Pago = Pago(
    id = id,
    idFactura = idFactura,
    referencia = referencia,
    monto = monto,
    fecha = fechaPago.toFecha(),
    estado = EstadoPago.desde(estado),
    metodo = metodo?.nombre,
    observaciones = observaciones
)

fun PagoRegistradoDto.toDomain(): PagoRegistrado = PagoRegistrado(
    idPago = idPago,
    referencia = referencia,
    estado = EstadoPago.desde(estado),
    saldoPendiente = saldoPendiente,
    estadoFactura = EstadoFactura.desde(estadoFactura)
)
KT

crear "$D/domain/mappers/FeedbackMapper.kt" <<'KT'
package __PKG__.domain.mappers

import __PKG__.data.remote.dto.ResenaDto
import __PKG__.data.remote.dto.SolicitudSoporteDto
import __PKG__.domain.models.EstadoSoporte
import __PKG__.domain.models.PrioridadSoporte
import __PKG__.domain.models.Resena
import __PKG__.domain.models.SolicitudSoporte

fun ResenaDto.toDomain(): Resena = Resena(
    id = id,
    idServicio = idServicio,
    idEncargo = idEncargo,
    puntuacion = puntuacion,
    comentario = comentario,
    fecha = fechaCreacion.toFecha()
)

fun SolicitudSoporteDto.toDomain(): SolicitudSoporte = SolicitudSoporte(
    id = id,
    asunto = asunto,
    descripcion = descripcion,
    prioridad = PrioridadSoporte.desde(prioridad),
    estado = EstadoSoporte.desde(estado),
    fecha = creadoEn.toFecha()
)
KT

# ============================================================
# DOMAIN / UTILS & VALIDATION
# ============================================================
crear "$D/domain/utils/Results.kt" <<'KT'
package __PKG__.domain.utils

import kotlin.coroutines.cancellation.CancellationException

suspend fun <T> resultOf(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Throwable) {
    Result.failure(e)
}

suspend fun <T> valorONull(block: suspend () -> T?): T? = try {
    block()
} catch (e: CancellationException) {
    throw e
} catch (e: Throwable) {
    null
}
KT

crear "$D/domain/utils/Formatters.kt" <<'KT'
package __PKG__.domain.utils

import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val localeEs: Locale = Locale.forLanguageTag("es-SV")
private val formatoFecha: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", localeEs)
private val formatoFechaHora: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", localeEs)

fun Double.comoMoneda(): String = "$" + String.format(Locale.US, "%,.2f", this)

fun Double.comoCantidad(): String =
    if (this % 1.0 == 0.0) this.toLong().toString() else String.format(Locale.US, "%.2f", this)

fun OffsetDateTime?.comoFecha(): String =
    this?.atZoneSameInstant(ZoneId.systemDefault())?.format(formatoFecha) ?: "—"

fun OffsetDateTime?.comoFechaHora(): String =
    this?.atZoneSameInstant(ZoneId.systemDefault())?.format(formatoFechaHora) ?: "—"
KT

crear "$D/domain/utils/WhatsAppMessages.kt" <<'KT'
package __PKG__.domain.utils

import __PKG__.domain.models.AuthUser
import __PKG__.domain.models.CotizacionCreada
import __PKG__.domain.models.ItemCotizacion

object WhatsAppMessages {

    fun cotizacion(
        usuario: AuthUser,
        cotizacion: CotizacionCreada,
        items: List<ItemCotizacion>,
        notas: String?
    ): String = buildString {
        appendLine("¡Hola, 3DForge Studio! 👋")
        appendLine("Soy ${usuario.nombreVisible} y solicité la cotización ${cotizacion.codigo} desde la app.")
        appendLine()
        items.forEach { item ->
            append("• ${item.servicio.nombre} x ${item.cantidad.comoCantidad()}")
            item.detalle?.takeIf { it.isNotBlank() }?.let { append(" — $it") }
            appendLine()
        }
        appendLine()
        appendLine("Total estimado: ${cotizacion.total.comoMoneda()} (IVA incluido)")
        notas?.let { appendLine("Notas: $it") }
        append("Correo de contacto: ${usuario.email}")
    }

    fun cotizacionPersonalizada(usuario: AuthUser, descripcion: String): String = buildString {
        appendLine("¡Hola, 3DForge Studio! 👋")
        appendLine("Soy ${usuario.nombreVisible} y quiero cotizar un proyecto de impresión 3D:")
        appendLine()
        appendLine(descripcion)
        appendLine()
        append("Correo de contacto: ${usuario.email}")
    }

    fun asesoria(usuario: AuthUser, tema: String, mensaje: String?, horario: String?): String = buildString {
        appendLine("¡Hola, 3DForge Studio! 👋")
        appendLine("Soy ${usuario.nombreVisible} y me gustaría agendar una asesoría.")
        appendLine()
        appendLine("Tema: $tema")
        mensaje?.let { appendLine("Detalle: $it") }
        horario?.let { appendLine("Horario preferido: $it") }
        append("Correo de contacto: ${usuario.email}")
    }
}
KT

crear "$D/domain/validation/InputValidator.kt" <<'KT'
package __PKG__.domain.validation

import __PKG__.domain.models.ValidationException

object InputValidator {

    private val EMAIL = Regex("""^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$""")
    private val TELEFONO = Regex("""^\+?[0-9 ()-]{8,20}$""")
    private val OTP = Regex("""^[0-9]{6,10}$""")

    fun requireEmail(valor: String) {
        if (!EMAIL.matches(valor.trim())) throw ValidationException("Ingresa un correo electrónico válido.")
    }

    fun requirePassword(valor: String) {
        if (valor.length < 8 || valor.none { it.isDigit() } || valor.none { it.isLetter() }) {
            throw ValidationException("La contraseña debe tener al menos 8 caracteres e incluir letras y números.")
        }
    }

    fun requireNombre(valor: String?, campo: String) {
        if (valor.isNullOrBlank() || valor.trim().length < 2) throw ValidationException("Ingresa $campo válido.")
    }

    fun requireTelefonoOpcional(valor: String?) {
        if (!valor.isNullOrBlank() && !TELEFONO.matches(valor.trim())) {
            throw ValidationException("Ingresa un número de teléfono válido.")
        }
    }

    fun requireOtp(valor: String) {
        if (!OTP.matches(valor.trim())) throw ValidationException("El código de verificación debe tener 6 dígitos.")
    }

    fun requireLongitud(valor: String?, campo: String, minimo: Int, maximo: Int) {
        val largo = valor?.trim()?.length ?: 0
        if (largo < minimo) throw ValidationException("$campo debe tener al menos $minimo caracteres.")
        if (largo > maximo) throw ValidationException("$campo no puede superar $maximo caracteres.")
    }
}
KT

crear "$D/domain/validation/CardValidator.kt" <<'KT'
package __PKG__.domain.validation

import __PKG__.domain.models.MarcaTarjeta
import __PKG__.domain.models.TarjetaPago
import __PKG__.domain.models.ValidationException
import java.time.YearMonth

object CardValidator {

    fun limpiarNumero(numero: String): String = numero.filter { it.isDigit() }

    fun detectarMarca(numero: String): MarcaTarjeta {
        val n = limpiarNumero(numero)
        val dos = n.take(2).toIntOrNull() ?: -1
        val cuatro = n.take(4).toIntOrNull() ?: -1
        return when {
            n.startsWith("4") -> MarcaTarjeta.VISA
            dos in 51..55 || cuatro in 2221..2720 -> MarcaTarjeta.MASTERCARD
            dos == 34 || dos == 37 -> MarcaTarjeta.AMEX
            else -> MarcaTarjeta.DESCONOCIDA
        }
    }

    fun luhn(numero: String): Boolean {
        val n = limpiarNumero(numero)
        if (n.isEmpty()) return false
        var suma = 0
        var duplicar = false
        for (i in n.indices.reversed()) {
            var digito = n[i] - '0'
            if (duplicar) {
                digito *= 2
                if (digito > 9) digito -= 9
            }
            suma += digito
            duplicar = !duplicar
        }
        return suma % 10 == 0
    }

    fun enmascarar(numero: String): String = "•••• " + limpiarNumero(numero).takeLast(4)

    fun validar(tarjeta: TarjetaPago): MarcaTarjeta {
        val numero = limpiarNumero(tarjeta.numero)
        if (numero.length !in 13..19 || !luhn(numero)) {
            throw ValidationException("El número de tarjeta no es válido.")
        }
        if (tarjeta.titular.trim().length < 3) {
            throw ValidationException("Ingresa el nombre del titular como aparece en la tarjeta.")
        }
        if (tarjeta.mesExpiracion !in 1..12) {
            throw ValidationException("El mes de expiración no es válido.")
        }
        val anio = if (tarjeta.anioExpiracion < 100) 2000 + tarjeta.anioExpiracion else tarjeta.anioExpiracion
        if (YearMonth.of(anio, tarjeta.mesExpiracion).isBefore(YearMonth.now())) {
            throw ValidationException("La tarjeta está vencida.")
        }
        val marca = detectarMarca(numero)
        val largoCvv = if (marca == MarcaTarjeta.AMEX) 4 else 3
        if (tarjeta.cvv.length != largoCvv || tarjeta.cvv.any { !it.isDigit() }) {
            throw ValidationException("El código de seguridad (CVV) no es válido.")
        }
        return marca
    }
}
KT

# ============================================================
# DOMAIN / USECASES
# ============================================================
UC="$D/domain/usecases"

crear "$UC/auth/ObserveAuthStateUseCase.kt" <<'KT'
package __PKG__.domain.usecases.auth

import __PKG__.data.repositories.AuthRepository
import __PKG__.domain.models.AuthState
import kotlinx.coroutines.flow.Flow

class ObserveAuthStateUseCase(private val authRepository: AuthRepository) {
    operator fun invoke(): Flow<AuthState> = authRepository.authState
}
KT

crear "$UC/auth/GetCurrentUserUseCase.kt" <<'KT'
package __PKG__.domain.usecases.auth

import __PKG__.data.repositories.AuthRepository
import __PKG__.domain.models.AuthUser

class GetCurrentUserUseCase(private val authRepository: AuthRepository) {
    operator fun invoke(): AuthUser? = authRepository.usuarioActual()
}
KT

crear "$UC/auth/RegisterWithEmailUseCase.kt" <<'KT'
package __PKG__.domain.usecases.auth

import __PKG__.data.repositories.AuthRepository
import __PKG__.data.repositories.ClienteRepository
import __PKG__.data.repositories.PreferencesRepository
import __PKG__.domain.models.SignUpResult
import __PKG__.domain.models.ValidationException
import __PKG__.domain.utils.resultOf
import __PKG__.domain.validation.InputValidator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RegisterWithEmailUseCase(
    private val authRepository: AuthRepository,
    private val clienteRepository: ClienteRepository,
    private val preferencesRepository: PreferencesRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(
        nombres: String,
        apellidos: String,
        correo: String,
        clave: String,
        confirmacion: String
    ): Result<SignUpResult> = withContext(dispatcher) {
        resultOf {
            val email = correo.trim().lowercase()
            InputValidator.requireNombre(nombres, "un nombre")
            InputValidator.requireNombre(apellidos, "un apellido")
            InputValidator.requireEmail(email)
            InputValidator.requirePassword(clave)
            if (clave != confirmacion) throw ValidationException("Las contraseñas no coinciden.")

            when (val resultado = authRepository.registrar(email, clave, nombres.trim(), apellidos.trim())) {
                is SignUpResult.VerificationRequired -> {
                    preferencesRepository.guardarCorreoPendiente(email)
                    resultado
                }
                is SignUpResult.SignedIn -> {
                    preferencesRepository.guardarUltimoCorreo(email)
                    clienteRepository.asegurarCliente(nombres, apellidos, null, null)
                    resultado
                }
            }
        }
    }
}
KT

crear "$UC/auth/VerifyEmailCodeUseCase.kt" <<'KT'
package __PKG__.domain.usecases.auth

import __PKG__.data.repositories.AuthRepository
import __PKG__.data.repositories.ClienteRepository
import __PKG__.data.repositories.PreferencesRepository
import __PKG__.domain.models.AuthUser
import __PKG__.domain.utils.resultOf
import __PKG__.domain.validation.InputValidator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VerifyEmailCodeUseCase(
    private val authRepository: AuthRepository,
    private val clienteRepository: ClienteRepository,
    private val preferencesRepository: PreferencesRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(correo: String, codigo: String): Result<AuthUser> = withContext(dispatcher) {
        resultOf {
            val email = correo.trim().lowercase()
            InputValidator.requireEmail(email)
            InputValidator.requireOtp(codigo)
            val usuario = authRepository.verificarCodigoRegistro(email, codigo.trim())
            preferencesRepository.guardarCorreoPendiente(null)
            preferencesRepository.guardarUltimoCorreo(email)
            clienteRepository.asegurarCliente(usuario.nombres, usuario.apellidos, null, usuario.avatarUrl)
            usuario
        }
    }
}
KT

crear "$UC/auth/ResendVerificationCodeUseCase.kt" <<'KT'
package __PKG__.domain.usecases.auth

import __PKG__.data.repositories.AuthRepository
import __PKG__.domain.utils.resultOf
import __PKG__.domain.validation.InputValidator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ResendVerificationCodeUseCase(
    private val authRepository: AuthRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(correo: String): Result<Unit> = withContext(dispatcher) {
        resultOf {
            val email = correo.trim().lowercase()
            InputValidator.requireEmail(email)
            authRepository.reenviarCodigoRegistro(email)
        }
    }
}
KT

crear "$UC/auth/GetPendingVerificationEmailUseCase.kt" <<'KT'
package __PKG__.domain.usecases.auth

import __PKG__.data.repositories.PreferencesRepository
import kotlinx.coroutines.flow.Flow

class GetPendingVerificationEmailUseCase(private val preferencesRepository: PreferencesRepository) {
    operator fun invoke(): Flow<String?> = preferencesRepository.correoPendienteVerificacion
}
KT

crear "$UC/auth/GetLastEmailUseCase.kt" <<'KT'
package __PKG__.domain.usecases.auth

import __PKG__.data.repositories.PreferencesRepository
import kotlinx.coroutines.flow.Flow

class GetLastEmailUseCase(private val preferencesRepository: PreferencesRepository) {
    operator fun invoke(): Flow<String?> = preferencesRepository.ultimoCorreo
}
KT

crear "$UC/auth/LoginWithEmailUseCase.kt" <<'KT'
package __PKG__.domain.usecases.auth

import __PKG__.data.repositories.AuthRepository
import __PKG__.data.repositories.ClienteRepository
import __PKG__.data.repositories.PreferencesRepository
import __PKG__.domain.models.AuthUser
import __PKG__.domain.models.EmailNotVerifiedException
import __PKG__.domain.models.ValidationException
import __PKG__.domain.utils.resultOf
import __PKG__.domain.validation.InputValidator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoginWithEmailUseCase(
    private val authRepository: AuthRepository,
    private val clienteRepository: ClienteRepository,
    private val preferencesRepository: PreferencesRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(correo: String, clave: String): Result<AuthUser> = withContext(dispatcher) {
        resultOf {
            val email = correo.trim().lowercase()
            InputValidator.requireEmail(email)
            if (clave.isBlank()) throw ValidationException("Ingresa tu contraseña.")
            val usuario = try {
                authRepository.iniciarSesion(email, clave)
            } catch (e: EmailNotVerifiedException) {
                preferencesRepository.guardarCorreoPendiente(email)
                throw e
            }
            preferencesRepository.guardarUltimoCorreo(email)
            clienteRepository.asegurarCliente(usuario.nombres, usuario.apellidos, null, usuario.avatarUrl)
            usuario
        }
    }
}
KT

crear "$UC/auth/SignInWithGoogleUseCase.kt" <<'KT'
package __PKG__.domain.usecases.auth

import android.content.Context
import __PKG__.data.repositories.AuthRepository
import __PKG__.data.repositories.ClienteRepository
import __PKG__.data.repositories.PreferencesRepository
import __PKG__.domain.models.AuthUser
import __PKG__.domain.utils.resultOf

class SignInWithGoogleUseCase(
    private val authRepository: AuthRepository,
    private val clienteRepository: ClienteRepository,
    private val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke(activityContext: Context): Result<AuthUser> = resultOf {
        val usuario = authRepository.iniciarSesionConGoogle(activityContext)
        preferencesRepository.guardarUltimoCorreo(usuario.email)
        clienteRepository.asegurarCliente(usuario.nombres, usuario.apellidos, null, usuario.avatarUrl)
        usuario
    }
}
KT

crear "$UC/auth/SendPasswordResetUseCase.kt" <<'KT'
package __PKG__.domain.usecases.auth

import __PKG__.data.repositories.AuthRepository
import __PKG__.domain.utils.resultOf
import __PKG__.domain.validation.InputValidator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SendPasswordResetUseCase(
    private val authRepository: AuthRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(correo: String): Result<Unit> = withContext(dispatcher) {
        resultOf {
            val email = correo.trim().lowercase()
            InputValidator.requireEmail(email)
            authRepository.enviarRecuperacionClave(email)
        }
    }
}
KT

crear "$UC/auth/LogoutUseCase.kt" <<'KT'
package __PKG__.domain.usecases.auth

import __PKG__.data.repositories.AuthRepository
import __PKG__.data.repositories.ClienteRepository
import __PKG__.domain.utils.resultOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LogoutUseCase(
    private val authRepository: AuthRepository,
    private val clienteRepository: ClienteRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(): Result<Unit> = withContext(dispatcher) {
        resultOf {
            authRepository.cerrarSesion()
            clienteRepository.limpiarCache()
        }
    }
}
KT

crear "$UC/perfil/GetClienteProfileUseCase.kt" <<'KT'
package __PKG__.domain.usecases.perfil

import __PKG__.data.repositories.AuthRepository
import __PKG__.data.repositories.ClienteRepository
import __PKG__.domain.models.Cliente
import __PKG__.domain.utils.resultOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetClienteProfileUseCase(
    private val authRepository: AuthRepository,
    private val clienteRepository: ClienteRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(): Result<Cliente> = withContext(dispatcher) {
        resultOf {
            val usuario = authRepository.requireUser()
            clienteRepository.obtenerMiCliente()
                ?: clienteRepository.asegurarCliente(usuario.nombres, usuario.apellidos, null, usuario.avatarUrl)
        }
    }
}
KT

crear "$UC/perfil/ObserveClienteProfileUseCase.kt" <<'KT'
package __PKG__.domain.usecases.perfil

import __PKG__.data.repositories.ClienteRepository
import __PKG__.domain.models.Cliente
import kotlinx.coroutines.flow.StateFlow

class ObserveClienteProfileUseCase(private val clienteRepository: ClienteRepository) {
    operator fun invoke(): StateFlow<Cliente?> = clienteRepository.clienteActual
}
KT

crear "$UC/perfil/UpdateClienteProfileUseCase.kt" <<'KT'
package __PKG__.domain.usecases.perfil

import __PKG__.data.repositories.AuthRepository
import __PKG__.data.repositories.ClienteRepository
import __PKG__.domain.models.Cliente
import __PKG__.domain.models.ClienteUpdate
import __PKG__.domain.models.TipoCliente
import __PKG__.domain.utils.resultOf
import __PKG__.domain.validation.InputValidator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UpdateClienteProfileUseCase(
    private val authRepository: AuthRepository,
    private val clienteRepository: ClienteRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(cambios: ClienteUpdate): Result<Cliente> = withContext(dispatcher) {
        resultOf {
            authRepository.requireUser()
            when (cambios.tipo) {
                TipoCliente.PERSONA -> {
                    InputValidator.requireNombre(cambios.nombres, "tus nombres")
                    InputValidator.requireNombre(cambios.apellidos, "tus apellidos")
                }
                TipoCliente.EMPRESA -> InputValidator.requireNombre(cambios.nombreEmpresa, "el nombre de la empresa")
            }
            InputValidator.requireTelefonoOpcional(cambios.telefono)
            if (!cambios.direccion.isNullOrBlank()) {
                InputValidator.requireLongitud(cambios.direccion, "La dirección", 5, 250)
            }
            val idCliente = clienteRepository.requireClienteId()
            clienteRepository.actualizar(idCliente, cambios)
        }
    }
}
KT

crear "$UC/catalogo/GetProductosUseCase.kt" <<'KT'
package __PKG__.domain.usecases.catalogo

import __PKG__.data.repositories.CatalogRepository
import __PKG__.domain.models.Producto
import __PKG__.domain.utils.resultOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetProductosUseCase(
    private val catalogRepository: CatalogRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(busqueda: String? = null): Result<List<Producto>> = withContext(dispatcher) {
        resultOf { catalogRepository.obtenerProductos(busqueda) }
    }
}
KT

crear "$UC/catalogo/GetProductoDetailUseCase.kt" <<'KT'
package __PKG__.domain.usecases.catalogo

import __PKG__.data.repositories.CatalogRepository
import __PKG__.domain.models.AppException
import __PKG__.domain.models.Producto
import __PKG__.domain.utils.resultOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetProductoDetailUseCase(
    private val catalogRepository: CatalogRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(id: Long): Result<Producto> = withContext(dispatcher) {
        resultOf { catalogRepository.obtenerProducto(id) ?: throw AppException("El producto no está disponible.") }
    }
}
KT

crear "$UC/catalogo/GetServiciosUseCase.kt" <<'KT'
package __PKG__.domain.usecases.catalogo

import __PKG__.data.repositories.CatalogRepository
import __PKG__.domain.models.Servicio
import __PKG__.domain.utils.resultOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetServiciosUseCase(
    private val catalogRepository: CatalogRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(idCategoria: Long? = null): Result<List<Servicio>> = withContext(dispatcher) {
        resultOf { catalogRepository.obtenerServicios(idCategoria) }
    }
}
KT

crear "$UC/catalogo/GetServicioDetailUseCase.kt" <<'KT'
package __PKG__.domain.usecases.catalogo

import __PKG__.data.repositories.CatalogRepository
import __PKG__.domain.models.AppException
import __PKG__.domain.models.Servicio
import __PKG__.domain.utils.resultOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetServicioDetailUseCase(
    private val catalogRepository: CatalogRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(id: Long): Result<Servicio> = withContext(dispatcher) {
        resultOf { catalogRepository.obtenerServicio(id) ?: throw AppException("El servicio no está disponible.") }
    }
}
KT

crear "$UC/catalogo/GetCategoriasServiciosUseCase.kt" <<'KT'
package __PKG__.domain.usecases.catalogo

import __PKG__.data.repositories.CatalogRepository
import __PKG__.domain.models.CategoriaServicio
import __PKG__.domain.utils.resultOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetCategoriasServiciosUseCase(
    private val catalogRepository: CatalogRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(): Result<List<CategoriaServicio>> = withContext(dispatcher) {
        resultOf { catalogRepository.obtenerCategorias() }
    }
}
KT

crear "$UC/catalogo/GetPromocionesActivasUseCase.kt" <<'KT'
package __PKG__.domain.usecases.catalogo

import __PKG__.data.repositories.CatalogRepository
import __PKG__.domain.models.Promocion
import __PKG__.domain.utils.resultOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetPromocionesActivasUseCase(
    private val catalogRepository: CatalogRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(): Result<List<Promocion>> = withContext(dispatcher) {
        resultOf { catalogRepository.obtenerPromocionesActivas() }
    }
}
KT

crear "$UC/catalogo/GetNegocioInfoUseCase.kt" <<'KT'
package __PKG__.domain.usecases.catalogo

import __PKG__.data.config.AppConfig
import __PKG__.data.repositories.NegocioRepository
import __PKG__.domain.models.NegocioInfo
import __PKG__.domain.utils.resultOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetNegocioInfoUseCase(
    private val negocioRepository: NegocioRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(forzar: Boolean = false): Result<NegocioInfo> = withContext(dispatcher) {
        resultOf {
            negocioRepository.obtenerNegocio(forzar) ?: NegocioInfo(
                nombre = AppConfig.NOMBRE_NEGOCIO,
                correo = null,
                telefono = AppConfig.whatsappFallbackNumber.ifBlank { null },
                direccion = null,
                sitioWeb = null,
                logoUrl = null
            )
        }
    }
}
KT

crear "$UC/cotizaciones/RequestQuoteUseCase.kt" <<'KT'
package __PKG__.domain.usecases.cotizaciones

import __PKG__.data.external.WhatsAppLauncher
import __PKG__.data.repositories.AuthRepository
import __PKG__.data.repositories.CotizacionRepository
import __PKG__.data.repositories.CrmRepository
import __PKG__.data.repositories.NegocioRepository
import __PKG__.domain.models.ItemCotizacion
import __PKG__.domain.models.TipoInteraccion
import __PKG__.domain.models.ValidationException
import __PKG__.domain.models.WhatsAppRequest
import __PKG__.domain.utils.WhatsAppMessages
import __PKG__.domain.utils.resultOf
import __PKG__.domain.utils.valorONull
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RequestQuoteUseCase(
    private val authRepository: AuthRepository,
    private val cotizacionRepository: CotizacionRepository,
    private val crmRepository: CrmRepository,
    private val negocioRepository: NegocioRepository,
    private val whatsAppLauncher: WhatsAppLauncher,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(
        items: List<ItemCotizacion>,
        observaciones: String? = null
    ): Result<WhatsAppRequest> = withContext(dispatcher) {
        resultOf {
            val usuario = authRepository.requireUser()
            val notas = observaciones?.trim()?.takeIf { it.isNotEmpty() }
            val telefono = negocioRepository.obtenerTelefonoWhatsApp()

            if (items.isEmpty()) {
                if (notas == null || notas.length < 10) {
                    throw ValidationException("Describe tu proyecto (mínimo 10 caracteres) o agrega servicios.")
                }
                valorONull {
                    crmRepository.registrarInteraccion(
                        TipoInteraccion.SEGUIMIENTO,
                        "Solicitud de cotización personalizada (app)",
                        notas
                    )
                }
                val mensaje = WhatsAppMessages.cotizacionPersonalizada(usuario, notas)
                WhatsAppRequest(whatsAppLauncher.crearIntent(telefono, mensaje), null)
            } else {
                if (items.any { it.cantidad <= 0.0 }) {
                    throw ValidationException("Las cantidades deben ser mayores a cero.")
                }
                val creada = cotizacionRepository.crearCotizacion(items, notas)
                val mensaje = WhatsAppMessages.cotizacion(usuario, creada, items, notas)
                WhatsAppRequest(whatsAppLauncher.crearIntent(telefono, mensaje), creada.codigo)
            }
        }
    }
}
KT

crear "$UC/cotizaciones/GetMisCotizacionesUseCase.kt" <<'KT'
package __PKG__.domain.usecases.cotizaciones

import __PKG__.data.repositories.AuthRepository
import __PKG__.data.repositories.CotizacionRepository
import __PKG__.domain.models.Cotizacion
import __PKG__.domain.utils.resultOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetMisCotizacionesUseCase(
    private val authRepository: AuthRepository,
    private val cotizacionRepository: CotizacionRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(): Result<List<Cotizacion>> = withContext(dispatcher) {
        resultOf {
            authRepository.requireUser()
            cotizacionRepository.obtenerMisCotizaciones()
        }
    }
}
KT

crear "$UC/cotizaciones/RequestAdvisoryUseCase.kt" <<'KT'
package __PKG__.domain.usecases.cotizaciones

import __PKG__.data.external.WhatsAppLauncher
import __PKG__.data.repositories.AuthRepository
import __PKG__.data.repositories.CrmRepository
import __PKG__.data.repositories.NegocioRepository
import __PKG__.domain.models.TipoInteraccion
import __PKG__.domain.models.WhatsAppRequest
import __PKG__.domain.utils.WhatsAppMessages
import __PKG__.domain.utils.resultOf
import __PKG__.domain.utils.valorONull
import __PKG__.domain.validation.InputValidator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RequestAdvisoryUseCase(
    private val authRepository: AuthRepository,
    private val crmRepository: CrmRepository,
    private val negocioRepository: NegocioRepository,
    private val whatsAppLauncher: WhatsAppLauncher,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(
        tema: String,
        mensaje: String? = null,
        horarioPreferido: String? = null
    ): Result<WhatsAppRequest> = withContext(dispatcher) {
        resultOf {
            val usuario = authRepository.requireUser()
            InputValidator.requireLongitud(tema, "El tema", 3, 120)
            val detalle = mensaje?.trim()?.takeIf { it.isNotEmpty() }
            val horario = horarioPreferido?.trim()?.takeIf { it.isNotEmpty() }
            val telefono = negocioRepository.obtenerTelefonoWhatsApp()

            val descripcionCrm = listOfNotNull(detalle, horario?.let { "Horario preferido: $it" })
                .joinToString("\n")
                .ifBlank { null }
            valorONull {
                crmRepository.registrarInteraccion(TipoInteraccion.ASESORIA, "Asesoría: ${tema.trim()}", descripcionCrm)
            }

            val texto = WhatsAppMessages.asesoria(usuario, tema.trim(), detalle, horario)
            WhatsAppRequest(whatsAppLauncher.crearIntent(telefono, texto), null)
        }
    }
}
KT

crear "$UC/encargos/CreateProductOrderUseCase.kt" <<'KT'
package __PKG__.domain.usecases.encargos

import __PKG__.data.repositories.AuthRepository
import __PKG__.data.repositories.EncargoRepository
import __PKG__.domain.models.ItemPedido
import __PKG__.domain.models.PedidoCreado
import __PKG__.domain.models.ValidationException
import __PKG__.domain.utils.resultOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CreateProductOrderUseCase(
    private val authRepository: AuthRepository,
    private val encargoRepository: EncargoRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(
        items: List<ItemPedido>,
        observaciones: String? = null
    ): Result<PedidoCreado> = withContext(dispatcher) {
        resultOf {
            authRepository.requireUser()
            if (items.isEmpty()) throw ValidationException("Agrega al menos un producto al pedido.")
            items.forEach { item ->
                if (item.cantidad <= 0) throw ValidationException("Las cantidades deben ser mayores a cero.")
                if (item.cantidad > item.producto.stock) {
                    throw ValidationException("Solo hay ${item.producto.stock} unidades de ${item.producto.nombre}.")
                }
            }
            val agrupados = items.groupBy { it.producto.id }
                .map { (_, lista) -> ItemPedido(lista.first().producto, lista.sumOf { it.cantidad }) }
            encargoRepository.crearPedido(agrupados, observaciones?.trim()?.takeIf { it.isNotEmpty() })
        }
    }
}
KT

crear "$UC/encargos/GetMisEncargosUseCase.kt" <<'KT'
package __PKG__.domain.usecases.encargos

import __PKG__.data.repositories.AuthRepository
import __PKG__.data.repositories.EncargoRepository
import __PKG__.domain.models.Encargo
import __PKG__.domain.utils.resultOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetMisEncargosUseCase(
    private val authRepository: AuthRepository,
    private val encargoRepository: EncargoRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(): Result<List<Encargo>> = withContext(dispatcher) {
        resultOf {
            authRepository.requireUser()
            encargoRepository.obtenerMisEncargos()
        }
    }
}
KT

crear "$UC/encargos/GetEncargoDetailUseCase.kt" <<'KT'
package __PKG__.domain.usecases.encargos

import __PKG__.data.repositories.AuthRepository
import __PKG__.data.repositories.EncargoRepository
import __PKG__.domain.models.AppException
import __PKG__.domain.models.Encargo
import __PKG__.domain.utils.resultOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetEncargoDetailUseCase(
    private val authRepository: AuthRepository,
    private val encargoRepository: EncargoRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(id: Long): Result<Encargo> = withContext(dispatcher) {
        resultOf {
            authRepository.requireUser()
            encargoRepository.obtenerEncargo(id) ?: throw AppException("No se encontró el encargo.")
        }
    }
}
KT

crear "$UC/pagos/GetMisFacturasUseCase.kt" <<'KT'
package __PKG__.domain.usecases.pagos

import __PKG__.data.repositories.AuthRepository
import __PKG__.data.repositories.PagoRepository
import __PKG__.domain.models.Factura
import __PKG__.domain.utils.resultOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetMisFacturasUseCase(
    private val authRepository: AuthRepository,
    private val pagoRepository: PagoRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(soloPendientes: Boolean = false): Result<List<Factura>> = withContext(dispatcher) {
        resultOf {
            authRepository.requireUser()
            pagoRepository.obtenerMisFacturas(soloPendientes)
        }
    }
}
KT

crear "$UC/pagos/GetMisPagosUseCase.kt" <<'KT'
package __PKG__.domain.usecases.pagos

import __PKG__.data.repositories.AuthRepository
import __PKG__.data.repositories.PagoRepository
import __PKG__.domain.models.Pago
import __PKG__.domain.utils.resultOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetMisPagosUseCase(
    private val authRepository: AuthRepository,
    private val pagoRepository: PagoRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(): Result<List<Pago>> = withContext(dispatcher) {
        resultOf {
            authRepository.requireUser()
            pagoRepository.obtenerMisPagos()
        }
    }
}
KT

crear "$UC/pagos/ProcessSimulatedPaymentUseCase.kt" <<'KT'
package __PKG__.domain.usecases.pagos

import __PKG__.data.repositories.AuthRepository
import __PKG__.data.repositories.PagoRepository
import __PKG__.domain.models.Factura
import __PKG__.domain.models.PagoRegistrado
import __PKG__.domain.models.PaymentDeclinedException
import __PKG__.domain.models.TarjetaPago
import __PKG__.domain.models.ValidationException
import __PKG__.domain.utils.resultOf
import __PKG__.domain.validation.CardValidator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.round

class ProcessSimulatedPaymentUseCase(
    private val authRepository: AuthRepository,
    private val pagoRepository: PagoRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(
        factura: Factura,
        tarjeta: TarjetaPago,
        monto: Double? = null
    ): Result<PagoRegistrado> = withContext(dispatcher) {
        resultOf {
            authRepository.requireUser()
            if (!factura.pagable) throw ValidationException("Esta factura no admite pagos.")
            val marca = CardValidator.validar(tarjeta)
            val importe = round((monto ?: factura.saldoPendiente) * 100.0) / 100.0
            if (importe <= 0.0 || importe > factura.saldoPendiente + 0.001) {
                throw ValidationException("El monto debe ser mayor a cero y no superar el saldo pendiente.")
            }

            delay(TIEMPO_PROCESAMIENTO_MS)

            val numero = CardValidator.limpiarNumero(tarjeta.numero)
            if (numero in TARJETAS_RECHAZADAS) {
                throw PaymentDeclinedException("La tarjeta fue rechazada por el banco emisor (simulación).")
            }

            pagoRepository.registrarPagoSimulado(
                idFactura = factura.id,
                monto = importe,
                marca = marca.name,
                ultimos4 = numero.takeLast(4),
                titular = tarjeta.titular.trim()
            )
        }
    }

    companion object {
        private const val TIEMPO_PROCESAMIENTO_MS = 1500L
        val TARJETAS_RECHAZADAS = setOf("4000000000000002", "5105105105105100")
        val TARJETAS_PRUEBA = listOf("4242424242424242", "5555555555554444", "378282246310005")
    }
}
KT

crear "$UC/feedback/SubmitResenaUseCase.kt" <<'KT'
package __PKG__.domain.usecases.feedback

import __PKG__.data.repositories.AuthRepository
import __PKG__.data.repositories.ClienteRepository
import __PKG__.data.repositories.FeedbackRepository
import __PKG__.domain.models.Resena
import __PKG__.domain.models.ValidationException
import __PKG__.domain.utils.resultOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SubmitResenaUseCase(
    private val authRepository: AuthRepository,
    private val clienteRepository: ClienteRepository,
    private val feedbackRepository: FeedbackRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(
        puntuacion: Int,
        comentario: String?,
        idServicio: Long? = null,
        idEncargo: Long? = null
    ): Result<Resena> = withContext(dispatcher) {
        resultOf {
            authRepository.requireUser()
            if (puntuacion !in 1..5) throw ValidationException("Selecciona una calificación de 1 a 5 estrellas.")
            if (idServicio == null && idEncargo == null) {
                throw ValidationException("Selecciona el servicio o encargo que deseas calificar.")
            }
            val texto = comentario?.trim()?.takeIf { it.isNotEmpty() }
            if (texto != null && texto.length > 1000) throw ValidationException("El comentario es demasiado largo.")
            val idCliente = clienteRepository.requireClienteId()
            feedbackRepository.enviarResena(idCliente, puntuacion, texto, idServicio, idEncargo)
        }
    }
}
KT

crear "$UC/feedback/SubmitSupportRequestUseCase.kt" <<'KT'
package __PKG__.domain.usecases.feedback

import __PKG__.data.repositories.AuthRepository
import __PKG__.data.repositories.ClienteRepository
import __PKG__.data.repositories.FeedbackRepository
import __PKG__.domain.models.PrioridadSoporte
import __PKG__.domain.models.SolicitudSoporte
import __PKG__.domain.utils.resultOf
import __PKG__.domain.validation.InputValidator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SubmitSupportRequestUseCase(
    private val authRepository: AuthRepository,
    private val clienteRepository: ClienteRepository,
    private val feedbackRepository: FeedbackRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(
        asunto: String,
        descripcion: String,
        prioridad: PrioridadSoporte = PrioridadSoporte.MEDIA
    ): Result<SolicitudSoporte> = withContext(dispatcher) {
        resultOf {
            authRepository.requireUser()
            InputValidator.requireLongitud(asunto, "El asunto", 3, 150)
            InputValidator.requireLongitud(descripcion, "La descripción", 10, 2000)
            val idCliente = clienteRepository.requireClienteId()
            feedbackRepository.enviarSolicitudSoporte(idCliente, asunto.trim(), descripcion.trim(), prioridad)
        }
    }
}
KT

crear "$UC/feedback/GetMisResenasUseCase.kt" <<'KT'
package __PKG__.domain.usecases.feedback

import __PKG__.data.repositories.AuthRepository
import __PKG__.data.repositories.FeedbackRepository
import __PKG__.domain.models.Resena
import __PKG__.domain.utils.resultOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetMisResenasUseCase(
    private val authRepository: AuthRepository,
    private val feedbackRepository: FeedbackRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(): Result<List<Resena>> = withContext(dispatcher) {
        resultOf {
            authRepository.requireUser()
            feedbackRepository.obtenerMisResenas()
        }
    }
}
KT

crear "$UC/feedback/GetMisSolicitudesSoporteUseCase.kt" <<'KT'
package __PKG__.domain.usecases.feedback

import __PKG__.data.repositories.AuthRepository
import __PKG__.data.repositories.FeedbackRepository
import __PKG__.domain.models.SolicitudSoporte
import __PKG__.domain.utils.resultOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetMisSolicitudesSoporteUseCase(
    private val authRepository: AuthRepository,
    private val feedbackRepository: FeedbackRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(): Result<List<SolicitudSoporte>> = withContext(dispatcher) {
        resultOf {
            authRepository.requireUser()
            feedbackRepository.obtenerMisSolicitudes()
        }
    }
}
KT

crear "$UC/crm/RegisterCrmInteractionUseCase.kt" <<'KT'
package __PKG__.domain.usecases.crm

import __PKG__.data.repositories.AuthRepository
import __PKG__.data.repositories.CrmRepository
import __PKG__.domain.models.TipoInteraccion
import __PKG__.domain.utils.resultOf
import __PKG__.domain.validation.InputValidator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RegisterCrmInteractionUseCase(
    private val authRepository: AuthRepository,
    private val crmRepository: CrmRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(
        tipo: TipoInteraccion,
        asunto: String,
        descripcion: String? = null
    ): Result<Long> = withContext(dispatcher) {
        resultOf {
            authRepository.requireUser()
            InputValidator.requireLongitud(asunto, "El asunto", 3, 150)
            crmRepository.registrarInteraccion(tipo, asunto.trim(), descripcion?.trim()?.takeIf { it.isNotEmpty() })
        }
    }
}
KT

crear "$UC/preferencias/HasSeenOnboardingUseCase.kt" <<'KT'
package __PKG__.domain.usecases.preferencias

import __PKG__.data.repositories.PreferencesRepository
import kotlinx.coroutines.flow.Flow

class HasSeenOnboardingUseCase(private val preferencesRepository: PreferencesRepository) {
    operator fun invoke(): Flow<Boolean> = preferencesRepository.onboardingVisto
}
KT

crear "$UC/preferencias/MarkOnboardingSeenUseCase.kt" <<'KT'
package __PKG__.domain.usecases.preferencias

import __PKG__.data.repositories.PreferencesRepository

class MarkOnboardingSeenUseCase(private val preferencesRepository: PreferencesRepository) {
    suspend operator fun invoke() = preferencesRepository.marcarOnboardingVisto()
}
KT

# ============================================================
# DATA / DI
# ============================================================
crear "$D/data/di/AppContainer.kt" <<'KT'
package __PKG__.data.di

import android.content.Context
import __PKG__.data.external.WhatsAppLauncher
import __PKG__.data.remote.auth.GoogleCredentialProvider
import __PKG__.data.repositories.AuthRepository
import __PKG__.data.repositories.CatalogRepository
import __PKG__.data.repositories.ClienteRepository
import __PKG__.data.repositories.CotizacionRepository
import __PKG__.data.repositories.CrmRepository
import __PKG__.data.repositories.EncargoRepository
import __PKG__.data.repositories.FeedbackRepository
import __PKG__.data.repositories.NegocioRepository
import __PKG__.data.repositories.PagoRepository
import __PKG__.data.repositories.PreferencesRepository
import __PKG__.domain.usecases.auth.GetCurrentUserUseCase
import __PKG__.domain.usecases.auth.GetLastEmailUseCase
import __PKG__.domain.usecases.auth.GetPendingVerificationEmailUseCase
import __PKG__.domain.usecases.auth.LoginWithEmailUseCase
import __PKG__.domain.usecases.auth.LogoutUseCase
import __PKG__.domain.usecases.auth.ObserveAuthStateUseCase
import __PKG__.domain.usecases.auth.RegisterWithEmailUseCase
import __PKG__.domain.usecases.auth.ResendVerificationCodeUseCase
import __PKG__.domain.usecases.auth.SendPasswordResetUseCase
import __PKG__.domain.usecases.auth.SignInWithGoogleUseCase
import __PKG__.domain.usecases.auth.VerifyEmailCodeUseCase
import __PKG__.domain.usecases.catalogo.GetCategoriasServiciosUseCase
import __PKG__.domain.usecases.catalogo.GetNegocioInfoUseCase
import __PKG__.domain.usecases.catalogo.GetProductoDetailUseCase
import __PKG__.domain.usecases.catalogo.GetProductosUseCase
import __PKG__.domain.usecases.catalogo.GetPromocionesActivasUseCase
import __PKG__.domain.usecases.catalogo.GetServicioDetailUseCase
import __PKG__.domain.usecases.catalogo.GetServiciosUseCase
import __PKG__.domain.usecases.cotizaciones.GetMisCotizacionesUseCase
import __PKG__.domain.usecases.cotizaciones.RequestAdvisoryUseCase
import __PKG__.domain.usecases.cotizaciones.RequestQuoteUseCase
import __PKG__.domain.usecases.crm.RegisterCrmInteractionUseCase
import __PKG__.domain.usecases.encargos.CreateProductOrderUseCase
import __PKG__.domain.usecases.encargos.GetEncargoDetailUseCase
import __PKG__.domain.usecases.encargos.GetMisEncargosUseCase
import __PKG__.domain.usecases.feedback.GetMisResenasUseCase
import __PKG__.domain.usecases.feedback.GetMisSolicitudesSoporteUseCase
import __PKG__.domain.usecases.feedback.SubmitResenaUseCase
import __PKG__.domain.usecases.feedback.SubmitSupportRequestUseCase
import __PKG__.domain.usecases.pagos.GetMisFacturasUseCase
import __PKG__.domain.usecases.pagos.GetMisPagosUseCase
import __PKG__.domain.usecases.pagos.ProcessSimulatedPaymentUseCase
import __PKG__.domain.usecases.perfil.GetClienteProfileUseCase
import __PKG__.domain.usecases.perfil.ObserveClienteProfileUseCase
import __PKG__.domain.usecases.perfil.UpdateClienteProfileUseCase
import __PKG__.domain.usecases.preferencias.HasSeenOnboardingUseCase
import __PKG__.domain.usecases.preferencias.MarkOnboardingSeenUseCase

object AppContainer {

    private lateinit var appContext: Context

    val isInitialized: Boolean
        get() = ::appContext.isInitialized

    fun initialize(context: Context) {
        if (!isInitialized) appContext = context.applicationContext
    }

    private fun context(): Context {
        check(isInitialized) { "AppContainer.initialize(context) debe llamarse en MainActivity.onCreate()" }
        return appContext
    }

    val preferencesRepository: PreferencesRepository by lazy { PreferencesRepository(context()) }
    val authRepository: AuthRepository by lazy { AuthRepository(GoogleCredentialProvider()) }
    val clienteRepository: ClienteRepository by lazy { ClienteRepository() }
    val catalogRepository: CatalogRepository by lazy { CatalogRepository() }
    val negocioRepository: NegocioRepository by lazy { NegocioRepository() }
    val cotizacionRepository: CotizacionRepository by lazy { CotizacionRepository() }
    val encargoRepository: EncargoRepository by lazy { EncargoRepository() }
    val pagoRepository: PagoRepository by lazy { PagoRepository() }
    val feedbackRepository: FeedbackRepository by lazy { FeedbackRepository() }
    val crmRepository: CrmRepository by lazy { CrmRepository() }
    val whatsAppLauncher: WhatsAppLauncher by lazy { WhatsAppLauncher() }

    val observeAuthStateUseCase by lazy { ObserveAuthStateUseCase(authRepository) }
    val getCurrentUserUseCase by lazy { GetCurrentUserUseCase(authRepository) }
    val registerWithEmailUseCase by lazy { RegisterWithEmailUseCase(authRepository, clienteRepository, preferencesRepository) }
    val verifyEmailCodeUseCase by lazy { VerifyEmailCodeUseCase(authRepository, clienteRepository, preferencesRepository) }
    val resendVerificationCodeUseCase by lazy { ResendVerificationCodeUseCase(authRepository) }
    val getPendingVerificationEmailUseCase by lazy { GetPendingVerificationEmailUseCase(preferencesRepository) }
    val getLastEmailUseCase by lazy { GetLastEmailUseCase(preferencesRepository) }
    val loginWithEmailUseCase by lazy { LoginWithEmailUseCase(authRepository, clienteRepository, preferencesRepository) }
    val signInWithGoogleUseCase by lazy { SignInWithGoogleUseCase(authRepository, clienteRepository, preferencesRepository) }
    val sendPasswordResetUseCase by lazy { SendPasswordResetUseCase(authRepository) }
    val logoutUseCase by lazy { LogoutUseCase(authRepository, clienteRepository) }

    val getClienteProfileUseCase by lazy { GetClienteProfileUseCase(authRepository, clienteRepository) }
    val observeClienteProfileUseCase by lazy { ObserveClienteProfileUseCase(clienteRepository) }
    val updateClienteProfileUseCase by lazy { UpdateClienteProfileUseCase(authRepository, clienteRepository) }

    val getProductosUseCase by lazy { GetProductosUseCase(catalogRepository) }
    val getProductoDetailUseCase by lazy { GetProductoDetailUseCase(catalogRepository) }
    val getServiciosUseCase by lazy { GetServiciosUseCase(catalogRepository) }
    val getServicioDetailUseCase by lazy { GetServicioDetailUseCase(catalogRepository) }
    val getCategoriasServiciosUseCase by lazy { GetCategoriasServiciosUseCase(catalogRepository) }
    val getPromocionesActivasUseCase by lazy { GetPromocionesActivasUseCase(catalogRepository) }
    val getNegocioInfoUseCase by lazy { GetNegocioInfoUseCase(negocioRepository) }

    val requestQuoteUseCase by lazy {
        RequestQuoteUseCase(authRepository, cotizacionRepository, crmRepository, negocioRepository, whatsAppLauncher)
    }
    val getMisCotizacionesUseCase by lazy { GetMisCotizacionesUseCase(authRepository, cotizacionRepository) }
    val requestAdvisoryUseCase by lazy {
        RequestAdvisoryUseCase(authRepository, crmRepository, negocioRepository, whatsAppLauncher)
    }

    val createProductOrderUseCase by lazy { CreateProductOrderUseCase(authRepository, encargoRepository) }
    val getMisEncargosUseCase by lazy { GetMisEncargosUseCase(authRepository, encargoRepository) }
    val getEncargoDetailUseCase by lazy { GetEncargoDetailUseCase(authRepository, encargoRepository) }

    val getMisFacturasUseCase by lazy { GetMisFacturasUseCase(authRepository, pagoRepository) }
    val getMisPagosUseCase by lazy { GetMisPagosUseCase(authRepository, pagoRepository) }
    val processSimulatedPaymentUseCase by lazy { ProcessSimulatedPaymentUseCase(authRepository, pagoRepository) }

    val submitResenaUseCase by lazy { SubmitResenaUseCase(authRepository, clienteRepository, feedbackRepository) }
    val submitSupportRequestUseCase by lazy {
        SubmitSupportRequestUseCase(authRepository, clienteRepository, feedbackRepository)
    }
    val getMisResenasUseCase by lazy { GetMisResenasUseCase(authRepository, feedbackRepository) }
    val getMisSolicitudesSoporteUseCase by lazy { GetMisSolicitudesSoporteUseCase(authRepository, feedbackRepository) }

    val registerCrmInteractionUseCase by lazy { RegisterCrmInteractionUseCase(authRepository, crmRepository) }

    val hasSeenOnboardingUseCase by lazy { HasSeenOnboardingUseCase(preferencesRepository) }
    val markOnboardingSeenUseCase by lazy { MarkOnboardingSeenUseCase(preferencesRepository) }
}
KT

# ============================================================
# MAINACTIVITY
# ============================================================
crear "$D/MainActivity.kt" <<'KT'
package __PKG__

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import __PKG__.data.di.AppContainer
import __PKG__.data.remote.supabase.Database
import __PKG__.domain.models.AuthState

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppContainer.initialize(applicationContext)

        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DataLayerStatusScreen()
                }
            }
        }
    }
}

@Composable
private fun DataLayerStatusScreen() {
    val authFlow = remember { AppContainer.observeAuthStateUseCase() }
    val authState by authFlow.collectAsState(initial = AuthState.Loading)
    var catalogo by remember { mutableStateOf("Consultando catálogo…") }

    LaunchedEffect(Unit) {
        catalogo = AppContainer.getProductosUseCase().fold(
            onSuccess = { "Productos disponibles: ${it.size}" },
            onFailure = { it.message ?: "Error al consultar productos" }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("3DForge Studio", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("Capa de datos inicializada", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(24.dp))
        Text(
            text = if (Database.isConfigured) "Supabase: configurado" else "Supabase: pendiente de configurar",
            textAlign = TextAlign.Center
        )
        Text(
            text = when (val estado = authState) {
                AuthState.Loading -> "Sesión: cargando…"
                AuthState.NotConfigured -> "Sesión: no disponible"
                AuthState.Guest -> "Sesión: invitado"
                is AuthState.Authenticated -> "Sesión: ${estado.user.email}"
            },
            textAlign = TextAlign.Center
        )
        Text(catalogo, textAlign = TextAlign.Center)
    }
}
KT

# ============================================================
# SUPABASE SQL
# ============================================================
info "Generando scripts SQL para Supabase ..."
mkdir -p "$ROOT/supabase"

crear "$ROOT/supabase/app_movil.sql" <<'SQL'
-- ============================================================
-- 3DForge Studio - Extensión para la app móvil (Supabase)
-- Ejecutar DESPUÉS de 3DForge_Studio_inicializacion_postgresql_final.sql
-- ============================================================

ALTER TABLE clientes ADD COLUMN IF NOT EXISTS auth_uid UUID UNIQUE REFERENCES auth.users(id) ON DELETE SET NULL;
ALTER TABLE clientes ADD COLUMN IF NOT EXISTS foto_url TEXT;
CREATE INDEX IF NOT EXISTS idx_clientes_auth_uid ON clientes (auth_uid);

-- ------------------------------------------------------------
-- FUNCIONES AUXILIARES
-- ------------------------------------------------------------

CREATE OR REPLACE FUNCTION public.app_cliente_actual()
RETURNS BIGINT
LANGUAGE sql STABLE SECURITY DEFINER SET search_path = public AS $$
    SELECT id FROM clientes WHERE auth_uid = auth.uid() LIMIT 1;
$$;

CREATE OR REPLACE FUNCTION public.app_usuario_sistema()
RETURNS BIGINT
LANGUAGE plpgsql STABLE SECURITY DEFINER SET search_path = public AS $$
DECLARE
    v_id BIGINT;
BEGIN
    SELECT id INTO v_id
    FROM usuarios
    WHERE is_active AND (is_superuser OR is_staff)
    ORDER BY is_superuser DESC, id
    LIMIT 1;
    IF v_id IS NULL THEN
        RAISE EXCEPTION 'No existe un usuario del sistema (is_staff) para asignar solicitudes de la app';
    END IF;
    RETURN v_id;
END;
$$;

-- ------------------------------------------------------------
-- CLIENTE (perfil CRM vinculado a Supabase Auth)
-- ------------------------------------------------------------

CREATE OR REPLACE FUNCTION public.app_asegurar_cliente(
    p_nombres TEXT DEFAULT NULL,
    p_apellidos TEXT DEFAULT NULL,
    p_telefono TEXT DEFAULT NULL,
    p_foto_url TEXT DEFAULT NULL
)
RETURNS clientes
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public AS $$
DECLARE
    v_uid UUID := auth.uid();
    v_correo TEXT := lower(coalesce(auth.jwt() ->> 'email', ''));
    v_cliente clientes;
BEGIN
    IF v_uid IS NULL THEN
        RAISE EXCEPTION 'AUTH_REQUIRED';
    END IF;

    SELECT * INTO v_cliente FROM clientes WHERE auth_uid = v_uid;

    IF NOT FOUND AND v_correo <> '' THEN
        UPDATE clientes
        SET auth_uid = v_uid, actualizado_en = now()
        WHERE id = (
            SELECT id FROM clientes
            WHERE auth_uid IS NULL AND lower(correo) = v_correo
            ORDER BY id LIMIT 1
        )
        RETURNING * INTO v_cliente;
    END IF;

    IF v_cliente.id IS NULL THEN
        INSERT INTO clientes (auth_uid, tipo_cliente, nombres, apellidos, correo, telefono, foto_url, observaciones)
        VALUES (
            v_uid, 'PERSONA',
            nullif(trim(p_nombres), ''),
            nullif(trim(p_apellidos), ''),
            v_correo,
            nullif(trim(p_telefono), ''),
            nullif(p_foto_url, ''),
            'Registrado desde la app móvil'
        )
        RETURNING * INTO v_cliente;
    ELSE
        UPDATE clientes SET
            nombres = coalesce(nombres, nullif(trim(p_nombres), '')),
            apellidos = coalesce(apellidos, nullif(trim(p_apellidos), '')),
            telefono = coalesce(telefono, nullif(trim(p_telefono), '')),
            foto_url = coalesce(nullif(p_foto_url, ''), foto_url),
            actualizado_en = now()
        WHERE id = v_cliente.id
        RETURNING * INTO v_cliente;
    END IF;

    RETURN v_cliente;
END;
$$;

-- ------------------------------------------------------------
-- COTIZACIONES (precios calculados en el servidor)
-- ------------------------------------------------------------

CREATE OR REPLACE FUNCTION public.app_crear_cotizacion(
    p_items JSONB,
    p_observaciones TEXT DEFAULT NULL
)
RETURNS JSONB
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public AS $$
DECLARE
    v_cliente BIGINT := app_cliente_actual();
    v_iva NUMERIC := 0.13;
    v_cot_id BIGINT;
    v_codigo TEXT;
    v_item JSONB;
    v_servicio servicios;
    v_cantidad NUMERIC(10,2);
    v_sub NUMERIC(12,2);
    v_subtotal NUMERIC(12,2) := 0;
    v_impuesto NUMERIC(12,2);
BEGIN
    IF v_cliente IS NULL THEN RAISE EXCEPTION 'CLIENTE_NO_REGISTRADO'; END IF;
    IF p_items IS NULL OR jsonb_typeof(p_items) <> 'array' OR jsonb_array_length(p_items) = 0 THEN
        RAISE EXCEPTION 'SIN_ITEMS';
    END IF;

    v_codigo := 'COT-APP-' || to_char(now(), 'YYMMDDHH24MISS') || '-' || lpad((floor(random() * 1000))::INT::TEXT, 3, '0');

    INSERT INTO cotizaciones (id_cliente, id_usuario_responsable, codigo, fecha_vencimiento, estado, observaciones)
    VALUES (v_cliente, app_usuario_sistema(), v_codigo, now() + INTERVAL '15 days', 'BORRADOR', p_observaciones)
    RETURNING id INTO v_cot_id;

    FOR v_item IN SELECT value FROM jsonb_array_elements(p_items) LOOP
        SELECT * INTO v_servicio FROM servicios
        WHERE id = (v_item ->> 'id_servicio')::BIGINT AND activo;
        IF NOT FOUND THEN RAISE EXCEPTION 'SERVICIO_NO_DISPONIBLE'; END IF;

        v_cantidad := coalesce((v_item ->> 'cantidad')::NUMERIC, 1);
        IF v_cantidad <= 0 THEN RAISE EXCEPTION 'CANTIDAD_INVALIDA'; END IF;

        v_sub := round(v_servicio.precio_base * v_cantidad, 2);
        INSERT INTO cotizaciones_detalles (id_cotizacion, id_servicio, descripcion, cantidad, precio_unitario, impuesto, subtotal)
        VALUES (
            v_cot_id, v_servicio.id,
            left(coalesce(nullif(trim(v_item ->> 'descripcion'), ''), v_servicio.nombre), 250),
            v_cantidad, v_servicio.precio_base, round(v_sub * v_iva, 2), v_sub
        );
        v_subtotal := v_subtotal + v_sub;
    END LOOP;

    v_impuesto := round(v_subtotal * v_iva, 2);
    UPDATE cotizaciones
    SET subtotal = v_subtotal, impuesto = v_impuesto, total = v_subtotal + v_impuesto
    WHERE id = v_cot_id;

    RETURN jsonb_build_object('id', v_cot_id, 'codigo', v_codigo, 'total', v_subtotal + v_impuesto);
END;
$$;

-- ------------------------------------------------------------
-- PEDIDOS DE PRODUCTOS (encargo + factura pendiente)
-- ------------------------------------------------------------

CREATE OR REPLACE FUNCTION public.app_crear_pedido(
    p_items JSONB,
    p_observaciones TEXT DEFAULT NULL
)
RETURNS JSONB
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public AS $$
DECLARE
    v_cliente_id BIGINT := app_cliente_actual();
    v_cliente clientes;
    v_iva NUMERIC := 0.13;
    v_enc_id BIGINT;
    v_fac_id BIGINT;
    v_codigo TEXT;
    v_numero TEXT;
    v_sufijo TEXT := to_char(now(), 'YYMMDDHH24MISS') || '-' || lpad((floor(random() * 1000))::INT::TEXT, 3, '0');
    v_item JSONB;
    v_producto productos;
    v_cantidad INTEGER;
    v_sub NUMERIC(12,2);
    v_subtotal NUMERIC(12,2) := 0;
    v_impuesto NUMERIC(12,2);
BEGIN
    IF v_cliente_id IS NULL THEN RAISE EXCEPTION 'CLIENTE_NO_REGISTRADO'; END IF;
    IF p_items IS NULL OR jsonb_typeof(p_items) <> 'array' OR jsonb_array_length(p_items) = 0 THEN
        RAISE EXCEPTION 'SIN_ITEMS';
    END IF;

    SELECT * INTO v_cliente FROM clientes WHERE id = v_cliente_id;
    v_codigo := 'ENC-APP-' || v_sufijo;
    v_numero := 'FAC-APP-' || v_sufijo;

    INSERT INTO encargos (id_cliente, codigo, descripcion, estado, observaciones)
    VALUES (v_cliente_id, v_codigo, 'Pedido de productos desde la app móvil', 'PENDIENTE', p_observaciones)
    RETURNING id INTO v_enc_id;

    FOR v_item IN SELECT value FROM jsonb_array_elements(p_items) LOOP
        SELECT * INTO v_producto FROM productos
        WHERE id = (v_item ->> 'id_producto')::BIGINT AND activo;
        IF NOT FOUND THEN RAISE EXCEPTION 'PRODUCTO_NO_DISPONIBLE'; END IF;

        v_cantidad := coalesce((v_item ->> 'cantidad')::INTEGER, 1);
        IF v_cantidad <= 0 THEN RAISE EXCEPTION 'CANTIDAD_INVALIDA'; END IF;
        IF v_producto.stock < v_cantidad THEN
            RAISE EXCEPTION 'STOCK_INSUFICIENTE:%', v_producto.nombre;
        END IF;

        v_sub := round(v_producto.precio * v_cantidad, 2);
        INSERT INTO encargos_detalles (id_encargo, id_producto, descripcion, cantidad, precio_unitario, impuesto, subtotal)
        VALUES (v_enc_id, v_producto.id, v_producto.nombre, v_cantidad, v_producto.precio, round(v_sub * v_iva, 2), v_sub);
        v_subtotal := v_subtotal + v_sub;
    END LOOP;

    v_impuesto := round(v_subtotal * v_iva, 2);
    UPDATE encargos
    SET subtotal = v_subtotal, impuesto = v_impuesto, total = v_subtotal + v_impuesto
    WHERE id = v_enc_id;

    INSERT INTO historial_encargos (id_encargo, id_usuario, estado_anterior, estado_nuevo, comentario)
    VALUES (v_enc_id, app_usuario_sistema(), NULL, 'PENDIENTE', 'Pedido creado desde la app móvil');

    INSERT INTO facturas (
        id_cliente, id_encargo, numero_factura, fecha_vencimiento, nombre_facturacion,
        identificacion_fiscal, direccion_facturacion, subtotal, impuesto, total, saldo_pendiente, estado
    )
    VALUES (
        v_cliente_id, v_enc_id, v_numero, now() + INTERVAL '7 days',
        left(coalesce(nullif(v_cliente.nombre_empresa, ''),
                      nullif(trim(coalesce(v_cliente.nombres, '') || ' ' || coalesce(v_cliente.apellidos, '')), ''),
                      v_cliente.correo), 150),
        v_cliente.identificacion_fiscal, v_cliente.direccion,
        v_subtotal, v_impuesto, v_subtotal + v_impuesto, v_subtotal + v_impuesto, 'PENDIENTE'
    )
    RETURNING id INTO v_fac_id;

    INSERT INTO facturas_detalles (id_factura, id_producto, descripcion, cantidad, precio_unitario, impuesto, subtotal)
    SELECT v_fac_id, id_producto, left(descripcion, 250), cantidad, precio_unitario, impuesto, subtotal
    FROM encargos_detalles
    WHERE id_encargo = v_enc_id;

    RETURN jsonb_build_object(
        'id_encargo', v_enc_id,
        'codigo', v_codigo,
        'id_factura', v_fac_id,
        'numero_factura', v_numero,
        'total', v_subtotal + v_impuesto
    );
END;
$$;

-- ------------------------------------------------------------
-- PAGO SIMULADO CON TARJETA (solo últimos 4 dígitos)
-- ------------------------------------------------------------

CREATE OR REPLACE FUNCTION public.app_registrar_pago_simulado(
    p_id_factura BIGINT,
    p_monto NUMERIC,
    p_marca TEXT,
    p_ultimos4 TEXT,
    p_titular TEXT DEFAULT NULL
)
RETURNS JSONB
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public AS $$
DECLARE
    v_cliente BIGINT := app_cliente_actual();
    v_factura facturas;
    v_metodo SMALLINT;
    v_pago_id BIGINT;
    v_ref TEXT;
    v_monto NUMERIC(12,2) := round(p_monto, 2);
    v_saldo NUMERIC(12,2);
    v_estado TEXT;
BEGIN
    IF v_cliente IS NULL THEN RAISE EXCEPTION 'CLIENTE_NO_REGISTRADO'; END IF;

    SELECT * INTO v_factura FROM facturas
    WHERE id = p_id_factura AND id_cliente = v_cliente
    FOR UPDATE;
    IF NOT FOUND THEN RAISE EXCEPTION 'FACTURA_NO_ENCONTRADA'; END IF;
    IF v_factura.estado NOT IN ('PENDIENTE', 'PARCIALMENTE_PAGADA', 'VENCIDA') THEN
        RAISE EXCEPTION 'FACTURA_NO_PAGABLE';
    END IF;
    IF v_monto IS NULL OR v_monto <= 0 OR v_monto > v_factura.saldo_pendiente THEN
        RAISE EXCEPTION 'MONTO_INVALIDO';
    END IF;
    IF p_ultimos4 IS NULL OR p_ultimos4 !~ '^[0-9]{4}$' THEN
        RAISE EXCEPTION 'TARJETA_INVALIDA';
    END IF;

    SELECT id INTO v_metodo FROM metodos_pago WHERE upper(nombre) = 'TARJETA' LIMIT 1;
    IF v_metodo IS NULL THEN
        INSERT INTO metodos_pago (nombre, descripcion)
        VALUES ('TARJETA', 'Tarjeta de crédito o débito')
        RETURNING id INTO v_metodo;
    END IF;

    v_ref := 'SIM-' || upper(substr(md5(random()::TEXT || clock_timestamp()::TEXT), 1, 12));

    INSERT INTO pagos (id_factura, id_metodo_pago, referencia, monto, estado, observaciones)
    VALUES (
        v_factura.id, v_metodo, v_ref, v_monto, 'CONFIRMADO',
        format('Pago simulado desde app: %s ****%s', upper(coalesce(p_marca, 'TARJETA')), p_ultimos4)
    )
    RETURNING id INTO v_pago_id;

    INSERT INTO transacciones_pasarela (id_pago, proveedor, referencia_externa, estado_respuesta, respuesta, fecha_respuesta)
    VALUES (
        v_pago_id, 'SIMULADO', v_ref, 'APPROVED',
        jsonb_build_object('marca', p_marca, 'ultimos4', p_ultimos4, 'titular', p_titular, 'simulado', TRUE),
        now()
    );

    v_saldo := greatest(v_factura.saldo_pendiente - v_monto, 0);
    v_estado := CASE WHEN v_saldo = 0 THEN 'PAGADA' ELSE 'PARCIALMENTE_PAGADA' END;

    UPDATE facturas SET saldo_pendiente = v_saldo, estado = v_estado WHERE id = v_factura.id;

    IF v_estado = 'PAGADA' AND v_factura.id_encargo IS NOT NULL THEN
        UPDATE encargos SET estado = 'EN_REVISION'
        WHERE id = v_factura.id_encargo AND estado = 'PENDIENTE';
        IF FOUND THEN
            INSERT INTO historial_encargos (id_encargo, id_usuario, estado_anterior, estado_nuevo, comentario)
            VALUES (v_factura.id_encargo, app_usuario_sistema(), 'PENDIENTE', 'EN_REVISION', 'Pago confirmado desde la app móvil');
        END IF;
    END IF;

    RETURN jsonb_build_object(
        'id_pago', v_pago_id,
        'referencia', v_ref,
        'estado', 'CONFIRMADO',
        'saldo_pendiente', v_saldo,
        'estado_factura', v_estado
    );
END;
$$;

-- ------------------------------------------------------------
-- CRM: INTERACCIONES (asesorías, seguimientos)
-- ------------------------------------------------------------

CREATE OR REPLACE FUNCTION public.app_registrar_interaccion(
    p_tipo TEXT,
    p_asunto TEXT,
    p_descripcion TEXT DEFAULT NULL
)
RETURNS BIGINT
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public AS $$
DECLARE
    v_cliente BIGINT := app_cliente_actual();
    v_id BIGINT;
BEGIN
    IF v_cliente IS NULL THEN RAISE EXCEPTION 'CLIENTE_NO_REGISTRADO'; END IF;
    IF upper(p_tipo) NOT IN ('LLAMADA', 'CORREO', 'ASESORIA', 'REUNION', 'SEGUIMIENTO', 'RECLAMO') THEN
        RAISE EXCEPTION 'TIPO_INVALIDO';
    END IF;

    INSERT INTO interacciones_clientes (id_cliente, id_usuario_responsable, tipo_interaccion, asunto, descripcion)
    VALUES (v_cliente, app_usuario_sistema(), upper(p_tipo), left(p_asunto, 150), p_descripcion)
    RETURNING id INTO v_id;

    RETURN v_id;
END;
$$;

-- ------------------------------------------------------------
-- PERMISOS DE FUNCIONES
-- ------------------------------------------------------------

REVOKE ALL ON FUNCTION public.app_cliente_actual() FROM PUBLIC, anon;
REVOKE ALL ON FUNCTION public.app_usuario_sistema() FROM PUBLIC, anon, authenticated;
REVOKE ALL ON FUNCTION public.app_asegurar_cliente(TEXT, TEXT, TEXT, TEXT) FROM PUBLIC, anon;
REVOKE ALL ON FUNCTION public.app_crear_cotizacion(JSONB, TEXT) FROM PUBLIC, anon;
REVOKE ALL ON FUNCTION public.app_crear_pedido(JSONB, TEXT) FROM PUBLIC, anon;
REVOKE ALL ON FUNCTION public.app_registrar_pago_simulado(BIGINT, NUMERIC, TEXT, TEXT, TEXT) FROM PUBLIC, anon;
REVOKE ALL ON FUNCTION public.app_registrar_interaccion(TEXT, TEXT, TEXT) FROM PUBLIC, anon;

GRANT EXECUTE ON FUNCTION public.app_cliente_actual() TO authenticated;
GRANT EXECUTE ON FUNCTION public.app_asegurar_cliente(TEXT, TEXT, TEXT, TEXT) TO authenticated;
GRANT EXECUTE ON FUNCTION public.app_crear_cotizacion(JSONB, TEXT) TO authenticated;
GRANT EXECUTE ON FUNCTION public.app_crear_pedido(JSONB, TEXT) TO authenticated;
GRANT EXECUTE ON FUNCTION public.app_registrar_pago_simulado(BIGINT, NUMERIC, TEXT, TEXT, TEXT) TO authenticated;
GRANT EXECUTE ON FUNCTION public.app_registrar_interaccion(TEXT, TEXT, TEXT) TO authenticated;

-- ------------------------------------------------------------
-- ROW LEVEL SECURITY
-- Todas las tablas quedan cerradas por defecto; solo se abre
-- lo que la app necesita. Django (dueño de las tablas) no se ve afectado.
-- ------------------------------------------------------------

ALTER TABLE estados_usuarios        ENABLE ROW LEVEL SECURITY;
ALTER TABLE usuarios                ENABLE ROW LEVEL SECURITY;
ALTER TABLE clientes                ENABLE ROW LEVEL SECURITY;
ALTER TABLE empleados               ENABLE ROW LEVEL SECURITY;
ALTER TABLE categorias_servicios    ENABLE ROW LEVEL SECURITY;
ALTER TABLE servicios               ENABLE ROW LEVEL SECURITY;
ALTER TABLE productos               ENABLE ROW LEVEL SECURITY;
ALTER TABLE cotizaciones            ENABLE ROW LEVEL SECURITY;
ALTER TABLE cotizaciones_detalles   ENABLE ROW LEVEL SECURITY;
ALTER TABLE encargos                ENABLE ROW LEVEL SECURITY;
ALTER TABLE encargos_detalles       ENABLE ROW LEVEL SECURITY;
ALTER TABLE archivos_encargos       ENABLE ROW LEVEL SECURITY;
ALTER TABLE historial_encargos      ENABLE ROW LEVEL SECURITY;
ALTER TABLE facturas                ENABLE ROW LEVEL SECURITY;
ALTER TABLE facturas_detalles       ENABLE ROW LEVEL SECURITY;
ALTER TABLE metodos_pago            ENABLE ROW LEVEL SECURITY;
ALTER TABLE pagos                   ENABLE ROW LEVEL SECURITY;
ALTER TABLE transacciones_pasarela  ENABLE ROW LEVEL SECURITY;
ALTER TABLE interacciones_clientes  ENABLE ROW LEVEL SECURITY;
ALTER TABLE notas_clientes          ENABLE ROW LEVEL SECURITY;
ALTER TABLE solicitudes_soporte     ENABLE ROW LEVEL SECURITY;
ALTER TABLE notificaciones          ENABLE ROW LEVEL SECURITY;
ALTER TABLE promociones             ENABLE ROW LEVEL SECURITY;
ALTER TABLE resenas                 ENABLE ROW LEVEL SECURITY;
ALTER TABLE configuracion_negocio   ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS app_categorias_lectura ON categorias_servicios;
CREATE POLICY app_categorias_lectura ON categorias_servicios FOR SELECT TO anon, authenticated USING (activo);

DROP POLICY IF EXISTS app_servicios_lectura ON servicios;
CREATE POLICY app_servicios_lectura ON servicios FOR SELECT TO anon, authenticated USING (activo);

DROP POLICY IF EXISTS app_productos_lectura ON productos;
CREATE POLICY app_productos_lectura ON productos FOR SELECT TO anon, authenticated USING (activo);

DROP POLICY IF EXISTS app_promociones_lectura ON promociones;
CREATE POLICY app_promociones_lectura ON promociones FOR SELECT TO anon, authenticated USING (activo);

DROP POLICY IF EXISTS app_metodos_pago_lectura ON metodos_pago;
CREATE POLICY app_metodos_pago_lectura ON metodos_pago FOR SELECT TO anon, authenticated USING (activo);

DROP POLICY IF EXISTS app_negocio_lectura ON configuracion_negocio;
CREATE POLICY app_negocio_lectura ON configuracion_negocio FOR SELECT TO anon, authenticated USING (TRUE);

DROP POLICY IF EXISTS app_clientes_lectura ON clientes;
CREATE POLICY app_clientes_lectura ON clientes FOR SELECT TO authenticated
    USING (auth_uid = auth.uid());

DROP POLICY IF EXISTS app_clientes_actualizacion ON clientes;
CREATE POLICY app_clientes_actualizacion ON clientes FOR UPDATE TO authenticated
    USING (auth_uid = auth.uid())
    WITH CHECK (auth_uid = auth.uid());

REVOKE UPDATE ON clientes FROM anon, authenticated;
GRANT UPDATE (tipo_cliente, nombres, apellidos, nombre_empresa, telefono,
              identificacion_fiscal, direccion, foto_url, actualizado_en)
    ON clientes TO authenticated;

DROP POLICY IF EXISTS app_cotizaciones_lectura ON cotizaciones;
CREATE POLICY app_cotizaciones_lectura ON cotizaciones FOR SELECT TO authenticated
    USING (id_cliente = app_cliente_actual());

DROP POLICY IF EXISTS app_cot_detalles_lectura ON cotizaciones_detalles;
CREATE POLICY app_cot_detalles_lectura ON cotizaciones_detalles FOR SELECT TO authenticated
    USING (EXISTS (SELECT 1 FROM cotizaciones c WHERE c.id = id_cotizacion AND c.id_cliente = app_cliente_actual()));

DROP POLICY IF EXISTS app_encargos_lectura ON encargos;
CREATE POLICY app_encargos_lectura ON encargos FOR SELECT TO authenticated
    USING (id_cliente = app_cliente_actual());

DROP POLICY IF EXISTS app_enc_detalles_lectura ON encargos_detalles;
CREATE POLICY app_enc_detalles_lectura ON encargos_detalles FOR SELECT TO authenticated
    USING (EXISTS (SELECT 1 FROM encargos e WHERE e.id = id_encargo AND e.id_cliente = app_cliente_actual()));

DROP POLICY IF EXISTS app_historial_lectura ON historial_encargos;
CREATE POLICY app_historial_lectura ON historial_encargos FOR SELECT TO authenticated
    USING (EXISTS (SELECT 1 FROM encargos e WHERE e.id = id_encargo AND e.id_cliente = app_cliente_actual()));

DROP POLICY IF EXISTS app_facturas_lectura ON facturas;
CREATE POLICY app_facturas_lectura ON facturas FOR SELECT TO authenticated
    USING (id_cliente = app_cliente_actual());

DROP POLICY IF EXISTS app_fact_detalles_lectura ON facturas_detalles;
CREATE POLICY app_fact_detalles_lectura ON facturas_detalles FOR SELECT TO authenticated
    USING (EXISTS (SELECT 1 FROM facturas f WHERE f.id = id_factura AND f.id_cliente = app_cliente_actual()));

DROP POLICY IF EXISTS app_pagos_lectura ON pagos;
CREATE POLICY app_pagos_lectura ON pagos FOR SELECT TO authenticated
    USING (EXISTS (SELECT 1 FROM facturas f WHERE f.id = id_factura AND f.id_cliente = app_cliente_actual()));

DROP POLICY IF EXISTS app_resenas_lectura ON resenas;
CREATE POLICY app_resenas_lectura ON resenas FOR SELECT TO authenticated
    USING (id_cliente = app_cliente_actual());

DROP POLICY IF EXISTS app_resenas_insercion ON resenas;
CREATE POLICY app_resenas_insercion ON resenas FOR INSERT TO authenticated
    WITH CHECK (
        id_cliente = app_cliente_actual()
        AND (id_encargo IS NULL OR EXISTS (
            SELECT 1 FROM encargos e WHERE e.id = id_encargo AND e.id_cliente = app_cliente_actual()
        ))
    );

DROP POLICY IF EXISTS app_soporte_lectura ON solicitudes_soporte;
CREATE POLICY app_soporte_lectura ON solicitudes_soporte FOR SELECT TO authenticated
    USING (id_cliente = app_cliente_actual());

DROP POLICY IF EXISTS app_soporte_insercion ON solicitudes_soporte;
CREATE POLICY app_soporte_insercion ON solicitudes_soporte FOR INSERT TO authenticated
    WITH CHECK (id_cliente = app_cliente_actual() AND estado = 'ABIERTA' AND id_usuario_responsable IS NULL);
SQL

crear "$ROOT/supabase/datos_ejemplo.sql" <<'SQL'
-- ============================================================
-- 3DForge Studio - Datos de ejemplo para probar la app (opcional)
-- ============================================================

INSERT INTO estados_usuarios (nombre, descripcion)
VALUES ('ACTIVO', 'Usuario activo')
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO usuarios (password, is_superuser, nombres, apellidos, correo, is_staff, is_active, id_estado)
SELECT '!', TRUE, 'Sistema', 'App Móvil', 'sistema@3dforge.local', TRUE, TRUE, id
FROM estados_usuarios WHERE nombre = 'ACTIVO'
ON CONFLICT (correo) DO NOTHING;

INSERT INTO categorias_servicios (nombre, descripcion) VALUES
    ('Impresión FDM', 'Piezas funcionales en PLA, PETG y ABS'),
    ('Impresión en resina', 'Alta definición para miniaturas y joyería'),
    ('Modelado 3D', 'Diseño de piezas a partir de bocetos o planos'),
    ('Postprocesado', 'Lijado, pintura y acabados profesionales')
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO servicios (id_categoria, nombre, descripcion, precio_base, requiere_archivo)
SELECT c.id, s.nombre, s.descripcion, s.precio, s.archivo
FROM (VALUES
    ('Impresión FDM', 'Impresión FDM por pieza', 'Impresión de piezas medianas en PLA o PETG.', 8.50, TRUE),
    ('Impresión FDM', 'Prototipado rápido', 'Prototipos funcionales con entrega en 48 horas.', 25.00, TRUE),
    ('Impresión en resina', 'Miniaturas en resina', 'Figuras de alta definición hasta 10 cm.', 15.00, TRUE),
    ('Modelado 3D', 'Modelado de pieza simple', 'Diseño CAD de una pieza a partir de medidas.', 30.00, FALSE),
    ('Postprocesado', 'Pintura y acabado', 'Lijado, imprimación y pintura a mano.', 12.00, FALSE)
) AS s(categoria, nombre, descripcion, precio, archivo)
JOIN categorias_servicios c ON c.nombre = s.categoria
WHERE NOT EXISTS (SELECT 1 FROM servicios x WHERE x.nombre = s.nombre);

INSERT INTO productos (sku, nombre, descripcion, precio, stock, stock_minimo) VALUES
    ('PRD-LLAV-001', 'Llavero personalizado', 'Llavero en PLA con nombre o logo.', 3.50, 120, 20),
    ('PRD-MACE-002', 'Maceta geométrica', 'Maceta low-poly de 12 cm con drenaje.', 9.99, 40, 5),
    ('PRD-SOPO-003', 'Soporte para celular', 'Soporte ajustable compatible con cualquier teléfono.', 6.75, 60, 10),
    ('PRD-FIGU-004', 'Figura articulada dragón', 'Dragón articulado impreso en una sola pieza.', 18.00, 25, 5),
    ('PRD-ORGA-005', 'Organizador de escritorio', 'Organizador modular para lápices y accesorios.', 12.50, 30, 5)
ON CONFLICT (sku) DO NOTHING;

INSERT INTO metodos_pago (nombre, descripcion)
VALUES ('TARJETA', 'Tarjeta de crédito o débito')
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO promociones (nombre, descripcion, tipo_descuento, valor_descuento, fecha_inicio, fecha_fin)
SELECT 'Bienvenida 3DForge', '10% en tu primera impresión', 'PORCENTAJE', 10, now() - INTERVAL '1 day', now() + INTERVAL '60 days'
WHERE NOT EXISTS (SELECT 1 FROM promociones WHERE nombre = 'Bienvenida 3DForge');

INSERT INTO configuracion_negocio (nombre_negocio, correo, telefono, direccion, sitio_web)
SELECT '3DForge Studio', 'contacto@3dforge.studio', '50370000000', 'Santa Ana, El Salvador', 'https://3dforge.studio'
WHERE NOT EXISTS (SELECT 1 FROM configuracion_negocio);
SQL

# ============================================================
# GRADLE, MANIFEST Y LOCAL.PROPERTIES
# ============================================================
info "Configurando Gradle y Manifest ..."

if [[ -f "$ROOT_GRADLE" ]] && ! grep -qE 'plugin\.serialization|kotlin\.serialization|kotlin-serialization' "$ROOT_GRADLE"; then
    if grep -qE '^\s*plugins\s*\{' "$ROOT_GRADLE"; then
        sed -i -E "0,/^\s*plugins\s*\{/s//plugins {\n    id(\"org.jetbrains.kotlin.plugin.serialization\") version \"${KOTLIN_VERSION}\" apply false/" "$ROOT_GRADLE"
        ok "Plugin de serialización declarado en build.gradle.kts (raíz)"
    else
        aviso "No se encontró bloque plugins {} en build.gradle.kts raíz"
    fi
fi

if ! grep -qE 'plugin\.serialization|kotlin\.serialization' "$APP_GRADLE"; then
    sed -i -E '0,/^\s*plugins\s*\{/s//plugins {\n    id("org.jetbrains.kotlin.plugin.serialization")/' "$APP_GRADLE"
    ok "Plugin de serialización aplicado en app/build.gradle.kts"
fi

if grep -qE 'minSdk\s*=\s*(1[0-9]|2[0-5])\b' "$APP_GRADLE"; then
    sed -i -E 's/(minSdk\s*=\s*)(1[0-9]|2[0-5])\b/\126/' "$APP_GRADLE"
    ok "minSdk actualizado a 26 (requerido por supabase-kt)"
fi
if grep -qE 'minSdk\s*\{' "$APP_GRADLE" && grep -qE 'release\((1[0-9]|2[0-5])\)' "$APP_GRADLE"; then
    sed -i -E 's/release\((1[0-9]|2[0-5])\)/release(26)/' "$APP_GRADLE"
    ok "minSdk actualizado a 26 (requerido por supabase-kt)"
fi

sed -i '/\/\/ >>> 3DFORGE DATAFLOW >>>/,/\/\/ <<< 3DFORGE DATAFLOW <<</d' "$APP_GRADLE"
sed -e "s/__SUPABASE__/${SUPABASE_VERSION}/g" \
    -e "s/__KTOR__/${KTOR_VERSION}/g" \
    -e "s/__CREDENTIALS__/${CREDENTIALS_VERSION}/g" \
    -e "s/__GOOGLEID__/${GOOGLEID_VERSION}/g" \
    -e "s/__DATASTORE__/${DATASTORE_VERSION}/g" >> "$APP_GRADLE" <<'GRADLE'

// >>> 3DFORGE DATAFLOW >>>
val forgeLocalProperties = java.util.Properties().apply {
    val archivo = rootProject.file("local.properties")
    if (archivo.exists()) archivo.inputStream().use { load(it) }
}

fun forgeProperty(clave: String): String =
    (forgeLocalProperties.getProperty(clave) ?: System.getenv(clave) ?: "").trim()

android {
    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
        buildConfigField("String", "SUPABASE_URL", "\"${forgeProperty("SUPABASE_URL")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${forgeProperty("SUPABASE_ANON_KEY")}\"")
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${forgeProperty("GOOGLE_WEB_CLIENT_ID")}\"")
        buildConfigField("String", "WHATSAPP_NUMBER", "\"${forgeProperty("WHATSAPP_NUMBER")}\"")
    }
}

dependencies {
    implementation(platform("io.github.jan-tennert.supabase:bom:__SUPABASE__"))
    implementation("io.github.jan-tennert.supabase:auth-kt")
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.ktor:ktor-client-okhttp:__KTOR__")
    implementation("androidx.credentials:credentials:__CREDENTIALS__")
    implementation("androidx.credentials:credentials-play-services-auth:__CREDENTIALS__")
    implementation("com.google.android.libraries.identity.googleid:googleid:__GOOGLEID__")
    implementation("androidx.datastore:datastore-preferences:__DATASTORE__")
}
// <<< 3DFORGE DATAFLOW <<<
GRADLE
ok "Dependencias y BuildConfig agregados a app/build.gradle.kts"

if ! grep -q 'android.permission.INTERNET' "$MANIFEST"; then
    sed -i '0,/<application/s//<uses-permission android:name="android.permission.INTERNET" \/>\n\n    <application/' "$MANIFEST"
    ok "Permiso INTERNET agregado al AndroidManifest.xml"
fi

touch "$LOCAL_PROPS"
if ! grep -q '^SUPABASE_URL=' "$LOCAL_PROPS"; then
    cat >> "$LOCAL_PROPS" <<'PROPS'

# ---- 3DForge Studio ----
SUPABASE_URL=
SUPABASE_ANON_KEY=
GOOGLE_WEB_CLIENT_ID=
WHATSAPP_NUMBER=
PROPS
    ok "Claves agregadas a local.properties (vacías, para configurar)"
fi

echo
ok "Capa de datos generada: $ARCHIVOS_CREADOS archivos"
cat <<EOF

Siguientes pasos:
  1. En Supabase > SQL Editor ejecuta, en orden:
       - 3DForge_Studio_inicializacion_postgresql_final.sql
       - supabase/app_movil.sql
       - supabase/datos_ejemplo.sql   (opcional)
  2. Completa local.properties: SUPABASE_URL, SUPABASE_ANON_KEY,
     GOOGLE_WEB_CLIENT_ID (cliente Web de Google Cloud) y WHATSAPP_NUMBER.
  3. Supabase > Authentication > Email templates > "Confirm signup":
     incluye {{ .Token }} para que el correo envíe el código de 6 dígitos.
  4. Sync Gradle y ejecuta la app.

Respaldo de archivos previos: ${RESPALDO#"$ROOT"/}
EOF
