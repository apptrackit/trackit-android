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
            putLong("date", metrics.date)
            apply()
        }
    }

    fun loadMetrics(): HealthMetrics {
        val sharedPrefs = context.getSharedPreferences("health_metrics", Context.MODE_PRIVATE)
        return HealthMetrics(
            weight = sharedPrefs.getFloat("weight", 68.2f),
            height = sharedPrefs.getFloat("height", 180f),
            bodyFat = sharedPrefs.getFloat("bodyFat", 18.5f),
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

        // Check if entry already exists
        val newEntry = if (weight != null && height != null) {
            "$date:$value:$weight:$height"
        } else {
            "$date:$value"
        }
        
        if (currentHistory.contains("$date:$value")) return

        val updatedHistory = if (currentHistory.isEmpty()) newEntry else "$currentHistory|$newEntry"

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

        return historyString.split("|").map { entry ->
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
        }.sortedByDescending { it.date }
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
