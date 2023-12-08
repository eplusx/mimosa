package net.eplusx.logger

import SwitchBotClient

fun main() {
    println(SwitchBotClient().getDevices().body())
}
