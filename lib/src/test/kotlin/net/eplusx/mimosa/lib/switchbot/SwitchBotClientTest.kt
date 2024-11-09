package net.eplusx.mimosa.lib.switchbot

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beOfType
import io.mockk.every
import io.mockk.mockk
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import java.net.SocketTimeoutException

private fun buildResponse(body: String): Response {
    val request = Request.Builder().url("http://dummy_endpoint_prefix/").build()
    return Response
        .Builder()
        .request(request)
        .protocol(okhttp3.Protocol.HTTP_1_1)
        .code(200)
        .message("OK")
        .body(body.toResponseBody())
        .build()
}

@Suppress("unused")
class SwitchBotClientTest :
    ShouldSpec({
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
                        """.trimIndent(),
                    ),
                )
                server.start()

                SwitchBotClient(
                    "access_token",
                    "secret",
                    server.url("/").toString(),
                ).getDevices() shouldBeEqualToComparingFields
                    DevicesResponse(
                        statusCode = 100,
                        message = "success",
                        body =
                            Devices(
                                deviceList =
                                    listOf(
                                        Device(
                                            deviceId = "012345678900",
                                            deviceName = "My test bot",
                                            deviceType = "Bot",
                                            enableCloudService = true,
                                            hubDeviceId = "0123456789FF",
                                        ),
                                        Device(
                                            deviceId = "0123456789FF",
                                            deviceName = "My test hub",
                                            deviceType = "Hub",
                                            enableCloudService = true,
                                            hubDeviceId = "000000000000",
                                        ),
                                    ),
                                infraredRemoteList =
                                    listOf(
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

            should("retry on internal server error") {
                server.enqueue(MockResponse().setResponseCode(500).setBody("Internal server error"))
                server.enqueue(
                    MockResponse().setBody(
                        """
                        {
                            "statusCode": 100,
                            "message": "success",
                            "body": {
                                "deviceList": [],
                                "infraredRemoteList": []
                            }
                        }
                        """.trimIndent(),
                    ),
                )
                server.start()

                SwitchBotClient(
                    "access_token",
                    "secret",
                    server.url("/").toString(),
                ).getDevices() should beOfType<DevicesResponse>()
            }

            should("retry on socket timeout") {
                val mockHttpClient = mockk<OkHttpClient>()
                every { mockHttpClient.newCall(any()).execute() } answers {
                    throw SocketTimeoutException("Timeout")
                } andThenAnswer {
                    buildResponse(
                        """
                        {
                            "statusCode": 100,
                            "message": "success",
                            "body": {
                                "deviceList": [],
                                "infraredRemoteList": []
                            }
                        }
                        """.trimIndent(),
                    )
                }

                SwitchBotClient(
                    "access_token",
                    "secret",
                    "http://dummy_endpoint_prefix/",
                    httpClient = mockHttpClient,
                ).getDevices() should beOfType<DevicesResponse>()
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
                        """.trimIndent(),
                    ),
                )
                server.start()

                SwitchBotClient(
                    "access_token",
                    "secret",
                    server.url("/").toString(),
                ).getMeterStatus("012345678900") shouldBeEqualToComparingFields
                    MeterStatusResponse(
                        statusCode = 100,
                        message = "success",
                        body =
                            MeterStatus(
                                deviceId = "012345678900",
                                deviceType = "Meter",
                                hubDeviceId = "0123456789FF",
                                temperature = 25.5,
                                humidity = 61,
                                battery = 77,
                                version = "V2.9",
                            ),
                    )
                server.takeRequest().path shouldBe "/devices/012345678900/status"
            }
        }

        context("getPlugMiniStatus") {
            val typicalResponseJson =
                """
                {
                    "statusCode": 100,
                    "message": "success",
                    "body": {
                        "deviceId": "012345678900",
                        "deviceType": "Plug",
                        "hubDeviceId": "000000000000",
                        "version": "V1.5-1.5",
                        "voltage": 100,
                        "electricCurrent": 15.5,
                        "weight": 143.5,
                        "electricityOfDay": 1253
                    }
                }
                """.trimIndent()
            val typicalResponse =
                PlugMiniStatusResponse(
                    statusCode = 100,
                    message = "success",
                    body =
                        PlugMiniStatus(
                            deviceId = "012345678900",
                            deviceType = "Plug",
                            hubDeviceId = "000000000000",
                            version = "V1.5-1.5",
                            voltageVolt = 100.0,
                            electricCurrent = 15.5,
                            powerWatt = 143.5,
                            electricityOfDay = 1253,
                        ),
                )

            should("return plug mini status") {
                server.enqueue(MockResponse().setBody(typicalResponseJson))
                server.start()

                SwitchBotClient(
                    "access_token",
                    "secret",
                    server.url("/").toString(),
                ).getPlugMiniStatus("012345678900") shouldBeEqualToComparingFields typicalResponse
                server.takeRequest().path shouldBe "/devices/012345678900/status"
            }

            should("retry on HTTP 500 error") {
                server.enqueue(MockResponse().setResponseCode(500).setBody("Temporary error"))
                server.enqueue(MockResponse().setBody(typicalResponseJson))
                server.start()

                SwitchBotClient(
                    "access_token",
                    "secret",
                    server.url("/").toString(),
                ).getPlugMiniStatus("012345678900") shouldBeEqualToComparingFields typicalResponse
                server.takeRequest().path shouldBe "/devices/012345678900/status"
            }
        }

        context("getHub2Status") {
            should("return meter value") {
                server.enqueue(
                    MockResponse().setBody(
                        """
                        {
                            "statusCode": 100,
                            "message": "success",
                            "body": {
                                "deviceId": "012345678900",
                                "deviceType": "Hub 2",
                                "hubDeviceId": "000000000000",
                                "version": "V0.8-0.6",
                                "temperature": 13.5,
                                "humidity": 72,
                                "lightLevel": 3
                            }
                        }
                        """.trimIndent(),
                    ),
                )
                server.start()

                SwitchBotClient(
                    "access_token",
                    "secret",
                    server.url("/").toString(),
                ).getHub2Status("012345678900") shouldBeEqualToComparingFields
                    Hub2StatusResponse(
                        statusCode = 100,
                        message = "success",
                        body =
                            Hub2Status(
                                deviceId = "012345678900",
                                deviceType = "Hub 2",
                                hubDeviceId = "000000000000",
                                version = "V0.8-0.6",
                                temperature = 13.5,
                                humidity = 72,
                                lightLevel = 3,
                            ),
                    )
                server.takeRequest().path shouldBe "/devices/012345678900/status"
            }
        }
    })
