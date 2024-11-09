package net.eplusx.mimosa.lib.switchbot

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DevicesResponse(
    val statusCode: Int,
    val message: String,
    val body: Devices,
) {
    fun toJson(): String = json.to(this)

    companion object {
        val json = JsonSerializer(DevicesResponse::class.java)
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
    val enableCloudService: Boolean? = null, // Might be missing for devices with old firmware.
    val hubDeviceId: String,
    val curtainDeviceIds: List<String>? = null,
    val calibrate: Boolean? = null,
    val group: Boolean? = null,
    val master: Boolean? = null,
    val openDirection: String? = null,
)

@JsonClass(generateAdapter = true)
data class InfraredRemote(
    val deviceId: String,
    val deviceName: String,
    val remoteType: String,
    val hubDeviceId: String,
)
