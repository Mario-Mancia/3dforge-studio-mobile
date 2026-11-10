package com.example.a3df_studio_app.ui.screens.actividad

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a3df_studio_app.data.external.ArchivosExternos
import com.example.a3df_studio_app.data.repositories.TipoDocumento
import com.example.a3df_studio_app.domain.models.ArchivoAdjunto
import com.example.a3df_studio_app.domain.usecases.CancelOrderUseCase
import com.example.a3df_studio_app.domain.usecases.DownloadFileUseCase
import com.example.a3df_studio_app.domain.usecases.GetInvoiceUseCase
import com.example.a3df_studio_app.domain.usecases.GetOrderUseCase
import com.example.a3df_studio_app.domain.usecases.GetQuoteUseCase
import com.example.a3df_studio_app.domain.usecases.ListInvoicesUseCase
import com.example.a3df_studio_app.domain.usecases.ListOrdersUseCase
import com.example.a3df_studio_app.domain.usecases.ListQuotesUseCase
import com.example.a3df_studio_app.domain.usecases.RespondQuoteUseCase
import com.example.a3df_studio_app.ui.classes.ActividadUiState
import com.example.a3df_studio_app.ui.classes.CotizacionDetalleUiState
import com.example.a3df_studio_app.ui.classes.DescargaLista
import com.example.a3df_studio_app.ui.classes.FacturaDetalleUiState
import com.example.a3df_studio_app.ui.classes.PedidoDetalleUiState
import com.example.a3df_studio_app.ui.classes.PestanaActividad
import com.example.a3df_studio_app.ui.classes.cargarPagina
import com.example.a3df_studio_app.ui.classes.mensajeUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ActividadViewModel(
    private val listOrders: ListOrdersUseCase,
    private val listQuotes: ListQuotesUseCase,
    private val listInvoices: ListInvoicesUseCase
) : ViewModel() {

    private val _estado = MutableStateFlow(ActividadUiState())
    val estado: StateFlow<ActividadUiState> = _estado.asStateFlow()

    fun limpiar() {
        _estado.value = ActividadUiState()
    }

    fun iniciar() = cargarSiNecesario(_estado.value.pestana)

    fun seleccionar(pestana: PestanaActividad) {
        _estado.update { it.copy(pestana = pestana) }
        cargarSiNecesario(pestana)
    }

    private fun cargarSiNecesario(pestana: PestanaActividad) {
        val s = _estado.value
        when (pestana) {
            PestanaActividad.PEDIDOS -> if (!s.pedidos.cargada) cargar(pestana, true)
            PestanaActividad.COTIZACIONES -> if (!s.cotizaciones.cargada) cargar(pestana, true)
            PestanaActividad.FACTURAS -> if (!s.facturas.cargada) cargar(pestana, true)
        }
    }

    fun refrescar() = cargar(_estado.value.pestana, true)

    fun cargarMas() = cargar(_estado.value.pestana, false)

    private fun cargar(pestana: PestanaActividad, reiniciar: Boolean) {
        viewModelScope.launch {
            when (pestana) {
                PestanaActividad.PEDIDOS -> cargarPagina(
                    actual = _estado.value.pedidos,
                    reiniciar = reiniciar,
                    pedir = { pagina -> listOrders(pagina) },
                    publicar = { lista -> _estado.update { s -> s.copy(pedidos = lista) } }
                )
                PestanaActividad.COTIZACIONES -> cargarPagina(
                    actual = _estado.value.cotizaciones,
                    reiniciar = reiniciar,
                    pedir = { pagina -> listQuotes(pagina) },
                    publicar = { lista -> _estado.update { s -> s.copy(cotizaciones = lista) } }
                )
                PestanaActividad.FACTURAS -> cargarPagina(
                    actual = _estado.value.facturas,
                    reiniciar = reiniciar,
                    pedir = { pagina -> listInvoices(pagina) },
                    publicar = { lista -> _estado.update { s -> s.copy(facturas = lista) } }
                )
            }
        }
    }
}

