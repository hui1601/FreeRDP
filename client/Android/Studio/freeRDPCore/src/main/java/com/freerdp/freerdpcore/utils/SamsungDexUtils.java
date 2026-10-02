/*
   Samsung Dex Utils

   Copyright 2026 HeonHee Dong

   This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
   If a copy of the MPL was not distributed with this file, You can obtain one at
   http://mozilla.org/MPL/2.0/.
*/
package com.freerdp.freerdpcore.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

import java.lang.reflect.Method;

public class SamsungDexUtils {
    private static final String TAG = SamsungDexUtils.class.getSimpleName();
    private static Method requestMetaKeyEventMethod;
    private static Method registerSystemKeyEventMethod;
    private static Method unregisterSystemKeyEventMethod;
    private static Object manager;
    private static boolean isAvailable = false;

    static {
        try {
            Class<?> clazz = Class.forName("com.samsung.android.view.SemWindowManager");
            Method obtain = clazz.getMethod("getInstance");
            requestMetaKeyEventMethod = clazz.getDeclaredMethod("requestMetaKeyEvent", android.content.ComponentName.class, boolean.class);
            registerSystemKeyEventMethod = clazz.getDeclaredMethod("registerSystemKeyEvent", int.class, ComponentName.class, int.class);
            unregisterSystemKeyEventMethod = clazz.getDeclaredMethod("unregisterSystemKeyEvent", int.class, ComponentName.class);
            manager = obtain.invoke(null);
            isAvailable = true;
            android.util.Log.d(TAG, "com.samsung.android.view.SemWindowManager is available");
        } catch (Exception ignored) {
            requestMetaKeyEventMethod = null;
            registerSystemKeyEventMethod = null;
            unregisterSystemKeyEventMethod = null;
            manager = null;
            isAvailable = false;
            android.util.Log.d(TAG, "com.samsung.android.view.SemWindowManager is not available");
        }
    }

    static public boolean available() {
        return isAvailable && manager != null;
    }

    static public void dexMetaKeyCapture(Activity activity, boolean enable) {
        if (!available()) {
            Log.d(TAG, "dexMetaKeyCapture: SemWindowManager not available");
            return;
        }

        try {
            requestMetaKeyEventMethod.invoke(manager, activity.getComponentName(), enable);
            Log.d(TAG, "dexMetaKeyCapture: Successfully invoked requestMetaKeyEvent(enable=" + enable + ")");
        } catch (Exception it) {
            Log.d(TAG, "Could not call com.samsung.android.view.SemWindowManager.requestMetaKeyEvent");
            Log.d(TAG, it.getClass().getCanonicalName() + ": " + it.getMessage());
        }
    }

    /*
     * Registers a system key event to be handled by the component.
     * Corresponds to SemWindowManager.registerSystemKeyEvent(int i, ComponentName componentName, int i2)
     */
    static public void registerSystemKeyEvent(int keyCode, ComponentName componentName, int displayId) {
        if (!available() || registerSystemKeyEventMethod == null) {
            Log.d(TAG, "registerSystemKeyEvent: SemWindowManager not available or method not found");
            return;
        }

        try {
            registerSystemKeyEventMethod.invoke(manager, keyCode, componentName, displayId);
            Log.i(TAG, "registerSystemKeyEvent: " + componentName + " keyCode=" + keyCode);
        } catch (Exception e) {
            Log.e(TAG, "Failed registerSystemKeyEvent: " + e.getCause() + ", " + e.getMessage());
        }
    }

    /*
     * Unregisters a system key event.
     * Corresponds to SemWindowManager.unregisterSystemKeyEvent(int i, ComponentName componentName)
     */
    static public void unregisterSystemKeyEvent(int keyCode, ComponentName componentName) {
         if (!available() || unregisterSystemKeyEventMethod == null) {
            Log.d(TAG, "unregisterSystemKeyEvent: SemWindowManager not available or method not found");
            return;
        }

        try {
            unregisterSystemKeyEventMethod.invoke(manager, keyCode, componentName);
            Log.i(TAG, "unregisterSystemKeyEvent: " + componentName + " keyCode=" + keyCode);
        } catch (Exception e) {
            Log.e(TAG, "Failed unregisterSystemKeyEvent: " + e.getCause() + ", " + e.getMessage());
        }
    }

    @SuppressWarnings("JavaReflectionMemberAccess")
    public static boolean checkDeXEnabled(Context ctx) {
        Configuration config = ctx.getResources().getConfiguration();
        try {
            Class<?> c = config.getClass();
            return c.getField("SEM_DESKTOP_MODE_ENABLED").getInt(c)
                    == c.getField("semDesktopModeEnabled").getInt(config);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException ignored) {}
        return false;
    }
}
