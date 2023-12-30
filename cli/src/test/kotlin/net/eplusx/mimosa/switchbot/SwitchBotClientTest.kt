package net.eplusx.mimosa.switchbot

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

class SwitchBotClientTest : ShouldSpec({
    context("getDevices") {
        should("return devices") {
            val server = MockWebServer()
            server.enqueue(
                MockResponse().setBody(
                    """
                    {
                        "statusCode": 100,
                        "message": "success",
                        "body": {
                            "deviceList": [{
                                "deviceId": "012345678900",
                                "deviceName": "My test bot",
                                "deviceType": "Bot",
                                "enableCloudService": true,
                                "hubDeviceId": "0123456789FF"
                            }, {
                                "deviceId": "0123456789FF",
                                "deviceName": "My test hub",
                                "deviceType": "Hub",
                                "enableCloudService": true,
                                "hubDeviceId": "000000000000"
                            }],
                            "infraredRemoteList": [{
                                "deviceId": "012345678901",
                                "deviceName": "My test remote",
                                "remoteType": "Others",
                                "hubDeviceId": "0123456789FF"
                            }]
                      }
                    }
                    """.trimIndent()
                )
            )
            server.start()

            SwitchBotClient(server.url("/").toString()).getDevices() shouldBeEqualToComparingFields DevicesResponse(
                statusCode = 100,
                message = "success",
                body = Devices(
                    deviceList = listOf(
                        Device(
                            deviceId = "012345678900",
                            deviceName = "My test bot",
                            deviceType = "Bot",
                            enableCloudService = true,
                            hubDeviceId = "0123456789FF",
                        ), Device(
                            deviceId = "0123456789FF",
                            deviceName = "My test hub",
                            deviceType = "Hub",
                            enableCloudService = true,
                            hubDeviceId = "000000000000",
                        )
                    ),
                    infraredRemoteList = listOf(
                        InfraredRemote(
                            deviceId = "012345678901",
                            deviceName = "My test remote",
                            remoteType = "Others",
                            hubDeviceId = "0123456789FF",
                        ),
                    ),
                ),
            )
        }
    }
})