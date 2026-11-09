package com.example.a3df_studio_app.domain.usecases.pagos

import com.example.a3df_studio_app.data.repositories.AuthRepository
import com.example.a3df_studio_app.data.repositories.PagoRepository
import com.example.a3df_studio_app.domain.models.Factura
import com.example.a3df_studio_app.domain.models.PagoRegistrado
import com.example.a3df_studio_app.domain.models.PaymentDeclinedException
import com.example.a3df_studio_app.domain.models.TarjetaPago
import com.example.a3df_studio_app.domain.models.ValidationException
import com.example.a3df_studio_app.domain.utils.resultOf
import com.example.a3df_studio_app.domain.validation.CardValidator
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
