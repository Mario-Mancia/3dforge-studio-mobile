package com.example.a3df_studio_app.data.local

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Cifra el token de sesión con una clave AES-GCM guardada en Android Keystore. */
object TokenCipher {
    private const val ALIAS = "forge_token_key"
    private const val TRANSFORMACION = "AES/GCM/NoPadding"
    private const val LONGITUD_IV = 12

    private fun clave(): SecretKey {
        val almacen = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (almacen.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        val generador = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generador.init(
            KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generador.generateKey()
    }

    fun cifrar(texto: String): String {
        val cifrador = Cipher.getInstance(TRANSFORMACION)
        cifrador.init(Cipher.ENCRYPT_MODE, clave())
        val datos = cifrador.doFinal(texto.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(cifrador.iv + datos, Base64.NO_WRAP)
    }

    fun descifrar(valor: String): String? = try {
        val bytes = Base64.decode(valor, Base64.NO_WRAP)
        val iv = bytes.copyOfRange(0, LONGITUD_IV)
        val cifrador = Cipher.getInstance(TRANSFORMACION)
        cifrador.init(Cipher.DECRYPT_MODE, clave(), GCMParameterSpec(128, iv))
        String(cifrador.doFinal(bytes, LONGITUD_IV, bytes.size - LONGITUD_IV), Charsets.UTF_8)
    } catch (e: Exception) {
        null
    }
}
