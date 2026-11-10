package com.example.a3df_studio_app.ui.screens.catalogo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a3df_studio_app.domain.models.Producto
import com.example.a3df_studio_app.domain.usecases.AddToCartUseCase
import com.example.a3df_studio_app.domain.usecases.GetCategoriasUseCase
import com.example.a3df_studio_app.domain.usecases.GetProductoUseCase
import com.example.a3df_studio_app.domain.usecases.GetProductosUseCase
import com.example.a3df_studio_app.domain.usecases.GetServicioUseCase
import com.example.a3df_studio_app.domain.usecases.GetServiciosUseCase
import com.example.a3df_studio_app.ui.classes.CatalogoUiState
import com.example.a3df_studio_app.ui.classes.DetalleCatalogoUiState
import com.example.a3df_studio_app.ui.classes.PestanaCatalogo
import com.example.a3df_studio_app.ui.classes.cargarPagina
import com.example.a3df_studio_app.ui.classes.mensajeUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CatalogoViewModel(
    private val getProductos: GetProductosUseCase,
    private val getServicios: GetServiciosUseCase,
    private val getCategorias: GetCategoriasUseCase,
    private val addToCart: AddToCartUseCase
) : ViewModel() {

    private val _estado = MutableStateFlow(CatalogoUiState())
    val estado: StateFlow<CatalogoUiState> = _estado.asStateFlow()

    fun iniciar() {
        if (!_estado.value.productos.cargada) cargarProductos(true)
    }

    fun cambiarPestana(pestana: PestanaCatalogo) {
        _estado.update { it.copy(pestana = pestana) }
        if (pestana == PestanaCatalogo.SERVICIOS && !_estado.value.serviciosCargados) cargarServicios()
    }

    fun actualizarBusqueda(texto: String) {
        _estado.update { it.copy(busqueda = texto) }
    }

    fun buscar() = cargarProductos(true)

    fun cargarProductos(reiniciar: Boolean) {
        viewModelScope.launch {
            cargarPagina(
                actual = _estado.value.productos,
                reiniciar = reiniciar,
                pedir = { pagina -> getProductos(_estado.value.busqueda, pagina) },
                publicar = { lista -> _estado.update { s -> s.copy(productos = lista) } }
            )
        }
    }

    fun cargarMasProductos() = cargarProductos(false)

    fun cargarServicios() {
        viewModelScope.launch {
            _estado.update { it.copy(cargandoServicios = true, errorServicios = null) }
            val categorias = getCategorias().getOrNull().orEmpty()
            getServicios(_estado.value.categoriaSeleccionada)
                .onSuccess { lista ->
                    _estado.update { s ->
                        s.copy(servicios = lista, categorias = categorias, cargandoServicios = false, serviciosCargados = true)
                    }
                }
                .onFailure { e ->
                    _estado.update { s -> s.copy(cargandoServicios = false, errorServicios = e.mensajeUi(), serviciosCargados = true) }
                }
        }
    }

    fun seleccionarCategoria(id: Long?) {
        _estado.update { it.copy(categoriaSeleccionada = id) }
        cargarServicios()
    }

    /** [alResultado] recibe el texto para mostrar al usuario (éxito o motivo del rechazo). */
    fun agregarAlCarrito(producto: Producto, alResultado: (String) -> Unit) {
        viewModelScope.launch {
            addToCart(producto, 1)
                .onSuccess { alResultado("${producto.nombre} agregado al carrito") }
                .onFailure { e -> alResultado(e.mensajeUi() ?: "No se pudo agregar al carrito.") }
        }
    }
}

class DetalleCatalogoViewModel(
    private val getProducto: GetProductoUseCase,
    private val getServicio: GetServicioUseCase,
    private val addToCart: AddToCartUseCase
) : ViewModel() {

    private val _estado = MutableStateFlow(DetalleCatalogoUiState())
    val estado: StateFlow<DetalleCatalogoUiState> = _estado.asStateFlow()

    fun cargarProducto(id: Long) {
        viewModelScope.launch {
            _estado.value = DetalleCatalogoUiState(cargando = true)
            getProducto(id)
                .onSuccess { producto -> _estado.value = DetalleCatalogoUiState(cargando = false, producto = producto) }
                .onFailure { e -> _estado.value = DetalleCatalogoUiState(cargando = false, error = e.mensajeUi()) }
        }
    }

    fun cargarServicio(id: Long) {
        viewModelScope.launch {
            _estado.value = DetalleCatalogoUiState(cargando = true)
            getServicio(id)
                .onSuccess { servicio -> _estado.value = DetalleCatalogoUiState(cargando = false, servicio = servicio) }
                .onFailure { e -> _estado.value = DetalleCatalogoUiState(cargando = false, error = e.mensajeUi()) }
        }
    }

    fun agregar(cantidad: Int) {
        val producto = _estado.value.producto ?: return
        viewModelScope.launch {
            addToCart(producto, cantidad)
                .onSuccess { _estado.update { s -> s.copy(mensaje = "Agregado al carrito") } }
                .onFailure { e -> _estado.update { s -> s.copy(mensaje = e.mensajeUi()) } }
        }
    }

    fun consumirMensaje() {
        _estado.update { it.copy(mensaje = null) }
    }
}
