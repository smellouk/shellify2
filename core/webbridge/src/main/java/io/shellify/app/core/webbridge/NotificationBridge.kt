package io.shellify.app.core.webbridge

/**
 * Builds the JS payload that prototype-patches window.Notification for System WebView apps
 * so that PWA notification calls are intercepted and forwarded to ShellifyBridge.
 *
 * The script is idempotent: re-injection on repeated page loads is safe because
 * window.__shellifyNotificationLoaded acts as a guard.
 */
object NotificationBridge {

    fun buildScript(): String = SCRIPT

    private val SCRIPT = """
(function() {
  if (window.__shellifyNotificationLoaded) return;
  window.__shellifyNotificationLoaded = true;
  const OriginalNotification = window.Notification;
  window.Notification = function(title, options) {
    if (window.ShellifyBridge) {
      window.ShellifyBridge.onNotification(
        title || '',
        (options && options.body) || '',
        (options && options.icon) || ''
      );
    }
    if (OriginalNotification) {
      try { return new OriginalNotification(title, options); } catch(e) {}
    }
  };
  window.Notification.permission = 'granted';
  window.Notification.requestPermission = function(cb) {
    if (cb) cb('granted');
    return Promise.resolve('granted');
  };
})();
""".trimIndent()
}
