package net.eplusx.logger.switchbot

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import okio.BufferedSource

@JsonClass(generateAdapter = true)
data class DevicesResponse(
    val statusCode: Int,
    val message: String,
    val body: Devices,
) {
    fun toJson(indent: String = "  "): String = adapter.indent(indent).toJson(this)

    companion object {
        private val moshi = Moshi.Builder().build()
        private val adapter = moshi.adapter(DevicesResponse::class.java)

        fun fromJson(json: String): DevicesResponse = adapter.fromJson(json)!!

        fun fromJson(json: BufferedSource): DevicesResponse = adapter.fromJson(json)!!
    }
}

@JsonClass(generateAdapter = true)
data class Devices(
    val deviceList: List<Device>,
    val infraredRemoteList: List<InfraredRemote>,
)

/**
 * Physical device.
 *
 * Supported device types:
 * - Bot
 * - Curtain
 * - Hub, Hub Plus, Hub Mini, Hub 2
 * - Meter, Meter Plus, Outdoor Meter
 * - Remote
 * - Motion Sensor
 * - Contact Sensor
 * - Ceiling Light, Ceiling Light Pro
 * - Plug Mini (US), Plug Mini (JP), Plug
 * - Strip Light
 * - Color Bulb
 * - Robot Vacuum Cleaner S1, S1 Plus
 * - Humidifier
 * - Indoor Cam, Pan/Tilt Cam, Pan/Tilt Cam 2K
 *
 * Known unsupported device types (they need more optional fields to be defined):
 * - Lock
 * - Keypad, Keypad Touch
 * - Blind Tilt
 */
@JsonClass(generateAdapter = true)
data class Device(
    val deviceId: String,
    val deviceName: String,
    val deviceType: String,
    val enableCloudService: Boolean?, // Might be missing for devices with old firmware.
    val hubDeviceId: String,
    val curtainDeviceIds: List<String>?,
    val calibrate: Boolean?,
    val group: Boolean?,
    val master: Boolean?,
    val openDirection: String?,
)

@JsonClass(generateAdapter = true)
data class InfraredRemote(
    val deviceId: String,
    val deviceName: String,
    val remoteType: String,
    val hubDeviceId: String,
)