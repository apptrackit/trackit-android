/**
 * Calculates Body Surface Area (BSA) in square meters using the Mosteller formula.
 * @param weight Weight in kilograms
 * @param height Height in centimeters
 * @return Body Surface Area in square meters
 */
fun calculateBodySurfaceArea(weight: Float, height: Float): Float {
    // Mosteller formula: BSA (m²) = √[height(cm) × weight(kg) / 3600]
    return Math.sqrt((height * weight / 3600).toDouble()).toFloat()
} 