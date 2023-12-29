package net.eplusx.logger

import net.eplusx.logger.switchbot.SwitchBotClient

fun main() {
    println(SwitchBotClient().getDevices().toJson())
}
