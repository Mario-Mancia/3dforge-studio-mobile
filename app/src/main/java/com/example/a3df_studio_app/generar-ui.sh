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

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
    cat <<'EOF'
GENERADOR DE UI - 3DForge Studio (Jetpack Compose + Material 3)

Uso:
  ./generar-ui.sh [RUTA_PROYECTO_ANDROID]

Requiere haber ejecutado antes ./generar-dataflow.sh sobre el mismo proyecto.
Genera ui/ (tema, navegación, componentes, pantallas y ViewModels) y
reemplaza MainActivity.kt conservando la inicialización de la capa de datos.
EOF
    exit 0
fi

ROOT="$(cd "${1:-.}" && pwd)"
APP_GRADLE="$ROOT/app/build.gradle.kts"

[[ -f "$APP_GRADLE" ]] || error "No se encontró app/build.gradle.kts"
NAMESPACE="$(grep -E '^\s*namespace\s*=' "$APP_GRADLE" | head -1 | sed -E 's/.*"([^"]+)".*/\1/')"
[[ -n "$NAMESPACE" ]] || error "No se pudo detectar el namespace"
PKG="${PACKAGE_NAME:-$NAMESPACE}"
PKG_PATH="${PKG//./\/}"

if [[ -d "$ROOT/app/src/main/kotlin/$PKG_PATH" ]]; then
    D="$ROOT/app/src/main/kotlin/$PKG_PATH"
else
    D="$ROOT/app/src/main/java/$PKG_PATH"
fi

[[ -f "$D/data/di/AppContainer.kt" ]] || error "No existe la capa de datos. Ejecuta primero ./generar-dataflow.sh"

readonly NAVIGATION_VERSION="2.9.0"
readonly LIFECYCLE_VERSION="2.9.0"
readonly ICONS_VERSION="1.7.8"
readonly COIL_VERSION="3.1.0"

info "Proyecto: $ROOT"
info "Paquete:  $PKG"

RESPALDO="$ROOT/.respaldos/ui-$(date +%Y%m%d-%H%M%S)"
mkdir -p "$RESPALDO"
respaldar() {
    local origen="$1"
    [[ -e "$origen" ]] || return 0
    local relativo="${origen#"$ROOT"/}"
    mkdir -p "$RESPALDO/$(dirname "$relativo")"
    cp -r "$origen" "$RESPALDO/$relativo"
}
respaldar "$APP_GRADLE"
respaldar "$D/MainActivity.kt"
if [[ -d "$D/ui" ]]; then
    respaldar "$D/ui"
    rm -rf "$D/ui"
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

U="$D/ui"
info "Generando capa ui/ ..."

# ============================================================
# THEME
# ============================================================
crear "$U/theme/Color.kt" <<'KT'
package __PKG__.ui.theme

import androidx.compose.ui.graphics.Color

val ForgePrimary = Color(0xFF167C80)
val ForgePrimaryHover = Color(0xFF11666A)
val ForgePrimaryContainer = Color(0xFFD5F0ED)
val ForgeOnPrimaryContainer = Color(0xFF103F41)
val ForgeBackground = Color(0xFFF5F7F7)
val ForgeSurface = Color(0xFFFFFFFF)
val ForgeSurfaceSecondary = Color(0xFFEAF0F0)
val ForgeBorder = Color(0xFFD8E1E1)
val ForgeTextPrimary = Color(0xFF172B2D)
val ForgeTextSecondary = Color(0xFF52686A)

val ForgePrimaryDark = Color(0xFF65D4CE)
val ForgePrimaryHoverDark = Color(0xFF86E3DD)
val ForgePrimaryContainerDark = Color(0xFF173B3D)
val ForgeOnPrimaryContainerDark = Color(0xFFB4EFEB)
val ForgeBackgroundDark = Color(0xFF101819)
val ForgeSurfaceDark = Color(0xFF1A2527)
val ForgeSurfaceElevatedDark = Color(0xFF243234)
val ForgeSurfaceSecondaryDark = Color(0xFF202D2F)
val ForgeBorderDark = Color(0xFF354648)
val ForgeTextPrimaryDark = Color(0xFFE2E9E9)
val ForgeTextSecondaryDark = Color(0xFFA6B9B9)

val ForgeError = Color(0xFFBA1A1A)
val ForgeErrorDark = Color(0xFFFFB4AB)
val ForgeSuccess = Color(0xFF2E7D32)
val ForgeWarning = Color(0xFFB26A00)
KT

crear "$U/theme/Theme.kt" <<'KT'
package __PKG__.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = ForgePrimary,
    onPrimary = Color.White,
    primaryContainer = ForgePrimaryContainer,
    onPrimaryContainer = ForgeOnPrimaryContainer,
    secondary = ForgePrimaryHover,
    onSecondary = Color.White,
    secondaryContainer = ForgePrimaryContainer,
    onSecondaryContainer = ForgeOnPrimaryContainer,
    tertiary = ForgePrimaryHover,
    background = ForgeBackground,
    onBackground = ForgeTextPrimary,
    surface = ForgeSurface,
    onSurface = ForgeTextPrimary,
    surfaceVariant = ForgeSurfaceSecondary,
    onSurfaceVariant = ForgeTextSecondary,
    surfaceContainerLowest = ForgeSurface,
    surfaceContainerLow = ForgeSurface,
    surfaceContainer = ForgeSurface,
    surfaceContainerHigh = ForgeSurfaceSecondary,
    surfaceContainerHighest = ForgeSurfaceSecondary,
    outline = ForgeBorder,
    outlineVariant = ForgeBorder,
    error = ForgeError
)

private val DarkColors = darkColorScheme(
    primary = ForgePrimaryDark,
    onPrimary = ForgeBackgroundDark,
    primaryContainer = ForgePrimaryContainerDark,
    onPrimaryContainer = ForgeOnPrimaryContainerDark,
    secondary = ForgePrimaryHoverDark,
    onSecondary = ForgeBackgroundDark,
    secondaryContainer = ForgePrimaryContainerDark,
    onSecondaryContainer = ForgeOnPrimaryContainerDark,
    tertiary = ForgePrimaryHoverDark,
    background = ForgeBackgroundDark,
    onBackground = ForgeTextPrimaryDark,
    surface = ForgeSurfaceDark,
    onSurface = ForgeTextPrimaryDark,
    surfaceVariant = ForgeSurfaceSecondaryDark,
    onSurfaceVariant = ForgeTextSecondaryDark,
    surfaceContainerLowest = ForgeBackgroundDark,
    surfaceContainerLow = ForgeSurfaceDark,
    surfaceContainer = ForgeSurfaceDark,
    surfaceContainerHigh = ForgeSurfaceElevatedDark,
    surfaceContainerHighest = ForgeSurfaceElevatedDark,
    outline = ForgeBorderDark,
    outlineVariant = ForgeBorderDark,
    error = ForgeErrorDark
)

@Composable
fun ForgeStudioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = ForgeTypography,
        content = content
    )
}
KT

crear "$U/theme/Type.kt" <<'KT'
package __PKG__.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight

private val base = Typography()

val ForgeTypography = Typography(
    displaySmall = base.displaySmall.copy(fontWeight = FontWeight.Bold),
    headlineLarge = base.headlineLarge.copy(fontWeight = FontWeight.Bold),
    headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.Bold),
    headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
    titleLarge = base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    titleSmall = base.titleSmall.copy(fontWeight = FontWeight.SemiBold),
    labelLarge = base.labelLarge.copy(fontWeight = FontWeight.SemiBold)
)
KT

# ============================================================
# CLASSES (UI STATES)
# ============================================================
crear "$U/classes/MainUiState.kt" <<'KT'
package __PKG__.ui.classes

import __PKG__.domain.models.AuthState
import __PKG__.domain.models.AuthUser
import __PKG__.domain.models.Cliente

data class MainUiState(
    val authState: AuthState = AuthState.Loading,
    val cliente: Cliente? = null
) {
    val usuario: AuthUser?
        get() = (authState as? AuthState.Authenticated)?.user

    val isLoggedIn: Boolean
        get() = authState is AuthState.Authenticated

    val avatarUrl: String?
        get() = cliente?.fotoUrl ?: usuario?.avatarUrl
}
KT

crear "$U/classes/HomeUiState.kt" <<'KT'
package __PKG__.ui.classes

import __PKG__.domain.models.CategoriaServicio
import __PKG__.domain.models.NegocioInfo
import __PKG__.domain.models.Promocion
import __PKG__.domain.models.Servicio

data class HomeUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val negocio: NegocioInfo? = null,
    val promociones: List<Promocion> = emptyList(),
    val categorias: List<CategoriaServicio> = emptyList(),
    val servicios: List<Servicio> = emptyList(),
    val categoriaSeleccionada: Long? = null
) {
    val serviciosFiltrados: List<Servicio>
        get() = categoriaSeleccionada?.let { id -> servicios.filter { it.idCategoria == id } } ?: servicios
}
KT

crear "$U/classes/ProductosUiState.kt" <<'KT'
package __PKG__.ui.classes

import __PKG__.domain.models.ItemPedido
import __PKG__.domain.models.PedidoCreado
import __PKG__.domain.models.Producto

data class ProductosUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val busqueda: String = "",
    val productos: List<Producto> = emptyList(),
    val carrito: Map<Long, Int> = emptyMap(),
    val observaciones: String = "",
    val isOrdering: Boolean = false,
    val mensaje: String? = null,
    val pedidoCreado: PedidoCreado? = null
) {
    val itemsCarrito: List<ItemPedido>
        get() = carrito.mapNotNull { (id, cantidad) ->
            productos.firstOrNull { it.id == id }?.let { ItemPedido(it, cantidad) }
        }

    val totalCarrito: Double
        get() = itemsCarrito.sumOf { it.subtotal }

    val unidadesCarrito: Int
        get() = carrito.values.sum()
}
KT

crear "$U/classes/PedidosUiState.kt" <<'KT'
package __PKG__.ui.classes

import __PKG__.domain.models.Encargo
import __PKG__.domain.models.Factura
import __PKG__.domain.models.Pago

data class PedidosUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val encargos: List<Encargo> = emptyList(),
    val facturas: List<Factura> = emptyList(),
    val pagos: List<Pago> = emptyList(),
    val cargado: Boolean = false
)
KT

crear "$U/classes/PagoUiState.kt" <<'KT'
package __PKG__.ui.classes

import __PKG__.domain.models.MarcaTarjeta
import __PKG__.domain.models.PagoRegistrado

data class PagoUiState(
    val numero: String = "",
    val titular: String = "",
    val expiracion: String = "",
    val cvv: String = "",
    val marca: MarcaTarjeta = MarcaTarjeta.DESCONOCIDA,
    val isProcessing: Boolean = false,
    val error: String? = null,
    val resultado: PagoRegistrado? = null
)
KT

crear "$U/classes/CotizacionesUiState.kt" <<'KT'
package __PKG__.ui.classes

import android.content.Intent
import __PKG__.domain.models.Cotizacion
import __PKG__.domain.models.ItemCotizacion
import __PKG__.domain.models.Servicio

data class CotizacionesUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val servicios: List<Servicio> = emptyList(),
    val seleccion: List<ItemCotizacion> = emptyList(),
    val observaciones: String = "",
    val misCotizaciones: List<Cotizacion> = emptyList(),
    val temaAsesoria: String = "",
    val detalleAsesoria: String = "",
    val horarioAsesoria: String = "",
    val isSending: Boolean = false,
    val mensaje: String? = null,
    val whatsAppIntent: Intent? = null
) {
    val totalEstimado: Double
        get() = seleccion.sumOf { it.subtotalEstimado }
}
KT

crear "$U/classes/FeedbackUiState.kt" <<'KT'
package __PKG__.ui.classes

import __PKG__.domain.models.Encargo
import __PKG__.domain.models.PrioridadSoporte
import __PKG__.domain.models.Resena
import __PKG__.domain.models.Servicio
import __PKG__.domain.models.SolicitudSoporte

data class FeedbackUiState(
    val isLoading: Boolean = false,
    val servicios: List<Servicio> = emptyList(),
    val encargos: List<Encargo> = emptyList(),
    val misResenas: List<Resena> = emptyList(),
    val misSolicitudes: List<SolicitudSoporte> = emptyList(),
    val puntuacion: Int = 0,
    val comentario: String = "",
    val servicioSeleccionado: Long? = null,
    val encargoSeleccionado: Long? = null,
    val asunto: String = "",
    val descripcion: String = "",
    val prioridad: PrioridadSoporte = PrioridadSoporte.MEDIA,
    val isSending: Boolean = false,
    val mensaje: String? = null,
    val error: String? = null
)
KT

