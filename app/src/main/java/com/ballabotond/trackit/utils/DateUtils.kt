package com.example.trackit.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Formats a timestamp into a human-readable date string
 * @param timestamp The timestamp to format
 * @return A formatted date string in the format "MMM d, yyyy"
 */
fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

/**
 * Represents different time periods for filtering data
 */
enum class TimeFilter {
    WEEK,
    MONTH,
    YEAR
}
