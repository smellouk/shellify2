package io.shellify.app.core.engine

import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoSession

/**
 * Consolidates notification and permission delegate wiring so both [GeckoViewEngine.buildSession]
 * and [BackgroundNotificationService.initSession] use the same delegate logic without drift.
 *
 * Call [attach] immediately after opening a [GeckoSession] to install the delegates.
 */
object NotificationDelegateFactory {

    fun attach(session: GeckoSession, cb: BrowserEngineCallback) {
        session.permissionDelegate = buildPermissionDelegate(cb)
    }

    private fun buildPermissionDelegate(cb: BrowserEngineCallback): GeckoSession.PermissionDelegate =
        object : GeckoSession.PermissionDelegate {
            override fun onContentPermissionRequest(
                session: GeckoSession,
                perm: GeckoSession.PermissionDelegate.ContentPermission,
            ): GeckoResult<Int>? {
                if (perm.permission == GeckoSession.PermissionDelegate.PERMISSION_DESKTOP_NOTIFICATION) {
                    val result = GeckoResult<Int>()
                    cb.onNotificationPermissionRequested { granted ->
                        // VALUE_PROMPT instead of VALUE_DENY so GeckoView never caches a denial.
                        // The delegate is re-invoked on every requestPermission() call, which
                        // means the user can grant permission in Shellify settings and have it
                        // take effect immediately without clearing GeckoView's permission store.
                        result.complete(
                            if (granted) {
                                GeckoSession.PermissionDelegate.ContentPermission.VALUE_ALLOW
                            } else {
                                GeckoSession.PermissionDelegate.ContentPermission.VALUE_PROMPT
                            }
                        )
                    }
                    return result
                }
                return GeckoResult.fromValue(GeckoSession.PermissionDelegate.ContentPermission.VALUE_PROMPT)
            }
        }
}
