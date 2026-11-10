package com.example.a3df_studio_app.ui.classes

import com.example.a3df_studio_app.domain.models.CarritoValidado
import com.example.a3df_studio_app.domain.models.Cliente
import com.example.a3df_studio_app.domain.models.ArchivoLocal
import com.example.a3df_studio_app.domain.models.Cotizacion
import com.example.a3df_studio_app.domain.models.Encargo
import com.example.a3df_studio_app.domain.models.Factura
import com.example.a3df_studio_app.domain.models.InfoNegocio
import com.example.a3df_studio_app.domain.models.ItemCarrito
import com.example.a3df_studio_app.domain.models.Notificacion
import com.example.a3df_studio_app.domain.models.Producto
import com.example.a3df_studio_app.domain.models.PrioridadSoporte
import com.example.a3df_studio_app.domain.models.ResumenPanel
import com.example.a3df_studio_app.domain.models.Servicio
import com.example.a3df_studio_app.domain.models.CategoriaServicio
import com.example.a3df_studio_app.domain.models.SolicitudSoporte
import com.example.a3df_studio_app.domain.models.TipoCliente
import com.example.a3df_studio_app.domain.models.TotalesCarrito

data class AuthUiState(
    val cargando: Boolean = false,
    val cargandoGoogle: Boolean = false,
    val error: String? = null,
    val campos: Map<String, List<String>> = emptyMap(),
    val mensaje: String? = null,
    val correoPendiente: String? = null,
    val ultimoCorreo: String = "",
    val exito: Boolean = false
)

data class HomeUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val negocio: InfoNegocio? = null,
    val destacados: List<Producto> = emptyList(),
    val servicios: List<Servicio> = emptyList(),
    val panel: ResumenPanel? = null
)

enum class PestanaCatalogo { PRODUCTOS, SERVICIOS }

data class CatalogoUiState(
    val pestana: PestanaCatalogo = PestanaCatalogo.PRODUCTOS,
    val busqueda: String = "",
    val productos: ListaEstado<Producto> = ListaEstado(),
    val servicios: List<Servicio> = emptyList(),
    val categorias: List<CategoriaServicio> = emptyList(),
    val categoriaSeleccionada: Long? = null,
    val cargandoServicios: Boolean = false,
    val serviciosCargados: Boolean = false,
    val errorServicios: String? = null,
    val mensaje: String? = null
)

data class DetalleCatalogoUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val producto: Producto? = null,
    val servicio: Servicio? = null,
    val mensaje: String? = null
)

data class CarritoUiState(
    val items: List<ItemCarrito> = emptyList(),
    val totales: TotalesCarrito? = null,
    val validando: Boolean = false,
    val comprando: Boolean = false,
    val validado: CarritoValidado? = null,
    val error: String? = null,
    val pedidoCreado: Long? = null
)

enum class PestanaActividad { PEDIDOS, COTIZACIONES, FACTURAS }

data class ActividadUiState(
    val pestana: PestanaActividad = PestanaActividad.PEDIDOS,
    val pedidos: ListaEstado<Encargo> = ListaEstado(),
    val cotizaciones: ListaEstado<Cotizacion> = ListaEstado(),
    val facturas: ListaEstado<Factura> = ListaEstado()
)

data class PedidoDetalleUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val pedido: Encargo? = null,
    val accionando: Boolean = false,
    val descargando: Boolean = false,
    val descarga: DescargaLista? = null,
    val mensaje: String? = null
)

data class CotizacionDetalleUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val cotizacion: Cotizacion? = null,
    val accionando: Boolean = false,
    val descargando: Boolean = false,
    val descarga: DescargaLista? = null,
    val mensaje: String? = null
)

data class FacturaDetalleUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val factura: Factura? = null
)

data class CotizarUiState(
    val servicios: List<Servicio> = emptyList(),
    val cargandoServicios: Boolean = true,
    val servicioId: Long? = null,
    val cantidad: String = "1",
    val titulo: String = "",
    val solicitud: String = "",
    val material: String = "",
    val color: String = "",
    val dimensiones: String = "",
    val archivos: List<ArchivoLocal> = emptyList(),
    val enviando: Boolean = false,
    val error: String? = null,
    val campos: Map<String, List<String>> = emptyMap(),
    val creada: Long? = null
) {
    val servicio: Servicio? get() = servicios.firstOrNull { it.id == servicioId }
}

data class NotificacionesUiState(
    val lista: ListaEstado<Notificacion> = ListaEstado()
)

data class SoporteUiState(
    val lista: ListaEstado<SolicitudSoporte> = ListaEstado(),
    val detalle: SolicitudSoporte? = null,
    val cargandoDetalle: Boolean = false,
    val errorDetalle: String? = null,
    val asunto: String = "",
    val descripcion: String = "",
    val prioridad: PrioridadSoporte = PrioridadSoporte.MEDIA,
    val enviando: Boolean = false,
    val error: String? = null,
    val campos: Map<String, List<String>> = emptyMap(),
    val creada: Long? = null,
    val cerrando: Boolean = false
)

data class CuentaUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val cliente: Cliente? = null,
    val negocio: InfoNegocio? = null,
    val tipo: TipoCliente = TipoCliente.PERSONA,
    val nombres: String = "",
    val apellidos: String = "",
    val nombreEmpresa: String = "",
    val telefono: String = "",
    val identificacionFiscal: String = "",
    val direccion: String = "",
    val guardando: Boolean = false,
    val campos: Map<String, List<String>> = emptyMap(),
    val mensaje: String? = null,
    val cambiandoClave: Boolean = false,
    val errorClave: String? = null,
    val camposClave: Map<String, List<String>> = emptyMap(),
    val claveCambiada: Boolean = false
)
