package com.example.lifetracker.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.lifetracker.data.model.HealthMetrics
import com.example.lifetracker.data.model.HistoryEntry
import com.example.lifetracker.data.repository.MetricsRepository

class HealthViewModel(private val repository: MetricsRepository) {
    var metrics by mutableStateOf(repository.loadMetrics())
        private set

    fun updateWeight(value: String, date: Long) {
        value.toFloatOrNull()?.let {
            val newMetrics = metrics.copy(weight = it, date = date)
            metrics = newMetrics
            repository.saveMetrics(newMetrics)
            repository.saveMetricHistory("Weight", it, "kg", date)
        }
    }
    fun getLatestHistoryEntry(metricName: String, unit: String): Float? {
        val history = getMetricHistory(metricName, unit)
        return if (history.isNotEmpty()) {
            history.maxByOrNull { it.date }?.value
        } else {
            null
        }
    }

    fun saveMetricHistory(metricName: String, value: Float, unit: String, date: Long) {
        repository.saveMetricHistory(metricName, value, unit, date)
    }

    fun deleteHistoryEntry(metricName: String, entry: HistoryEntry) {
        repository.deleteHistoryEntry(metricName, entry)
    }
    fun getMetricHistory(metricName: String, unit: String): List<HistoryEntry> {
        return repository.getMetricHistory(metricName, unit)
    }


    fun updateHeight(value: String, date: Long) {
        value.toFloatOrNull()?.let {
            val newMetrics = metrics.copy(height = it, date = date)
            metrics = newMetrics
            repository.saveMetrics(newMetrics)
            repository.saveMetricHistory("Height", it, "cm", date)
        }
    }

    fun updateBodyFat(value: String, date: Long) {
        value.toFloatOrNull()?.let {
            val newMetrics = metrics.copy(bodyFat = it, date = date)
            metrics = newMetrics
            repository.saveMetrics(newMetrics)
            repository.saveMetricHistory("Body Fat", it, "%", date)
        }
    }

    fun getMetricHistory(metricName: String) = repository.getMetricHistory(metricName)
}
