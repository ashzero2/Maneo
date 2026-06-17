package com.maneo.app.core.data.db

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object DatabaseKeyProvider {

    private const val KEYSTORE_ALIAS = "maneo_db_wrap_key"
    private const val PREFS_FILE = "maneo_key_prefs"
    private const val PREF_WRAPPED = "wrapped_db_key"
    private const val PREF_IV = "wrap_iv"

    fun getKey(context: Context): ByteArray {
        ensureWrappingKeyExists()
        val prefs = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
        val wrappedB64 = prefs.getString(PREF_WRAPPED, null)
        val ivB64 = prefs.getString(PREF_IV, null)
        return if (wrappedB64 != null && ivB64 != null) {
            unwrap(Base64.decode(wrappedB64, Base64.NO_WRAP), Base64.decode(ivB64, Base64.NO_WRAP))
        } else {
            generateAndStore(prefs)
        }
    }

    private fun ensureWrappingKeyExists() {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        if (!ks.containsAlias(KEYSTORE_ALIAS)) {
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
                init(
                    KeyGenParameterSpec.Builder(
                        KEYSTORE_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(256)
                        .build()
                )
                generateKey()
            }
        }
    }

    private fun generateAndStore(prefs: SharedPreferences): ByteArray {
        val dbKey = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val (encrypted, iv) = wrap(dbKey)
        prefs.edit()
            .putString(PREF_WRAPPED, Base64.encodeToString(encrypted, Base64.NO_WRAP))
            .putString(PREF_IV, Base64.encodeToString(iv, Base64.NO_WRAP))
            .apply()
        return dbKey
    }

    private fun wrap(data: ByteArray): Pair<ByteArray, ByteArray> {
        val key = keystoreKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return Pair(cipher.doFinal(data), cipher.iv)
    }

    private fun unwrap(data: ByteArray, iv: ByteArray): ByteArray {
        val key = keystoreKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
        return cipher.doFinal(data)
    }

    private fun keystoreKey(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        return ks.getKey(KEYSTORE_ALIAS, null) as SecretKey
    }
}
