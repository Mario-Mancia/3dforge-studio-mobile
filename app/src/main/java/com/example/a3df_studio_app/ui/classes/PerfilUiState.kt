package com.example.a3df_studio_app.ui.classes

import com.example.a3df_studio_app.domain.models.TipoCliente

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
