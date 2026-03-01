# Freight Driver Launcher Roadmap

## P0 (Now - Partner Pilot Hardening)

- **Identity reliability**: Esper SDK activation + periodic refresh, footer placement aligned with cockpit mock.
- **Config safety**: validate/sanitize managed config before applying; clamp unsafe layout values.
- **Observability**: structured launcher events for startup, tile expand/collapse, and lock-blocked interactions.
- **Dispatch resilience**: keep auto-login redirect and WebView stability checks enabled.

## P1 (Next - Production Readiness)

- **Provider abstraction**: split identity, motion, dispatch, and tile-status providers behind interfaces.
- **State architecture**: centralize UI state with reducer/store to simplify predictability and testing.
- **Diagnostics panel**: in-app read-only diagnostics for config source, identity source, and last sync state.
- **Test matrix**: automated checks across Android 10/11/13+, Samsung/Zebra/Lenovo, enrolled vs non-enrolled.

## P2 (Scale - Delight and Operations)

- **Backend alias pipeline**: SDK deviceId -> backend Esper API lookup -> alias/device name cache.
- **Fleet analytics**: event ingestion dashboard for expand rates, lock blocks, and dispatch failure trends.
- **Remote kill-switches**: per-tile and per-feature emergency disable toggles.
- **Brand polish**: final visual pass and partner-branded themes with consistent spacing/type system.

## Current Sprint Focus

1. Ship P0 config safety + telemetry.
2. Validate on enrolled device and Esper-managed profile.
3. Prepare pilot checklist and acceptance report for partner.
