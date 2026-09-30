package com.tman1an.binaryclock

/**
 * Converts a time of day into the on/off pattern of a BCD (binary-coded decimal)
 * binary clock: four columns (hour tens, hour ones, minute tens, minute ones),
 * each showing one decimal digit as bits 8-4-2-1.
 */
object BinaryTime {

    /** Bit weights from the top row to the bottom row. */
    val BIT_WEIGHTS = intArrayOf(8, 4, 2, 1)

    /** The four decimal digits shown by the clock, e.g. 21:37 -> [2, 1, 3, 7]. */
    fun digits(hour: Int, minute: Int, use24Hour: Boolean): IntArray {
        require(hour in 0..23) { "hour out of range: $hour" }
        require(minute in 0..59) { "minute out of range: $minute" }
        val displayHour = if (use24Hour) hour else (hour % 12).let { if (it == 0) 12 else it }
        return intArrayOf(displayHour / 10, displayHour % 10, minute / 10, minute % 10)
    }

    /**
     * lit[column][row] is true when that dot should be on.
     * Rows are ordered like [BIT_WEIGHTS]: 8, 4, 2, 1.
     */
    fun litDots(hour: Int, minute: Int, use24Hour: Boolean): Array<BooleanArray> =
        digits(hour, minute, use24Hour).map { digit ->
            BooleanArray(BIT_WEIGHTS.size) { row -> digit and BIT_WEIGHTS[row] != 0 }
        }.toTypedArray()
}
