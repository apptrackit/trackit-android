package com.example.lifetracker.utils

import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Calculates BMI based on weight in kg and height in cm
 */
fun calculateBMI(weightKg: Float, heightCm: Float): Float {
    val heightMeters = heightCm / 100
    return weightKg / heightMeters.pow(2)
}

/**
 * Calculates lean body mass using the Boer formula
 * @param weightKg weight in kilograms
 * @param heightCm height in centimeters
 * @param bodyFatPercentage body fat percentage
 * @return lean body mass in kilograms
 */
fun calculateLeanBodyMass(weightKg: Float, bodyFatPercentage: Float): Float {
    return weightKg * (1 - bodyFatPercentage / 100)
}

/**
 * Calculates fat mass based on weight and body fat percentage
 * @param weightKg weight in kilograms
 * @param bodyFatPercentage body fat percentage
 * @return fat mass in kilograms
 */
fun calculateFatMass(weightKg: Float, bodyFatPercentage: Float): Float {
    return weightKg * (bodyFatPercentage / 100)
}

/**
 * Calculates Fat-Free Mass Index (FFMI)
 * @param leanMassKg lean body mass in kilograms
 * @param heightCm height in centimeters
 * @return FFMI value
 */
fun calculateFatFreeMassIndex(leanMassKg: Float, heightCm: Float): Float {
    val heightM = heightCm / 100
    return leanMassKg / heightM.pow(2)
}

/**
 * Calculates Basal Metabolic Rate using the Mifflin-St Jeor Equation
 * @param weightKg weight in kilograms
 * @param heightCm height in centimeters
 * @param ageYears age in years
 * @param isMale true if male, false if female
 * @return BMR in kcal/day
 */
fun calculateBMR(weightKg: Float, heightCm: Float, ageYears: Int = 25, isMale: Boolean = true): Float {
    return if (isMale) {
        (10 * weightKg) + (6.25f * heightCm) - (5 * ageYears) + 5
    } else {
        (10 * weightKg) + (6.25f * heightCm) - (5 * ageYears) - 161
    }
}

/**
 * Calculates Body Surface Area using the Mosteller formula
 * @param weightKg weight in kilograms
 * @param heightCm height in centimeters
 * @return BSA in square meters
 */
fun calculateBodySurfaceArea(weightKg: Float, heightCm: Float): Float {
    return sqrt((weightKg * heightCm) / 3600)
}
