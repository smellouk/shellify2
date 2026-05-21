# core:navigation

> Cross-feature navigation contracts. Lets one feature module dispatch into another via interfaces declared here, instead of importing the target feature module directly.

## Overview

`core:navigation` provides lightweight interface contracts that decouple feature modules from each other at compile time. Feature modules that need to trigger navigation in another feature module depend on an interface here instead of importing the target feature directly — preserving the CLAUDE.md hard rule that `feature:*` must not import other `feature:*` modules.

## Interfaces

| Interface | Purpose |
|---|---|
| `WebViewIntentFactory` | Abstracts `WebViewActivity.previewIntent` / `WebViewActivity.incognitoIntent` for `feature:link-dispatcher` |

## Architecture

Depends only on `:core:domain`. Implementations live in `feature:*` modules; `ShellifyApplication` wires them up via service-provider interfaces.

```
feature:link-dispatcher → core:navigation (WebViewIntentFactory interface)
                                  ↑
           feature:webview (WebViewIntentFactoryImpl — wired by ShellifyApplication)
```

This pattern ensures the cross-feature dispatch path is tested end-to-end without requiring `feature:link-dispatcher` to know about `feature:webview` at compile time.
