package com.viet.autostartfcm;

import android.util.Log;

import java.lang.reflect.Method;

import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface;

public final class AutoStartHook extends XposedModule {

    private static final String TAG = "AutoStartFCMHook";
    private static final String APP_OPS = "android.miui.AppOpsUtils";

    private static final String ZALO = "com.zing.zalo";
    private static final String GMS = "com.google.android.gms";
    private static final String GSF = "com.google.android.gsf";

    @Override
    public void onModuleLoaded(
            XposedModuleInterface.ModuleLoadedParam param) {

        log(
                Log.INFO,
                TAG,
                "loaded: " + param.getProcessName()
                        + ", systemServer=" + param.isSystemServer()
        );
    }

    @Override
    public void onSystemServerStarting(
            XposedModuleInterface.SystemServerStartingParam param) {

        hookAutoStart(
                param.getClassLoader(),
                "system_server"
        );
    }

    @Override
    public void onPackageReady(
            XposedModuleInterface.PackageReadyParam param) {

        String pkg = param.getPackageName();

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
            ClassLoader cl,
            String where) {

        try {

            Class<?> clazz = Class.forName(
                    APP_OPS,
                    false,
                    cl
            );

            Method m = clazz.getDeclaredMethod(
                    "getApplicationAutoStart",
                    android.content.Context.class,
                    String.class
            );

            m.setAccessible(true);

            hook(m)
                    .setExceptionMode(
                            XposedInterface.ExceptionMode.PROTECTIVE
                    )
                    .setId("autostart-result")
                    .intercept(chain -> {

                        Object pkgArg = chain.getArg(1);

                        if (pkgArg instanceof String
                                && isTarget((String) pkgArg)) {

                            // Xiaomi/MIUI AppOpsUtils:
                            // 0 = AutoStart allowed.
                            return 0;
                        }

                        return chain.proceed();
                    });

            log(
                    Log.INFO,
                    TAG,
                    "hooked getApplicationAutoStart in " + where
            );

        } catch (NoSuchMethodException e) {

            log(
                    Log.WARN,
                    TAG,
                    "AutoStart method not found in " + where,
                    e
            );

        } catch (Throwable t) {

            log(
                    Log.ERROR,
                    TAG,
                    "hook failed in " + where,
                    t
            );
        }
    }

    private static boolean isTarget(String pkg) {

        return ZALO.equals(pkg)
                || GMS.equals(pkg)
                || GSF.equals(pkg);
    }
}
