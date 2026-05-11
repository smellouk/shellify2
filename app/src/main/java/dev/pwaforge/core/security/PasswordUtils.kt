package dev.pwaforge.core.security

import java.security.MessageDigest

fun hashPassword(password: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}

fun verifyPassword(input: String, hash: String): Boolean = hashPassword(input) == hash
