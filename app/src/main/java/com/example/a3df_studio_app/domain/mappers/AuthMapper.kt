package com.example.a3df_studio_app.domain.mappers

import com.example.a3df_studio_app.domain.models.AuthUser
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

private fun JsonObject?.texto(clave: String): String? =
    (this?.get(clave) as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotBlank() }

fun UserInfo.toAuthUser(): AuthUser {
    val metadata = userMetadata
    val completo = metadata.texto("full_name") ?: metadata.texto("name")
    val nombres = metadata.texto("nombres") ?: completo?.substringBefore(" ")
    val apellidos = metadata.texto("apellidos")
        ?: completo?.substringAfter(" ", "")?.takeIf { it.isNotBlank() }
    val proveedor = appMetadata.texto("provider") ?: "email"
    return AuthUser(
        id = id,
        email = email.orEmpty(),
        nombreCompleto = completo ?: listOfNotNull(nombres, apellidos).joinToString(" ").ifBlank { null },
        nombres = nombres,
        apellidos = apellidos,
        avatarUrl = metadata.texto("avatar_url") ?: metadata.texto("picture"),
        emailVerificado = emailConfirmedAt != null || proveedor != "email",
        proveedor = proveedor
    )
}
