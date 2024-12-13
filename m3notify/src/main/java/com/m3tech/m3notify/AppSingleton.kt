package com.m3tech.m3notify

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import android.util.Base64
import java.security.SecureRandom
import javax.crypto.spec.IvParameterSpec

object AppSingleton {
    private val _dataFlow = MutableStateFlow("")
    val dataFlow: StateFlow<String> get() = _dataFlow

    fun updateData(newData: String) {
        _dataFlow.value = newData
    }

    fun getSecretKeyFromBase64(base64Key: String): SecretKey {
        val decodedKey = Base64.decode(base64Key, Base64.DEFAULT)
        return SecretKeySpec(decodedKey, "AES")
    }

    fun encryptOTPWithIV(otp: String, secretKey: SecretKey): Pair<String, String> {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)
        val ivSpec = IvParameterSpec(iv)

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)

        val encryptedBytes = cipher.doFinal(otp.toByteArray(Charsets.UTF_8))

        return Pair(Base64.encodeToString(encryptedBytes, Base64.DEFAULT), Base64.encodeToString(iv, Base64.DEFAULT))
    }

    fun decryptOTPWithIV(encryptedOtp: String, ivBase64: String, secretKey: SecretKey): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val iv = Base64.decode(ivBase64, Base64.DEFAULT)
        val ivSpec = IvParameterSpec(iv)

        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)

        val decodedBytes = Base64.decode(encryptedOtp, Base64.DEFAULT)

        val decryptedBytes = cipher.doFinal(decodedBytes)

        return String(decryptedBytes, Charsets.UTF_8)
    }
}