crear "$U/classes/AuthUiState.kt" <<'KT'
package __PKG__.ui.classes

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null,
    val correoVerificacion: String? = null,
    val autenticado: Boolean = false
)
KT

crear "$U/classes/PerfilUiState.kt" <<'KT'
package __PKG__.ui.classes

import __PKG__.domain.models.TipoCliente

data class PerfilUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null,
    val correo: String = "",
    val tipo: TipoCliente = TipoCliente.PERSONA,
    val nombres: String = "",
    val apellidos: String = "",
    val nombreEmpresa: String = "",
    val telefono: String = "",
    val identificacionFiscal: String = "",
    val direccion: String = "",
    val perfilCompleto: Boolean = false
)
KT

# ============================================================
# NAVIGATION / SCREEN
# ============================================================
crear "$U/navigation/Screen.kt" <<'KT'
package __PKG__.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.RequestQuote
import androidx.compose.material.icons.rounded.Feedback
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.RequestQuote
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Home : Screen("home")
    data object Productos : Screen("productos")
    data object Pedidos : Screen("pedidos")
    data object Cotizaciones : Screen("cotizaciones")
    data object Feedback : Screen("feedback")
    data object Login : Screen("login")
    data object Registro : Screen("registro")
    data object Perfil : Screen("perfil")

    data object Verificar : Screen("verificar/{correo}") {
        fun crear(correo: String) = "verificar/${android.net.Uri.encode(correo)}"
    }

    data object Pago : Screen("pago/{idFactura}") {
        fun crear(idFactura: Long) = "pago/$idFactura"
    }

    data object EncargoDetalle : Screen("encargo/{idEncargo}") {
        fun crear(idEncargo: Long) = "encargo/$idEncargo"
    }
}

data class BottomItem(
    val screen: Screen,
    val etiqueta: String,
    val icono: ImageVector,
    val iconoSeleccionado: ImageVector
)

val bottomItems = listOf(
    BottomItem(Screen.Home, "Inicio", Icons.Outlined.Home, Icons.Rounded.Home),
    BottomItem(Screen.Productos, "Productos", Icons.Outlined.Inventory2, Icons.Rounded.Inventory2),
    BottomItem(Screen.Pedidos, "Pedidos", Icons.Outlined.ReceiptLong, Icons.Rounded.ReceiptLong),
    BottomItem(Screen.Cotizaciones, "Cotizar", Icons.Outlined.RequestQuote, Icons.Rounded.RequestQuote),
    BottomItem(Screen.Feedback, "Opinión", Icons.Outlined.Feedback, Icons.Rounded.Feedback)
)
KT

# ============================================================
# VIEWMODELS BASE
# ============================================================
crear "$U/viewmodels/ForgeViewModelFactory.kt" <<'KT'
package __PKG__.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ForgeViewModelFactory<VM : ViewModel>(
    private val crear: () -> VM
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = crear() as T
}
KT

crear "$U/viewmodels/main/MainViewModel.kt" <<'KT'
package __PKG__.ui.viewmodels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import __PKG__.domain.models.AuthState
import __PKG__.domain.usecases.auth.ObserveAuthStateUseCase
import __PKG__.domain.usecases.perfil.GetClienteProfileUseCase
import __PKG__.domain.usecases.perfil.ObserveClienteProfileUseCase
import __PKG__.ui.classes.MainUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainViewModel(
    private val observeAuthStateUseCase: ObserveAuthStateUseCase,
    private val observeClienteProfileUseCase: ObserveClienteProfileUseCase,
    private val getClienteProfileUseCase: GetClienteProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(observeAuthStateUseCase(), observeClienteProfileUseCase()) { auth, cliente ->
                MainUiState(
                    authState = auth,
                    cliente = if (auth is AuthState.Authenticated) cliente else null
                )
            }.collect { _uiState.value = it }
        }
        viewModelScope.launch {
            observeAuthStateUseCase()
                .map { (it as? AuthState.Authenticated)?.user?.id }
                .distinctUntilChanged()
                .collect { id -> if (id != null) getClienteProfileUseCase() }
        }
    }
}
KT

# ============================================================
# COMPONENTS / TOPBAR & BOTTOMBAR
# ============================================================
crear "$U/components/common/UserAvatar.kt" <<'KT'
package __PKG__.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

@Composable
fun UserAvatar(
    avatarUrl: String?,
    iniciales: String?,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        when {
            !avatarUrl.isNullOrBlank() -> AsyncImage(
                model = avatarUrl,
                contentDescription = "Foto de perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size)
            )
            !iniciales.isNullOrBlank() -> Text(
                text = iniciales,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.labelLarge,
                fontSize = (size.value * 0.38f).sp
            )
            else -> Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = "Iniciar sesión",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(size * 0.6f)
            )
        }
    }
}
KT

crear "$U/components/topbar/ForgeTopBar.kt" <<'KT'
package __PKG__.ui.components.topbar

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ViewInAr
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import __PKG__.domain.models.AuthUser
import __PKG__.ui.components.common.UserAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgeTopBar(
    usuario: AuthUser?,
    avatarUrl: String?,
    onLogoClick: () -> Unit,
    onAvatarClick: () -> Unit
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onLogoClick) {
                Icon(
                    imageVector = Icons.Rounded.ViewInAr,
                    contentDescription = "3DForge Studio",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp)
                )
            }
        },
        title = { Text("3DForge Studio", style = MaterialTheme.typography.titleLarge) },
        actions = {
            IconButton(onClick = onAvatarClick, modifier = Modifier.padding(end = 4.dp)) {
                UserAvatar(avatarUrl = avatarUrl, iniciales = usuario?.iniciales)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackTopBar(
    titulo: String,
    onBack: () -> Unit
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
            }
        },
        title = { Text(titulo, style = MaterialTheme.typography.titleLarge) },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
    )
}
KT

crear "$U/components/bottombar/ForgeBottomBar.kt" <<'KT'
package __PKG__.ui.components.bottombar

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import __PKG__.ui.navigation.Screen
import __PKG__.ui.navigation.bottomItems

@Composable
fun ForgeBottomBar(
    rutaActual: String?,
    onNavigate: (Screen) -> Unit
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        bottomItems.forEach { item ->
            val seleccionado = rutaActual == item.screen.route
            NavigationBarItem(
                selected = seleccionado,
                onClick = { if (!seleccionado) onNavigate(item.screen) },
                icon = {
                    Icon(
                        imageVector = if (seleccionado) item.iconoSeleccionado else item.icono,
                        contentDescription = item.etiqueta
                    )
                },
                label = { Text(item.etiqueta) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}
KT

# ============================================================
# COMPONENTS / COMMON
# ============================================================
crear "$U/components/common/States.kt" <<'KT'
package __PKG__.ui.components.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun MessageState(
    icono: ImageVector,
    titulo: String,
    mensaje: String?,
    modifier: Modifier = Modifier,
    accion: String? = null,
    onAccion: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(56.dp))
        Spacer(Modifier.height(16.dp))
        Text(titulo, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        if (!mensaje.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                mensaje,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        if (accion != null && onAccion != null) {
            Spacer(Modifier.height(20.dp))
            OutlinedButton(onClick = onAccion) { Text(accion) }
        }
    }
}

@Composable
fun ErrorState(mensaje: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    MessageState(
        icono = Icons.Outlined.CloudOff,
        titulo = "No pudimos cargar la información",
        mensaje = mensaje,
        accion = "Reintentar",
        onAccion = onRetry,
        modifier = modifier
    )
}

@Composable
fun AuthRequiredState(
    titulo: String,
    mensaje: String,
    onLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Outlined.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(56.dp))
        Spacer(Modifier.height(16.dp))
        Text(titulo, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            mensaje,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = onLogin) { Text("Iniciar sesión o registrarme") }
    }
}
KT

crear "$U/components/common/Elements.kt" <<'KT'
package __PKG__.ui.components.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun ForgeCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colores = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    val borde = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    val forma = RoundedCornerShape(16.dp)
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier, shape = forma, colors = colores, border = borde) {
            Column(Modifier.padding(16.dp), content = content)
        }
    } else {
        Card(modifier = modifier, shape = forma, colors = colores, border = borde) {
            Column(Modifier.padding(16.dp), content = content)
        }
    }
}

@Composable
fun SectionTitle(
    texto: String,
    modifier: Modifier = Modifier,
    accion: String? = null,
    onAccion: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(texto, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        if (accion != null && onAccion != null) {
            TextButton(onClick = onAccion) { Text(accion) }
        }
    }
}

@Composable
fun StatusChip(texto: String, color: Color = MaterialTheme.colorScheme.primary) {
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.14f),
        contentColor = color
    ) {
        Text(
            texto,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun ImageOrIcon(
    url: String?,
    icono: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (!url.isNullOrBlank()) {
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size)
            )
        } else {
            Icon(
                icono,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(size * 0.5f)
            )
        }
    }
}
KT

# ============================================================
# COMPONENTS / CARDS
# ============================================================
crear "$U/components/helpers/EstadoColores.kt" <<'KT'
package __PKG__.ui.components.helpers

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import __PKG__.domain.models.EstadoCotizacion
import __PKG__.domain.models.EstadoEncargo
import __PKG__.domain.models.EstadoFactura
import __PKG__.domain.models.EstadoPago
import __PKG__.ui.theme.ForgeSuccess
import __PKG__.ui.theme.ForgeWarning

@Composable
fun EstadoEncargo.color(): Color = when (this) {
    EstadoEncargo.CANCELADO -> MaterialTheme.colorScheme.error
    EstadoEncargo.ENTREGADO, EstadoEncargo.FINALIZADO -> ForgeSuccess
    EstadoEncargo.EN_ESPERA, EstadoEncargo.PENDIENTE -> ForgeWarning
    else -> MaterialTheme.colorScheme.primary
}

@Composable
fun EstadoFactura.color(): Color = when (this) {
    EstadoFactura.PAGADA -> ForgeSuccess
    EstadoFactura.ANULADA, EstadoFactura.VENCIDA -> MaterialTheme.colorScheme.error
    else -> ForgeWarning
}

@Composable
fun EstadoPago.color(): Color = when (this) {
    EstadoPago.CONFIRMADO -> ForgeSuccess
    EstadoPago.RECHAZADO, EstadoPago.CANCELADO -> MaterialTheme.colorScheme.error
    else -> ForgeWarning
}

@Composable
fun EstadoCotizacion.color(): Color = when (this) {
    EstadoCotizacion.ACEPTADA -> ForgeSuccess
    EstadoCotizacion.RECHAZADA, EstadoCotizacion.CANCELADA, EstadoCotizacion.VENCIDA -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.primary
}
KT

crear "$U/components/card/ProductoCard.kt" <<'KT'
package __PKG__.ui.components.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddShoppingCart
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import __PKG__.domain.models.Producto
import __PKG__.domain.utils.comoMoneda
import __PKG__.ui.components.common.ForgeCard
import __PKG__.ui.components.common.ImageOrIcon

