# Deeplinks

## Overview

Shellify supports two deep link schemes for adding apps externally:

| Scheme | Example | Status |
|---|---|---|
| Custom scheme | `shellify://add?url=https://example.com&name=Example` | Works now, no server needed |
| HTTPS App Link | `https://shellify.app/add?url=https://example.com&name=Example` | Requires `assetlinks.json` hosted at the domain |

Without `assetlinks.json`, Android cannot verify that `shellify.app` belongs to the app. HTTPS links open in the browser instead of routing directly into Shellify. The custom `shellify://` scheme works immediately regardless.

---

## How Android App Links Work

Android verifies domain ownership **at install time** by fetching:

```
https://shellify.app/.well-known/assetlinks.json
```

If the file is present and valid, Android trusts the app to intercept all `https://shellify.app/...` links with no browser prompt. Without it, the link falls through to the browser.

---

## Fix: Host assetlinks.json

Place the following file at exactly:

```
https://shellify.app/.well-known/assetlinks.json
```

Content:

```json
[{
  "relation": ["delegate_permission/common.handle_all_urls"],
  "target": {
    "namespace": "android_app",
    "package_name": "io.shellify.app",
    "sha256_cert_fingerprints": ["YOUR_SIGNING_CERT_FINGERPRINT"]
  }
}]
```

### Get your SHA-256 fingerprint

Release keystore:
```bash
keytool -list -v -keystore your-release-key.jks
```

Debug keystore (testing only):
```bash
keytool -list -v -keystore ~/.android/debug.keystore \
  -alias androiddebugkey \
  -storepass android \
  -keypass android | grep SHA256
```

Replace `YOUR_SIGNING_CERT_FINGERPRINT` with the output, e.g.:
```
"14:6D:E9:83:C5:73:06:50:D8:EE:B9:95:2F:34:FC:64:16:A0:83:42:E6:1D:BE:A8:8A:04:96:B2:3F:CF:44:E5"
```

---

## Verify

Once the file is live, check reachability:

```
https://shellify.app/.well-known/assetlinks.json
```

Validate with Google's Digital Asset Links API:

```
https://digitalassetlinks.googleapis.com/v1/statements:list?source.web.site=https://shellify.app&relation=delegate_permission/common.handle_all_urls
```

---

## Current State

- `shellify://` custom scheme: **works today** — primary deeplink for sharing, no server dependency
- `https://shellify.app/...` App Link: **falls back to browser** until `assetlinks.json` is hosted

If there is no web server at `shellify.app`, the custom scheme is the reliable primary deeplink and the HTTPS variant is a browser-friendly fallback for sharing contexts (SMS, email, QR codes).
