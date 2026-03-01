# Esper Integration Compatibility Notes

This project is now prepared for future Esper SDK and Esper API integrations.

## What is already compatible

- Gradle repository includes Esper Device SDK artifact source:
  - `https://artifact.esper.io/artifactory/esper-device-sdk/`
- Launcher module includes Esper Device SDK dependency.
- Manifest includes Esper DPC package query:
  - `<queries><package android:name="io.shoonya.shoonyadpc" /></queries>`
- Manifest includes SDK compatibility override for imported library:
  - `<uses-sdk tools:overrideLibrary="esper.library" />`
- Managed config schema includes Esper-related keys for future customization.

## Integration building blocks in code

- `launcher/.../integration/esper/EsperDeviceSdkClient.kt`
  - activation check
  - SDK activation
  - managed-device identity retrieval (`deviceId`, `serialNo`, `uuid`)
- `launcher/.../integration/esper/EsperApiClient.kt`
  - REST request helper for Esper device metadata lookup by `enterpriseId + deviceId`
  - alias/device name extraction from common field variants

## Recommended production pattern

1. Use `EsperDeviceSdkClient` on-device to fetch `deviceId`.
2. Send `deviceId` to your backend.
3. Backend calls Esper Cloud API using secure server-side credentials.
4. Backend returns only safe metadata (alias/display name) to launcher.

Do not embed long-lived Esper API keys directly in the APK.

## Managed config keys added

- `esper_sdk_enabled`
- `esper_api_token`
- `esper_tenant_url` (direct test mode)
- `esper_backend_alias_endpoint`
- `esper_enterprise_id`
- `esper_identity_hard_disable`
- `esper_config_revision`
- `esper_use_managed_alias`
- `esper_show_device_name`
- `esper_device_alias`
- `esper_device_name`

Visibility controls:
- `esper_use_managed_alias=true|false`: show/hide alias
- `esper_show_device_name=true|false`: show/hide device name
- when both are `false`, launcher hides both identity lines
- `esper_identity_hard_disable=true`: hard-disables identity and skips SDK/cloud calls

Reliability note:
- Some DPCs merge managed-config updates. Push full Esper identity keys each rollout and increment `esper_config_revision` to keep behavior deterministic.

## Validation checklist

- App builds with Esper SDK dependency.
- Device with Shoonya/Esper DPC installed passes SDK activation flow.
- Alias fallback path works when managed alias is supplied.
- Backend API path works with token rotation and audit logs.