@Composable
fun ProductoCard(
    producto: Producto,
    cantidad: Int,
    onAgregar: () -> Unit,
    onQuitar: () -> Unit,
    modifier: Modifier = Modifier
) {
    ForgeCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ImageOrIcon(url = producto.imagenUrl, icono = Icons.Outlined.Category)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(producto.nombre, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                producto.descripcion?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(producto.precio.comoMoneda(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Text(
                    if (producto.disponible) "${producto.stock} disponibles" else "Agotado",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (producto.disponible) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        if (cantidad == 0) {
            FilledTonalButton(
                onClick = onAgregar,
                enabled = producto.disponible,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.AddShoppingCart, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Agregar al pedido")
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(onClick = onQuitar) { Icon(Icons.Rounded.Remove, contentDescription = "Quitar") }
                Text("$cantidad en el pedido", style = MaterialTheme.typography.titleSmall)
                FilledTonalIconButton(onClick = onAgregar, enabled = cantidad < producto.stock) {
                    Icon(Icons.Rounded.Add, contentDescription = "Agregar")
                }
            }
        }
    }
}
KT

crear "$U/components/card/ServicioCard.kt" <<'KT'
package __PKG__.ui.components.card

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PrecisionManufacturing
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import __PKG__.domain.models.Servicio
import __PKG__.domain.utils.comoMoneda
import __PKG__.ui.components.common.ForgeCard
import __PKG__.ui.components.common.ImageOrIcon
import __PKG__.ui.components.common.StatusChip

@Composable
fun ServicioCard(
    servicio: Servicio,
    accion: String,
    onAccion: () -> Unit,
    modifier: Modifier = Modifier
) {
    ForgeCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top) {
            ImageOrIcon(url = null, icono = Icons.Outlined.PrecisionManufacturing, size = 56.dp)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                servicio.categoriaNombre?.let {
                    StatusChip(it)
                    Spacer(Modifier.height(6.dp))
                }
                Text(servicio.nombre, style = MaterialTheme.typography.titleMedium)
                Text(
                    servicio.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Desde", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(servicio.precioBase.comoMoneda(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }
            if (servicio.requiereArchivo) {
                Text("Requiere archivo 3D", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(8.dp))
            }
            TextButton(onClick = onAccion) {
                Icon(Icons.Outlined.PrecisionManufacturing, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text(accion)
            }
        }
    }
}
KT

crear "$U/components/card/PromocionCard.kt" <<'KT'
package __PKG__.ui.components.card

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import __PKG__.domain.models.Promocion
import __PKG__.domain.models.TipoDescuento
import __PKG__.domain.utils.comoFecha
import __PKG__.domain.utils.comoMoneda

@Composable
fun PromocionCard(promocion: Promocion, modifier: Modifier = Modifier) {
    val etiqueta = when (promocion.tipoDescuento) {
        TipoDescuento.PORCENTAJE -> "${promocion.valorDescuento.toInt()}% OFF"
        TipoDescuento.MONTO_FIJO -> "${promocion.valorDescuento.comoMoneda()} de descuento"
    }
    Card(
        modifier = modifier.width(280.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(etiqueta, style = MaterialTheme.typography.headlineSmall)
            Text(promocion.nombre, style = MaterialTheme.typography.titleMedium)
            promocion.descripcion?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Text("Válido hasta ${promocion.fechaFin.comoFecha()}", style = MaterialTheme.typography.labelSmall)
        }
    }
}
KT

crear "$U/components/card/EncargoCard.kt" <<'KT'
package __PKG__.ui.components.card

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import __PKG__.domain.models.Encargo
import __PKG__.domain.models.EstadoEncargo
import __PKG__.domain.utils.comoFecha
import __PKG__.domain.utils.comoMoneda
import __PKG__.ui.components.common.ForgeCard
import __PKG__.ui.components.common.StatusChip
import __PKG__.ui.components.helpers.color

@Composable
fun EncargoCard(encargo: Encargo, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ForgeCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(encargo.codigo, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            StatusChip(encargo.estado.etiqueta, encargo.estado.color())
        }
        Text(
            "Solicitado el ${encargo.fechaSolicitud.comoFecha()} · ${encargo.detalles.size} artículo(s)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(10.dp))
        if (encargo.estado.paso >= 0) {
            LinearProgressIndicator(
                progress = { encargo.estado.paso / EstadoEncargo.TOTAL_PASOS.toFloat() },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
        }
        Text("Total ${encargo.total.comoMoneda()}", style = MaterialTheme.typography.titleSmall)
    }
}
KT

crear "$U/components/card/FacturaCard.kt" <<'KT'
package __PKG__.ui.components.card

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import __PKG__.domain.models.Factura
import __PKG__.domain.utils.comoFecha
import __PKG__.domain.utils.comoMoneda
import __PKG__.ui.components.common.ForgeCard
import __PKG__.ui.components.common.StatusChip
import __PKG__.ui.components.helpers.color

@Composable
fun FacturaCard(factura: Factura, onPagar: () -> Unit, modifier: Modifier = Modifier) {
    ForgeCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(factura.numero, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            StatusChip(factura.estado.etiqueta, factura.estado.color())
        }
        Text(
            "Emitida ${factura.fechaEmision.comoFecha()} · vence ${factura.fechaVencimiento.comoFecha()}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Total ${factura.total.comoMoneda()}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Pendiente ${factura.saldoPendiente.comoMoneda()}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (factura.pagable) {
                Button(onClick = onPagar) {
                    Icon(Icons.Rounded.CreditCard, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Pagar")
                }
            }
        }
    }
}
KT

# ============================================================
# SCREENS / SPLASH
# ============================================================
crear "$U/screens/splash/SplashScreen.kt" <<'KT'
package __PKG__.ui.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ViewInAr
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import __PKG__.domain.models.AuthState
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    authState: AuthState,
    onFinish: () -> Unit
) {
    LaunchedEffect(authState is AuthState.Loading) {
        if (authState !is AuthState.Loading) {
            delay(1200)
            onFinish()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Rounded.ViewInAr,
            contentDescription = "3DForge Studio",
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(120.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text("3DForge Studio", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onPrimary)
        Text("Impresión 3D a tu medida", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimary)
        Spacer(Modifier.height(40.dp))
        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
    }
}
KT

# ============================================================
# SCREENS / HOME
# ============================================================
crear "$U/screens/home/HomeViewModel.kt" <<'KT'
package __PKG__.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import __PKG__.domain.usecases.catalogo.GetCategoriasServiciosUseCase
import __PKG__.domain.usecases.catalogo.GetNegocioInfoUseCase
import __PKG__.domain.usecases.catalogo.GetPromocionesActivasUseCase
import __PKG__.domain.usecases.catalogo.GetServiciosUseCase
import __PKG__.ui.classes.HomeUiState
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getNegocioInfoUseCase: GetNegocioInfoUseCase,
    private val getPromocionesActivasUseCase: GetPromocionesActivasUseCase,
    private val getCategoriasServiciosUseCase: GetCategoriasServiciosUseCase,
    private val getServiciosUseCase: GetServiciosUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val negocio = async { getNegocioInfoUseCase() }
            val promociones = async { getPromocionesActivasUseCase() }
            val categorias = async { getCategoriasServiciosUseCase() }
            val servicios = async { getServiciosUseCase() }

            val resultadoServicios = servicios.await()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    negocio = negocio.await().getOrNull(),
                    promociones = promociones.await().getOrDefault(emptyList()),
                    categorias = categorias.await().getOrDefault(emptyList()),
                    servicios = resultadoServicios.getOrDefault(emptyList()),
                    error = resultadoServicios.exceptionOrNull()?.message
                )
            }
        }
    }

    fun seleccionarCategoria(id: Long?) {
        _uiState.update { it.copy(categoriaSeleccionada = if (it.categoriaSeleccionada == id) null else id) }
    }
}
KT

crear "$U/screens/home/HomeScreen.kt" <<'KT'
package __PKG__.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import __PKG__.domain.models.AuthUser
import __PKG__.domain.models.Servicio
import __PKG__.ui.components.card.PromocionCard
import __PKG__.ui.components.card.ServicioCard
import __PKG__.ui.components.common.ErrorState
import __PKG__.ui.components.common.ForgeCard
import __PKG__.ui.components.common.LoadingState
import __PKG__.ui.components.common.SectionTitle

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    usuario: AuthUser?,
    onCotizarServicio: (Servicio) -> Unit,
    onAsesoria: () -> Unit,
    onVerProductos: () -> Unit
) {
    val uiState by homeViewModel.uiState.collectAsState()

    if (uiState.isLoading && uiState.servicios.isEmpty()) {
        LoadingState()
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        usuario?.let { "¡Hola, ${it.primerNombre}!" } ?: "¡Bienvenido!",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Convertimos tus ideas en piezas reales: prototipos, repuestos, figuras y más.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onAsesoria) {
                            Icon(Icons.Rounded.SupportAgent, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Asesoría")
                        }
                        OutlinedButton(
                            onClick = onVerProductos,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                        ) {
                            Icon(Icons.Rounded.Inventory2, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Productos")
                        }
                    }
                }
            }
        }

        if (uiState.promociones.isNotEmpty()) {
            item { SectionTitle("Promociones") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.promociones, key = { it.id }) { PromocionCard(it) }
                }
            }
        }

        item { SectionTitle("Nuestros servicios") }

        if (uiState.categorias.isNotEmpty()) {
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.categorias, key = { it.id }) { categoria ->
                        FilterChip(
                            selected = uiState.categoriaSeleccionada == categoria.id,
                            onClick = { homeViewModel.seleccionarCategoria(categoria.id) },
                            label = { Text(categoria.nombre) }
                        )
                    }
                }
            }
        }

        val error = uiState.error
        if (error != null && uiState.servicios.isEmpty()) {
            item { ErrorState(mensaje = error, onRetry = homeViewModel::cargar) }
        }

        items(uiState.serviciosFiltrados, key = { it.id }) { servicio ->
            ServicioCard(
                servicio = servicio,
                accion = "Cotizar",
                onAccion = { onCotizarServicio(servicio) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        uiState.negocio?.let { negocio ->
            item { SectionTitle("Contacto") }
            item {
                ForgeCard(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
                    Text(negocio.nombre, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    negocio.telefono?.let { ContactoFila(Icons.Outlined.Phone, it) }
                    negocio.correo?.let { ContactoFila(Icons.Outlined.Email, it) }
                    negocio.direccion?.let { ContactoFila(Icons.Outlined.LocationOn, it) }
                }
            }
        }
    }
}

@Composable
private fun ContactoFila(icono: ImageVector, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Text(texto, style = MaterialTheme.typography.bodyMedium)
    }
}
KT

# ============================================================
# SCREENS / PRODUCTOS
# ============================================================
crear "$U/screens/productos/ProductosViewModel.kt" <<'KT'
package __PKG__.ui.screens.productos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import __PKG__.domain.usecases.catalogo.GetProductosUseCase
import __PKG__.domain.usecases.encargos.CreateProductOrderUseCase
import __PKG__.ui.classes.ProductosUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProductosViewModel(
    private val getProductosUseCase: GetProductosUseCase,
    private val createProductOrderUseCase: CreateProductOrderUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductosUiState())
    val uiState: StateFlow<ProductosUiState> = _uiState.asStateFlow()
    private var busquedaJob: Job? = null

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getProductosUseCase(_uiState.value.busqueda).fold(
                onSuccess = { lista -> _uiState.update { it.copy(isLoading = false, productos = lista) } },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
            )
        }
    }

    fun onBusqueda(texto: String) {
        _uiState.update { it.copy(busqueda = texto) }
        busquedaJob?.cancel()
        busquedaJob = viewModelScope.launch {
            delay(400)
            cargar()
        }
    }

    fun agregar(idProducto: Long) {
        _uiState.update { estado ->
            val stock = estado.productos.firstOrNull { it.id == idProducto }?.stock ?: 0
            val actual = estado.carrito[idProducto] ?: 0
            if (actual >= stock) estado else estado.copy(carrito = estado.carrito + (idProducto to actual + 1))
        }
    }

    fun quitar(idProducto: Long) {
        _uiState.update { estado ->
            val actual = estado.carrito[idProducto] ?: return@update estado
            val nuevo = if (actual <= 1) estado.carrito - idProducto else estado.carrito + (idProducto to actual - 1)
            estado.copy(carrito = nuevo)
        }
    }

    fun onObservaciones(texto: String) {
        _uiState.update { it.copy(observaciones = texto) }
    }

    fun realizarPedido() {
        val estado = _uiState.value
        if (estado.isOrdering) return
        viewModelScope.launch {
            _uiState.update { it.copy(isOrdering = true, mensaje = null) }
            createProductOrderUseCase(estado.itemsCarrito, estado.observaciones).fold(
                onSuccess = { pedido ->
                    _uiState.update {
                        it.copy(isOrdering = false, carrito = emptyMap(), observaciones = "", pedidoCreado = pedido)
                    }
                    cargar()
                },
                onFailure = { e -> _uiState.update { it.copy(isOrdering = false, mensaje = e.message) } }
            )
        }
    }

    fun consumirMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }

    fun cerrarPedidoCreado() {
        _uiState.update { it.copy(pedidoCreado = null) }
    }
}
KT

crear "$U/screens/productos/ProductosScreen.kt" <<'KT'
package __PKG__.ui.screens.productos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import __PKG__.domain.utils.comoMoneda
import __PKG__.ui.components.card.ProductoCard
import __PKG__.ui.components.common.ErrorState
import __PKG__.ui.components.common.LoadingState
import __PKG__.ui.components.common.MessageState

