package com.example.lifetracker.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.lifetracker.data.model.HistoryEntry
import com.example.lifetracker.data.repository.MetricsRepository
import com.example.lifetracker.utils.calculateBMI

class HealthViewModel(private val repository: MetricsRepository) : ViewModel() {
    var metrics by mutableStateOf(repository.loadMetrics())
        private set

    fun updateWeight(value: String, date: Long) {
        value.toFloatOrNull()?.let {
            val newMetrics = metrics.copy(weight = it, date = date)
            metrics = newMetrics
            repository.saveMetrics(newMetrics)
            repository.saveMetricHistory("Weight", it, "kg", date)
            
            // Update BMI if height is available
            val height = getLatestHistoryEntry("Height", "cm")
            if (height != null && height > 0) {
                val bmi = calculateBMI(it, height)
                repository.saveMetricHistory("BMI", bmi, "", date, it, height)
            }
            
            // Recalculate all past BMIs
            recalculateAllBMIs()
        }
    }

    fun updateHeight(value: String, date: Long) {
        value.toFloatOrNull()?.let {
            val newMetrics = metrics.copy(height = it, date = date)
            metrics = newMetrics
            repository.saveMetrics(newMetrics)
            repository.saveMetricHistory("Height", it, "cm", date)
            
            // Update BMI if weight is available
            val weight = getLatestHistoryEntry("Weight", "kg")
            if (weight != null && weight > 0) {
                val bmi = calculateBMI(weight, it)
                repository.saveMetricHistory("BMI", bmi, "", date, weight, it)
            }
            
            // Recalculate all past BMIs
            recalculateAllBMIs()
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
    
    // New methods for additional body measurements
    fun updateWaist(value: String, date: Long) {
        value.toFloatOrNull()?.let {
            val newMetrics = metrics.copy(waist = it, date = date)
            metrics = newMetrics
            repository.saveMetrics(newMetrics)
            repository.saveMetricHistory("Waist", it, "cm", date)
        }
    }
    
    fun updateBicep(value: String, date: Long) {
        value.toFloatOrNull()?.let {
            val newMetrics = metrics.copy(bicep = it, date = date)
            metrics = newMetrics
            repository.saveMetrics(newMetrics)
            repository.saveMetricHistory("Bicep", it, "cm", date)
        }
    }
    
    fun updateChest(value: String, date: Long) {
        value.toFloatOrNull()?.let {
            val newMetrics = metrics.copy(chest = it, date = date)
            metrics = newMetrics
            repository.saveMetrics(newMetrics)
            repository.saveMetricHistory("Chest", it, "cm", date)
        }
    }
    
    fun updateThigh(value: String, date: Long) {
        value.toFloatOrNull()?.let {
            val newMetrics = metrics.copy(thigh = it, date = date)
            metrics = newMetrics
            repository.saveMetrics(newMetrics)
            repository.saveMetricHistory("Thigh", it, "cm", date)
        }
    }
    
    fun updateShoulder(value: String, date: Long) {
        value.toFloatOrNull()?.let {
            val newMetrics = metrics.copy(shoulder = it, date = date)
            metrics = newMetrics
            repository.saveMetrics(newMetrics)
            repository.saveMetricHistory("Shoulder", it, "cm", date)
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
        
        // If weight or height is deleted, recalculate all BMIs
        if (metricName == "Weight" || metricName == "Height") {
            recalculateAllBMIs()
        }
    }

    fun getMetricHistory(metricName: String, unit: String): List<HistoryEntry> {
        return try {
            repository.getMetricHistory(metricName, unit)
        } catch (e: Exception) {
            // Return empty list if there's an error retrieving history
            emptyList()
        }
    }
    
    /**
     * Recalculates all past BMIs based on historical weight and height data.
     * This ensures that BMI history is complete and accurate, even for past entries.
     */
    private fun recalculateAllBMIs() {
        // Get all weight and height history entries
        val weightHistory = getMetricHistory("Weight", "kg")
        val heightHistory = getMetricHistory("Height", "cm")
        
        if (weightHistory.isEmpty() || heightHistory.isEmpty()) {
            return
        }
        
        // Clear existing BMI history
        val existingBMIHistory = getMetricHistory("BMI", "")
        existingBMIHistory.forEach { entry ->
            repository.deleteHistoryEntry("BMI", entry)
        }
        
        // For each weight entry, find the closest height entry that was recorded before or at the same time
        weightHistory.forEach { weightEntry ->
            // Find the closest height entry that was recorded before or at the same time as the weight entry
            val closestHeightEntry = heightHistory
                .filter { it.date <= weightEntry.date }
                .maxByOrNull { it.date }
            
            // If a height entry is found, calculate BMI
            closestHeightEntry?.let { heightEntry ->
                val bmi = calculateBMI(weightEntry.value, heightEntry.value)
                repository.saveMetricHistory("BMI", bmi, "", weightEntry.date, weightEntry.value, heightEntry.value)
            }
        }
    }
    
    /**
     * Ensures that all metrics have proper history entries.
     * This is called when the app starts to make sure all metrics have history.
     */
    fun ensureMetricHistory() {
        // Check if height has history entries
        val heightHistory = getMetricHistory("Height", "cm")
        if (heightHistory.isEmpty() && metrics.height > 0) {
            // If no height history but we have a height value, create a history entry
            repository.saveMetricHistory("Height", metrics.height, "cm", metrics.date)
        }
        
        // Check if weight has history entries
        val weightHistory = getMetricHistory("Weight", "kg")
        if (weightHistory.isEmpty() && metrics.weight > 0) {
            // If no weight history but we have a weight value, create a history entry
            repository.saveMetricHistory("Weight", metrics.weight, "kg", metrics.date)
        }
        
        // Check if body fat has history entries
        val bodyFatHistory = getMetricHistory("Body Fat", "%")
        if (bodyFatHistory.isEmpty() && metrics.bodyFat > 0) {
            // If no body fat history but we have a body fat value, create a history entry
            repository.saveMetricHistory("Body Fat", metrics.bodyFat, "%", metrics.date)
        }
        
        // Recalculate all BMIs to ensure they're up to date
        recalculateAllBMIs()
    }
}
