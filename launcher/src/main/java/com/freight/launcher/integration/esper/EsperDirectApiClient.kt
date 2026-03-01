package com.freight.launcher.integration.esper

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class EsperDirectApiClient(
    private val tenantUrl: String,
    private val apiToken: String
) {
    private val tag = "FreightEsperDirectApi"

    fun fetchAlias(
        enterpriseId: String,
        deviceIdHint: String,
        serialNumber: String? = null,
        sdkUuid: String? = null
    ): Result<AliasMetadata> {
        return runCatching {
            val apiBase = normalizeApiBase(tenantUrl)
            val endpoint = "$apiBase/api/enterprise/$enterpriseId/device/?limit=200"
            val payload = requestJson(endpoint)
                ?: throw IllegalStateException("Esper direct API request failed")

            val results = payload.optJSONArray("results") ?: JSONArray()
            val matched = findMatchingDevice(results, deviceIdHint, serialNumber, sdkUuid)
                ?: throw IllegalStateException("Esper direct API device not found")

            val alias = firstNonBlank(
                optString(matched, "alias_name"),
                optString(matched, "device_alias"),
                optString(matched, "alias")
            )
            val deviceName = firstNonBlank(
                optString(matched, "device_name"),
                optString(matched, "name")
            )

            Log.d(tag, "Esper direct API alias lookup success")
            AliasMetadata(alias = alias, deviceName = deviceName)
        }
    }

    private fun requestJson(endpoint: String): JSONObject? {
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10000
            readTimeout = 10000
            setRequestProperty("Authorization", "Bearer $apiToken")
            setRequestProperty("Accept", "application/json")
        }

        return connection.use {
            val status = connection.responseCode
            val body = readResponseBody(connection, status in 200..299)
            if (status !in 200..299) {
                Log.w(tag, "Esper direct API non-2xx status=$status")
                return@use null
            }
            runCatching { JSONObject(body) }.getOrNull()
        }
    }

    private fun findMatchingDevice(
        results: JSONArray,
        deviceIdHint: String,
        serialNumber: String?,
        sdkUuid: String?
    ): JSONObject? {
        val hint = deviceIdHint.trim()
        val serial = serialNumber?.trim().orEmpty()
        val uuid = sdkUuid?.trim().orEmpty()

        for (i in 0 until results.length()) {
            val item = results.optJSONObject(i) ?: continue
            val id = optString(item, "id").orEmpty()
            val name = optString(item, "device_name").orEmpty()
            val alias = optString(item, "alias_name").orEmpty()
            val hwSerial = item.optJSONObject("hardwareInfo")?.optString("serialNumber").orEmpty()

            if (
                id.equals(uuid, true) ||
                id.equals(hint, true) ||
                name.equals(hint, true) ||
                alias.equals(hint, true) ||
                (serial.isNotBlank() && hwSerial.equals(serial, true))
            ) {
                return item
            }
        }

        return null
    }

    private fun normalizeApiBase(rawTenantUrl: String): String {
        val trimmed = rawTenantUrl.trim().trimEnd('/')
        if (trimmed.contains("-api.esper.cloud")) {
            return trimmed
        }
        if (trimmed.contains(".esper.cloud")) {
            return trimmed.replace(".esper.cloud", "-api.esper.cloud")
        }
        return trimmed
    }

    private fun optString(json: JSONObject, key: String): String? {
        if (!json.has(key) || json.isNull(key)) return null
        return json.optString(key).takeIf { it.isNotBlank() }
    }

    private fun firstNonBlank(vararg values: String?): String? {
        return values.firstOrNull { !it.isNullOrBlank() }
    }

    private fun readResponseBody(connection: HttpURLConnection, successful: Boolean): String {
        val stream = if (successful) connection.inputStream else connection.errorStream
        if (stream == null) return ""
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
