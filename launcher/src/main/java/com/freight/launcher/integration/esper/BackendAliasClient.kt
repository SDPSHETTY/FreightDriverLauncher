package com.freight.launcher.integration.esper

import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class AliasMetadata(
    val alias: String?,
    val deviceName: String?
)

class BackendAliasClient(
    private val endpoint: String,
    private val authTokenProvider: () -> String?
) {
    private val tag = "FreightAliasBackend"

    fun fetchAlias(
        deviceIdHint: String,
        serialNumber: String? = null,
        sdkUuid: String? = null,
        enterpriseId: String? = null
    ): Result<AliasMetadata> {
        return runCatching {
            val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 10000
                readTimeout = 10000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")

                val token = authTokenProvider()?.trim().orEmpty()
                if (token.isNotBlank()) {
                    setRequestProperty("Authorization", "Bearer $token")
                }
            }

            val payload = JSONObject().apply {
                put("deviceIdHint", deviceIdHint)
                put("serialNumber", serialNumber)
                put("sdkUuid", sdkUuid)
                put("enterpriseId", enterpriseId)
            }

            connection.outputStream.bufferedWriter().use { writer ->
                writer.write(payload.toString())
            }

            connection.use {
                val status = connection.responseCode
                val body = readResponseBody(connection, status in 200..299)
                if (status !in 200..299) {
                    throw IllegalStateException("Backend alias lookup failed HTTP $status body=$body")
                }

                val json = JSONObject(body)
                val alias = firstNonBlank(
                    optString(json, "alias"),
                    optString(json, "alias_name"),
                    optString(json, "device_alias")
                )
                val deviceName = firstNonBlank(
                    optString(json, "deviceName"),
                    optString(json, "device_name"),
                    optString(json, "name")
                )

                Log.d(tag, "Backend alias lookup success")
                AliasMetadata(alias = alias, deviceName = deviceName)
            }
        }
    }

    private fun optString(json: JSONObject, key: String): String? {
        if (!json.has(key) || json.isNull(key)) {
            return null
        }
        return json.optString(key).takeIf { it.isNotBlank() }
    }

    private fun firstNonBlank(vararg values: String?): String? {
        return values.firstOrNull { !it.isNullOrBlank() }
    }

    private fun readResponseBody(connection: HttpURLConnection, successful: Boolean): String {
        val stream = if (successful) connection.inputStream else connection.errorStream
        if (stream == null) {
            return ""
        }
        return BufferedReader(InputStreamReader(stream)).use { it.readText() }
    }
}

private inline fun <T : HttpURLConnection, R> T.use(block: (T) -> R): R {
    return try {
        block(this)
    } finally {
        disconnect()
    }
}
