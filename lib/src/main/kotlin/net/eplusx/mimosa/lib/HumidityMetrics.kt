package net.eplusx.mimosa.lib

import kotlin.math.ln
import kotlin.math.pow

/**
 * Compute the Vapor Pressure Deficit (VPD) in kPa.
 *
 * @param temperature Temperature in degrees Celsius.
 * @param humidity Humidity in [0, 1].
 */
fun computeVaporPressureDeficit(temperature: Double, humidity: Double): Double {
    val a = -1.0440397e4
    val b = -11.29465
    val c = -2.7022355e-2
    val d = 1.289036e-5
    val e = -2.4780681e-9
    val f = 6.5459673
    val t = 1.8 * (temperature + 273.15)
    val t2 = t * t
    val saturationVaporPressurePsi = Math.E.pow(a / t + b + c * t + d * t2 + e * t2 * t + f * ln(t))
    val saturationVaporPressureKpa = 6.89476 * saturationVaporPressurePsi
    return (1 - humidity) * saturationVaporPressureKpa
}

/**
 * Compute the volumetric humidity in grams per cubic meter.
 */
fun computeVolumetricHumidity(temperature: Double, humidity: Double): Double {
    val et = 6.1078 * 10.0.pow(7.5 * temperature / (temperature + 237.3))
    val at = 217 * et / (temperature + 273.15)
    return humidity * at
}