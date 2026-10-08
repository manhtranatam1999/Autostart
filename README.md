# AutoStartFCMHook API 102

Modern LSPosed/libxposed module for the user's Xiaomi 17 / HyperOS 3.0.44.0.WPCCNXM / Android 16.

## Targets
- com.zing.zalo
- com.google.android.gms
- com.google.android.gsf

## Hook
`android.miui.AppOpsUtils.getApplicationAutoStart(Context, String)`

For the three target packages the hook returns `0`, the Xiaomi/MIUI convention used by this API for AutoStart enabled.

## Modern API 102
This version deliberately does NOT use `de.robv.android.xposed.*`, `assets/xposed_init`, or legacy Xposed metadata. It uses:
- `io.github.libxposed:api:102.0.0`
- `XposedModule`
- `META-INF/xposed/java_init.list`
- `META-INF/xposed/module.prop`
- `META-INF/xposed/scope.list`
- interceptor-chain `hook(Method).intercept(...)`

## Scope
`system`, `com.miui.securitycenter`, `com.zing.zalo`, `com.google.android.gms`, `com.google.android.gsf`.

## Important
This is an AutoStart result hook. It does not itself disable Doze, App Standby, battery restrictions, LMKD, or every Xiaomi background policy. Therefore it is intended to remove AutoStart as one possible FCM blocker, not to guarantee permanent process residency.

Build with Java 17 and Android SDK. GitHub Actions workflow is included.
