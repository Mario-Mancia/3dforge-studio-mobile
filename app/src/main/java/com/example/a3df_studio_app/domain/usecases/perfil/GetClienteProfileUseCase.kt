package com.example.a3df_studio_app.domain.usecases.perfil

import com.example.a3df_studio_app.data.repositories.AuthRepository
import com.example.a3df_studio_app.data.repositories.ClienteRepository
import com.example.a3df_studio_app.domain.models.Cliente
import com.example.a3df_studio_app.domain.utils.resultOf
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
