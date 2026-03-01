package com.freight.launcher.integration.esper

import android.content.Context
import android.util.Log
import com.freight.common.LauncherConfig

data class DeviceIdentityDisplay(
    val primary: String? = null,
    val secondary: String? = null
)

class DeviceIdentityResolver(
    private val context: Context,
    private val launcherConfig: LauncherConfig,
    private val esperSdkClient: EsperDeviceSdkClient = EsperDeviceSdkClient(context)
) {
    private val tag = "FreightEsperIdentity"
    private val backendAliasClient: BackendAliasClient? by lazy {
        val endpoint = launcherConfig.esper.backendAliasEndpoint?.trim().orEmpty()
        if (endpoint.isBlank()) {
            null
        } else {
            BackendAliasClient(
                endpoint = endpoint,
                authTokenProvider = { launcherConfig.esper.apiToken }
            )
        }
    }
    private val directApiClient: EsperDirectApiClient? by lazy {
        val tenantUrl = launcherConfig.esper.tenantUrl?.trim().orEmpty()
        val token = launcherConfig.esper.apiToken?.trim().orEmpty()
        if (tenantUrl.isBlank() || token.isBlank()) {
            null
        } else {
            EsperDirectApiClient(tenantUrl = tenantUrl, apiToken = token)
        }
    }

    @Volatile
    private var cachedCloudIdentity: DeviceIdentityDisplay? = null

    @Volatile
    private var lastCloudFetchEpochMs: Long = 0L

    fun localDisplayIdentity(): DeviceIdentityDisplay {
        if (!shouldShowAnyIdentity()) {
            Log.d(tag, "Alias and device name hidden by config")
            return DeviceIdentityDisplay()
        }

        val managedAlias = launcherConfig.esper.managedAlias?.trim().orEmpty()
        val managedDeviceName = launcherConfig.esper.managedDeviceName?.trim().orEmpty()

        val display = buildDisplay(alias = managedAlias, deviceName = managedDeviceName)
        return display ?: DeviceIdentityDisplay()
    }

    fun resolveEsperIdentity(onResolved: (DeviceIdentityDisplay?) -> Unit) {
        if (!shouldShowAnyIdentity()) {
            Log.d(tag, "Skipping Esper identity resolution because identity visibility is disabled")
            onResolved(null)
            return
        }

        if (!launcherConfig.esper.enabled) {
            onResolved(null)
            return
        }

        val token = launcherConfig.esper.apiToken?.trim().orEmpty()

        esperSdkClient.isActivated { activationResult ->
            val isActive = activationResult.getOrNull() == true
            Log.d(tag, "Esper SDK activation status=$isActive")
            if (isActive || token.isBlank()) {
                fetchIdentityDisplay(onResolved)
                return@isActivated
            }

            esperSdkClient.activate(token) {
                Log.d(tag, "Esper SDK activation attempted; success=${it.isSuccess}")
                fetchIdentityDisplay(onResolved)
            }
        }
    }

    private fun fetchIdentityDisplay(onResolved: (DeviceIdentityDisplay?) -> Unit) {
        esperSdkClient.fetchDeviceIdentity { identityResult ->
            val identity = identityResult.getOrNull()
            if (identity == null) {
                Log.w(tag, "Esper identity unavailable: ${identityResult.exceptionOrNull()?.message}")
                onResolved(null)
                return@fetchDeviceIdentity
            }

            val display = buildDisplay(
                alias = identity.esperAliasName,
                deviceName = identity.esperDeviceName
            )

            if (shouldFetchCloudMetadata(display, identity)) {
                val cached = cachedCloudIdentity
                if (cached != null && !isCloudCacheExpired()) {
                    Log.d(tag, "Using cached Esper cloud alias/name")
                    onResolved(mergeDisplay(display, cached))
                    return@fetchDeviceIdentity
                }

                val backendClient = backendAliasClient
                val enterpriseId = launcherConfig.esper.enterpriseId?.trim().orEmpty()
                if (backendClient == null && (directApiClient == null || enterpriseId.isBlank())) {
                    onResolved(display)
                    return@fetchDeviceIdentity
                }

                Thread {
                    val cloudResult = if (backendClient != null) {
                        backendClient.fetchAlias(
                            deviceIdHint = identity.deviceId,
                            serialNumber = identity.serialNo,
                            sdkUuid = identity.uuid,
                            enterpriseId = launcherConfig.esper.enterpriseId
                        )
                    } else {
                        directApiClient!!.fetchAlias(
                            enterpriseId = enterpriseId,
                            deviceIdHint = identity.deviceId,
                            serialNumber = identity.serialNo,
                            sdkUuid = identity.uuid
                        )
                    }

                    val cloud = cloudResult.getOrNull()
                    if (cloud != null) {
                        val cloudDisplay = buildDisplay(alias = cloud.alias, deviceName = cloud.deviceName)
                            ?: DeviceIdentityDisplay()

                        cachedCloudIdentity = cloudDisplay
                        lastCloudFetchEpochMs = System.currentTimeMillis()
                        Log.d(tag, "Resolved alias/name from cloud enrichment path")
                        onResolved(mergeDisplay(display, cloudDisplay))
                    } else {
                        Log.w(tag, "Cloud alias lookup failed: ${cloudResult.exceptionOrNull()?.message}")
                        onResolved(display)
                    }
                }.start()
                return@fetchDeviceIdentity
            }

            onResolved(display)
        }
    }

    private fun shouldFetchCloudMetadata(
        currentDisplay: DeviceIdentityDisplay?,
        sdkIdentity: EsperDeviceIdentity
    ): Boolean {
        val aliasVisible = launcherConfig.esper.useManagedConfigAlias
        val deviceVisible = launcherConfig.esper.showDeviceName
        if (!aliasVisible && !deviceVisible) {
            return false
        }

        val backendConfigured = !launcherConfig.esper.backendAliasEndpoint.isNullOrBlank()
        val directConfigured = !launcherConfig.esper.tenantUrl.isNullOrBlank() &&
            !launcherConfig.esper.apiToken.isNullOrBlank() &&
            !launcherConfig.esper.enterpriseId.isNullOrBlank()
        if (!backendConfigured && !directConfigured) {
            return false
        }

        if (currentDisplay == null) {
            return true
        }

        val sdkAlias = sdkIdentity.esperAliasName?.trim().orEmpty()
        val sdkDeviceName = sdkIdentity.esperDeviceName?.trim().orEmpty()
        if (aliasVisible && sdkAlias.isBlank()) {
            return true
        }
        if (deviceVisible && sdkDeviceName.isBlank()) {
            return true
        }

        val primary = currentDisplay.primary.orEmpty()
        if (primary.isBlank()) {
            return true
        }

        val sdkDeviceId = sdkIdentity.deviceId.trim()
        val looksLikeRawDeviceName = sdkDeviceName.isNotBlank() && primary.equals(sdkDeviceName, ignoreCase = true)
        val looksLikeRawDeviceId = primary.equals(sdkDeviceId, ignoreCase = true)

        return (looksLikeRawDeviceName || looksLikeRawDeviceId) && currentDisplay.secondary.isNullOrBlank()
    }

    private fun shouldShowAnyIdentity(): Boolean {
        return !launcherConfig.esper.identityHardDisable &&
            (launcherConfig.esper.useManagedConfigAlias || launcherConfig.esper.showDeviceName)
    }

    private fun buildDisplay(alias: String?, deviceName: String?): DeviceIdentityDisplay? {
        val normalizedAlias = alias?.trim().orEmpty()
        val normalizedDeviceName = deviceName?.trim().orEmpty()

        val visibleAlias = if (launcherConfig.esper.useManagedConfigAlias) normalizedAlias else ""
        val visibleDeviceName = if (launcherConfig.esper.showDeviceName) normalizedDeviceName else ""

        return when {
            visibleAlias.isNotEmpty() && visibleDeviceName.isNotEmpty() && !visibleAlias.equals(visibleDeviceName, ignoreCase = true) -> {
                DeviceIdentityDisplay(primary = visibleAlias, secondary = visibleDeviceName)
            }
            visibleAlias.isNotEmpty() -> {
                DeviceIdentityDisplay(primary = visibleAlias)
            }
            visibleDeviceName.isNotEmpty() -> {
                DeviceIdentityDisplay(primary = visibleDeviceName)
            }
            else -> null
        }
    }

    private fun isCloudCacheExpired(): Boolean {
        return System.currentTimeMillis() - lastCloudFetchEpochMs > 10 * 60 * 1000
    }

    private fun mergeDisplay(base: DeviceIdentityDisplay?, cloud: DeviceIdentityDisplay): DeviceIdentityDisplay {
        return DeviceIdentityDisplay(
            primary = cloud.primary ?: base?.primary,
            secondary = cloud.secondary ?: base?.secondary
        )
    }

}