class PedidoDetalleViewModel(
    private val getOrder: GetOrderUseCase,
    private val cancelOrder: CancelOrderUseCase,
    private val downloadFile: DownloadFileUseCase
) : ViewModel() {

    private val _estado = MutableStateFlow(PedidoDetalleUiState())
    val estado: StateFlow<PedidoDetalleUiState> = _estado.asStateFlow()

    fun cargar(id: Long) {
        viewModelScope.launch {
            _estado.value = PedidoDetalleUiState(cargando = true)
            getOrder(id)
                .onSuccess { pedido -> _estado.value = PedidoDetalleUiState(cargando = false, pedido = pedido) }
                .onFailure { e -> _estado.value = PedidoDetalleUiState(cargando = false, error = e.mensajeUi()) }
        }
    }

    fun cancelar() {
        val pedido = _estado.value.pedido ?: return
        viewModelScope.launch {
            _estado.update { it.copy(accionando = true) }
            cancelOrder(pedido)
                .onSuccess { actualizado -> _estado.update { s -> s.copy(accionando = false, pedido = actualizado, mensaje = "Pedido cancelado") } }
                .onFailure { e -> _estado.update { s -> s.copy(accionando = false, mensaje = e.mensajeUi()) } }
        }
    }

    fun descargar(archivo: ArchivoAdjunto) {
        val pedido = _estado.value.pedido ?: return
        viewModelScope.launch {
            _estado.update { it.copy(descargando = true) }
            downloadFile(TipoDocumento.PEDIDO, pedido.id, archivo)
                .onSuccess { file -> _estado.update { s -> s.copy(descargando = false, descarga = DescargaLista(file, ArchivosExternos.mimeDe(archivo.nombre))) } }
                .onFailure { e -> _estado.update { s -> s.copy(descargando = false, mensaje = e.mensajeUi()) } }
        }
    }

    fun consumirDescarga() {
        _estado.update { it.copy(descarga = null) }
    }

    fun consumirMensaje() {
        _estado.update { it.copy(mensaje = null) }
    }
}

class CotizacionDetalleViewModel(
    private val getQuote: GetQuoteUseCase,
    private val respondQuote: RespondQuoteUseCase,
    private val downloadFile: DownloadFileUseCase
) : ViewModel() {

    private val _estado = MutableStateFlow(CotizacionDetalleUiState())
    val estado: StateFlow<CotizacionDetalleUiState> = _estado.asStateFlow()

    fun cargar(id: Long) {
        viewModelScope.launch {
            _estado.value = CotizacionDetalleUiState(cargando = true)
            getQuote(id)
                .onSuccess { cotizacion -> _estado.value = CotizacionDetalleUiState(cargando = false, cotizacion = cotizacion) }
                .onFailure { e -> _estado.value = CotizacionDetalleUiState(cargando = false, error = e.mensajeUi()) }
        }
    }

    /** accion: "aceptar", "rechazar" o "cancelar". */
    fun responder(accion: String) {
        val cotizacion = _estado.value.cotizacion ?: return
        viewModelScope.launch {
            _estado.update { it.copy(accionando = true) }
            respondQuote(cotizacion, accion)
                .onSuccess { actualizada ->
                    val texto = when (accion) {
                        "aceptar" -> "Cotización aceptada. Nuestro equipo creará tu pedido."
                        "rechazar" -> "Cotización rechazada."
                        else -> "Solicitud cancelada."
                    }
                    _estado.update { s -> s.copy(accionando = false, cotizacion = actualizada, mensaje = texto) }
                }
                .onFailure { e ->
                    _estado.update { s -> s.copy(accionando = false, mensaje = e.mensajeUi()) }
                    cargar(cotizacion.id)
                }
        }
    }

    fun descargar(archivo: ArchivoAdjunto) {
        val cotizacion = _estado.value.cotizacion ?: return
        viewModelScope.launch {
            _estado.update { it.copy(descargando = true) }
            downloadFile(TipoDocumento.COTIZACION, cotizacion.id, archivo)
                .onSuccess { file -> _estado.update { s -> s.copy(descargando = false, descarga = DescargaLista(file, ArchivosExternos.mimeDe(archivo.nombre))) } }
                .onFailure { e -> _estado.update { s -> s.copy(descargando = false, mensaje = e.mensajeUi()) } }
        }
    }

    fun consumirDescarga() {
        _estado.update { it.copy(descarga = null) }
    }

    fun consumirMensaje() {
        _estado.update { it.copy(mensaje = null) }
    }
}

class FacturaDetalleViewModel(private val getInvoice: GetInvoiceUseCase) : ViewModel() {

    private val _estado = MutableStateFlow(FacturaDetalleUiState())
    val estado: StateFlow<FacturaDetalleUiState> = _estado.asStateFlow()

    fun cargar(id: Long) {
        viewModelScope.launch {
            _estado.value = FacturaDetalleUiState(cargando = true)
            getInvoice(id)
                .onSuccess { factura -> _estado.value = FacturaDetalleUiState(cargando = false, factura = factura) }
                .onFailure { e -> _estado.value = FacturaDetalleUiState(cargando = false, error = e.mensajeUi()) }
        }
    }
}
