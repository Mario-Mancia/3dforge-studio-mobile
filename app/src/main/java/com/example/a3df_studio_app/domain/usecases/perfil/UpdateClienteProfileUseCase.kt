package com.example.a3df_studio_app.domain.usecases.perfil

import com.example.a3df_studio_app.data.repositories.AuthRepository
import com.example.a3df_studio_app.data.repositories.ClienteRepository
import com.example.a3df_studio_app.domain.models.Cliente
import com.example.a3df_studio_app.domain.models.ClienteUpdate
import com.example.a3df_studio_app.domain.models.TipoCliente
import com.example.a3df_studio_app.domain.utils.resultOf
import com.example.a3df_studio_app.domain.validation.InputValidator
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
