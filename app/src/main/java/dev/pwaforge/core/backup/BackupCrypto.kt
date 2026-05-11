package dev.pwaforge.core.backup

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object BackupCrypto {

    private const val PBKDF2_ITERATIONS = 100_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_LENGTH = 32
    private const val IV_LENGTH = 12
    private const val GCM_TAG_BITS = 128
    private const val ALGORITHM = "AES/GCM/NoPadding"

    // Wire format: [salt 32][iv 12][GCM ciphertext+tag]
    fun encrypt(data: ByteArray, password: String): ByteArray {
        val salt = ByteArray(SALT_LENGTH).also { SecureRandom().nextBytes(it) }
        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(data)
        return salt + iv + ciphertext
    }

    fun decrypt(data: ByteArray, password: String): ByteArray {
        require(data.size > SALT_LENGTH + IV_LENGTH) { "Invalid backup file" }
        val salt = data.copyOfRange(0, SALT_LENGTH)
        val iv = data.copyOfRange(SALT_LENGTH, SALT_LENGTH + IV_LENGTH)
        val ciphertext = data.copyOfRange(SALT_LENGTH + IV_LENGTH, data.size)
        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        return cipher.doFinal(ciphertext)
    }

    private fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
    }
}
