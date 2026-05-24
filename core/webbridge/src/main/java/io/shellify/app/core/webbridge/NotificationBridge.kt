package io.shellify.app.core.webbridge

/**
 * Builds JS payloads for System WebView notification handling.
 *
 * [buildScript] — full bridge for GRANTED state: patches window.Notification so that
 * `new Notification()` calls are forwarded to ShellifyBridge.onNotification().
 *
 * [buildPermissionRequestScript] — lightweight interceptor for NOT_ASKED state: patches
 * window.Notification.requestPermission() to route the request through the native dialog
 * via ShellifyBridge.requestNotificationPermission(). The native side resolves the Promise
 * by calling window.__shellifyResolvePermission(granted) after the dialog closes.
 *
 * Both scripts are idempotent via their own load guards.
 */
object NotificationBridge {

    fun buildScript(): String = SCRIPT

    fun buildPermissionRequestScript(): String = PERMISSION_REQUEST_SCRIPT

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

    private val PERMISSION_REQUEST_SCRIPT = """
(function() {
  if (window.__shellifyPermRequestLoaded) return;
  window.__shellifyPermRequestLoaded = true;
  var _resolve = null;
  window.__shellifyResolvePermission = function(granted) {
    window.Notification.permission = granted ? 'granted' : 'denied';
    if (_resolve) { _resolve(granted ? 'granted' : 'denied'); _resolve = null; }
  };
  var Orig = window.Notification;
  window.Notification = function(title, options) {
    if (Orig) { try { return new Orig(title, options); } catch(e) {} }
  };
  window.Notification.permission = 'default';
  window.Notification.requestPermission = function(cb) {
    return new Promise(function(resolve) {
      _resolve = function(r) { if (cb) cb(r); resolve(r); };
      if (window.ShellifyBridge) {
        window.ShellifyBridge.requestNotificationPermission();
      } else {
        window.__shellifyResolvePermission(false);
      }
    });
  };
})();
""".trimIndent()
}
