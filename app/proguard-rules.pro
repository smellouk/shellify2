-keepattributes SourceFile,LineNumberTable
-keep class io.shellify.app.domain.model.** { *; }
-keep class io.shellify.app.data.local.entity.** { *; }
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
# snakeyaml is a transitive dependency that references java.beans.* which doesn't exist on Android
-dontwarn java.beans.**
-dontwarn org.yaml.snakeyaml.**

# GeckoView: all Java classes are called back from native libxul.so via JNI.
# R8's aggressive optimizations remove methods it considers unreachable from Java,
# causing null function-pointer dereferences (SIGSEGV) in the native launcher thread.
-keep class org.mozilla.geckoview.** { *; }
-keep class org.mozilla.gecko.** { *; }
-keep interface org.mozilla.geckoview.** { *; }
-keepclassmembers class org.mozilla.geckoview.** { *; }
-keepclassmembers class * extends org.mozilla.geckoview.GeckoSession$* { *; }
-dontwarn org.mozilla.geckoview.**

# tor-android (Guardian Project): TorService runs in the :tor process and exposes
# STATUS_ON / STATUS_STARTING / STATUS_OFF / ACTION_STATUS / EXTRA_STATUS as static
# String constants compared at runtime in TorManager's BroadcastReceiver. R8 strips
# or renames these because it cannot see the intra-process broadcast sender. JNI
# callbacks from the native Tor binary (libTorAndroid.so) also require the full
# class hierarchy to remain intact.
-keep class org.torproject.jni.** { *; }
-keepclassmembers class org.torproject.jni.** { *; }
-dontwarn org.torproject.jni.**

# jtorctl: TorControlConnection is injected optionally (null in production) but its
# class body and signal() method must survive R8 so the Tor control-port socket path
# works when a non-null instance is wired in (e.g. new-identity requests).
-keep class net.freehaven.tor.control.** { *; }
-keepclassmembers class net.freehaven.tor.control.** { *; }
-dontwarn net.freehaven.tor.control.**
