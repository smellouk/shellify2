# Analytics — Usage Insights

**Privacy principle:** all data stays on-device, inside the existing SQLCipher-encrypted database. No
external telemetry, no network calls.

---

## What to Track

| Metric                     | Description                                        |
|----------------------------|----------------------------------------------------|
| **Session duration**       | Time spent inside each WebViewActivity instance    |
| **Launch count**           | How many times a PWA was opened                    |
| **Last used**              | Timestamp of the most recent session               |
| **Ads blocked (per-app)**  | Running counter incremented by `AdBlocker`         |
| **Daily / weekly totals**  | Aggregated time per app rolled up to day buckets   |

---

## Data Model

### New table: `sessions`

```sql
CREATE TABLE sessions (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    webAppId   INTEGER NOT NULL REFERENCES web_apps(id) ON DELETE CASCADE,
    startedAt  INTEGER NOT NULL,   -- epoch ms
    endedAt    INTEGER,            -- null if crash/kill before onStop
    adsBlocked INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX idx_sessions_webAppId ON sessions(webAppId);
CREATE INDEX idx_sessions_startedAt ON sessions(startedAt);
```

### Extend `web_apps` (migration 13)

```sql
ALTER TABLE web_apps ADD COLUMN totalAdsBlocked INTEGER NOT NULL DEFAULT 0;
```

Keeping a denormalized `totalAdsBlocked` in `web_apps` avoids a full-table aggregation on every
home-screen load.

---

## Architecture

```
core/analytics/              ← new module (mirrors core/database layout)
  SessionEntity.kt           ← Room entity
  SessionDao.kt              ← insert / update / aggregate queries
  AnalyticsRepository.kt     ← interface (domain)
  AnalyticsRepositoryImpl.kt ← Room-backed implementation
  AnalyticsUseCases.kt       ← GetAppStats, GetTopApps, GetDailySummary

feature/analytics/           ← new screen
  AnalyticsScreen.kt         ← Compose UI
  AnalyticsViewModel.kt
```

The new module depends on `core/domain` and `core/database`.  
`AppDatabase` gains `sessionDao()` and migration 12→13.

---

## Session Recording

Hook into `WebViewActivity` lifecycle:

```kotlin
// onResume — record session start
private var sessionId: Long = -1
private var sessionStart: Long = 0

override fun onResume() {
    super.onResume()
    sessionStart = System.currentTimeMillis()
    lifecycleScope.launch {
        sessionId = analyticsRepository.startSession(webAppId = appId)
    }
}

// onStop — close the session with elapsed time + ads blocked this session
override fun onStop() {
    super.onStop()
    val duration = System.currentTimeMillis() - sessionStart
    lifecycleScope.launch {
        analyticsRepository.endSession(
            id        = sessionId,
            endedAt   = System.currentTimeMillis(),
            adsBlocked = webViewServiceProvider.adsBlockedThisSession(),
        )
    }
}
```

`AdBlocker` already intercepts requests. Add an in-memory counter there and expose
`adsBlockedThisSession()` via `WebViewServiceProvider`.

---

## Aggregate Queries (SessionDao)

```kotlin
@Query("""
    SELECT webAppId,
           COUNT(*)                             AS launchCount,
           SUM(endedAt - startedAt)             AS totalMs,
           MAX(startedAt)                       AS lastUsedAt,
           SUM(adsBlocked)                      AS totalAds
    FROM   sessions
    WHERE  webAppId = :id AND endedAt IS NOT NULL
""")
fun statsForApp(id: Long): Flow<AppStats>

@Query("""
    SELECT webAppId, SUM(endedAt - startedAt) AS totalMs
    FROM   sessions
    WHERE  endedAt IS NOT NULL
      AND  startedAt >= :since
    GROUP  BY webAppId
    ORDER  BY totalMs DESC
    LIMIT  :limit
""")
fun topApps(since: Long, limit: Int): Flow<List<AppUsage>>

@Query("""
    SELECT (startedAt / 86400000) AS dayBucket,
           webAppId,
           SUM(endedAt - startedAt) AS totalMs
    FROM   sessions
    WHERE  webAppId = :id AND endedAt IS NOT NULL
      AND  startedAt >= :since
    GROUP  BY dayBucket
    ORDER  BY dayBucket
""")
fun dailyBreakdown(id: Long, since: Long): Flow<List<DayUsage>>
```

---

## UI — Analytics Screen

Entry point: new **Insights** tab / button on `HomeScreen` (or inside `AppSettingsScreen` for
per-app stats).

### Global Insights view

| Widget             | Content                                                |
|--------------------|--------------------------------------------------------|
| **Top 5 apps**     | Horizontal bar chart — total time this week            |
| **Total time**     | Sum across all apps today / this week                  |
| **Ads blocked**    | Grand total (sum of `totalAdsBlocked` from web_apps)   |
| **Longest streak** | Most consecutive days at least one app was launched    |

### Per-app Stats view (inside app's Settings sheet)

| Widget             | Content                                                |
|--------------------|--------------------------------------------------------|
| **Launch count**   | Total opens, past 7 days                               |
| **Time spent**     | Today / this week / all-time                           |
| **Ads blocked**    | All-time counter from `totalAdsBlocked`                |
| **Daily timeline** | 7-day spark-line chart (Compose Canvas — no lib needed)|

---

## Privacy Controls

| Control               | Location                     | Behaviour                                     |
|-----------------------|------------------------------|-----------------------------------------------|
| **Clear all stats**   | Global Settings → Privacy    | DELETE FROM sessions; reset totalAdsBlocked   |
| **Clear app stats**   | Per-app Settings             | DELETE FROM sessions WHERE webAppId = :id     |
| **Disable tracking**  | Global Settings → Privacy    | Boolean DataStore flag; skips onResume hook   |
| **Data stays local**  | N/A — architectural          | No network permission required for this module|

---

## Migration Path

| Step | What                                                                  |
|------|-----------------------------------------------------------------------|
| 1    | Add `SessionEntity` + `SessionDao` to `core/database`                 |
| 2    | Migration 12→13: create `sessions` table + `totalAdsBlocked` column   |
| 3    | Wire `analyticsRepository` into DI (Hilt module)                      |
| 4    | Hook `WebViewActivity.onResume/onStop`                                 |
| 5    | Add `adsBlockedThisSession()` counter to `AdBlocker`/`WebViewServiceProvider` |
| 6    | Build `feature/analytics` with `AnalyticsScreen` + `AnalyticsViewModel` |
| 7    | Add entry point to `HomeScreen` (Insights icon in top bar)            |
| 8    | Add clear-stats controls to Privacy settings                          |

---

## Highest-Leverage Starting Point

Start with **steps 1-4** (data collection) before any UI — once sessions are being recorded in
production the query layer and charts follow with no further schema changes.
