package com.freight.launcher.telemetry

import android.util.Log
import org.json.JSONObject

object LauncherEventLogger {
    private const val TAG = "FreightEvent"

    fun log(event: String, attributes: Map<String, Any?> = emptyMap()) {
        val payload = JSONObject()
        payload.put("event", event)
        attributes.forEach { (key, value) ->
            payload.put(key, value)
        }
        Log.i(TAG, payload.toString())
    }
}
