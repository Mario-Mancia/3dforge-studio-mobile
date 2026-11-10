package com.example.a3df_studio_app.ui.screens.cotizar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a3df_studio_app.domain.models.ArchivoLocal
import com.example.a3df_studio_app.domain.models.Opciones3d
import com.example.a3df_studio_app.domain.models.Seleccion3d
import com.example.a3df_studio_app.domain.usecases.Estimar3dUseCase
import com.example.a3df_studio_app.domain.usecases.GetOpciones3dUseCase
import com.example.a3df_studio_app.domain.usecases.Solicitar3dUseCase
import com.example.a3df_studio_app.ui.classes.Cotizar3dUiState
import com.example.a3df_studio_app.ui.classes.camposUi
import com.example.a3df_studio_app.ui.classes.mensajeUi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Cotización automática 3D: elegir archivo → el servidor estima → el cliente solicita la cotización (queda en revisión). */
class Cotizar3DViewModel(
    private val getOpciones: GetOpciones3dUseCase,
    private val estimarModelo: Estimar3dUseCase,
    private val solicitarCotizacion: Solicitar3dUseCase
) : ViewModel() {

    private val _estado = MutableStateFlow(Cotizar3dUiState())
    val estado: StateFlow<Cotizar3dUiState> = _estado.asStateFlow()

    private var trabajo: Job? = null

    private fun seleccionInicial(o: Opciones3d): Seleccion3d? {
        val filamento = o.filamentos.firstOrNull() ?: return null
        return Seleccion3d(
            filamentoId = filamento.id,
            relleno = if (20 in o.rellenos) 20 else (o.rellenos.firstOrNull() ?: 20),
            calidad = if (o.calidades.any { it.codigo == "estandar" }) "estandar" else (o.calidades.firstOrNull()?.codigo ?: "estandar"),
            escala = if (100 in o.escalas) 100 else (o.escalas.firstOrNull() ?: 100),
            unidades = if (o.unidades.any { it.codigo == "mm" }) "mm" else (o.unidades.firstOrNull()?.codigo ?: "mm"),
            soportes = if (o.soportes.any { it.codigo == "auto" }) "auto" else (o.soportes.firstOrNull()?.codigo ?: "auto"),
            cantidad = 1
        )
    }

    fun iniciar() {
        if (_estado.value.opciones == null) cargarOpciones()
    }

    fun cargarOpciones() {
        viewModelScope.launch {
            _estado.update { it.copy(cargandoOpciones = true, errorOpciones = null) }
            getOpciones()
                .onSuccess { opciones ->
                    _estado.update { s ->
                        s.copy(cargandoOpciones = false, opciones = opciones, seleccion = s.seleccion ?: seleccionInicial(opciones))
                    }
                }
                .onFailure { e -> _estado.update { s -> s.copy(cargandoOpciones = false, errorOpciones = e.mensajeUi()) } }
        }
    }

    fun elegirArchivo(archivo: ArchivoLocal) {
        _estado.update {
            it.copy(archivo = archivo, analisisJson = null, estimacion = null, errorEstimacion = null, error = null, campos = emptyMap())
        }
        calcular()
    }

    private fun cambiarSeleccion(cambio: (Seleccion3d) -> Seleccion3d) {
        val actual = _estado.value.seleccion ?: return
        _estado.update { it.copy(seleccion = cambio(actual), error = null) }
        if (_estado.value.archivo != null) calcular()
    }

    fun elegirFilamento(id: Long) = cambiarSeleccion { it.copy(filamentoId = id) }
    fun elegirRelleno(valor: Int) = cambiarSeleccion { it.copy(relleno = valor) }
    fun elegirCalidad(codigo: String) = cambiarSeleccion { it.copy(calidad = codigo) }
    fun elegirEscala(valor: Int) = cambiarSeleccion { it.copy(escala = valor) }
    fun elegirUnidades(codigo: String) = cambiarSeleccion { it.copy(unidades = codigo) }
    fun elegirSoportes(codigo: String) = cambiarSeleccion { it.copy(soportes = codigo) }

    fun cambiarCantidad(valor: Int) {
        val maximo = _estado.value.opciones?.cantidadMaxima ?: 50
        cambiarSeleccion { it.copy(cantidad = valor.coerceIn(1, maximo)) }
    }

    fun cambiarNombre(valor: String) = _estado.update { it.copy(nombrePieza = valor, campos = it.campos - "descripcion") }
    fun cambiarComentarios(valor: String) = _estado.update { it.copy(comentarios = valor, campos = it.campos - "comentarios") }

    /** Primera vez sube el archivo; con el análisis ya hecho solo recalcula (rápido y sin gastar el límite de análisis). */
    fun calcular() {
        val s = _estado.value
        val seleccion = s.seleccion ?: return
        val archivo = s.archivo ?: return
        val analisis = s.analisisJson
        trabajo?.cancel()
        trabajo = viewModelScope.launch {
            _estado.update { it.copy(estimando = true, errorEstimacion = null) }
            estimarModelo(if (analisis == null) archivo else null, analisis, seleccion)
                .onSuccess { resultado ->
                    _estado.update { st -> st.copy(estimando = false, analisisJson = resultado.analisisJson, estimacion = resultado.estimacion) }
                }
                .onFailure { e ->
                    _estado.update { st -> st.copy(estimando = false, errorEstimacion = e.mensajeUi(), estimacion = null) }
                }
        }
    }

    fun solicitar() {
        val s = _estado.value
        val seleccion = s.seleccion ?: return
        if (s.enviando || s.estimacion?.cabe != true) return
        viewModelScope.launch {
            _estado.update { it.copy(enviando = true, error = null, campos = emptyMap()) }
            solicitarCotizacion(s.archivo, seleccion, s.nombrePieza, s.comentarios)
                .onSuccess { cotizacion -> _estado.update { st -> st.copy(enviando = false, creada = cotizacion.id) } }
                .onFailure { e -> _estado.update { st -> st.copy(enviando = false, error = e.mensajeUi(), campos = e.camposUi()) } }
        }
    }

    /** Deja el formulario limpio después de crear la solicitud (conserva las opciones del servidor). */
    fun reiniciar() {
        trabajo?.cancel()
        _estado.update { s ->
            Cotizar3dUiState(
                cargandoOpciones = false,
                opciones = s.opciones,
                seleccion = s.opciones?.let { o -> seleccionInicial(o) }
            )
        }
    }
}
