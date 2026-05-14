# Browser Replacement & Share-to-Shellify

Two complementary flows: users can share any link *from* another app *into* a Shellify PWA, and
share a Shellify app's current URL *out* with a deep link that opens it back in the right PWA on
any Android device.

---

## Flow 1 — Open URL in Shellify (Browser Replacement)

Android treats any app that registers `ACTION_VIEW` + `BROWSABLE` for `http/https` as a candidate
browser. When a user taps a link in Gmail, Slack, or any other app, Shellify appears alongside
Chrome in the "Open with…" chooser.

### What happens after the user picks Shellify

1. A new **LinkDispatcherActivity** receives the URL.
2. It checks whether the URL matches any installed PWA's registered domain:
   - **Match found** → opens `WebViewActivity` for that app, navigating to the URL.
   - **Multiple matches** → shows a bottom sheet: "Open in which app?"
   - **No match** → shows "Add as new app?" prompt (reuses existing add-app flow) or "Open in
     temporary session" (isolation-mode, no persistence).
3. The back stack is handled cleanly: the dispatcher finishes itself after launching
   `WebViewActivity` so the back button returns the user to the originating app.

### Manifest change (one new intent-filter on LinkDispatcherActivity)

```xml
<activity
    android:name=".presentation.link.LinkDispatcherActivity"
    android:exported="true"
    android:launchMode="singleTask"
    android:noHistory="true"
    android:theme="@style/Theme.Shellify.Translucent">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="http" />
        <data android:scheme="https" />
    </intent-filter>
    <intent-filter>
        <action android:name="android.intent.action.SEND" />
        <category android:name="android.intent.category.DEFAULT" />
        <data android:mimeType="text/plain" />
    </intent-filter>
</activity>
```

The `ACTION_SEND` filter catches the Android share sheet ("Share URL" from Chrome or any app that
exposes `text/plain`).

### Domain matching

Each `WebApp` already stores a `url` field. Extract the host from that URL and compare it against
the incoming link's host. Implement this as a use case in `core/domain`:

```kotlin
class FindAppsForUrlUseCase(private val repo: WebAppRepository) {
    suspend operator fun invoke(url: String): List<WebApp> {
        val host = Uri.parse(url).host ?: return emptyList()
        return repo.getAll().filter { Uri.parse(it.url).host == host }
    }
}
```

### New screen: LinkDispatcherActivity

Thin Activity (no Fragment, no NavGraph — it's transient):

- If exactly one match: launch `WebViewActivity` immediately, no UI shown.
- If multiple matches or no match: display a `ModalBottomSheet` (Compose) with the list of
  matching apps and an "Add as new app" / "Open temporarily" option.
- `noHistory="true"` ensures it never enters the back stack.

---

## Flow 2 — Share URL into a Specific Shellify App (Deep Link)

Extends the existing `shellify://` scheme with a new host: `open`.

```
shellify://open?appId=42&url=https%3A%2F%2Fexample.com%2Farticle
```

| Parameter | Required | Description                                      |
|-----------|----------|--------------------------------------------------|
| `appId`   | yes      | The `WebApp.id` of the target app                |
| `url`     | no       | URL to navigate to (defaults to the app's root)  |

An HTTPS equivalent for cross-device QR sharing:

```
https://shellify.app/open?appId=42&url=<base64url-encoded-url>
```

### DeepLinkHandler extension

```kotlin
fun parseOpen(uri: Uri): OpenCommand? {
    val isCustom  = uri.scheme == "shellify" && uri.host == "open"
    val isHttps   = uri.scheme == "https" && uri.host == "shellify.app" && uri.path == "/open"
    if (!isCustom && !isHttps) return null
    val appId = uri.getQueryParameter("appId")?.toLongOrNull() ?: return null
    val url   = uri.getQueryParameter("url")?.let { decodeUrl(it) }
    return OpenCommand(appId = appId, url = url)
}

data class OpenCommand(val appId: Long, val url: String?)
```

`MainActivity.onNewIntent` (already handles `shellify://add`) gains a branch for `shellify://open`.
It looks up the `WebApp` by `appId`, then starts `WebViewActivity` with an optional override URL.

### Share sheet integration (inside AppShareSheet)

Add a second share option alongside the existing "copy link / QR code":

- **"Share to this app"** — copies or shares `shellify://open?appId=<id>&url=<current-url>`.
  Useful for sending a friend a link that opens directly in the right PWA.
- **"Share URL externally"** — existing behaviour, sends the bare URL via `ACTION_SEND`.

---

## UI Entry Points

| Where                        | Action                                            |
|------------------------------|---------------------------------------------------|
| System link tap / share      | LinkDispatcherActivity intercepts → match / add   |
| WebViewActivity toolbar      | Share button → AppShareSheet → "Share to app" new option |
| HomeScreen long-press on app | "Copy app link" → `shellify://open?appId=<id>`    |
| QR code scanner (external)   | Scans `shellify://open` link → opens correct app  |

---

## Privacy & Trust Considerations

| Concern                          | Mitigation                                               |
|----------------------------------|----------------------------------------------------------|
| Arbitrary URL injection via link | `LinkDispatcherActivity` only navigates to `https://`    |
| App spoofing via `appId`         | Look up by DB id — unknown ids show the add-app dialog   |
| Existing disclosure flow         | `DeepLinkConfirmDialog` (already built) gates add-app    |
| `shellify://open` bypass         | Route through same confirm dialog if app lock is set     |

The existing `DeepLinkConfirmDialog` (implemented in the deeplink feature) must gate the
`shellify://open` path the same way it gates `shellify://add`, so a malicious link cannot silently
navigate a locked PWA to an attacker-controlled URL.

---

## Implementation Steps

| Step | What                                                                                          |
|------|-----------------------------------------------------------------------------------------------|
| 1    | Add `LinkDispatcherActivity` + translucent theme; register in app manifest                    |
| 2    | Implement `FindAppsForUrlUseCase` in `core/domain`                                            |
| 3    | Build dispatcher UI (bottom sheet with app list + "Add as new" + "Open temporarily")          |
| 4    | Extend `DeepLinkHandler.parseOpen()` + `OpenCommand` model                                    |
| 5    | Handle `shellify://open` in `MainActivity.onNewIntent`                                        |
| 6    | Add "Share to app" option to `AppShareSheet`                                                  |
| 7    | Add "Copy app link" to HomeScreen long-press menu                                             |
| 8    | Gate `shellify://open` with `DeepLinkConfirmDialog` when target app has a lock set            |

---

## Highest-Leverage Starting Point

**Step 1–3 (LinkDispatcherActivity)** unlocks the browser-replacement use case immediately with
zero schema changes. Users get Shellify in the Android "Open with…" chooser right away.  
Steps 4–6 add the outbound sharing direction and can ship as a follow-up.