@Composable
fun ProductosScreen(
    productosViewModel: ProductosViewModel,
    isLoggedIn: Boolean,
    snackbarHostState: SnackbarHostState,
    onRequireLogin: () -> Unit,
    onPagar: (Long) -> Unit,
    onVerPedidos: () -> Unit
) {
    val uiState by productosViewModel.uiState.collectAsState()
    var confirmar by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            productosViewModel.consumirMensaje()
        }
    }

    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = uiState.busqueda,
            onValueChange = productosViewModel::onBusqueda,
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            placeholder = { Text("Buscar productos") },
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        val error = uiState.error
        when {
            uiState.isLoading && uiState.productos.isEmpty() -> LoadingState(Modifier.weight(1f))
            error != null && uiState.productos.isEmpty() ->
                ErrorState(error, productosViewModel::cargar, Modifier.weight(1f))
            uiState.productos.isEmpty() -> MessageState(
                icono = Icons.Outlined.Inventory2,
                titulo = "Sin resultados",
                mensaje = "No encontramos productos con ese nombre.",
                modifier = Modifier.weight(1f)
            )
            else -> LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.productos, key = { it.id }) { producto ->
                    ProductoCard(
                        producto = producto,
                        cantidad = uiState.carrito[producto.id] ?: 0,
                        onAgregar = { productosViewModel.agregar(producto.id) },
                        onQuitar = { productosViewModel.quitar(producto.id) }
                    )
                }
            }
        }

        if (uiState.unidadesCarrito > 0) {
            Surface(tonalElevation = 3.dp, shadowElevation = 6.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("${uiState.unidadesCarrito} artículo(s)", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "${uiState.totalCarrito.comoMoneda()} + IVA",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Button(
                        onClick = { if (isLoggedIn) confirmar = true else onRequireLogin() },
                        enabled = !uiState.isOrdering
                    ) {
                        if (uiState.isOrdering) {
                            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Realizar pedido")
                        }
                    }
                }
            }
        }
    }

    if (confirmar) {
        AlertDialog(
            onDismissRequest = { confirmar = false },
            title = { Text("Confirmar pedido") },
            text = {
                Column {
                    uiState.itemsCarrito.forEach {
                        Text("• ${it.producto.nombre} x ${it.cantidad} — ${it.subtotal.comoMoneda()}")
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = uiState.observaciones,
                        onValueChange = productosViewModel::onObservaciones,
                        label = { Text("Observaciones (opcional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Se aplicará IVA (13%) al generar la factura.", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(onClick = {
                    confirmar = false
                    productosViewModel.realizarPedido()
                }) { Text("Confirmar") }
            },
            dismissButton = { TextButton(onClick = { confirmar = false }) { Text("Cancelar") } }
        )
    }

    uiState.pedidoCreado?.let { pedido ->
        AlertDialog(
            onDismissRequest = productosViewModel::cerrarPedidoCreado,
            title = { Text("¡Pedido registrado!") },
            text = {
                Text("Tu pedido ${pedido.codigo} fue creado con la factura ${pedido.numeroFactura} por ${pedido.total.comoMoneda()}.")
            },
            confirmButton = {
                Button(onClick = {
                    productosViewModel.cerrarPedidoCreado()
                    onPagar(pedido.idFactura)
                }) { Text("Pagar ahora") }
            },
            dismissButton = {
                TextButton(onClick = {
                    productosViewModel.cerrarPedidoCreado()
                    onVerPedidos()
                }) { Text("Ver mis pedidos") }
            }
        )
    }
}
KT

# ============================================================
# SCREENS / PEDIDOS
# ============================================================
crear "$U/screens/pedidos/PedidosViewModel.kt" <<'KT'
package __PKG__.ui.screens.pedidos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import __PKG__.domain.models.Factura
import __PKG__.domain.usecases.encargos.GetMisEncargosUseCase
import __PKG__.domain.usecases.pagos.GetMisFacturasUseCase
import __PKG__.domain.usecases.pagos.GetMisPagosUseCase
import __PKG__.ui.classes.PedidosUiState
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PedidosViewModel(
    private val getMisEncargosUseCase: GetMisEncargosUseCase,
    private val getMisFacturasUseCase: GetMisFacturasUseCase,
    private val getMisPagosUseCase: GetMisPagosUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PedidosUiState())
    val uiState: StateFlow<PedidosUiState> = _uiState.asStateFlow()

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val encargos = async { getMisEncargosUseCase() }
            val facturas = async { getMisFacturasUseCase() }
            val pagos = async { getMisPagosUseCase() }
            val resultadoEncargos = encargos.await()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    cargado = true,
                    encargos = resultadoEncargos.getOrDefault(emptyList()),
                    facturas = facturas.await().getOrDefault(emptyList()),
                    pagos = pagos.await().getOrDefault(emptyList()),
                    error = resultadoEncargos.exceptionOrNull()?.message
                )
            }
        }
    }

    fun factura(id: Long): Factura? = _uiState.value.facturas.firstOrNull { it.id == id }

    fun limpiar() {
        _uiState.value = PedidosUiState()
    }
}
KT

