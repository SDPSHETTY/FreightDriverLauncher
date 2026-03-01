package com.freight.launcher.integration.esper

import android.content.Context
import android.util.Log
import io.esper.devicesdk.EsperDeviceSDK
import org.json.JSONObject

data class EsperDeviceIdentity(
    val deviceId: String,
    val serialNo: String? = null,
    val uuid: String? = null,
    val esperDeviceName: String? = null,
    val esperAliasName: String? = null
)

class EsperDeviceSdkClient(private val context: Context) {
    private val tag = "FreightEsperSdk"

    private val sdk: EsperDeviceSDK by lazy {
        EsperDeviceSDK.getInstance(context.applicationContext)
    }

    fun isActivated(callback: (Result<Boolean>) -> Unit) {
        sdk.isActivated(object : EsperDeviceSDK.Callback<Boolean> {
            override fun onResponse(response: Boolean?) {
                Log.d(tag, "isActivated response=$response")
                callback(Result.success(response == true))
            }

            override fun onFailure(t: Throwable) {
                Log.w(tag, "isActivated failure: ${t.message}")
                callback(Result.failure(t))
            }
        })
    }

    fun activate(token: String, callback: (Result<Unit>) -> Unit) {
        sdk.activateSDK(token, object : EsperDeviceSDK.Callback<Void> {
            override fun onResponse(response: Void?) {
                Log.d(tag, "activateSDK success")
                callback(Result.success(Unit))
            }

            override fun onFailure(t: Throwable) {
                Log.w(tag, "activateSDK failure: ${t.message}")
                callback(Result.failure(t))
            }
        })
    }

    fun fetchDeviceIdentity(callback: (Result<EsperDeviceIdentity>) -> Unit) {
        fetchFromEsperDeviceInfo { infoResult ->
            val infoIdentity = infoResult.getOrNull()
            if (infoIdentity == null) {
                callback(infoResult)
                return@fetchFromEsperDeviceInfo
            }

            fetchDeviceSettings { settingsResult ->
                val settings = settingsResult.getOrNull()
                if (settings == null) {
                    callback(Result.success(infoIdentity))
                    return@fetchDeviceSettings
                }

                val mergedIdentity = infoIdentity.copy(
                    esperDeviceName = firstNonBlank(
                        infoIdentity.esperDeviceName,
                        extractNestedString(settings, listOf("device_name", "deviceName", "name")),
                        extractNestedString(settings.optJSONObject("DPC_PARAMS"), listOf("device_name", "deviceName", "name"))
                    ),
                    esperAliasName = firstNonBlank(
                        infoIdentity.esperAliasName,
                        extractNestedString(settings, listOf("device_alias", "deviceAlias", "alias")),
                        extractNestedString(settings.optJSONObject("DPC_PARAMS"), listOf("device_alias", "deviceAlias", "alias"))
                    )
                )

                callback(Result.success(mergedIdentity))
            }
        }
    }

    private fun fetchFromEsperDeviceInfo(callback: (Result<EsperDeviceIdentity>) -> Unit) {
        val callbackProxy = object : EsperDeviceSDK.Callback<Any> {
            override fun onResponse(response: Any?) {
                if (response == null) {
                    callback(Result.failure(IllegalStateException("Esper device info unavailable")))
                    return
                }

                try {
                    val clazz = response.javaClass
                    Log.d(tag, "getEsperDeviceInfo response class=${clazz.name}")
                    val deviceId = invokeStringGetter(response, clazz, "getDeviceId")
                    if (deviceId.isNullOrBlank()) {
                        callback(Result.failure(IllegalStateException("Esper device ID unavailable")))
                        return
                    }

                    callback(
                        Result.success(
                            EsperDeviceIdentity(
                                deviceId = deviceId,
                                serialNo = invokeStringGetter(response, clazz, "getSerialNo"),
                                uuid = invokeStringGetter(response, clazz, "getUUID"),
                                esperDeviceName = firstNonBlank(
                                    invokeStringGetter(response, clazz, "getDeviceName"),
                                    invokeStringGetter(response, clazz, "getName")
                                ),
                                esperAliasName = firstNonBlank(
                                    invokeStringGetter(response, clazz, "getDeviceAlias"),
                                    invokeStringGetter(response, clazz, "getAlias"),
                                    invokeStringGetter(response, clazz, "getAliasName")
                                )
                            )
                        )
                    )
                    Log.d(tag, "SDK identity values: deviceId=$deviceId")
                } catch (t: Throwable) {
                    Log.w(tag, "getEsperDeviceInfo parse failure: ${t.message}")
                    callback(Result.failure(t))
                }
            }

            override fun onFailure(t: Throwable) {
                Log.w(tag, "getEsperDeviceInfo failure: ${t.message}")
                callback(Result.failure(t))
            }
        }

        try {
            val method = sdk.javaClass.methods.firstOrNull {
                it.name == "getEsperDeviceInfo" && it.parameterTypes.size == 1
            } ?: throw NoSuchMethodException("Esper SDK getEsperDeviceInfo method not found")

            method.invoke(sdk, callbackProxy)
        } catch (t: Throwable) {
            Log.w(tag, "getEsperDeviceInfo invoke failure: ${t.message}")
            callback(Result.failure(t))
        }
    }

    private fun fetchDeviceSettings(callback: (Result<JSONObject>) -> Unit) {
        val callbackProxy = object : EsperDeviceSDK.Callback<Any> {
            override fun onResponse(response: Any?) {
                val json = when (response) {
                    is JSONObject -> response
                    is String -> runCatching { JSONObject(response) }.getOrNull()
                    else -> null
                }

                if (json == null) {
                    Log.w(tag, "getDeviceSettings returned non-JSON response")
                    callback(Result.failure(IllegalStateException("Esper device settings unavailable")))
                    return
                }

                Log.d(tag, "getDeviceSettings returned payload")

                callback(Result.success(json))
            }

            override fun onFailure(t: Throwable) {
                Log.w(tag, "getDeviceSettings failure: ${t.message}")
                callback(Result.failure(t))
            }
        }

        try {
            val method = sdk.javaClass.methods.firstOrNull {
                it.name == "getDeviceSettings" && it.parameterTypes.size == 1
            } ?: throw NoSuchMethodException("Esper SDK getDeviceSettings method not found")

            method.invoke(sdk, callbackProxy)
        } catch (_: Throwable) {
            Log.w(tag, "getDeviceSettings API not found on current SDK version")
            callback(Result.failure(IllegalStateException("Esper device settings API unavailable")))
        }
    }

    private fun invokeStringGetter(target: Any, clazz: Class<*>, methodName: String): String? {
        return runCatching { clazz.getMethod(methodName).invoke(target) as? String }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
    }

    private fun extractNestedString(payload: JSONObject?, keys: List<String>): String? {
        if (payload == null) {
            return null
        }

        keys.forEach { key ->
            if (payload.has(key) && !payload.isNull(key)) {
                val value = payload.optString(key)
                if (value.isNotBlank()) {
                    return value
                }
            }
        }

        return null
    }

    private fun firstNonBlank(vararg values: String?): String? {
        return values.firstOrNull { !it.isNullOrBlank() }
    }
}
