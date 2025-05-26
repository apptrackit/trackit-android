package com.example.lifetracker.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.lifetracker.data.model.HistoryEntry
import com.example.lifetracker.data.repository.MetricsRepository
import com.example.lifetracker.utils.calculateBMI
import com.example.lifetracker.utils.calculateBMR
import com.example.lifetracker.utils.calculateBodySurfaceArea
import com.example.lifetracker.utils.calculateFatFreeMassIndex
import com.example.lifetracker.utils.calculateFatMass
import com.example.lifetracker.utils.calculateLeanBodyMass

class HealthViewModel(private val repository: MetricsRepository) : ViewModel() {
    var metrics by mutableStateOf(repository.loadMetrics())
        private set

    fun updateWeight(value: String, date: Long) {
        value.toFloatOrNull()?.let {
            val newMetrics = metrics.copy(weight = it, date = date)
            metrics = newMetrics
            repository.saveMetrics(newMetrics)
            repository.saveMetricHistory("Weight", it, "kg", date)
            recalculateAllMetrics() // Changed from recalculateMetricsForDate
        }
    }

    fun updateHeight(value: String, date: Long) {
        value.toFloatOrNull()?.let {
            val newMetrics = metrics.copy(height = it, date = date)
            metrics = newMetrics
            repository.saveMetrics(newMetrics)
            repository.saveMetricHistory("Height", it, "cm", date)
            recalculateAllMetrics()
        }
    }

    fun updateBodyFat(value: String, date: Long) {
        value.toFloatOrNull()?.let {
            val newMetrics = metrics.copy(bodyFat = it, date = date)
            metrics = newMetrics
            repository.saveMetrics(newMetrics)
            repository.saveMetricHistory("Body Fat", it, "%", date)
            recalculateAllMetrics()
        }
    }
    
    // New methods for additional body measurements
    fun updateWaist(value: String, date: Long) {
        value.toFloatOrNull()?.let {
            val newMetrics = metrics.copy(waist = it, date = date)
            metrics = newMetrics
            repository.saveMetrics(newMetrics)
            repository.saveMetricHistory("Waist", it, "cm", date)
            recalculateAllMetrics()
        }
    }
    
    fun updateBicep(value: String, date: Long) {
        value.toFloatOrNull()?.let {
            val newMetrics = metrics.copy(bicep = it, date = date)
            metrics = newMetrics
            repository.saveMetrics(newMetrics)
            repository.saveMetricHistory("Bicep", it, "cm", date)
            recalculateAllMetrics()
        }
    }
    
    fun updateChest(value: String, date: Long) {
        value.toFloatOrNull()?.let {
            val newMetrics = metrics.copy(chest = it, date = date)
            metrics = newMetrics
            repository.saveMetrics(newMetrics)
            repository.saveMetricHistory("Chest", it, "cm", date)
            recalculateAllMetrics()
        }
    }
    
    fun updateThigh(value: String, date: Long) {
        value.toFloatOrNull()?.let {
            val newMetrics = metrics.copy(thigh = it, date = date)
            metrics = newMetrics
            repository.saveMetrics(newMetrics)
            repository.saveMetricHistory("Thigh", it, "cm", date)
            recalculateAllMetrics()
        }
    }
    
