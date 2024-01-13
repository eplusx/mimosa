package net.eplusx.mimosa.daemon

import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk
import net.eplusx.mimosa.lib.Secrets
import net.eplusx.mimosa.lib.nature.NatureClient

fun main(args: Array<String>) {
    val daemon = MimosaDaemon(
        AutoConfiguredOpenTelemetrySdk.initialize().openTelemetrySdk,
        NatureClient(Secrets.Nature.accessToken),
    )
    daemon.start()
}