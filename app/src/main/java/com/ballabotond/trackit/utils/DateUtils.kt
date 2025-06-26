package com.ballabotond.trackit.utils

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

/**
 * Converts a timestamp (milliseconds since epoch) to an ISO 8601 string in UTC (e.g., 2024-06-01T12:00:00.000Z)
 */
fun toIso8601Utc(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
    sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
    return sdf.format(Date(timestamp))
}
