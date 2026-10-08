package com.manhtranatam.autostartfcm;

import android.util.Log;

import java.lang.reflect.Method;

import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface;

public final class AutoStartHook extends XposedModule {

    private static final String TAG = "AutoStartFCMHook";

    private static final String APP_OPS =
            "android.miui.AppOpsUtils";

    private static final String ZALO =
            "com.zing.zalo";

    private static final String GMS =
            "com.google.android.gms";

    private static final String GSF =
            "com.google.android.gsf";

    /*
     * Log ngay khi class được khởi tạo.
     * Mục đích: xác nhận LSPosed thực sự load class này.
     */
    public AutoStartHook() {
        super();
        Log.i(TAG, "========== AutoStartHook CLASS LOADED ==========");
    }

    @Override
    public void onModuleLoaded(
            XposedModuleInterface.ModuleLoadedParam param) {

        Log.i(TAG,
                "MODULE LOADED | process="
                        + param.getProcessName()
                        + " | systemServer="
                        + param.isSystemServer());

        log(
                Log.INFO,
                TAG,
                "loaded: "
                        + param.getProcessName()
                        + ", systemServer="
                        + param.isSystemServer()
        );
    }

    @Override
    public void onSystemServerStarting(
            XposedModuleInterface.SystemServerStartingParam param) {

        Log.i(TAG,
                "SYSTEM SERVER STARTING - installing AutoStart hook");

        hookAutoStart(
                param.getClassLoader(),
                "system_server"
        );
    }

    @Override
    public void onPackageReady(
            XposedModuleInterface.PackageReadyParam param) {

        String pkg = param.getPackageName();

        Log.i(TAG,
                "PACKAGE READY: " + pkg);

        if (ZALO.equals(pkg)
                || GMS.equals(pkg)
                || GSF.equals(pkg)
                || "com.miui.securitycenter".equals(pkg)) {

            hookAutoStart(
                    param.getClassLoader(),
                    pkg
            );
        }
    }

    private void hookAutoStart(
            ClassLoader classLoader,
            String where) {

        try {

            Log.i(TAG,
                    "Attempting AutoStart hook in: "
                            + where);

            Class<?> clazz = Class.forName(
                    APP_OPS,
                    false,
                    classLoader
            );

            Log.i(TAG,
                    "Found class: "
                            + APP_OPS
                            + " in "
                            + where);

            Method method =
                    clazz.getDeclaredMethod(
                            "getApplicationAutoStart",
                            android.content.Context.class,
                            String.class
                    );

            method.setAccessible(true);

            Log.i(TAG,
                    "Found method: getApplicationAutoStart in "
                            + where);

            hook(method)
                    .setExceptionMode(
                            XposedInterface.ExceptionMode.PROTECTIVE
                    )
                    .setId(
                            "autostart-result"
                    )
                    .intercept(chain -> {

                        Object pkgArg =
                                chain.getArg(1);

                        if (pkgArg instanceof String
                                && isTarget((String) pkgArg)) {

                            String pkg =
                                    (String) pkgArg;

                            Log.i(TAG,
                                    "AUTOSTART OVERRIDE -> "
                                            + pkg
                                            + " = 0");

                            return 0;
                        }

                        return chain.proceed();
                    });

            Log.i(TAG,
                    "========== HOOK INSTALLED =========="
                            + " | "
                            + where);

            log(
                    Log.INFO,
                    TAG,
                    "hooked getApplicationAutoStart in "
                            + where
            );

        } catch (NoSuchMethodException e) {

            Log.e(TAG,
                    "AutoStart method NOT FOUND in "
                            + where,
                    e);

            log(
                    Log.WARN,
                    TAG,
                    "AutoStart method not found in "
                            + where,
                    e
            );

        } catch (Throwable t) {

            Log.e(TAG,
                    "HOOK FAILED in "
                            + where,
                    t);

            log(
                    Log.ERROR,
                    TAG,
                    "hook failed in "
                            + where,
                    t
            );
        }
    }

    private static boolean isTarget(
            String pkg) {

        return ZALO.equals(pkg)
                || GMS.equals(pkg)
                || GSF.equals(pkg);
    }
}
