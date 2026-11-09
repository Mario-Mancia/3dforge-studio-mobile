package com.example.a3df_studio_app.ui.screens.pago

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a3df_studio_app.domain.models.Factura
import com.example.a3df_studio_app.domain.models.TarjetaPago
import com.example.a3df_studio_app.domain.usecases.pagos.ProcessSimulatedPaymentUseCase
import com.example.a3df_studio_app.domain.validation.CardValidator
import com.example.a3df_studio_app.ui.classes.PagoUiState
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
