package com.example.a3df_studio_app.data.remote.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.a3df_studio_app.data.config.AppConfig
import com.example.a3df_studio_app.domain.models.AppException
import com.example.a3df_studio_app.domain.models.GoogleSignInCancelledException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import java.security.MessageDigest
import java.util.UUID

class GoogleCredentialProvider {

    data class GoogleToken(
        val idToken: String,
        val rawNonce: String
    )

    suspend fun requestIdToken(activityContext: Context): GoogleToken {
        if (!AppConfig.isGoogleConfigured) {
            throw AppException("Configura GOOGLE_WEB_CLIENT_ID en local.properties para usar Google.")
        }

        val rawNonce = UUID.randomUUID().toString()
        val hashedNonce = MessageDigest.getInstance("SHA-256")
            .digest(rawNonce.toByteArray())
            .joinToString("") { "%02x".format(it) }

        val googleOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(AppConfig.googleWebClientId)
            .setNonce(hashedNonce)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleOption)
            .build()

        val response = try {
            CredentialManager.create(activityContext).getCredential(activityContext, request)
        } catch (e: GetCredentialCancellationException) {
            throw GoogleSignInCancelledException()
        } catch (e: NoCredentialException) {
            throw AppException("No se encontró una cuenta de Google en este dispositivo.", e)
        } catch (e: GetCredentialException) {
            throw AppException("No se pudo iniciar sesión con Google: ${e.message}", e)
        }

        val credential = response.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
            return GoogleToken(idToken = googleCredential.idToken, rawNonce = rawNonce)
        }
        throw AppException("La credencial recibida de Google no es válida.")
    }
}
