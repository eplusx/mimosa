package net.eplusx.logger

import SwitchBotClient

fun main() {
    SwitchBotClient().getDevices().use { response ->
        println("HTTP ${response.code}")
        println(response.body!!.string())
    }
}
