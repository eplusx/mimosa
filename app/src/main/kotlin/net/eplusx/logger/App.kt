package net.eplusx.logger

import net.eplusx.logger.switchbot.SwitchBotClient

fun main(args: Array<String>) {
    require(args.isNotEmpty()) { "Usage: App <command> [args...]" }
    when (args[0]) {
        "get-devices" -> {
            require(args.size == 1) { "Usage: App get-devices" }
            println(SwitchBotClient().getDevices().toJson())
        }
        else -> throw IllegalArgumentException("Unknown command: ${args[0]}")
    }
}
