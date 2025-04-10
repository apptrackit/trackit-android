package com.example.lifetracker.data.repository

import android.content.Context
import com.example.lifetracker.data.model.HealthMetrics
import com.example.lifetracker.data.model.HistoryEntry

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
    /*fun saveMetricHistory(metricName: String, value: Float, unit: String, date: Long) {
        val history = getMetricHistory(metricName).toMutableList()
        history.add(HistoryEntry(value, unit, date))

        val sharedPrefs = context.getSharedPreferences("health_metrics_history", Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            // In a real app, you'd need proper serialization here
            putString(metricName, history.toString())
            apply()
        }
    }*/

    fun saveMetricHistory(metricName: String, value: Float, unit: String, date: Long, weight: Float? = null, height: Float? = null) {
        val sharedPrefs = context.getSharedPreferences("health_metrics_history", Context.MODE_PRIVATE)
        val historyKey = "${metricName.lowercase()}_history"
        val currentHistory = sharedPrefs.getString(historyKey, "") ?: ""

        // Split current history into entries and filter out empty ones
        val entries = currentHistory.split("|").filter { it.isNotEmpty() }.toMutableList()

        // Create new entry
        val newEntry = if (weight != null && height != null) {
            "$date:$value:$weight:$height"
        } else {
            "$date:$value"
        }

        // Remove any existing entry with the same date
        entries.removeAll { it.startsWith("$date:") }

        // Add the new entry
        entries.add(newEntry)

        // Join entries back together and save
        val updatedHistory = entries.joinToString("|")

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
                .sortedByDescending { it.date }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun deleteHistoryEntry(metricName: String, entry: HistoryEntry) {
        val sharedPrefs = context.getSharedPreferences("health_metrics_history", Context.MODE_PRIVATE)
        val historyKey = "${metricName.lowercase()}_history"
        val historyString = sharedPrefs.getString(historyKey, "") ?: ""

        if (historyString.isEmpty()) return

        val entries = historyString.split("|").toMutableList()
        val targetEntry = "${entry.date}:${entry.value}"

        entries.removeAll { it.startsWith(targetEntry) }

        with(sharedPrefs.edit()) {
            putString(historyKey, entries.joinToString("|"))
            apply()
        }
    }
    fun getMetricHistory(metricName: String): List<HistoryEntry> {
        // This is a placeholder. In a real app, you'd implement proper serialization/deserialization
        return emptyList()
    }
}
