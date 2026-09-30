package com.tman1an.binaryclock

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class BinaryTimeTest {

    @Test
    fun digits24Hour() {
        assertArrayEquals(intArrayOf(2, 1, 3, 7), BinaryTime.digits(21, 37, use24Hour = true))
        assertArrayEquals(intArrayOf(0, 0, 0, 0), BinaryTime.digits(0, 0, use24Hour = true))
        assertArrayEquals(intArrayOf(2, 3, 5, 9), BinaryTime.digits(23, 59, use24Hour = true))
    }

    @Test
    fun digits12Hour() {
        assertArrayEquals(intArrayOf(1, 2, 0, 5), BinaryTime.digits(0, 5, use24Hour = false))
        assertArrayEquals(intArrayOf(1, 2, 3, 0), BinaryTime.digits(12, 30, use24Hour = false))
        assertArrayEquals(intArrayOf(0, 9, 4, 5), BinaryTime.digits(21, 45, use24Hour = false))
    }

    @Test
    fun litDotsEncodeEachDigitAs8421() {
        // 21:37 -> 2 = 0010, 1 = 0001, 3 = 0011, 7 = 0111
        val lit = BinaryTime.litDots(21, 37, use24Hour = true)
        val asBits = lit.map { col -> col.joinToString("") { if (it) "1" else "0" } }
        assertEquals(listOf("0010", "0001", "0011", "0111"), asBits)
    }

    @Test
    fun everyValidTimeRoundTrips() {
        for (h in 0..23) for (m in 0..59) {
            val lit = BinaryTime.litDots(h, m, use24Hour = true)
            val decoded = lit.map { col ->
                col.indices.sumOf { row -> if (col[row]) BinaryTime.BIT_WEIGHTS[row] else 0 }
            }
            assertEquals(listOf(h / 10, h % 10, m / 10, m % 10), decoded)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsInvalidHour() {
        BinaryTime.digits(24, 0, use24Hour = true)
    }
}
