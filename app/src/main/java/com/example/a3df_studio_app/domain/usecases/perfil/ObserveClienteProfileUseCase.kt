package com.example.a3df_studio_app.domain.usecases.perfil

import com.example.a3df_studio_app.data.repositories.ClienteRepository
import com.example.a3df_studio_app.domain.models.Cliente
import kotlinx.coroutines.flow.StateFlow

class ObserveClienteProfileUseCase(private val clienteRepository: ClienteRepository) {
    operator fun invoke(): StateFlow<Cliente?> = clienteRepository.clienteActual
}
