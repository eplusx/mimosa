package net.eplusx.mimosa.lib.switchbot

import com.squareup.moshi.Moshi
import okio.BufferedSource

class JsonSerializer<T>(clazz: Class<T>) {
    private val moshi = Moshi.Builder().build()
    private val adapter = moshi.adapter(clazz)

    fun to(obj: T, indent: String = "  "): String = adapter.indent(indent).toJson(obj)

    fun from(json: String): T = adapter.fromJson(json)!!

    fun from(json: BufferedSource): T = adapter.fromJson(json)!!
}