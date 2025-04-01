package com.example.lifetracker.utils

import kotlin.math.pow

fun calculateBMI(weight: Float, height: Float): Float {
    val heightInMeters = height / 100
    return (weight / heightInMeters.pow(2))
}
