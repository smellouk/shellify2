# Launcher Mode

## Overview

Shellify can replace the Android home screen entirely, making PWAs first-class home screen icons with no separate launcher needed. Since Shellify already has categories, isolation, and per-app locking, this becomes a privacy-first home screen where each PWA is a locked, isolated cell — something no stock launcher offers.

---

## How to Register as a Launcher

Add `HOME` and `DEFAULT` intent categories to an Activity in `AndroidManifest.xml`:

```xml
<activity android:name=".presentation.launcher.LauncherActivity">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.HOME" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</activity>
```

Android will then offer Shellify in the "Choose default launcher" dialog when the user presses the home button.

---

## What to Build

### LauncherActivity
A new `LauncherActivity` that becomes the home screen entry point:
- Displays the PWA grid (reuse existing home screen composables)
- Handles wallpaper rendering via `WallpaperManager`
- Intercepts the home button correctly (no back-stack issues)
- Respects `KeyguardManager` so it only shows after the lock screen

### Gesture Navigation
| Gesture | Action |
|---|---|
| Swipe up | Open app drawer (native Android apps) |
| Swipe down | Pull notification shade |
| Long press on icon | App shortcuts / edit / delete |
| Long press on background | Wallpaper / widget picker |

### Native App Drawer
A minimal drawer alongside the PWA grid so users can still reach installed Android apps — without this, the device becomes PWA-only which is too restrictive for most users.

### Wallpaper Support
```kotlin
val wallpaperManager = WallpaperManager.getInstance(context)
val wallpaperDrawable = wallpaperManager.drawable
```

---

## Key Integration Points

| Concern | Hook-in point |
|---|---|
| PWA grid + categories | Reuse `HomeScreen` composables and `HomeViewModel` |
| Per-app lock on launch | Existing `LockType` + `SecurityManager` flow |
| App isolation | `IsolationManager` — unchanged |
| Home button handling | `LauncherActivity` with `FLAG_ACTIVITY_NEW_TASK` and no history |
| Lock screen awareness | `KeyguardManager.isKeyguardLocked()` check on resume |

---

## Privacy Angle

Because Shellify already has per-app isolation and locking, a Shellify launcher enables a home screen where:
- Each PWA runs in its own sandboxed profile
- Individual apps can require biometric or password unlock
- The panic button (see `feature-ideas.md`) can wipe all app data from the home screen itself
- No third-party launcher ever sees which PWAs the user has installed
