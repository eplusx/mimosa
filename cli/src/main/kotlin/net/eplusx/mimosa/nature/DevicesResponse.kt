package net.eplusx.mimosa.nature

import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okio.BufferedSource

class DevicesResponse(list: List<Device>) : List<Device> by list {
    fun toJson(indent: String = "  "): String = adapter.indent(indent).toJson(this)

    companion object {
        private val moshi = Moshi.Builder().build()
        private val adapter: JsonAdapter<List<Device>> =
            moshi.adapter(Types.newParameterizedType(List::class.java, Device::class.java))

        fun fromJson(json: String): DevicesResponse = DevicesResponse(adapter.fromJson(json)!!)

        fun fromJson(json: BufferedSource): DevicesResponse = DevicesResponse(adapter.fromJson(json)!!)
    }
}

@JsonClass(generateAdapter = true)
data class Device(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String,
    @Json(name = "serial_number") val serialNumber: String,
    @Json(name = "firmware_version") val firmwareVersion: String,
    @Json(name = "online") val online: Boolean? = null,
    @Json(name = "newest_events") val newestEvents: Map<String, NewestEvent>? = null,
    @Json(name = "temperature_offset") val temperatureOffset: Float? = null,
    @Json(name = "humidity_offset") val humidityOffset: Float? = null,
    @Json(name = "mac_address") val macAddress: String? = null,
    @Json(name = "bt_mac_address") val btMacAddress: String? = null,
)

@JsonClass(generateAdapter = true)
data class NewestEvent(
    @Json(name = "val") val value: Float,
    @Json(name = "created_at") val createdAt: String,
)