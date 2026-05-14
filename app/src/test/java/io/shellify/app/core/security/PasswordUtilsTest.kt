package io.shellify.app.core.security

import io.shellify.app.util.JavaStandardBase64Codec
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordUtilsTest {

    private val codec = JavaStandardBase64Codec

    @Test
    fun `hashPassword returns v2 prefixed string`() {
        val hash = hashPassword("secret", codec)
        assertTrue(hash.startsWith("v2:"))
    }

    @Test
    fun `hashPassword output has three colon-separated parts`() {
        val hash = hashPassword("secret", codec)
        val parts = hash.split(":")
        // "v2", "<saltB64>", "<hashB64>"
        assertTrue(parts.size >= 3)
    }

    @Test
    fun `hashPassword produces different output each call due to random salt`() {
        val hash1 = hashPassword("same-password", codec)
        val hash2 = hashPassword("same-password", codec)
        assertFalse(hash1 == hash2)
    }

    @Test
    fun `verifyPassword returns true for correct password`() {
        val hash = hashPassword("correct", codec)
        assertTrue(verifyPassword("correct", hash, codec))
    }

    @Test
    fun `verifyPassword returns false for wrong password`() {
        val hash = hashPassword("correct", codec)
        assertFalse(verifyPassword("wrong", hash, codec))
    }

    @Test
    fun `verifyPassword is case sensitive`() {
        val hash = hashPassword("Password", codec)
        assertFalse(verifyPassword("password", hash, codec))
    }

    @Test
    fun `verifyPassword returns false for malformed stored hash`() {
        assertFalse(verifyPassword("any", "v2:onlyOneSegment", codec))
    }

    @Test
    fun `verifyPassword returns false for invalid base64 in stored hash`() {
        assertFalse(verifyPassword("any", "v2:!!!:###", codec))
    }

    @Test
    fun `verifyPassword handles legacy SHA-256 format correctly`() {
        // SHA-256 of "legacy" as lowercase hex
        val legacyHash = java.security.MessageDigest.getInstance("SHA-256")
            .digest("legacy".toByteArray())
            .joinToString("") { "%02x".format(it) }
        assertTrue(verifyPassword("legacy", legacyHash, codec))
    }

    @Test
    fun `verifyPassword rejects wrong input against legacy hash`() {
        val legacyHash = java.security.MessageDigest.getInstance("SHA-256")
            .digest("legacy".toByteArray())
            .joinToString("") { "%02x".format(it) }
        assertFalse(verifyPassword("wrong", legacyHash, codec))
    }

    @Test
    fun `isLegacyHash returns false for v2 hash`() {
        val hash = hashPassword("test", codec)
        assertFalse(isLegacyHash(hash))
    }

    @Test
    fun `isLegacyHash returns true for non-v2 hash`() {
        assertTrue(isLegacyHash("someoldhash"))
        assertTrue(isLegacyHash("abcdef1234567890"))
    }

    @Test
    fun `verifyPassword works with unicode password`() {
        val hash = hashPassword("パスワード", codec)
        assertTrue(verifyPassword("パスワード", hash, codec))
        assertFalse(verifyPassword("passwort", hash, codec))
    }

    @Test
    fun `verifyPassword works with empty password`() {
        val hash = hashPassword("", codec)
        assertTrue(verifyPassword("", hash, codec))
        assertFalse(verifyPassword("notempty", hash, codec))
    }
}
