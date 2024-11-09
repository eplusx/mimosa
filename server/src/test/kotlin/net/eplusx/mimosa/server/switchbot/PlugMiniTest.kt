package net.eplusx.mimosa.server.switchbot

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

@Suppress("unused")
class PlugMiniTest :
    ShouldSpec({
        context("PlugMini") {
            should("be powered on even if power is 0") {
                val plugMini = PlugMini("deviceId", "deviceName", 100.0, 0.0)
                plugMini.powerState shouldBe true
            }
        }
    })
