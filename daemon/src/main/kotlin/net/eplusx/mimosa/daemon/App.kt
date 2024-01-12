package net.eplusx.mimosa.daemon

import net.eplusx.mimosa.lib.Secrets
import net.eplusx.mimosa.lib.nature.NatureClient

fun main(args: Array<String>) {
    val natureClient = NatureClient(Secrets.Nature.accessToken)
    println(natureClient.getDevices().toJson())
}