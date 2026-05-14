# Feature Ideas

Codebase review date: 2026-05-14

## Privacy & Security

The isolation, crypto, and lock infrastructure is already solid — these layer naturally on top.

| Feature | What it adds | Hook-in point |
|---|---|---|
| **Stealth mode** | Disguise app icon/name in recents and launcher | `WebViewActivity`, `ShortcutManager` |
| **Cookie auto-wipe on close** | Clear session data after each use (per-app toggle) | `IsolationManager` |
| **Tracking protection** | Block known tracker domains beyond just ads | `AdBlocker` rule engine |
| **Privacy report** | Show count of blocked ads/trackers per session | `AdBlocker` |
| **Incognito sessions** | One-time ephemeral profile, no persistence | `IsolationManager` |
| **Panic button** | Quick-clear all app data with one gesture (hold power) | `IsolationManager`, Keystore |
| **Tor / .onion support** | Route traffic through the Tor network for .onion URLs and optional per-app Tor-only mode | `WebViewActivity`, `GeckoEngineManager`, SOCKS5 proxy config |

---

## Productivity & Browser

| Feature | What it adds | Hook-in point |
|---|---|---|
| **Custom JS/CSS injection** | Per-app scripts (dark mode force, UI tweaks) | `WebViewActivity` `onPageFinished` |
| **Reader mode** | Strip page clutter for reading | Inject into WebView |
| **Download manager** | Catch and save file downloads | `WebViewClient.onDownloadRequest` |
| **Password manager** | Auto-fill credentials per app | `WebViewActivity`, Keystore |
| **Force dark mode** | CSS color inversion for sites without dark support | `WebSettings` or CSS inject |
| **Font size override** | Per-app text scaling | `WebSettings.textZoom` |

---

## Notifications

Currently no notification bridge exists — this would be a meaningful differentiator.

| Feature | What it adds |
|---|---|
| **Web Push notifications** | Bridge PWA `Notification` API to Android notification channels — requires service worker support; GeckoView (already in stack) makes this feasible |
| **Badge count** | Show unread count on launcher icon per app |
| **Per-app notification DND** | Suppress notifications during configurable hours |

---

## Organization & Discovery

| Feature | What it adds | Hook-in point |
|---|---|---|
| **Tags** (vs. categories only) | Many-to-many labeling | New `tags` table + `webapp_tags` join |
| **Usage stats** | Time-in-app per PWA, last used | New `sessions` table or DataStore |
| **Home screen widget** | Pinned app grid without opening Shellify | `AppWidgetProvider` |
| **PWA directory / import** | Browse known PWAs and one-tap install | New data source + `PwaAnalyzer` |

---

## Cloud & Sync

Backup is local-only today.

| Feature | What it adds |
|---|---|
| **Cloud backup** | Sync `.pwab` files to Google Drive / custom WebDAV |
| **Cross-device import** | QR or link-based transfer of a single app config (sharing infrastructure already exists) |
| **Scheduled cloud backup** | Extend existing `WorkManager` job to upload |

---

## UI / Launch

| Feature | What it adds |
|---|---|
| **Floating bubble launcher** | Overlay bubble to switch apps without going to home screen |
| **Android widget** | 4×2 grid of app icons on home screen |
| **App shortcuts menu** | Long-press dynamic shortcuts via `ShortcutManager` API |
| **Shellify as a Launcher** | Replace the Android home screen entirely — PWAs appear as first-class app icons, swipe gestures, wallpaper support, and no separate launcher needed |

---

## Highest-Leverage Next Features

1. **Web Push notifications** — biggest gap vs. native apps; GeckoView makes it viable
2. **Custom JS/CSS injection** — power-user feature, minimal implementation effort
3. **Usage stats** — low effort, high perceived value, feeds into smart features later
4. **Cloud backup** — backup infrastructure is complete, just needs a cloud target
