package com.example.lifetracker.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.lifetracker.data.model.HistoryEntry
import com.example.lifetracker.data.repository.MetricsRepository
import com.example.lifetracker.utils.calculateBMI
import com.example.lifetracker.utils.calculateLeanBodyMass
import com.example.lifetracker.utils.calculateFatMass
import com.example.lifetracker.utils.calculateFFMI
import com.example.lifetracker.utils.calculateBMR
import com.example.lifetracker.utils.calculateBSA

class HealthViewModel(private val repository: MetricsRepository) : ViewModel() {
    var metrics by mutableStateOf(repository.loadMetrics())
        private set

    fun updateWeight(value: String, date: Long) {
        value.toFloatOrNull()?.let {
            val newMetrics = metrics.copy(weight = it, date = date)
            metrics = newMetrics
            repository.saveMetrics(newMetrics)
            repository.saveMetricHistory("Weight", it, "kg", date)
            calculateAllDerivedMetrics()
        }
    }

    fun updateHeight(value: String, date: Long) {
        value.toFloatOrNull()?.let {
            val newMetrics = metrics.copy(height = it, date = date)
            metrics = newMetrics
            repository.saveMetrics(newMetrics)
            repository.saveMetricHistory("Height", it, "cm", date)
            calculateAllDerivedMetrics()
        }
    }

    fun updateBodyFat(value: String, date: Long) {
        value.toFloatOrNull()?.let {
            val newMetrics = metrics.copy(bodyFat = it, date = date)
            metrics = newMetrics
            repository.saveMetrics(newMetrics)
            repository.saveMetricHistory("Body Fat", it, "%", date)
            calculateAllDerivedMetrics()
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

    /**
     * Gets the history entry closest to the provided date.
     * Useful for comparing metrics at specific dates (e.g. for photo comparison).
     */
    fun getHistoryEntryAtDate(metricName: String, unit: String, targetDate: Long): Float? {
        val history = getMetricHistory(metricName, unit)
        if (history.isEmpty()) return null
        
        // Find entries within a 24-hour window of the target date
        val window = 24 * 60 * 60 * 1000L // 24 hours in milliseconds
        val entriesInWindow = history.filter { 
            targetDate - window <= it.date && it.date <= targetDate + window 
        }
        
        // If entries exist in window, get the one closest to the target date
        if (entriesInWindow.isNotEmpty()) {
            return entriesInWindow.minByOrNull { Math.abs(it.date - targetDate) }?.value
        }
        
        // If no entries in window, get the entry closest to the target date
        // This is a fallback and might return entries far from the target date
        return history.minByOrNull { Math.abs(it.date - targetDate) }?.value
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
        
        // Check if waist has history entries
        val waistHistory = getMetricHistory("Waist", "cm")
        if (waistHistory.isEmpty() && metrics.waist > 0) {
            repository.saveMetricHistory("Waist", metrics.waist, "cm", metrics.date)
        }
        
        // Check if bicep has history entries
        val bicepHistory = getMetricHistory("Bicep", "cm")
        if (bicepHistory.isEmpty() && metrics.bicep > 0) {
            repository.saveMetricHistory("Bicep", metrics.bicep, "cm", metrics.date)
        }
        
        // Check if chest has history entries
        val chestHistory = getMetricHistory("Chest", "cm")
        if (chestHistory.isEmpty() && metrics.chest > 0) {
            repository.saveMetricHistory("Chest", metrics.chest, "cm", metrics.date)
        }
        
        // Check if thigh has history entries
        val thighHistory = getMetricHistory("Thigh", "cm")
        if (thighHistory.isEmpty() && metrics.thigh > 0) {
            repository.saveMetricHistory("Thigh", metrics.thigh, "cm", metrics.date)
        }
        
        // Check if shoulder has history entries
        val shoulderHistory = getMetricHistory("Shoulder", "cm")
        if (shoulderHistory.isEmpty() && metrics.shoulder > 0) {
            repository.saveMetricHistory("Shoulder", metrics.shoulder, "cm", metrics.date)
        }
        
        // Recalculate all BMIs to ensure they're up to date
        recalculateAllBMIs()
    }

    private fun recalculateAllMetrics() {
        val weight = getLatestHistoryEntry("Weight", "kg") ?: return
        val height = getLatestHistoryEntry("Height", "cm") ?: return
        val bodyFat = getLatestHistoryEntry("Body Fat", "%") ?: return
        
        // Calculate and save all metrics
        val date = System.currentTimeMillis()
        
        // BMI
        val bmi = calculateBMI(weight, height)
        repository.saveMetricHistory("BMI", bmi, "", date, weight, height)
        
        // Lean Body Mass
        val lbm = calculateLeanBodyMass(weight, bodyFat)
        repository.saveMetricHistory("Lean Body Mass", lbm, "kg", date, weight, height)
        
        // Fat Mass
        val fatMass = calculateFatMass(weight, bodyFat)
        repository.saveMetricHistory("Fat Mass", fatMass, "kg", date, weight, height)
        
        // FFMI
        val ffmi = calculateFFMI(weight, height, bodyFat)
        repository.saveMetricHistory("FFMI", ffmi, "", date, weight, height)
        
        // BMR (using default age 30 and male for now)
        val bmr = calculateBMR(weight, height, 30, true)
        repository.saveMetricHistory("BMR", bmr, "kcal", date, weight, height)
        
        // BSA
        val bsa = calculateBSA(weight, height)
        repository.saveMetricHistory("BSA", bsa, "m²", date, weight, height)
    }

    /**
     * Initializes and calculates all metrics.
     * This should be called when the app starts to ensure all metrics are calculated.
     */
    fun initializeAndCalculateMetrics() {
        // First ensure all base metrics have history entries
        ensureMetricHistory()
        
        // Force calculate all derived metrics
        calculateAllDerivedMetrics()
    }
    
    /**
     * Calculates all derived metrics (BMI, LBM, etc.) without causing recursion.
     */
    private fun calculateAllDerivedMetrics() {
        // Only continue if we have the basic required measurements
        val weight = getLatestHistoryEntry("Weight", "kg")
        val height = getLatestHistoryEntry("Height", "cm")
        
        if (weight != null && height != null) {
            val date = System.currentTimeMillis()
            
            // Calculate BMI
            val bmi = calculateBMI(weight, height)
            repository.saveMetricHistory("BMI", bmi, "", date, weight, height)
            
            // Calculate other metrics if body fat is available
            val bodyFat = getLatestHistoryEntry("Body Fat", "%")
            
            if (bodyFat != null) {
                // Lean Body Mass
                val lbm = calculateLeanBodyMass(weight, bodyFat)
                repository.saveMetricHistory("Lean Body Mass", lbm, "kg", date, weight, height)
                
                // Fat Mass
                val fatMass = calculateFatMass(weight, bodyFat)
                repository.saveMetricHistory("Fat Mass", fatMass, "kg", date, weight, height)
                
                // FFMI
                val ffmi = calculateFFMI(weight, height, bodyFat)
                repository.saveMetricHistory("FFMI", ffmi, "", date, weight, height)
            }
            
            // BMR (using default age 30 and male for now)
            val bmr = calculateBMR(weight, height, 30, true)
            repository.saveMetricHistory("BMR", bmr, "kcal", date, weight, height)
            
            // BSA
            val bsa = calculateBSA(weight, height)
            repository.saveMetricHistory("BSA", bsa, "m²", date, weight, height)
        }
    }
}