crear "$U/screens/pedidos/PedidosScreen.kt" <<'KT'
package __PKG__.ui.screens.pedidos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import __PKG__.domain.utils.comoFechaHora
import __PKG__.domain.utils.comoMoneda
import __PKG__.ui.components.card.EncargoCard
import __PKG__.ui.components.card.FacturaCard
import __PKG__.ui.components.common.AuthRequiredState
import __PKG__.ui.components.common.ErrorState
import __PKG__.ui.components.common.ForgeCard
import __PKG__.ui.components.common.LoadingState
import __PKG__.ui.components.common.MessageState
import __PKG__.ui.components.common.StatusChip
import __PKG__.ui.components.helpers.color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidosScreen(
    pedidosViewModel: PedidosViewModel,
    isLoggedIn: Boolean,
    onRequireLogin: () -> Unit,
    onEncargo: (Long) -> Unit,
    onPagar: (Long) -> Unit,
    onIrProductos: () -> Unit
) {
    if (!isLoggedIn) {
        AuthRequiredState(
            titulo = "Tus pedidos y pagos",
            mensaje = "Inicia sesión para ver el estado de tus encargos, tus facturas y realizar pagos en línea.",
            onLogin = onRequireLogin
        )
        return
    }

    val uiState by pedidosViewModel.uiState.collectAsState()
    var pestana by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(Unit) { pedidosViewModel.cargar() }

    Column(Modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = pestana) {
            listOf("Encargos", "Facturas", "Pagos").forEachIndexed { indice, titulo ->
                Tab(selected = pestana == indice, onClick = { pestana = indice }, text = { Text(titulo) })
            }
        }

        val error = uiState.error
        when {
            uiState.isLoading && !uiState.cargado -> LoadingState()
            error != null && uiState.encargos.isEmpty() -> ErrorState(error, pedidosViewModel::cargar)
            else -> PullToRefreshBox(
                isRefreshing = uiState.isLoading,
                onRefresh = pedidosViewModel::cargar,
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (pestana) {
                        0 -> {
                            if (uiState.encargos.isEmpty()) item {
                                MessageState(Icons.Outlined.ShoppingBag, "Aún no tienes encargos", "Explora el catálogo y realiza tu primer pedido.", accion = "Ver productos", onAccion = onIrProductos)
                            }
                            items(uiState.encargos, key = { it.id }) { EncargoCard(it, onClick = { onEncargo(it.id) }) }
                        }
                        1 -> {
                            if (uiState.facturas.isEmpty()) item {
                                MessageState(Icons.Outlined.ReceiptLong, "Sin facturas", "Aquí aparecerán las facturas de tus pedidos.")
                            }
                            items(uiState.facturas, key = { it.id }) { FacturaCard(it, onPagar = { onPagar(it.id) }) }
                        }
                        else -> {
                            if (uiState.pagos.isEmpty()) item {
                                MessageState(Icons.Outlined.Payments, "Sin pagos registrados", null)
                            }
                            items(uiState.pagos, key = { it.id }) { pago ->
                                ForgeCard(Modifier.fillMaxWidth()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(pago.monto.comoMoneda(), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                        StatusChip(pago.estado.etiqueta, pago.estado.color())
                                    }
                                    Text(pago.fecha.comoFechaHora(), style = MaterialTheme.typography.bodySmall)
                                    pago.referencia?.let { Text("Ref. $it", style = MaterialTheme.typography.bodySmall) }
                                    pago.observaciones?.let {
                                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
KT

crear "$U/screens/pedidos/EncargoDetalleScreen.kt" <<'KT'
package __PKG__.ui.screens.pedidos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import __PKG__.domain.utils.comoCantidad
import __PKG__.domain.utils.comoFecha
import __PKG__.domain.utils.comoFechaHora
import __PKG__.domain.utils.comoMoneda
import __PKG__.ui.components.common.ForgeCard
import __PKG__.ui.components.common.MessageState
import __PKG__.ui.components.common.StatusChip
import __PKG__.ui.components.helpers.color
import androidx.compose.material.icons.outlined.SearchOff

@Composable
fun EncargoDetalleScreen(
    pedidosViewModel: PedidosViewModel,
    idEncargo: Long
) {
    val uiState by pedidosViewModel.uiState.collectAsState()
    val encargo = uiState.encargos.firstOrNull { it.id == idEncargo }

    if (encargo == null) {
        MessageState(Icons.Outlined.SearchOff, "Encargo no encontrado", null)
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ForgeCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(encargo.codigo, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    StatusChip(encargo.estado.etiqueta, encargo.estado.color())
                }
                Spacer(Modifier.height(8.dp))
                Text("Solicitado: ${encargo.fechaSolicitud.comoFecha()}", style = MaterialTheme.typography.bodyMedium)
                Text("Entrega estimada: ${encargo.fechaEntregaEstimada.comoFecha()}", style = MaterialTheme.typography.bodyMedium)
                encargo.observaciones?.let { Text("Notas: $it", style = MaterialTheme.typography.bodySmall) }
            }
        }
        item { Text("Detalle", style = MaterialTheme.typography.titleMedium) }
        item {
            ForgeCard(Modifier.fillMaxWidth()) {
                encargo.detalles.forEach { detalle ->
                    Row(Modifier.padding(vertical = 4.dp)) {
                        Text("${detalle.descripcion} x ${detalle.cantidad.comoCantidad()}", modifier = Modifier.weight(1f))
                        Text(detalle.subtotal.comoMoneda())
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                FilaTotal("Subtotal", encargo.subtotal.comoMoneda())
                FilaTotal("IVA", encargo.impuesto.comoMoneda())
                if (encargo.descuento > 0) FilaTotal("Descuento", "-${encargo.descuento.comoMoneda()}")
                FilaTotal("Total", encargo.total.comoMoneda(), destacado = true)
            }
        }
        if (encargo.historial.isNotEmpty()) {
            item { Text("Seguimiento", style = MaterialTheme.typography.titleMedium) }
            items(encargo.historial, key = { it.id }) { evento ->
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = evento.estadoNuevo.color(), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(evento.estadoNuevo.etiqueta, style = MaterialTheme.typography.titleSmall)
                        Text(evento.fecha.comoFechaHora(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        evento.comentario?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaTotal(etiqueta: String, valor: String, destacado: Boolean = false) {
    val estilo = if (destacado) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium
    Row(Modifier.fillMaxWidth()) {
        Text(etiqueta, style = estilo, modifier = Modifier.weight(1f))
        Text(valor, style = estilo, color = if (destacado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
    }
}
KT

# ============================================================
# SCREENS / PAGO
# ============================================================
crear "$U/screens/pago/PagoViewModel.kt" <<'KT'
package __PKG__.ui.screens.pago

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import __PKG__.domain.models.Factura
import __PKG__.domain.models.TarjetaPago
import __PKG__.domain.usecases.pagos.ProcessSimulatedPaymentUseCase
import __PKG__.domain.validation.CardValidator
import __PKG__.ui.classes.PagoUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PagoViewModel(
    private val processSimulatedPaymentUseCase: ProcessSimulatedPaymentUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PagoUiState())
    val uiState: StateFlow<PagoUiState> = _uiState.asStateFlow()

    fun onNumero(texto: String) {
        val digitos = texto.filter { it.isDigit() }.take(19)
        _uiState.update { it.copy(numero = digitos, marca = CardValidator.detectarMarca(digitos), error = null) }
    }

    fun onTitular(texto: String) {
        _uiState.update { it.copy(titular = texto.take(60), error = null) }
    }

    fun onExpiracion(texto: String) {
        _uiState.update { it.copy(expiracion = texto.filter { c -> c.isDigit() }.take(4), error = null) }
    }

    fun onCvv(texto: String) {
        _uiState.update { it.copy(cvv = texto.filter { c -> c.isDigit() }.take(4), error = null) }
    }

    fun pagar(factura: Factura) {
        val estado = _uiState.value
        if (estado.isProcessing) return
        val mes = estado.expiracion.take(2).toIntOrNull() ?: 0
        val anio = estado.expiracion.drop(2).toIntOrNull() ?: 0
        val tarjeta = TarjetaPago(estado.numero, estado.titular, mes, anio, estado.cvv)
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, error = null) }
            processSimulatedPaymentUseCase(factura, tarjeta).fold(
                onSuccess = { pago -> _uiState.update { it.copy(isProcessing = false, resultado = pago) } },
                onFailure = { e -> _uiState.update { it.copy(isProcessing = false, error = e.message) } }
            )
        }
    }

    fun reiniciar() {
        _uiState.value = PagoUiState()
    }
}
KT

crear "$U/screens/pago/PagoScreen.kt" <<'KT'
package __PKG__.ui.screens.pago

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import __PKG__.domain.utils.comoMoneda
import __PKG__.ui.components.common.MessageState
import __PKG__.ui.screens.pedidos.PedidosViewModel

@Composable
fun PagoScreen(
    idFactura: Long,
    pedidosViewModel: PedidosViewModel,
    pagoViewModel: PagoViewModel,
    onFinalizado: () -> Unit
) {
    val pedidos by pedidosViewModel.uiState.collectAsState()
    val uiState by pagoViewModel.uiState.collectAsState()
    val factura = pedidos.facturas.firstOrNull { it.id == idFactura }

    DisposableEffect(Unit) { onDispose { pagoViewModel.reiniciar() } }
    LaunchedEffect(idFactura) {
        if (pedidosViewModel.factura(idFactura) == null) pedidosViewModel.cargar()
    }

    if (factura == null) {
        MessageState(Icons.Outlined.SearchOff, "Cargando factura…", "Si no aparece, revisa la pestaña Facturas en Pedidos.")
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(20.dp)) {
                Row {
                    Text(uiState.marca.etiqueta, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    Icon(Icons.Rounded.CreditCard, contentDescription = null)
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    uiState.numero.padEnd(16, '•').chunked(4).joinToString(" "),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(12.dp))
                Row {
                    Text(uiState.titular.ifBlank { "NOMBRE DEL TITULAR" }.uppercase(), modifier = Modifier.weight(1f))
                    Text(uiState.expiracion.padEnd(4, '•').chunked(2).joinToString("/"))
                }
            }
        }

        Text("Factura ${factura.numero}", style = MaterialTheme.typography.titleMedium)
        Text(
            "Saldo a pagar: ${factura.saldoPendiente.comoMoneda()}",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )

        OutlinedTextField(
            value = uiState.numero,
            onValueChange = pagoViewModel::onNumero,
            label = { Text("Número de tarjeta") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.titular,
            onValueChange = pagoViewModel::onTitular,
            label = { Text("Nombre del titular") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = uiState.expiracion,
                onValueChange = pagoViewModel::onExpiracion,
                label = { Text("MMAA") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = uiState.cvv,
                onValueChange = pagoViewModel::onCvv,
                label = { Text("CVV") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        uiState.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Button(
            onClick = { pagoViewModel.pagar(factura) },
            enabled = !uiState.isProcessing,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isProcessing) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Rounded.Lock, contentDescription = null)
                Text("  Pagar ${factura.saldoPendiente.comoMoneda()}")
            }
        }

        Text(
            "Pago simulado con fines académicos. No se almacena el número de tarjeta, solo sus últimos 4 dígitos. " +
                "Prueba con 4242 4242 4242 4242 (aprobada) o 4000 0000 0000 0002 (rechazada).",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    uiState.resultado?.let { pago ->
        AlertDialog(
            onDismissRequest = {},
            title = { Text("¡Pago aprobado!") },
            text = {
                Text("Referencia ${pago.referencia}\nEstado de la factura: ${pago.estadoFactura.etiqueta}\nSaldo pendiente: ${pago.saldoPendiente.comoMoneda()}")
            },
            confirmButton = {
                Button(onClick = {
                    pedidosViewModel.cargar()
                    onFinalizado()
                }) { Text("Listo") }
            }
        )
    }
}
KT

# ============================================================
# SCREENS / COTIZACIONES
# ============================================================
crear "$U/screens/cotizaciones/CotizacionesViewModel.kt" <<'KT'
package __PKG__.ui.screens.cotizaciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import __PKG__.domain.models.ItemCotizacion
import __PKG__.domain.models.Servicio
import __PKG__.domain.usecases.catalogo.GetServiciosUseCase
import __PKG__.domain.usecases.cotizaciones.GetMisCotizacionesUseCase
import __PKG__.domain.usecases.cotizaciones.RequestAdvisoryUseCase
import __PKG__.domain.usecases.cotizaciones.RequestQuoteUseCase
import __PKG__.ui.classes.CotizacionesUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CotizacionesViewModel(
    private val getServiciosUseCase: GetServiciosUseCase,
    private val requestQuoteUseCase: RequestQuoteUseCase,
    private val requestAdvisoryUseCase: RequestAdvisoryUseCase,
    private val getMisCotizacionesUseCase: GetMisCotizacionesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CotizacionesUiState())
    val uiState: StateFlow<CotizacionesUiState> = _uiState.asStateFlow()

    init {
        cargarServicios()
    }

    fun cargarServicios() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getServiciosUseCase().fold(
                onSuccess = { lista -> _uiState.update { it.copy(isLoading = false, servicios = lista) } },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
            )
        }
    }

    fun cargarMisCotizaciones() {
        viewModelScope.launch {
            getMisCotizacionesUseCase().onSuccess { lista -> _uiState.update { it.copy(misCotizaciones = lista) } }
        }
    }

    fun agregarServicio(servicio: Servicio) {
        _uiState.update { estado ->
            if (estado.seleccion.any { it.servicio.id == servicio.id }) estado
            else estado.copy(seleccion = estado.seleccion + ItemCotizacion(servicio))
        }
    }

    fun cambiarCantidad(idServicio: Long, delta: Int) {
        _uiState.update { estado ->
            estado.copy(seleccion = estado.seleccion.mapNotNull { item ->
                if (item.servicio.id != idServicio) item
                else (item.cantidad + delta).takeIf { it >= 1.0 }?.let { item.copy(cantidad = it) }
            })
        }
    }

    fun onObservaciones(texto: String) = _uiState.update { it.copy(observaciones = texto) }
    fun onTema(texto: String) = _uiState.update { it.copy(temaAsesoria = texto) }
    fun onDetalle(texto: String) = _uiState.update { it.copy(detalleAsesoria = texto) }
    fun onHorario(texto: String) = _uiState.update { it.copy(horarioAsesoria = texto) }

    fun solicitarCotizacion() {
        val estado = _uiState.value
        if (estado.isSending) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true) }
            requestQuoteUseCase(estado.seleccion, estado.observaciones).fold(
                onSuccess = { solicitud ->
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            seleccion = emptyList(),
                            observaciones = "",
                            whatsAppIntent = solicitud.intent,
                            mensaje = solicitud.codigoReferencia?.let { codigo -> "Cotización $codigo registrada" }
                        )
                    }
                    cargarMisCotizaciones()
                },
                onFailure = { e -> _uiState.update { it.copy(isSending = false, mensaje = e.message) } }
            )
        }
    }

    fun solicitarAsesoria() {
        val estado = _uiState.value
        if (estado.isSending) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true) }
            requestAdvisoryUseCase(estado.temaAsesoria, estado.detalleAsesoria, estado.horarioAsesoria).fold(
                onSuccess = { solicitud ->
                    _uiState.update {
                        it.copy(isSending = false, temaAsesoria = "", detalleAsesoria = "", horarioAsesoria = "", whatsAppIntent = solicitud.intent)
                    }
                },
                onFailure = { e -> _uiState.update { it.copy(isSending = false, mensaje = e.message) } }
            )
        }
    }

    fun consumirIntent() = _uiState.update { it.copy(whatsAppIntent = null) }
    fun consumirMensaje() = _uiState.update { it.copy(mensaje = null) }
}
KT

crear "$U/screens/cotizaciones/CotizacionesScreen.kt" <<'KT'
package __PKG__.ui.screens.cotizaciones

import android.content.ActivityNotFoundException
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RequestQuote
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import __PKG__.domain.utils.comoCantidad
import __PKG__.domain.utils.comoFecha
import __PKG__.domain.utils.comoMoneda
import __PKG__.ui.components.common.ForgeCard
import __PKG__.ui.components.common.LoadingState
import __PKG__.ui.components.common.MessageState
import __PKG__.ui.components.common.StatusChip
import __PKG__.ui.components.helpers.color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CotizacionesScreen(
    cotizacionesViewModel: CotizacionesViewModel,
    isLoggedIn: Boolean,
    snackbarHostState: SnackbarHostState,
    pestanaInicial: Int,
    onRequireLogin: () -> Unit
) {
    val uiState by cotizacionesViewModel.uiState.collectAsState()
    val context = LocalContext.current
    var pestana by rememberSaveable(pestanaInicial) { mutableIntStateOf(pestanaInicial) }

    LaunchedEffect(isLoggedIn) { if (isLoggedIn) cotizacionesViewModel.cargarMisCotizaciones() }

    LaunchedEffect(uiState.whatsAppIntent) {
        uiState.whatsAppIntent?.let { intent ->
            try {
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                snackbarHostState.showSnackbar("No se encontró una aplicación para abrir WhatsApp.")
            }
            cotizacionesViewModel.consumirIntent()
        }
    }

    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            cotizacionesViewModel.consumirMensaje()
        }
    }

    val enviar: (() -> Unit) -> Unit = { accion -> if (isLoggedIn) accion() else onRequireLogin() }

    Column(Modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = pestana) {
            listOf("Cotizar", "Asesoría", "Historial").forEachIndexed { indice, titulo ->
                Tab(selected = pestana == indice, onClick = { pestana = indice }, text = { Text(titulo) })
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (pestana) {
                0 -> {
                    item {
                        Text(
                            "Arma tu cotización con nuestros servicios o describe tu proyecto. La enviaremos por WhatsApp a un asesor.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    items(uiState.seleccion, key = { it.servicio.id }) { item ->
                        ForgeCard(Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(item.servicio.nombre, style = MaterialTheme.typography.titleSmall)
                                    Text(item.subtotalEstimado.comoMoneda(), color = MaterialTheme.colorScheme.primary)
                                }
                                FilledTonalIconButton(onClick = { cotizacionesViewModel.cambiarCantidad(item.servicio.id, -1) }) {
                                    Icon(Icons.Rounded.Remove, contentDescription = "Menos")
                                }
                                Text(item.cantidad.comoCantidad(), style = MaterialTheme.typography.titleMedium)
                                FilledTonalIconButton(onClick = { cotizacionesViewModel.cambiarCantidad(item.servicio.id, 1) }) {
                                    Icon(Icons.Rounded.Add, contentDescription = "Más")
                                }
                            }
                        }
                    }
                    item {
                        Text("Agregar servicios", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(4.dp))
                        if (uiState.isLoading) LoadingState(Modifier.height(80.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            uiState.servicios
                                .filter { s -> uiState.seleccion.none { it.servicio.id == s.id } }
                                .forEach { servicio ->
                                    AssistChip(
                                        onClick = { cotizacionesViewModel.agregarServicio(servicio) },
                                        label = { Text("${servicio.nombre} · ${servicio.precioBase.comoMoneda()}") },
                                        leadingIcon = { Icon(Icons.Rounded.Add, contentDescription = null) }
                                    )
                                }
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = uiState.observaciones,
                            onValueChange = cotizacionesViewModel::onObservaciones,
                            label = { Text("Describe tu proyecto, medidas, material o color") },
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        if (uiState.seleccion.isNotEmpty()) {
                            Text(
                                "Estimado: ${uiState.totalEstimado.comoMoneda()} + IVA",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                        BotonWhatsApp("Solicitar cotización", uiState.isSending) {
                            enviar(cotizacionesViewModel::solicitarCotizacion)
                        }
                    }
                }
                1 -> {
                    item {
                        Text(
                            "Agenda una asesoría con nuestro equipo para elegir material, técnica de impresión o resolver dudas de diseño.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = uiState.temaAsesoria,
                            onValueChange = cotizacionesViewModel::onTema,
                            label = { Text("Tema de la asesoría") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = uiState.detalleAsesoria,
                            onValueChange = cotizacionesViewModel::onDetalle,
                            label = { Text("Detalle (opcional)") },
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = uiState.horarioAsesoria,
                            onValueChange = cotizacionesViewModel::onHorario,
                            label = { Text("Horario preferido (opcional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        BotonWhatsApp("Agendar por WhatsApp", uiState.isSending) {
                            enviar(cotizacionesViewModel::solicitarAsesoria)
                        }
                    }
                }
                else -> {
                    if (!isLoggedIn) {
                        item {
                            MessageState(Icons.Outlined.RequestQuote, "Inicia sesión para ver tus cotizaciones", null, accion = "Iniciar sesión", onAccion = onRequireLogin)
                        }
                    } else if (uiState.misCotizaciones.isEmpty()) {
                        item { MessageState(Icons.Outlined.RequestQuote, "Aún no tienes cotizaciones", null) }
                    }
                    items(uiState.misCotizaciones, key = { it.id }) { cotizacion ->
                        ForgeCard(Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(cotizacion.codigo, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                StatusChip(cotizacion.estado.etiqueta, cotizacion.estado.color())
                            }
                            Text(
                                "Emitida ${cotizacion.fechaEmision.comoFecha()} · vence ${cotizacion.fechaVencimiento.comoFecha()}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            cotizacion.detalles.forEach {
                                Text("• ${it.descripcion} x ${it.cantidad.comoCantidad()}", style = MaterialTheme.typography.bodySmall)
                            }
                            Text("Total ${cotizacion.total.comoMoneda()}", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BotonWhatsApp(texto: String, enviando: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = !enviando, modifier = Modifier.fillMaxWidth()) {
        if (enviando) {
            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
        } else {
            Icon(Icons.Rounded.Chat, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(texto)
        }
    }
}
KT

# ============================================================
# SCREENS / FEEDBACK
# ============================================================
crear "$U/screens/feedback/FeedbackViewModel.kt" <<'KT'
package __PKG__.ui.screens.feedback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import __PKG__.domain.models.PrioridadSoporte
import __PKG__.domain.usecases.catalogo.GetServiciosUseCase
import __PKG__.domain.usecases.encargos.GetMisEncargosUseCase
import __PKG__.domain.usecases.feedback.GetMisResenasUseCase
import __PKG__.domain.usecases.feedback.GetMisSolicitudesSoporteUseCase
import __PKG__.domain.usecases.feedback.SubmitResenaUseCase
import __PKG__.domain.usecases.feedback.SubmitSupportRequestUseCase
import __PKG__.ui.classes.FeedbackUiState
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FeedbackViewModel(
    private val getServiciosUseCase: GetServiciosUseCase,
    private val getMisEncargosUseCase: GetMisEncargosUseCase,
    private val getMisResenasUseCase: GetMisResenasUseCase,
    private val getMisSolicitudesSoporteUseCase: GetMisSolicitudesSoporteUseCase,
    private val submitResenaUseCase: SubmitResenaUseCase,
    private val submitSupportRequestUseCase: SubmitSupportRequestUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedbackUiState())
    val uiState: StateFlow<FeedbackUiState> = _uiState.asStateFlow()

    fun cargar(isLoggedIn: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val servicios = async { getServiciosUseCase() }
            if (isLoggedIn) {
                val encargos = async { getMisEncargosUseCase() }
                val resenas = async { getMisResenasUseCase() }
                val solicitudes = async { getMisSolicitudesSoporteUseCase() }
                _uiState.update {
                    it.copy(
                        encargos = encargos.await().getOrDefault(emptyList()),
                        misResenas = resenas.await().getOrDefault(emptyList()),
                        misSolicitudes = solicitudes.await().getOrDefault(emptyList())
                    )
                }
            }
            _uiState.update { it.copy(isLoading = false, servicios = servicios.await().getOrDefault(emptyList())) }
        }
    }

    fun onPuntuacion(valor: Int) = _uiState.update { it.copy(puntuacion = valor) }
    fun onComentario(texto: String) = _uiState.update { it.copy(comentario = texto) }
    fun onAsunto(texto: String) = _uiState.update { it.copy(asunto = texto) }
    fun onDescripcion(texto: String) = _uiState.update { it.copy(descripcion = texto) }
    fun onPrioridad(valor: PrioridadSoporte) = _uiState.update { it.copy(prioridad = valor) }

    fun seleccionarServicio(id: Long?) = _uiState.update { it.copy(servicioSeleccionado = id, encargoSeleccionado = null) }
    fun seleccionarEncargo(id: Long?) = _uiState.update { it.copy(encargoSeleccionado = id, servicioSeleccionado = null) }

    fun enviarResena() {
        val estado = _uiState.value
        if (estado.isSending) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true) }
            submitResenaUseCase(estado.puntuacion, estado.comentario, estado.servicioSeleccionado, estado.encargoSeleccionado).fold(
                onSuccess = { resena ->
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            puntuacion = 0,
                            comentario = "",
                            misResenas = listOf(resena) + it.misResenas,
                            mensaje = "¡Gracias por tu opinión!"
                        )
                    }
                },
                onFailure = { e -> _uiState.update { it.copy(isSending = false, mensaje = e.message) } }
            )
        }
    }

    fun enviarSoporte() {
        val estado = _uiState.value
        if (estado.isSending) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true) }
            submitSupportRequestUseCase(estado.asunto, estado.descripcion, estado.prioridad).fold(
                onSuccess = { solicitud ->
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            asunto = "",
                            descripcion = "",
                            misSolicitudes = listOf(solicitud) + it.misSolicitudes,
                            mensaje = "Solicitud enviada. Te contactaremos pronto."
                        )
                    }
                },
                onFailure = { e -> _uiState.update { it.copy(isSending = false, mensaje = e.message) } }
            )
        }
    }

    fun consumirMensaje() = _uiState.update { it.copy(mensaje = null) }
}
KT

crear "$U/screens/feedback/FeedbackScreen.kt" <<'KT'
package __PKG__.ui.screens.feedback

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import __PKG__.domain.models.PrioridadSoporte
import __PKG__.domain.utils.comoFecha
import __PKG__.ui.components.common.AuthRequiredState
import __PKG__.ui.components.common.ForgeCard
import __PKG__.ui.components.common.StatusChip

@Composable
fun FeedbackScreen(
    feedbackViewModel: FeedbackViewModel,
    isLoggedIn: Boolean,
    snackbarHostState: SnackbarHostState,
    onRequireLogin: () -> Unit
) {
    val uiState by feedbackViewModel.uiState.collectAsState()

    LaunchedEffect(isLoggedIn) { feedbackViewModel.cargar(isLoggedIn) }
    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            feedbackViewModel.consumirMensaje()
        }
    }

    if (!isLoggedIn) {
        AuthRequiredState(
            titulo = "Tu opinión nos ayuda a mejorar",
            mensaje = "Inicia sesión para calificar nuestros servicios, tus encargos o enviarnos una solicitud de soporte.",
            onLogin = onRequireLogin
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Califica un servicio o encargo", style = MaterialTheme.typography.titleMedium) }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.encargos, key = { "e${it.id}" }) { encargo ->
                    FilterChip(
                        selected = uiState.encargoSeleccionado == encargo.id,
                        onClick = { feedbackViewModel.seleccionarEncargo(encargo.id) },
                        label = { Text(encargo.codigo) }
                    )
                }
                items(uiState.servicios, key = { "s${it.id}" }) { servicio ->
                    FilterChip(
                        selected = uiState.servicioSeleccionado == servicio.id,
                        onClick = { feedbackViewModel.seleccionarServicio(servicio.id) },
                        label = { Text(servicio.nombre) }
                    )
                }
            }
        }
        item {
            Row {
                (1..5).forEach { valor ->
                    IconButton(onClick = { feedbackViewModel.onPuntuacion(valor) }) {
                        Icon(
                            if (valor <= uiState.puntuacion) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                            contentDescription = "$valor estrellas",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
        item {
            OutlinedTextField(
                value = uiState.comentario,
                onValueChange = feedbackViewModel::onComentario,
                label = { Text("Comentario (opcional)") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Button(onClick = feedbackViewModel::enviarResena, enabled = !uiState.isSending, modifier = Modifier.fillMaxWidth()) {
                Text("Enviar calificación")
            }
        }

        item { Text("¿Necesitas ayuda?", style = MaterialTheme.typography.titleMedium) }
        item {
            OutlinedTextField(
                value = uiState.asunto,
                onValueChange = feedbackViewModel::onAsunto,
                label = { Text("Asunto") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = uiState.descripcion,
                onValueChange = feedbackViewModel::onDescripcion,
                label = { Text("Describe tu solicitud, sugerencia o reclamo") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PrioridadSoporte.entries.forEach { prioridad ->
                    FilterChip(
                        selected = uiState.prioridad == prioridad,
                        onClick = { feedbackViewModel.onPrioridad(prioridad) },
                        label = { Text(prioridad.etiqueta) }
                    )
                }
            }
        }
        item {
            Button(onClick = feedbackViewModel::enviarSoporte, enabled = !uiState.isSending, modifier = Modifier.fillMaxWidth()) {
                Text("Enviar solicitud")
            }
        }

        if (uiState.misSolicitudes.isNotEmpty()) {
            item { Text("Mis solicitudes", style = MaterialTheme.typography.titleMedium) }
            items(uiState.misSolicitudes, key = { "sol${it.id}" }) { solicitud ->
                ForgeCard(Modifier.fillMaxWidth()) {
                    Row {
                        Text(solicitud.asunto, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                        StatusChip(solicitud.estado.etiqueta)
                    }
                    Text(solicitud.fecha.comoFecha(), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
KT

# ============================================================
# SCREENS / AUTH
# ============================================================
crear "$U/screens/auth/AuthViewModel.kt" <<'KT'
package __PKG__.ui.screens.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import __PKG__.domain.models.EmailNotVerifiedException
import __PKG__.domain.models.GoogleSignInCancelledException
import __PKG__.domain.models.SignUpResult
import __PKG__.domain.usecases.auth.GetLastEmailUseCase
import __PKG__.domain.usecases.auth.LoginWithEmailUseCase
import __PKG__.domain.usecases.auth.RegisterWithEmailUseCase
import __PKG__.domain.usecases.auth.ResendVerificationCodeUseCase
import __PKG__.domain.usecases.auth.SendPasswordResetUseCase
import __PKG__.domain.usecases.auth.SignInWithGoogleUseCase
import __PKG__.domain.usecases.auth.VerifyEmailCodeUseCase
import __PKG__.ui.classes.AuthUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val loginWithEmailUseCase: LoginWithEmailUseCase,
    private val registerWithEmailUseCase: RegisterWithEmailUseCase,
    private val verifyEmailCodeUseCase: VerifyEmailCodeUseCase,
    private val resendVerificationCodeUseCase: ResendVerificationCodeUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val sendPasswordResetUseCase: SendPasswordResetUseCase,
    getLastEmailUseCase: GetLastEmailUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    val ultimoCorreo: Flow<String?> = getLastEmailUseCase()

    private fun ejecutar(bloque: suspend () -> Unit) {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, mensaje = null) }
            bloque()
        }
    }

    private fun fallo(e: Throwable) {
        when (e) {
            is EmailNotVerifiedException ->
                _uiState.update { it.copy(isLoading = false, correoVerificacion = e.correo, mensaje = e.message) }
            is GoogleSignInCancelledException -> _uiState.update { it.copy(isLoading = false) }
            else -> _uiState.update { it.copy(isLoading = false, error = e.message) }
        }
    }

    fun login(correo: String, clave: String) = ejecutar {
        loginWithEmailUseCase(correo, clave).fold(
            onSuccess = { _uiState.update { it.copy(isLoading = false, autenticado = true) } },
            onFailure = ::fallo
        )
    }

    fun loginGoogle(activityContext: Context) = ejecutar {
        signInWithGoogleUseCase(activityContext).fold(
            onSuccess = { _uiState.update { it.copy(isLoading = false, autenticado = true) } },
            onFailure = ::fallo
        )
    }

    fun registrar(nombres: String, apellidos: String, correo: String, clave: String, confirmacion: String) = ejecutar {
        registerWithEmailUseCase(nombres, apellidos, correo, clave, confirmacion).fold(
            onSuccess = { resultado ->
                when (resultado) {
                    is SignUpResult.VerificationRequired -> _uiState.update {
                        it.copy(isLoading = false, correoVerificacion = resultado.correo)
                    }
                    is SignUpResult.SignedIn -> _uiState.update { it.copy(isLoading = false, autenticado = true) }
                }
            },
            onFailure = ::fallo
        )
    }

    fun verificar(correo: String, codigo: String) = ejecutar {
        verifyEmailCodeUseCase(correo, codigo).fold(
            onSuccess = { _uiState.update { it.copy(isLoading = false, autenticado = true) } },
            onFailure = ::fallo
        )
    }

    fun reenviarCodigo(correo: String) = ejecutar {
        resendVerificationCodeUseCase(correo).fold(
            onSuccess = { _uiState.update { it.copy(isLoading = false, mensaje = "Te enviamos un nuevo código.") } },
            onFailure = ::fallo
        )
    }

    fun recuperarClave(correo: String) = ejecutar {
        sendPasswordResetUseCase(correo).fold(
            onSuccess = { _uiState.update { it.copy(isLoading = false, mensaje = "Revisa tu correo para restablecer la contraseña.") } },
            onFailure = ::fallo
        )
    }

    fun consumirNavegacion() {
        _uiState.update { it.copy(correoVerificacion = null, autenticado = false) }
    }

    fun limpiarMensajes() {
        _uiState.update { it.copy(error = null, mensaje = null) }
    }
}
KT

crear "$U/screens/auth/AuthComponents.kt" <<'KT'
package __PKG__.ui.screens.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun CampoClave(valor: String, onCambio: (String) -> Unit, etiqueta: String) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        singleLine = true,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(if (visible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, contentDescription = "Mostrar contraseña")
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun BotonGoogle(cargando: Boolean, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, enabled = !cargando, modifier = Modifier.fillMaxWidth()) {
        if (cargando) {
            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
        } else {
            Icon(Icons.Rounded.AccountCircle, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Continuar con Google")
        }
    }
}
KT

crear "$U/screens/auth/LoginScreen.kt" <<'KT'
package __PKG__.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ViewInAr
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onAutenticado: () -> Unit,
    onVerificar: (String) -> Unit,
    onRegistro: () -> Unit
) {
    val uiState by authViewModel.uiState.collectAsState()
    val ultimoCorreo by authViewModel.ultimoCorreo.collectAsState(initial = null)
    val context = LocalContext.current
    var correo by rememberSaveable { mutableStateOf("") }
    var clave by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(ultimoCorreo) { if (correo.isBlank()) ultimoCorreo?.let { correo = it } }
    LaunchedEffect(uiState.autenticado, uiState.correoVerificacion) {
        when {
            uiState.autenticado -> { authViewModel.consumirNavegacion(); onAutenticado() }
            uiState.correoVerificacion != null -> {
                val destino = uiState.correoVerificacion.orEmpty()
                authViewModel.consumirNavegacion()
                onVerificar(destino)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(Icons.Rounded.ViewInAr, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(72.dp))
        Text("Bienvenido de nuevo", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Inicia sesión para cotizar, hacer pedidos y dar seguimiento a tus encargos.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        CampoClave(clave, { clave = it }, "Contraseña")
        TextButton(onClick = { authViewModel.recuperarClave(correo) }, modifier = Modifier.align(Alignment.End)) {
            Text("¿Olvidaste tu contraseña?")
        }
        uiState.error?.let { Text(it, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center) }
        uiState.mensaje?.let { Text(it, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center) }
        Button(
            onClick = { authViewModel.login(correo, clave) },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isLoading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            else Text("Iniciar sesión")
        }
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        BotonGoogle(uiState.isLoading) { authViewModel.loginGoogle(context) }
        TextButton(onClick = onRegistro) { Text("¿No tienes cuenta? Regístrate") }
    }
}
KT

crear "$U/screens/auth/RegisterScreen.kt" <<'KT'
package __PKG__.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onAutenticado: () -> Unit,
    onVerificar: (String) -> Unit,
    onLogin: () -> Unit
) {
    val uiState by authViewModel.uiState.collectAsState()
    val context = LocalContext.current
    var nombres by rememberSaveable { mutableStateOf("") }
    var apellidos by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var clave by rememberSaveable { mutableStateOf("") }
    var confirmacion by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(uiState.autenticado, uiState.correoVerificacion) {
        when {
            uiState.autenticado -> { authViewModel.consumirNavegacion(); onAutenticado() }
            uiState.correoVerificacion != null -> {
                val destino = uiState.correoVerificacion.orEmpty()
                authViewModel.consumirNavegacion()
                onVerificar(destino)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Crea tu cuenta", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Te enviaremos un código a tu correo para verificar tu cuenta.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(nombres, { nombres = it }, label = { Text("Nombres") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(apellidos, { apellidos = it }, label = { Text("Apellidos") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        CampoClave(clave, { clave = it }, "Contraseña (mín. 8, letras y números)")
        CampoClave(confirmacion, { confirmacion = it }, "Confirmar contraseña")
        uiState.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(
            onClick = { authViewModel.registrar(nombres, apellidos, correo, clave, confirmacion) },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isLoading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            else Text("Registrarme")
        }
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        BotonGoogle(uiState.isLoading) { authViewModel.loginGoogle(context) }
        TextButton(onClick = onLogin, modifier = Modifier.fillMaxWidth()) { Text("¿Ya tienes cuenta? Inicia sesión") }
    }
}
KT

crear "$U/screens/auth/VerifyEmailScreen.kt" <<'KT'
package __PKG__.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun VerifyEmailScreen(
    authViewModel: AuthViewModel,
    correo: String,
    onVerificado: () -> Unit
) {
    val uiState by authViewModel.uiState.collectAsState()
    var codigo by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(uiState.autenticado) {
        if (uiState.autenticado) {
            authViewModel.consumirNavegacion()
            onVerificado()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(Icons.Outlined.MarkEmailRead, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(72.dp))
        Text("Verifica tu correo", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Ingresa el código que enviamos a $correo",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        OutlinedTextField(
            value = codigo,
            onValueChange = { codigo = it.filter(Char::isDigit).take(10) },
            label = { Text("Código de verificación") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth()
        )
        uiState.error?.let { Text(it, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center) }
        uiState.mensaje?.let { Text(it, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center) }
        Button(
            onClick = { authViewModel.verificar(correo, codigo) },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isLoading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            else Text("Verificar")
        }
        TextButton(onClick = { authViewModel.reenviarCodigo(correo) }) { Text("Reenviar código") }
    }
}
KT

# ============================================================
# SCREENS / PERFIL
# ============================================================
crear "$U/screens/perfil/PerfilViewModel.kt" <<'KT'
package __PKG__.ui.screens.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import __PKG__.domain.models.Cliente
import __PKG__.domain.models.ClienteUpdate
import __PKG__.domain.models.TipoCliente
import __PKG__.domain.usecases.auth.LogoutUseCase
import __PKG__.domain.usecases.perfil.GetClienteProfileUseCase
import __PKG__.domain.usecases.perfil.UpdateClienteProfileUseCase
import __PKG__.ui.classes.PerfilUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PerfilViewModel(
    private val getClienteProfileUseCase: GetClienteProfileUseCase,
    private val updateClienteProfileUseCase: UpdateClienteProfileUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getClienteProfileUseCase().fold(
                onSuccess = ::aplicar,
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
            )
        }
    }

    private fun aplicar(cliente: Cliente) {
        _uiState.update {
            it.copy(
                isLoading = false,
                isSaving = false,
                correo = cliente.correo,
                tipo = cliente.tipo,
                nombres = cliente.nombres.orEmpty(),
                apellidos = cliente.apellidos.orEmpty(),
                nombreEmpresa = cliente.nombreEmpresa.orEmpty(),
                telefono = cliente.telefono.orEmpty(),
                identificacionFiscal = cliente.identificacionFiscal.orEmpty(),
                direccion = cliente.direccion.orEmpty(),
                perfilCompleto = cliente.perfilCompleto
            )
        }
    }

    fun onTipo(valor: TipoCliente) = _uiState.update { it.copy(tipo = valor) }
    fun onNombres(valor: String) = _uiState.update { it.copy(nombres = valor) }
    fun onApellidos(valor: String) = _uiState.update { it.copy(apellidos = valor) }
    fun onEmpresa(valor: String) = _uiState.update { it.copy(nombreEmpresa = valor) }
    fun onTelefono(valor: String) = _uiState.update { it.copy(telefono = valor) }
    fun onIdentificacion(valor: String) = _uiState.update { it.copy(identificacionFiscal = valor) }
    fun onDireccion(valor: String) = _uiState.update { it.copy(direccion = valor) }

    fun guardar() {
        val e = _uiState.value
        if (e.isSaving) return
        val cambios = ClienteUpdate(e.tipo, e.nombres, e.apellidos, e.nombreEmpresa, e.telefono, e.identificacionFiscal, e.direccion)
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, mensaje = null) }
            updateClienteProfileUseCase(cambios).fold(
                onSuccess = { cliente ->
                    aplicar(cliente)
                    _uiState.update { it.copy(mensaje = "Perfil actualizado") }
                },
                onFailure = { ex -> _uiState.update { it.copy(isSaving = false, mensaje = ex.message) } }
            )
        }
    }

    fun cerrarSesion(onListo: () -> Unit) {
        viewModelScope.launch {
            logoutUseCase()
            _uiState.value = PerfilUiState()
            onListo()
        }
    }

    fun consumirMensaje() = _uiState.update { it.copy(mensaje = null) }
}
KT

crear "$U/screens/perfil/PerfilScreen.kt" <<'KT'
package __PKG__.ui.screens.perfil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import __PKG__.domain.models.AuthUser
import __PKG__.domain.models.TipoCliente
import __PKG__.ui.components.common.ErrorState
import __PKG__.ui.components.common.LoadingState
import __PKG__.ui.components.common.StatusChip
import __PKG__.ui.components.common.UserAvatar

@Composable
fun PerfilScreen(
    perfilViewModel: PerfilViewModel,
    usuario: AuthUser?,
    avatarUrl: String?,
    snackbarHostState: SnackbarHostState,
    onLogout: () -> Unit
) {
    val uiState by perfilViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { perfilViewModel.cargar() }
    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            perfilViewModel.consumirMensaje()
        }
    }

    val error = uiState.error
    if (uiState.isLoading) { LoadingState(); return }
    if (error != null && uiState.correo.isBlank()) { ErrorState(error, perfilViewModel::cargar); return }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            UserAvatar(avatarUrl = avatarUrl, iniciales = usuario?.iniciales, size = 72.dp)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(usuario?.nombreVisible.orEmpty(), style = MaterialTheme.typography.titleLarge)
                Text(uiState.correo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.size(4.dp))
                StatusChip(if (uiState.perfilCompleto) "Perfil completo" else "Completa tus datos para tus pedidos")
            }
        }

        Text("Datos para pedidos y facturación", style = MaterialTheme.typography.titleMedium)
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            TipoCliente.entries.forEachIndexed { indice, tipo ->
                SegmentedButton(
                    selected = uiState.tipo == tipo,
                    onClick = { perfilViewModel.onTipo(tipo) },
                    shape = SegmentedButtonDefaults.itemShape(indice, TipoCliente.entries.size)
                ) { Text(tipo.etiqueta) }
            }
        }
        if (uiState.tipo == TipoCliente.EMPRESA) {
            Campo(uiState.nombreEmpresa, perfilViewModel::onEmpresa, "Nombre de la empresa")
        }
        Campo(uiState.nombres, perfilViewModel::onNombres, "Nombres")
        Campo(uiState.apellidos, perfilViewModel::onApellidos, "Apellidos")
        Campo(uiState.telefono, perfilViewModel::onTelefono, "Teléfono", KeyboardType.Phone)
        Campo(uiState.identificacionFiscal, perfilViewModel::onIdentificacion, "DUI / NIT (opcional)")
        OutlinedTextField(
            value = uiState.direccion,
            onValueChange = perfilViewModel::onDireccion,
            label = { Text("Dirección de entrega") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = perfilViewModel::guardar, enabled = !uiState.isSaving, modifier = Modifier.fillMaxWidth()) {
            if (uiState.isSaving) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            else Text("Guardar cambios")
        }
        OutlinedButton(onClick = { perfilViewModel.cerrarSesion(onLogout) }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Cerrar sesión")
        }
    }
}

@Composable
private fun Campo(valor: String, onCambio: (String) -> Unit, etiqueta: String, teclado: KeyboardType = KeyboardType.Text) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        modifier = Modifier.fillMaxWidth()
    )
}
KT

# ============================================================
# VIEWMODELS HOLDER & NAVHOST
# ============================================================
crear "$U/viewmodels/ForgeViewModels.kt" <<'KT'
package __PKG__.ui.viewmodels

import __PKG__.ui.screens.auth.AuthViewModel
import __PKG__.ui.screens.cotizaciones.CotizacionesViewModel
import __PKG__.ui.screens.feedback.FeedbackViewModel
import __PKG__.ui.screens.home.HomeViewModel
import __PKG__.ui.screens.pago.PagoViewModel
import __PKG__.ui.screens.pedidos.PedidosViewModel
import __PKG__.ui.screens.perfil.PerfilViewModel
import __PKG__.ui.screens.productos.ProductosViewModel
import __PKG__.ui.viewmodels.main.MainViewModel

data class ForgeViewModels(
    val main: MainViewModel,
    val home: HomeViewModel,
    val productos: ProductosViewModel,
    val pedidos: PedidosViewModel,
    val pago: PagoViewModel,
    val cotizaciones: CotizacionesViewModel,
    val feedback: FeedbackViewModel,
    val auth: AuthViewModel,
    val perfil: PerfilViewModel
)
KT

crear "$U/navigation/AppNavHost.kt" <<'KT'
package __PKG__.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import __PKG__.ui.components.bottombar.ForgeBottomBar
import __PKG__.ui.components.topbar.BackTopBar
import __PKG__.ui.components.topbar.ForgeTopBar
import __PKG__.ui.screens.auth.LoginScreen
import __PKG__.ui.screens.auth.RegisterScreen
import __PKG__.ui.screens.auth.VerifyEmailScreen
import __PKG__.ui.screens.cotizaciones.CotizacionesScreen
import __PKG__.ui.screens.feedback.FeedbackScreen
import __PKG__.ui.screens.home.HomeScreen
import __PKG__.ui.screens.pago.PagoScreen
import __PKG__.ui.screens.pedidos.EncargoDetalleScreen
import __PKG__.ui.screens.pedidos.PedidosScreen
import __PKG__.ui.screens.perfil.PerfilScreen
import __PKG__.ui.screens.productos.ProductosScreen
import __PKG__.ui.screens.splash.SplashScreen
import __PKG__.ui.viewmodels.ForgeViewModels

private val rutasPrincipales = bottomItems.map { it.screen.route }.toSet()

private val titulos = mapOf(
    Screen.Login.route to "Iniciar sesión",
    Screen.Registro.route to "Registro",
    Screen.Verificar.route to "Verificación",
    Screen.Perfil.route to "Mi perfil",
    Screen.Pago.route to "Pago en línea",
    Screen.EncargoDetalle.route to "Detalle del encargo"
)

@Composable
fun AppNavHost(
    navController: NavHostController,
    viewModels: ForgeViewModels
) {
    val mainUiState by viewModels.main.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val entrada by navController.currentBackStackEntryAsState()
    val rutaActual = entrada?.destination?.route
    var pestanaCotizacion by rememberSaveable { mutableIntStateOf(0) }

    val irA: (Screen) -> Unit = { destino ->
        navController.navigate(destino.route) {
            popUpTo(Screen.Home.route) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
    val requerirLogin: () -> Unit = { navController.navigate(Screen.Login.route) { launchSingleTop = true } }
    val salirDeAuth: () -> Unit = {
        if (!navController.popBackStack(Screen.Login.route, inclusive = true) &&
            !navController.popBackStack(Screen.Registro.route, inclusive = true)
        ) {
            irA(Screen.Home)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            when (rutaActual) {
                null, Screen.Splash.route -> Unit
                in rutasPrincipales -> ForgeTopBar(
                    usuario = mainUiState.usuario,
                    avatarUrl = mainUiState.avatarUrl,
                    onLogoClick = { irA(Screen.Home) },
                    onAvatarClick = { if (mainUiState.isLoggedIn) navController.navigate(Screen.Perfil.route) else requerirLogin() }
                )
                else -> BackTopBar(titulos[rutaActual].orEmpty()) { navController.popBackStack() }
            }
        },
        bottomBar = {
            if (rutaActual in rutasPrincipales) ForgeBottomBar(rutaActual, irA)
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            NavHost(navController = navController, startDestination = Screen.Splash.route) {

                composable(Screen.Splash.route) {
                    SplashScreen(authState = mainUiState.authState) {
                        navController.navigate(Screen.Home.route) { popUpTo(0) { inclusive = true } }
                    }
                }

                composable(Screen.Home.route) {
                    HomeScreen(
                        homeViewModel = viewModels.home,
                        usuario = mainUiState.usuario,
                        onCotizarServicio = { servicio ->
                            viewModels.cotizaciones.agregarServicio(servicio)
                            pestanaCotizacion = 0
                            irA(Screen.Cotizaciones)
                        },
                        onAsesoria = {
                            pestanaCotizacion = 1
                            irA(Screen.Cotizaciones)
                        },
                        onVerProductos = { irA(Screen.Productos) }
                    )
                }

                composable(Screen.Productos.route) {
                    ProductosScreen(
                        productosViewModel = viewModels.productos,
                        isLoggedIn = mainUiState.isLoggedIn,
                        snackbarHostState = snackbarHostState,
                        onRequireLogin = requerirLogin,
                        onPagar = { idFactura ->
                            viewModels.pedidos.cargar()
                            navController.navigate(Screen.Pago.crear(idFactura))
                        },
                        onVerPedidos = { irA(Screen.Pedidos) }
                    )
                }

                composable(Screen.Pedidos.route) {
                    PedidosScreen(
                        pedidosViewModel = viewModels.pedidos,
                        isLoggedIn = mainUiState.isLoggedIn,
                        onRequireLogin = requerirLogin,
                        onEncargo = { navController.navigate(Screen.EncargoDetalle.crear(it)) },
                        onPagar = { navController.navigate(Screen.Pago.crear(it)) },
                        onIrProductos = { irA(Screen.Productos) }
                    )
                }

                composable(Screen.Cotizaciones.route) {
                    CotizacionesScreen(
                        cotizacionesViewModel = viewModels.cotizaciones,
                        isLoggedIn = mainUiState.isLoggedIn,
                        snackbarHostState = snackbarHostState,
                        pestanaInicial = pestanaCotizacion,
                        onRequireLogin = requerirLogin
                    )
                }

                composable(Screen.Feedback.route) {
                    FeedbackScreen(
                        feedbackViewModel = viewModels.feedback,
                        isLoggedIn = mainUiState.isLoggedIn,
                        snackbarHostState = snackbarHostState,
                        onRequireLogin = requerirLogin
                    )
                }

                composable(Screen.Login.route) {
                    LoginScreen(
                        authViewModel = viewModels.auth,
                        onAutenticado = salirDeAuth,
                        onVerificar = { navController.navigate(Screen.Verificar.crear(it)) },
                        onRegistro = { navController.navigate(Screen.Registro.route) }
                    )
                }

                composable(Screen.Registro.route) {
                    RegisterScreen(
                        authViewModel = viewModels.auth,
                        onAutenticado = salirDeAuth,
                        onVerificar = { navController.navigate(Screen.Verificar.crear(it)) },
                        onLogin = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.Verificar.route,
                    arguments = listOf(navArgument("correo") { type = NavType.StringType })
                ) { backStack ->
                    VerifyEmailScreen(
                        authViewModel = viewModels.auth,
                        correo = backStack.arguments?.getString("correo").orEmpty(),
                        onVerificado = salirDeAuth
                    )
                }

                composable(Screen.Perfil.route) {
                    PerfilScreen(
                        perfilViewModel = viewModels.perfil,
                        usuario = mainUiState.usuario,
                        avatarUrl = mainUiState.avatarUrl,
                        snackbarHostState = snackbarHostState,
                        onLogout = {
                            viewModels.pedidos.limpiar()
                            navController.navigate(Screen.Home.route) { popUpTo(0) { inclusive = true } }
                        }
                    )
                }

                composable(
                    route = Screen.Pago.route,
                    arguments = listOf(navArgument("idFactura") { type = NavType.LongType })
                ) { backStack ->
                    PagoScreen(
                        idFactura = backStack.arguments?.getLong("idFactura") ?: 0L,
                        pedidosViewModel = viewModels.pedidos,
                        pagoViewModel = viewModels.pago,
                        onFinalizado = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.EncargoDetalle.route,
                    arguments = listOf(navArgument("idEncargo") { type = NavType.LongType })
                ) { backStack ->
                    EncargoDetalleScreen(
                        pedidosViewModel = viewModels.pedidos,
                        idEncargo = backStack.arguments?.getLong("idEncargo") ?: 0L
                    )
                }
            }
        }
    }
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
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import __PKG__.data.di.AppContainer
import __PKG__.ui.navigation.AppNavHost
import __PKG__.ui.screens.auth.AuthViewModel
import __PKG__.ui.screens.cotizaciones.CotizacionesViewModel
import __PKG__.ui.screens.feedback.FeedbackViewModel
import __PKG__.ui.screens.home.HomeViewModel
import __PKG__.ui.screens.pago.PagoViewModel
import __PKG__.ui.screens.pedidos.PedidosViewModel
import __PKG__.ui.screens.perfil.PerfilViewModel
import __PKG__.ui.screens.productos.ProductosViewModel
import __PKG__.ui.theme.ForgeStudioTheme
import __PKG__.ui.viewmodels.ForgeViewModelFactory
import __PKG__.ui.viewmodels.ForgeViewModels
import __PKG__.ui.viewmodels.main.MainViewModel

class MainActivity : ComponentActivity() {

    private inline fun <reified VM : ViewModel> crearViewModel(noinline crear: () -> VM): VM =
        ViewModelProvider(this, ForgeViewModelFactory(crear))[VM::class.java]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppContainer.initialize(applicationContext)
        val c = AppContainer

        val viewModels = ForgeViewModels(
            main = crearViewModel {
                MainViewModel(c.observeAuthStateUseCase, c.observeClienteProfileUseCase, c.getClienteProfileUseCase)
            },
            home = crearViewModel {
                HomeViewModel(c.getNegocioInfoUseCase, c.getPromocionesActivasUseCase, c.getCategoriasServiciosUseCase, c.getServiciosUseCase)
            },
            productos = crearViewModel { ProductosViewModel(c.getProductosUseCase, c.createProductOrderUseCase) },
            pedidos = crearViewModel { PedidosViewModel(c.getMisEncargosUseCase, c.getMisFacturasUseCase, c.getMisPagosUseCase) },
            pago = crearViewModel { PagoViewModel(c.processSimulatedPaymentUseCase) },
            cotizaciones = crearViewModel {
                CotizacionesViewModel(c.getServiciosUseCase, c.requestQuoteUseCase, c.requestAdvisoryUseCase, c.getMisCotizacionesUseCase)
            },
            feedback = crearViewModel {
                FeedbackViewModel(
                    c.getServiciosUseCase, c.getMisEncargosUseCase, c.getMisResenasUseCase,
                    c.getMisSolicitudesSoporteUseCase, c.submitResenaUseCase, c.submitSupportRequestUseCase
                )
            },
            auth = crearViewModel {
                AuthViewModel(
                    c.loginWithEmailUseCase, c.registerWithEmailUseCase, c.verifyEmailCodeUseCase,
                    c.resendVerificationCodeUseCase, c.signInWithGoogleUseCase, c.sendPasswordResetUseCase,
                    c.getLastEmailUseCase
                )
            },
            perfil = crearViewModel { PerfilViewModel(c.getClienteProfileUseCase, c.updateClienteProfileUseCase, c.logoutUseCase) }
        )

        enableEdgeToEdge()
        setContent {
            ForgeStudioTheme {
                ForgeStudioApplication(viewModels)
            }
        }
    }
}

@Composable
fun ForgeStudioApplication(viewModels: ForgeViewModels) {
    val navController = rememberNavController()
    AppNavHost(navController = navController, viewModels = viewModels)
}
KT

# ============================================================
# GRADLE
# ============================================================
info "Configurando dependencias de UI ..."
sed -i '/\/\/ >>> 3DFORGE UI >>>/,/\/\/ <<< 3DFORGE UI <<</d' "$APP_GRADLE"
sed -e "s/__NAV__/${NAVIGATION_VERSION}/g" \
    -e "s/__LIFECYCLE__/${LIFECYCLE_VERSION}/g" \
    -e "s/__ICONS__/${ICONS_VERSION}/g" \
    -e "s/__COIL__/${COIL_VERSION}/g" >> "$APP_GRADLE" <<'GRADLE'

// >>> 3DFORGE UI >>>
dependencies {
    implementation("androidx.navigation:navigation-compose:__NAV__")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:__LIFECYCLE__")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:__LIFECYCLE__")
    implementation("androidx.compose.material:material-icons-extended:__ICONS__")
    implementation("io.coil-kt.coil3:coil-compose:__COIL__")
    implementation("io.coil-kt.coil3:coil-network-okhttp:__COIL__")
}
// <<< 3DFORGE UI <<<
GRADLE
ok "Dependencias de UI agregadas a app/build.gradle.kts"

echo
ok "Capa de UI generada: $ARCHIVOS_CREADOS archivos"
cat <<EOF

Siguientes pasos:
  1. Sync Gradle y ejecuta la app.
  2. Reemplaza el ícono Icons.Rounded.ViewInAr por tu logo en:
       ui/components/topbar/ForgeTopBar.kt, ui/screens/splash/SplashScreen.kt
       y ui/screens/auth/LoginScreen.kt (painterResource(R.drawable.tu_logo)).

Respaldo de archivos previos: ${RESPALDO#"$ROOT"/}
EOF
