package net.eplusx.mimosa.lib.nature

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

class NatureClientTest : ShouldSpec({
    val server = MockWebServer()

    afterTest {
        server.shutdown()
    }

    context("getDevices") {
        should("return devices") {
            server.enqueue(
                MockResponse().setBody(
                    """
                    [{
                        "id": "01234567-0123-4567-89ab-09123456789ab",
                        "name": "My test device",
                        "created_at": "2023-01-02T01:23:45",
                        "updated_at": "2023-03-04T12:34:56Z",
                        "serial_number": "2S0123456",
                        "firmware_version": "Remo-mini/1.2.3-01234567"
                    }]
                    """.trimIndent()
                )
            )
            server.start()

            NatureClient(
                "access_token",
                server.url("/").toString()
            ).getDevices() shouldBeEqualToComparingFields DevicesResponse(
                listOf(
                    Device(
                        id = "01234567-0123-4567-89ab-09123456789ab",
                        name = "My test device",
                        createdAt = "2023-01-02T01:23:45",
                        updatedAt = "2023-03-04T12:34:56Z",
                        serialNumber = "2S0123456",
                        firmwareVersion = "Remo-mini/1.2.3-01234567",
                    )
                )
            )
        }
    }
})