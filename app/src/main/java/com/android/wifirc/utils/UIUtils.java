package com.android.wifirc.utils;

import android.app.UiModeManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

public abstract class UIUtils {
    private static final String TAG = UIUtils.class.getSimpleName();
    public static final int NIGHT_UNDEFINED = -1;
    public static final int NIGHT_AUTO = 0;
    public static final int NIGHT_NO = 1;
    public static final int NIGHT_YES = 2;

    public UIUtils() {

    }

    public static int getSystemNightMode(@NonNull Context context) {
        int uiMode = context.getApplicationContext().getResources().getConfiguration().uiMode;
        int systemNightMode = uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (systemNightMode) {
            case Configuration.UI_MODE_NIGHT_YES:
                return NIGHT_YES;
            case Configuration.UI_MODE_NIGHT_NO:
                return NIGHT_NO;
            default:
                return NIGHT_UNDEFINED;
        }
    }

    public static int getStoredCustomNightMode(@NonNull Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String theme = sharedPreferences.getString("theme", "follow_system");
        switch (theme) {
            case "light":
                return NIGHT_NO;
            case "dark":
                return NIGHT_YES;
            case "follow_system":
                return NIGHT_AUTO;
            default:
                return NIGHT_UNDEFINED;
        }
    }

    public static int getCurrentCustomNightMode(UiModeManager uiModeManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            int nightMode = uiModeManager.getNightMode();
            switch (nightMode) {
                case UiModeManager.MODE_NIGHT_AUTO:
                    return NIGHT_AUTO;
                case UiModeManager.MODE_NIGHT_NO:
                    return NIGHT_NO;
                case UiModeManager.MODE_NIGHT_YES:
                    return NIGHT_YES;
                default:
                    return NIGHT_UNDEFINED;
            }
        } else {
            int nightMode = AppCompatDelegate.getDefaultNightMode();
            switch (nightMode) {
                case AppCompatDelegate.MODE_NIGHT_NO:
                    return NIGHT_NO;
                case AppCompatDelegate.MODE_NIGHT_YES:
                    return NIGHT_YES;
                case AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY:
                    return NIGHT_AUTO;
                default:
                    return NIGHT_UNDEFINED;
            }
        }
    }

    public static void setCurrentCustomNightMode(@NonNull Context context, UiModeManager uiModeManager) {
        int customNightMode = getStoredCustomNightMode(context);
        switch (customNightMode) {
            case NIGHT_NO:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_NO);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                break;
            case NIGHT_YES:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }
                break;
            case NIGHT_AUTO:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_AUTO);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);
                }
                break;
            default:
                break;
        }
    }

    public void refresh(UiModeManager uiModeManager) {
        int nightMode = getCurrentCustomNightMode(uiModeManager);
        switch (nightMode) {
            case NIGHT_AUTO:
                nightAuto();
                break;
            case NIGHT_NO:
                nightNo();
                break;
            case NIGHT_YES:
                nightYes();
                break;
            default:
                break;
        }
    }

    public abstract void nightAuto();

    public abstract void nightNo();

    public abstract void nightYes();
}
