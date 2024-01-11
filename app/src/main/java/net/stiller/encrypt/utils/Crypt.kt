package net.stiller.encrypt.utils

import android.annotation.SuppressLint
import android.util.Base64
import net.stiller.encrypt.data.models.User
import java.nio.charset.StandardCharsets
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.SecretKeySpec

class Crypt {

    @SuppressLint("GetInstance")
    fun encryptDES(value: String, keyString: String): String {
        return try {
            val cipher = Cipher.getInstance("DES/ECB/ZeroBytePadding")
            val key = SecretKeySpec(keyString.toByteArray(), "DES")
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val cleartext = value.toByteArray(StandardCharsets.UTF_8)
            Base64.encodeToString(cipher.doFinal(cleartext), Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            "Encryption Error"
        }
    }

    @SuppressLint("GetInstance")
    fun encryptAES(value: String, keyString: String): String {
        return try {
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            val key = SecretKeySpec(keyString.toByteArray(), "AES")
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val cleartext = value.toByteArray(StandardCharsets.UTF_8)
            Base64.encodeToString(cipher.doFinal(cleartext), Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            "Encryption Error"
        }
    }

    @SuppressLint("GetInstance")
    fun decryptDES(value: String, keyString: String): String {
        return try {
            val cipher = Cipher.getInstance("DES/ECB/ZeroBytePadding")
            val key = SecretKeySpec(keyString.toByteArray(), "DES")
            cipher.init(Cipher.DECRYPT_MODE, key)
            val bytesDecoded =
                Base64.decode(value.toByteArray(StandardCharsets.UTF_8), Base64.DEFAULT)
            val textDecrypted = cipher.doFinal(bytesDecoded)
            String(textDecrypted)
        } catch (e: Exception) {
            e.printStackTrace()
            "Decryption Error"
        }
    }

    @SuppressLint("GetInstance")
    fun decryptAES(value: String, keyString: String): String {
        return try {
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            val key = SecretKeySpec(keyString.toByteArray(), "AES")
            cipher.init(Cipher.DECRYPT_MODE, key)
            val bytesDecoded =
                Base64.decode(value.toByteArray(StandardCharsets.UTF_8), Base64.DEFAULT)
            val textDecrypted = cipher.doFinal(bytesDecoded)
            String(textDecrypted)
        } catch (e: Exception) {
            e.printStackTrace()
            "Decryption Error"
        }
    }

    fun generateKey(): String? {
        return try {
            val keyGenerator = KeyGenerator.getInstance("AES")
            keyGenerator.init(128)
            val sKey = keyGenerator.generateKey()
            val key = Base64.encodeToString(sKey.encoded, Base64.DEFAULT)
            val charsToReplace = "[/.#$\\[\\]=]"
            key.substring(0, 16).replace(charsToReplace.toRegex(), "X")
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        }
    }

    private fun getDESKey(id: String): String {
        return id.substring(0, 8)
    }

    private fun getAESKey(id: String): String {
        return id.substring(0, 16)
    }

    fun encrypt(user: User): User {
        user.password = encryptAES(user.password, getAESKey(user.id!!)).trim()
        user.email = encryptDES(user.email, getDESKey(user.id!!)).trim()
        return user
    }

    fun decrypt(user: User): User {
        user.password = decryptAES(user.password, getAESKey(user.id!!)).trim()
        user.email = decryptDES(user.email, getDESKey(user.id!!)).trim()
        return user
    }
}