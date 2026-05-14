package io.shellify.app.core.deeplink

import com.google.zxing.NotFoundException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class QrCodeGeneratorTest {

    @Test
    fun `encodeMatrix produces a matrix with the requested size`() {
        val size = 256
        val matrix = QrCodeGenerator.encodeMatrix("https://example.com", size)
        assertEquals(size, matrix.width)
        assertEquals(size, matrix.height)
    }

    @Test
    fun `encodeMatrix encodes non-empty content`() {
        val matrix = QrCodeGenerator.encodeMatrix("Hello, QR!", 256)
        // A valid QR matrix has both set and unset modules
        var hasSet = false
        var hasUnset = false
        for (x in 0 until matrix.width) {
            for (y in 0 until matrix.height) {
                if (matrix[x, y]) hasSet = true else hasUnset = true
            }
        }
        assertTrue("Expected some set modules", hasSet)
        assertTrue("Expected some unset modules", hasUnset)
    }

    @Test
    fun `encodeMatrix produces different matrices for different content`() {
        val m1 = QrCodeGenerator.encodeMatrix("https://app-one.com", 256)
        val m2 = QrCodeGenerator.encodeMatrix("https://app-two.com", 256)
        var differ = false
        outer@ for (x in 0 until m1.width) {
            for (y in 0 until m1.height) {
                if (m1[x, y] != m2[x, y]) {
                    differ = true; break@outer
                }
            }
        }
        assertTrue("Matrices for different content must differ", differ)
    }

    @Test
    fun `encodeMatrix works with various sizes`() {
        listOf(128, 256, 512).forEach { size ->
            val matrix = QrCodeGenerator.encodeMatrix("test", size)
            assertEquals(size, matrix.width)
            assertEquals(size, matrix.height)
        }
    }

    @Test
    fun `encodeMatrix is deterministic for the same input`() {
        val m1 = QrCodeGenerator.encodeMatrix("stable-content", 256)
        val m2 = QrCodeGenerator.encodeMatrix("stable-content", 256)
        for (x in 0 until m1.width) {
            for (y in 0 until m1.height) {
                assertEquals("Mismatch at ($x,$y)", m1[x, y], m2[x, y])
            }
        }
    }

    @Test
    fun `encodeMatrix handles long urls`() {
        val longUrl = "https://example.com/" + "a".repeat(200)
        val matrix = QrCodeGenerator.encodeMatrix(longUrl, 512)
        assertTrue(matrix.width > 0)
    }
}
