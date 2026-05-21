# `feature:link-dispatcher`

> Transparent trampoline Activity that intercepts incoming URLs and routes them to the appropriate PWA or shows a chooser bottom sheet.

## Overview

`feature:link-dispatcher` handles three entry points defined in its `AndroidManifest.xml`:

1. **Generic web handler (INTG-01)** — ACTION_VIEW for any http/https URL. Android shows Shellify in the system "Open with" chooser.
2. **Share sheet (INTG-05)** — ACTION_SEND for text/plain. Users can share URLs directly into Shellify.
3. **`shellify://open` deep link (INTG-06)** — ACTION_VIEW for the custom deep link scheme.

The `https://shellify.app/open` App Links filter (INTG-09) is declared in the `:app` manifest (required for verified App Links).

## Architecture

`LinkDispatcherActivity` is a transparent trampoline Activity (no UI of its own). It delegates all logic to `LinkDispatcherViewModel` and observes commands to dispatch to:

- **Single match** → `WebViewIntentFactory.previewIntent` with `EXTRA_LOCK_TYPE` (lock gate respected)
- **Multiple matches** → Bottom sheet chooser via `DispatchSheet.Chooser`
- **No match** → Bottom sheet offering "Open temporarily" (`WebViewIntentFactory.incognitoIntent`) or "Add as new"
- **Dismiss** → Browser fallback via `PackageManager.resolveActivity`

## Dependencies

- `:core:domain` — `FindAppsForUrlUseCase`, `WebApp`
- `:core:deeplink` — `DeepLinkHandler.parseOpen()` / `buildOpen()`
- `:core:security` — `Base64Codec` (needed at call site for `DeepLinkHandler.parseOpen` default parameter)
- `:core:ui` — shared theme tokens, `Dimens`, `AppIcon`
- `:core:navigation` — `WebViewIntentFactory` interface (decouples this module from `:feature:webview`)

**Architecture constraint:** This module must NOT import `:core:database` directly. All data access goes through use cases in `:core:domain` (Konsist enforces this at build time).

## Key Classes

| Class | Purpose |
|---|---|
| `LinkDispatcherActivity` | Transparent trampoline; resolves URL, dispatches to ViewModel, handles commands |
| `LinkDispatcherViewModel` | State machine: dispatch → 0/1/N match routing |
| `LinkDispatcherSheet` | Material 3 bottom sheet; Chooser and NoMatch variants |
| `LinkDispatcherServiceProvider` | Interface implemented by `ShellifyApplication`; supplies `FindAppsForUrlUseCase` and the `WebViewIntentFactory` used to launch the WebView without a direct `:feature:webview` dependency |
| `LinkDispatcherUiState` | `data class` with `DispatchSheet` (None / Chooser / NoMatch) |
| `LinkDispatcherCommand` | One-shot side effects: LaunchApp, OpenTemporarily, AddAsNew, FallbackToBrowser |
