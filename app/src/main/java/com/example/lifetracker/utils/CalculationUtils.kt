package com.example.lifetracker.utils

import kotlin.math.pow
import kotlin.math.sqrt

fun calculateBMI(weight: Float, height: Float): Float {
    val heightInMeters = height / 100
    return (weight / heightInMeters.pow(2))
}

fun calculateLeanBodyMass(weight: Float, bodyFat: Float): Float {
    // LBM = Weight × (1 - BodyFat%)
    return weight * (1 - (bodyFat / 100))
}

fun calculateFatMass(weight: Float, bodyFat: Float): Float {
    // Fat Mass = Weight × BodyFat%
    return weight * (bodyFat / 100)
}

fun calculateFFMI(weight: Float, height: Float, bodyFat: Float): Float {
    // FFMI = (LBM / 2.2) / ((height / 100)²)
    val lbm = calculateLeanBodyMass(weight, bodyFat)
    val heightInMeters = height / 100
    return (lbm / 2.2f) / (heightInMeters * heightInMeters)
}

fun calculateBMR(weight: Float, height: Float, age: Int, isMale: Boolean): Float {
    // BMR = (10 × weight) + (6.25 × height) - (5 × age) + s
    // where s = +5 for males, -161 for females
    val s = if (isMale) 5f else -161f
    return (10f * weight) + (6.25f * height) - (5f * age) + s
}

fun calculateBSA(weight: Float, height: Float): Float {
    // BSA = √(height × weight / 3600)
    return sqrt((height * weight) / 3600f)
}
