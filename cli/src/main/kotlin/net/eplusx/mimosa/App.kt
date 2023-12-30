package net.eplusx.mimosa

import net.eplusx.mimosa.nature.NatureClient
import net.eplusx.mimosa.switchbot.SwitchBotClient

fun main(args: Array<String>) {
    require(args.isNotEmpty()) { "Usage: mimosa-cli <command> [args...]" }
    when (args[0]) {
        "switchbot" -> {
            require(args.size >= 2) { "Usage: mimosa-cli switchbot <command> [args...]" }
            when (args[1]) {
                "get-devices" -> println(SwitchBotClient().getDevices().toJson())
                else -> throw IllegalArgumentException("Unknown switchbot command: ${args[1]}")
            }
        }
        "nature" -> {
            require(args.size >= 2) { "Usage: mimosa-cli nature <command> [args...]" }
            when (args[1]) {
                "get-devices" -> println(NatureClient().getDevices().toJson())
                else -> throw IllegalArgumentException("Unknown nature command: ${args[1]}")
            }
        }
        else -> throw IllegalArgumentException("Unknown command: ${args[0]}")
    }
}
