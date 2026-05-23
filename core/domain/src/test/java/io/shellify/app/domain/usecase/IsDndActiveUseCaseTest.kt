package io.shellify.app.domain.usecase

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IsDndActiveUseCaseTest {

    private val useCase = IsDndActiveUseCase()

    // ── Disabled cases ────────────────────────────────────────────────────────

    @Test
    fun `returns false when both bounds are minus 1 (DND disabled)`() {
        assertFalse(useCase(-1, -1, hour = 12))
    }

    @Test
    fun `returns false when start is minus 1 regardless of end`() {
        assertFalse(useCase(-1, 17, hour = 12))
    }

    @Test
    fun `returns false when end is minus 1 regardless of start`() {
        assertFalse(useCase(9, -1, hour = 12))
    }

    @Test
    fun `returns false when start equals end (zero-length window)`() {
        assertFalse(useCase(5, 5, hour = 5))
        assertFalse(useCase(5, 5, hour = 0))
        assertFalse(useCase(5, 5, hour = 23))
    }

    // ── Same-day window 9 to 17 ───────────────────────────────────────────────

    @Test
    fun `returns false for hour before window start (9 to 17)`() {
        assertFalse(useCase(9, 17, hour = 8))
    }

    @Test
    fun `returns true at window start (9 to 17, hour 9)`() {
        assertTrue(useCase(9, 17, hour = 9))
    }

    @Test
    fun `returns true inside window (9 to 17, hour 16)`() {
        assertTrue(useCase(9, 17, hour = 16))
    }

    @Test
    fun `returns false at window end boundary (9 to 17, hour 17)`() {
        assertFalse(useCase(9, 17, hour = 17))
    }

    // ── Midnight-crossing window 22 to 8 ─────────────────────────────────────

    @Test
    fun `returns false for hour just before midnight window (22 to 8, hour 21)`() {
        assertFalse(useCase(22, 8, hour = 21))
    }

    @Test
    fun `returns true at midnight window start (22 to 8, hour 22)`() {
        assertTrue(useCase(22, 8, hour = 22))
    }

    @Test
    fun `returns true before midnight in midnight-crossing window (22 to 8, hour 23)`() {
        assertTrue(useCase(22, 8, hour = 23))
    }

    @Test
    fun `returns true at midnight in midnight-crossing window (22 to 8, hour 0)`() {
        assertTrue(useCase(22, 8, hour = 0))
    }

    @Test
    fun `returns true just before end in midnight-crossing window (22 to 8, hour 7)`() {
        assertTrue(useCase(22, 8, hour = 7))
    }

    @Test
    fun `returns false at end boundary of midnight-crossing window (22 to 8, hour 8)`() {
        assertFalse(useCase(22, 8, hour = 8))
    }

    @Test
    fun `returns false after end of midnight-crossing window (22 to 8, hour 9)`() {
        assertFalse(useCase(22, 8, hour = 9))
    }
}
