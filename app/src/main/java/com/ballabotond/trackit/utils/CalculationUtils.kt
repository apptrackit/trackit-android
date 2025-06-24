package com.example.trackit.utils

import kotlin.math.pow
import kotlin.math.sqrt

fun calculateBMI(weightKg: Float, heightCm: Float): Float {
    if (weightKg <= 0 || heightCm <= 0) return 0f
    val heightMeters = heightCm / 100
    return weightKg / heightMeters.pow(2)
}

fun calculateLeanBodyMass(weightKg: Float, bodyFatPercentage: Float): Float {
    if (weightKg <= 0) return 0f
    // If body fat is not available, assume average values
    val bodyFat = if (bodyFatPercentage <= 0) 15f else bodyFatPercentage
    return weightKg * (1 - bodyFat / 100)
}

fun calculateFatMass(weightKg: Float, bodyFatPercentage: Float): Float {
    if (weightKg <= 0) return 0f
    // If body fat is not available, assume average values
    val bodyFat = if (bodyFatPercentage <= 0) 15f else bodyFatPercentage
    return weightKg * (bodyFat / 100)
}

fun calculateFatFreeMassIndex(leanMassKg: Float, heightCm: Float): Float {
    if (leanMassKg <= 0 || heightCm <= 0) return 0f
    val heightM = heightCm / 100
    return leanMassKg / heightM.pow(2)
}

fun calculateBMR(weightKg: Float, heightCm: Float, ageYears: Int = 25, isMale: Boolean = true): Float {
    if (weightKg <= 0 || heightCm <= 0) return 0f
    return if (isMale) {
        (10 * weightKg) + (6.25f * heightCm) - (5 * ageYears) + 5
    } else {
        (10 * weightKg) + (6.25f * heightCm) - (5 * ageYears) - 161
    }
}

fun calculateBodySurfaceArea(weightKg: Float, heightCm: Float): Float {
    if (weightKg <= 0 || heightCm <= 0) return 0f
    return sqrt((weightKg * heightCm) / 3600)
}
