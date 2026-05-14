# Tor / .onion Support

## Overview

Allow Shellify to route traffic through the Tor network, enabling access to `.onion` sites and providing an optional per-app Tor-only mode for maximum privacy.

---

## Implementation Notes

### Engine: GeckoView is the right path

Android's system WebView has no SOCKS5 proxy API — it cannot be pointed at a Tor daemon. GeckoView exposes `GeckoRuntime` network settings where a SOCKS5 proxy can be configured:

```kotlin
val runtimeSettings = GeckoRuntimeSettings.Builder()
    .proxySettings(
        ProxySettings.Builder()
            .type(ProxySettings.Type.SOCKS5)
            .host("127.0.0.1")
            .port(9050)
            .build()
    )
    .build()

val torRuntime = GeckoRuntime.create(context, runtimeSettings)
```

This means Tor mode is only available when `EngineType.GECKOVIEW` is selected for the app.

---

### Tor Daemon: tor-android

Bundle [tor-android](https://github.com/guardianproject/tor-android) by Guardian Project. It runs `tor` as a background process exposing a local SOCKS5 proxy on `127.0.0.1:9050`.

```gradle
implementation "info.guardianproject:tor-android:0.4.x"
```

The daemon is started on demand when the first Tor-enabled app is launched and stopped when no Tor apps are active.

---

### Per-App Toggle

Add a `useTor: Boolean` flag to the `WebApp` domain model:

```kotlin
// domain/model/WebApp.kt
data class WebApp(
    ...
    val useTor: Boolean = false,
)
```

In `GeckoEngineManager`, maintain two `GeckoRuntime` instances — one standard, one with the SOCKS5 proxy config — and select based on the flag:

```kotlin
fun getRuntimeFor(webApp: WebApp): GeckoRuntime {
    return if (webApp.useTor) torRuntime else standardRuntime
}
```

A new database migration is needed to add the `use_tor` column to the `web_apps` table.

---

### .onion URL Validation

`PwaAnalyzer` must skip manifest fetching for `.onion` URLs — they are unreachable without the proxy being active, and the standard HTTP client has no route to them:

```kotlin
// core/pwa/PwaAnalyzer.kt
if (url.host?.endsWith(".onion") == true) {
    return PwaManifest(name = url.host, startUrl = url.toString())
}
```

Similarly, favicon fetching should be skipped or deferred until the Tor session is established.

---

## Integration Points

| Concern | Hook-in point |
|---|---|
| SOCKS5 proxy config | `GeckoEngineManager` — separate `GeckoRuntime` for Tor sessions |
| Tor daemon lifecycle | New `TorManager` started/stopped around Tor-enabled app sessions |
| Per-app flag | `WebApp.useTor` + DB migration + settings UI toggle |
| .onion manifest skip | `PwaAnalyzer.analyze()` — early return for `.onion` hosts |
| Engine guard | Tor toggle in UI should be disabled when `EngineType.SYSTEM_WEBVIEW` is selected |

---

## Constraints

- Tor mode requires `EngineType.GECKOVIEW` — surface this clearly in the UI.
- First connection through Tor is slow (circuit establishment ~5–10s) — a loading indicator is needed.
- `.onion` v3 addresses are 56-character base32 strings — URL validation in `AddWebAppScreen` should accept them.
- Tor increases battery and data usage — worth surfacing in the per-app settings.
