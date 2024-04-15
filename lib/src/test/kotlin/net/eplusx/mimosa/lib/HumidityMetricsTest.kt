package net.eplusx.mimosa.lib

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan

class HumidityMetricsTest : ShouldSpec({
    context("computeVaporPressureDeficit") {
        should("increase with temperature") {
            computeVaporPressureDeficit(10.0, 0.5) shouldBeGreaterThan computeVaporPressureDeficit(5.0, 0.5)
        }

        should("decrease with humidity") {
            computeVaporPressureDeficit(10.0, 0.5) shouldBeGreaterThan computeVaporPressureDeficit(10.0, 0.6)
        }
    }

    context("computeVolumeHumidity") {
        should("increase with temperature") {
            computeVolumetricHumidity(10.0, 0.5) shouldBeGreaterThan computeVolumetricHumidity(5.0, 0.5)
        }

        should("increase with humidity") {
            computeVolumetricHumidity(10.0, 0.5) shouldBeLessThan computeVolumetricHumidity(10.0, 0.6)
        }
    }
})