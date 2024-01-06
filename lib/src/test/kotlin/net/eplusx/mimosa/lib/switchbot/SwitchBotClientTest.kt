package net.eplusx.mimosa.lib.switchbot

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

class SwitchBotClientTest : ShouldSpec({
    lateinit var server: MockWebServer

    beforeTest {
        server = MockWebServer()
    }

    afterTest {
        server.shutdown()
    }

    context("getDevices") {
        should("return devices") {
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

            SwitchBotClient(
                "access_token",
                "secret",
                server.url("/").toString()
            ).getDevices() shouldBeEqualToComparingFields DevicesResponse(
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

    context("getMeterStatus") {
        should("return meter value") {
            server.enqueue(
                MockResponse().setBody(
                    """
                    {
                        "statusCode": 100,
                        "message": "success",
                        "body": {
                            "deviceId": "012345678900",
                            "deviceType": "Meter",
                            "hubDeviceId": "0123456789FF",
                            "temperature": 25.5,
                            "humidity": 61,
                            "battery": 77,
                            "version": "V2.9"
                        }
                    }
                    """.trimIndent()
                )
            )
            server.start()

            SwitchBotClient(
                "access_token",
                "secret",
                server.url("/").toString()
            ).getMeterStatus("012345678900") shouldBeEqualToComparingFields MeterStatusResponse(
                statusCode = 100,
                message = "success",
                body = MeterStatus(
                    deviceId = "012345678900",
                    deviceType = "Meter",
                    hubDeviceId = "0123456789FF",
                    temperature = 25.5f,
                    humidity = 61,
                    battery = 77,
                    version = "V2.9",
                ),
            )
        }
    }
})