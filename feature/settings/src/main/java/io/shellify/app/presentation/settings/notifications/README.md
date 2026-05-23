# notifications

Per-app notification history screen.

## Contents

- `NotificationHistoryViewModel` — loads `PwaNotification` list for a given `appId` via `GetNotificationsUseCase`; exposes `NotificationHistoryUiState`.
- `NotificationHistoryScreen` — Compose screen with TopAppBar, LazyColumn of notification items with relative timestamps, empty state, and 30-day footer.
- `NotificationHistoryContent` — internal composable extracted for screenshot testability.

## Navigation

Reached from `AppSettingsScreen` via the "Notification history" row.
Route: `Screen.NotificationHistory` (`notification_history/{appId}`).
