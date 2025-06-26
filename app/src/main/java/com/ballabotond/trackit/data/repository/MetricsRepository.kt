package com.ballabotond.trackit.data.repository

import android.content.Context
import com.ballabotond.trackit.data.model.HealthMetrics
import com.ballabotond.trackit.data.model.HistoryEntry
import androidx.core.content.edit

class MetricsRepository(private val context: Context) {

    fun saveMetrics(metrics: HealthMetrics) {
        val sharedPrefs = context.getSharedPreferences("health_metrics", Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            putFloat("weight", metrics.weight)
            putFloat("height", metrics.height)
            putFloat("bodyFat", metrics.bodyFat)
            putFloat("waist", metrics.waist)
            putFloat("bicep", metrics.bicep)
            putFloat("chest", metrics.chest)
            putFloat("thigh", metrics.thigh)
            putFloat("shoulder", metrics.shoulder)
            putLong("date", metrics.date)
            apply()
        }
    }

    fun loadMetrics(): HealthMetrics {
        val sharedPrefs = context.getSharedPreferences("health_metrics", Context.MODE_PRIVATE)
        return HealthMetrics(
            weight = sharedPrefs.getFloat("weight", 0f),
            height = sharedPrefs.getFloat("height", 0f),
            bodyFat = sharedPrefs.getFloat("bodyFat", 0f),
            waist = sharedPrefs.getFloat("waist", 0f),
            bicep = sharedPrefs.getFloat("bicep", 0f),
            chest = sharedPrefs.getFloat("chest", 0f),
            thigh = sharedPrefs.getFloat("thigh", 0f),
            shoulder = sharedPrefs.getFloat("shoulder", 0f),
            date = sharedPrefs.getLong("date", System.currentTimeMillis())
        )
    }

    fun saveMetricHistory(metricName: String, value: Float, unit: String, date: Long, weight: Float? = null, height: Float? = null) {
        val sharedPrefs = context.getSharedPreferences("health_metrics_history", Context.MODE_PRIVATE)
        val historyKey = "${metricName.lowercase()}_history"
        val currentHistory = sharedPrefs.getString(historyKey, "") ?: ""

        // Split current history into entries and filter out empty ones
        val entries = currentHistory.split("|")
            .filter { it.isNotEmpty() }
            .map { entry ->
                val parts = entry.split(":")
                val entryDate = parts[0].toLong()
                val entryValue = parts[1].toFloat()
                val entryWeight = if (parts.size > 2) parts[2].toFloat() else null
                val entryHeight = if (parts.size > 3) parts[3].toFloat() else null
                EntryData(entryDate, entryValue, entryWeight, entryHeight)
            }
            .toMutableList()

        // Remove any entry with the same date (to prevent duplicates on edit)
        entries.removeAll { it.date == date }

        // Add the new/edited entry
        entries.add(EntryData(date, value, weight, height))

        // Convert back to string format and save
        val updatedHistory = entries
            .sortedByDescending { it.date }
            .map { entry ->
                if (entry.weight != null && entry.height != null) {
                    "${entry.date}:${entry.value}:${entry.weight}:${entry.height}"
                } else {
                    "${entry.date}:${entry.value}"
                }
            }
            .joinToString("|")

        with(sharedPrefs.edit()) {
            putString(historyKey, updatedHistory)
            apply()
        }
    }

    fun getMetricHistory(metricName: String, unit: String): List<HistoryEntry> {
        val sharedPrefs = context.getSharedPreferences("health_metrics_history", Context.MODE_PRIVATE)
        val historyKey = "${metricName.lowercase()}_history"
        val historyString = sharedPrefs.getString(historyKey, "") ?: ""

        if (historyString.isEmpty()) return emptyList()

        return try {
            historyString.split("|")
                .filter { it.isNotEmpty() }
                .mapNotNull { entry ->
                    try {
                        val parts = entry.split(":")
                        val date = parts[0].toLong()
                        val value = parts[1].toFloat()

                        // Check if weight and height are included
                        if (parts.size >= 4) {
                            val weight = parts[2].toFloat()
                            val height = parts[3].toFloat()
                            HistoryEntry(value, unit, date, metricName, weight, height)
                        } else {
                            HistoryEntry(value, unit, date, metricName)
                        }
                    } catch (e: Exception) {
                        null
                    }
                }
                .distinctBy { "${it.date}_${it.value}" } // Keep only unique date-value combinations
                .sortedByDescending { it.date }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun deleteHistoryEntry(metricName: String, entry: HistoryEntry) {
        val sharedPrefs =
            context.getSharedPreferences("health_metrics_history", Context.MODE_PRIVATE)
        val historyKey = "${metricName.lowercase()}_history"
        val historyString = sharedPrefs.getString(historyKey, "") ?: ""

        if (historyString.isEmpty()) return

        val entries = historyString.split("|").toMutableList()
        // Remove entries that match date and value (and weight/height if present)
        entries.removeAll { raw ->
            val parts = raw.split(":")
            val entryDate = parts.getOrNull(0)?.toLongOrNull()
            val entryValue = parts.getOrNull(1)?.toFloatOrNull()
            val entryWeight = parts.getOrNull(2)?.toFloatOrNull()
            val entryHeight = parts.getOrNull(3)?.toFloatOrNull()
            entryDate == entry.date && entryValue == entry.value &&
                (entry.weight == null || entryWeight == entry.weight) &&
                (entry.height == null || entryHeight == entry.height)
        }

        with(sharedPrefs.edit()) {
            putString(historyKey, entries.joinToString("|"))
            apply()
        }
    }

    fun clearMetricHistory(metricName: String) {
        val sharedPrefs = context.getSharedPreferences("health_metrics_history", Context.MODE_PRIVATE)
        val historyKey = "${metricName.lowercase()}_history"
        with(sharedPrefs.edit()) {
            remove(historyKey)
            apply()
        }
    }

// --- User Profile Data ---

    fun getUserName(): String? {
        val sharedPrefs = context.getSharedPreferences("user_profile", Context.MODE_PRIVATE)
        return sharedPrefs.getString("user_name", null)
    }

    fun setUserName(name: String) {
        val sharedPrefs = context.getSharedPreferences("user_profile", Context.MODE_PRIVATE)
        sharedPrefs.edit { putString("user_name", name) }
    }

    fun getBirthYear(): Int? {
        val sharedPrefs = context.getSharedPreferences("user_profile", Context.MODE_PRIVATE)
        return if (sharedPrefs.contains("birth_year")) sharedPrefs.getInt("birth_year", 0).takeIf { it > 0 } else null
    }

    fun setBirthYear(year: Int) {
        val sharedPrefs = context.getSharedPreferences("user_profile", Context.MODE_PRIVATE)
        sharedPrefs.edit { putInt("birth_year", year) }
    }
    fun getGender(): String? {
        val sharedPrefs = context.getSharedPreferences("user_profile", Context.MODE_PRIVATE)
        return sharedPrefs.getString("gender", null)
    }

    fun setGender(gender: String) {
        val sharedPrefs = context.getSharedPreferences("user_profile", Context.MODE_PRIVATE)
        sharedPrefs.edit { putString("gender", gender) }
    }

    private data class EntryData(
        val date: Long,
        val value: Float,
        val weight: Float? = null,
        val height: Float? = null
    )
}
