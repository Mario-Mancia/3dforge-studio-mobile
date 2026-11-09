package com.example.a3df_studio_app.domain.models

open class AppException(message: String, cause: Throwable? = null) : Exception(message, cause)

class NotConfiguredException :
    AppException("Supabase no está configurado. Define SUPABASE_URL y SUPABASE_ANON_KEY en local.properties.")

class AuthRequiredException :
    AppException("Debes iniciar sesión o registrarte para continuar.")

class EmailNotVerifiedException(val correo: String) :
    AppException("Debes verificar tu correo antes de continuar.")

class GoogleSignInCancelledException :
    AppException("Inicio de sesión con Google cancelado.")

class ValidationException(message: String) : AppException(message)

class PaymentDeclinedException(message: String) : AppException(message)