    fun updateShoulder(value: String, date: Long) {
        value.toFloatOrNull()?.let {
            val newMetrics = metrics.copy(shoulder = it, date = date)
            metrics = newMetrics
            repository.saveMetrics(newMetrics)
            repository.saveMetricHistory("Shoulder", it, "cm", date)
            recalculateAllMetrics()
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
        // Always recalculate all metrics when any entry is deleted
        recalculateAllMetrics()
    }

    fun getMetricHistory(metricName: String, unit: String): List<HistoryEntry> {
        // Remove automatic recalculation to prevent recursive calls
        return repository.getMetricHistory(metricName, unit)
    }
    
    /**
     * Recalculates all past BMIs based on historical weight and height data.
     * This ensures that BMI history is complete and accurate, even for past entries.
     */
    private fun recalculateAllBMIs() {
        val weightHistory = getMetricHistory("Weight", "kg")
        val heightHistory = getMetricHistory("Height", "cm")

        weightHistory.forEach { weightEntry ->
            // Find closest height measurement
            val closestHeight = getHistoryEntryAtDate("Height", "cm", weightEntry.date)
            if (closestHeight != null) {
                val bmi = calculateBMI(weightEntry.value, closestHeight)
                repository.saveMetricHistory("BMI", bmi, "", weightEntry.date, weightEntry.value, closestHeight)
            }
        }
    }
    
    /**
     * Ensures that all metrics have proper history entries.
     * This is called when the app starts to make sure all metrics have history.
     */
    fun ensureMetricHistory() {
        // Create history entries for current metrics if they don't exist
        with(metrics) {
            if (weight > 0) repository.saveMetricHistory("Weight", weight, "kg", date)
            if (height > 0) repository.saveMetricHistory("Height", height, "cm", date)
            if (bodyFat > 0) repository.saveMetricHistory("Body Fat", bodyFat, "%", date)
            if (waist > 0) repository.saveMetricHistory("Waist", waist, "cm", date)
            if (bicep > 0) repository.saveMetricHistory("Bicep", bicep, "cm", date)
            if (chest > 0) repository.saveMetricHistory("Chest", chest, "cm", date)
            if (thigh > 0) repository.saveMetricHistory("Thigh", thigh, "cm", date)
            if (shoulder > 0) repository.saveMetricHistory("Shoulder", shoulder, "cm", date)
        }
    }

    private fun updateCalculatedMetrics(date: Long) {
        val weight = getLatestHistoryEntry("Weight", "kg") ?: return
        val height = getLatestHistoryEntry("Height", "cm") ?: return
        val bodyFat = getLatestHistoryEntry("Body Fat", "%") ?: 0f

        // Calculate and save lean body mass
        val leanBodyMass = calculateLeanBodyMass(weight, bodyFat)
        repository.saveMetricHistory("Lean Body Mass", leanBodyMass, "kg", date)

        // Calculate and save fat mass
        val fatMass = calculateFatMass(weight, bodyFat)
        repository.saveMetricHistory("Fat Mass", fatMass, "kg", date)

        // Calculate and save FFMI
        val ffmi = calculateFatFreeMassIndex(leanBodyMass, height)
        repository.saveMetricHistory("Fat-Free Mass Index", ffmi, "", date)

        // Calculate and save BMR
        val bmr = calculateBMR(weight, height)
        repository.saveMetricHistory("Basal Metabolic Rate", bmr, "kcal", date)

        // Calculate and save BSA
        val bsa = calculateBodySurfaceArea(weight, height)
        repository.saveMetricHistory("Body Surface Area", bsa, "m²", date)
    }

    fun getCalculatedMetrics(): Map<String, Float> {
        val latestWeight = getLatestHistoryEntry("Weight", "kg") ?: 0f
        val latestHeight = getLatestHistoryEntry("Height", "cm") ?: 0f
        val latestBodyFat = getLatestHistoryEntry("Body Fat", "%")

        val result = mutableMapOf<String, Float>()

        // BMI only requires weight and height
        if (latestWeight > 0 && latestHeight > 0) {
            result["BMI"] = calculateBMI(latestWeight, latestHeight)
            result["Basal Metabolic Rate"] = calculateBMR(latestWeight, latestHeight)
            result["Body Surface Area"] = calculateBodySurfaceArea(latestWeight, latestHeight)
        }

        // These metrics require body fat percentage
        if (latestWeight > 0 && latestBodyFat != null && latestBodyFat > 0) {
            result["Lean Body Mass"] = calculateLeanBodyMass(latestWeight, latestBodyFat)
            result["Fat Mass"] = calculateFatMass(latestWeight, latestBodyFat)
            
            // FFMI requires height as well
            if (latestHeight > 0) {
                val leanMass = calculateLeanBodyMass(latestWeight, latestBodyFat)
                result["Fat-Free Mass Index"] = calculateFatFreeMassIndex(leanMass, latestHeight)
            }
        }

        return result
    }

    private fun clearCalculatedMetrics() {
        val calculatedMetrics = listOf(
            "BMI",
            "Lean Body Mass",
            "Fat Mass",
            "Fat-Free Mass Index",
            "Basal Metabolic Rate",
            "Body Surface Area"
        )
        
        calculatedMetrics.forEach { metric ->
            val existingHistory = getMetricHistory(metric, "")
            existingHistory.forEach { entry ->
                repository.deleteHistoryEntry(metric, entry)
            }
        }
    }

    private fun recalculateAllMetrics() {
        try {
            clearCalculatedMetrics()
            val weightHistory = repository.getMetricHistory("Weight", "kg")
            
            // Sort weight entries chronologically
            val sortedWeightHistory = weightHistory.sortedBy { it.date }
            
            // Recalculate for each weight entry date
            sortedWeightHistory.forEach { weightEntry ->
                recalculateMetricsForDate(weightEntry.date)
            }
        } catch (e: Exception) {
            // Log error but don't crash
            e.printStackTrace()
        }
    }

    private fun findClosestHistoricalValue(targetDate: Long, metricName: String, unit: String): Float? {
        val history = repository.getMetricHistory(metricName, unit)
        if (history.isEmpty()) return null
        
        // Get only values that existed before or on the target date
        return history
            .filter { it.date <= targetDate }
            .maxByOrNull { it.date }
            ?.value
    }

    private fun recalculateMetricsForDate(targetDate: Long) {
        try {
            // Get required values - only use measurements that existed at target date
            val weight = findClosestHistoricalValue(targetDate, "Weight", "kg") ?: return
            val height = findClosestHistoricalValue(targetDate, "Height", "cm")
            val bodyFat = findClosestHistoricalValue(targetDate, "Body Fat", "%")

            // Only calculate metrics if historical height exists
            if (height != null) {
                val bmi = calculateBMI(weight, height)
                repository.saveMetricHistory("BMI", bmi, "", targetDate, weight, height)
                
                val bmr = calculateBMR(weight, height)
                repository.saveMetricHistory("Basal Metabolic Rate", bmr, "kcal", targetDate)
                
                val bsa = calculateBodySurfaceArea(weight, height)
                repository.saveMetricHistory("Body Surface Area", bsa, "m²", targetDate)

                // Calculate body composition metrics if historical body fat exists
                if (bodyFat != null) {
                    val leanMass = calculateLeanBodyMass(weight, bodyFat)
                    repository.saveMetricHistory("Lean Body Mass", leanMass, "kg", targetDate)
                    
                    val fatMass = calculateFatMass(weight, bodyFat)
                    repository.saveMetricHistory("Fat Mass", fatMass, "kg", targetDate)
                    
                    val ffmi = calculateFatFreeMassIndex(leanMass, height)
                    repository.saveMetricHistory("Fat-Free Mass Index", ffmi, "", targetDate)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Returns the most recent measurement entries across all metrics, sorted by date descending.
     */
    fun getRecentMeasurements(limit: Int): List<HistoryEntry> {
        val allMetrics = listOf(
            Triple("Weight", "kg", "Weight"),
            Triple("Body Fat", "%", "Body Fat"),
            Triple("Height", "cm", "Height"),
            Triple("Thigh", "cm", "Thigh"),
            Triple("Bicep", "cm", "Bicep"),
            Triple("Waist", "cm", "Waist"),
            Triple("Chest", "cm", "Chest"),
            Triple("Shoulder", "cm", "Shoulder")
        )
        val allEntries = allMetrics.flatMap { (name, unit, metricName) ->
            getMetricHistory(name, unit).map { it.copy(metricName = metricName) }
        }
        return allEntries.sortedByDescending { it.date }.take(limit)
    }

    /**
     * Returns filtered history based on the selected time range.
     */
    fun getFilteredMetricHistory(metricName: String, unit: String, timeFilter: String): List<HistoryEntry> {
        val history = getMetricHistory(metricName, unit)
        if (history.isEmpty()) return emptyList()

        val now = System.currentTimeMillis()
        val filteredHistory = when (timeFilter) {
            "W" -> history.filter { it.date >= now - 7 * 24 * 60 * 60 * 1000L }
            "M" -> history.filter { it.date >= now - 30 * 24 * 60 * 60 * 1000L }
            "6M" -> history.filter { it.date >= now - 180 * 24 * 60 * 60 * 1000L }
            "Y" -> history.filter { it.date >= now - 365 * 24 * 60 * 60 * 1000L }
            else -> history
        }
        return filteredHistory.sortedBy { it.date }
    }

    init {
        // Initialize calculated metrics on startup
        ensureMetricHistory()
        recalculateAllMetrics()
    }
}
