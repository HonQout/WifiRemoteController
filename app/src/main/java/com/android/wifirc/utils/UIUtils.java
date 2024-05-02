package com.android.wifirc.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

public abstract class UIUtils {
    private Context context;
    public String theme;
    public boolean isNightModeEnabled;

    public UIUtils(@NonNull Context context) {
        this.context = context;
    }

    private void init(@NonNull Context context) {
        int uiMode = context.getApplicationContext().getResources().getConfiguration().uiMode;
        int systemNightMode = uiMode & Configuration.UI_MODE_NIGHT_MASK;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String theme = sharedPreferences.getString("theme", "follow_system");
        isNightModeEnabled = theme.equals("dark") || (theme.equals("follow_system")
                && (systemNightMode == Configuration.UI_MODE_NIGHT_YES));
    }

    public void refresh(@NonNull Context context) {
        init(context);
        if (isNightModeEnabled) {
            whenEnabledNightMode();
        } else {
            whenDisabledNightMode();
        }
    }

    public abstract void whenEnabledNightMode();

    public abstract void whenDisabledNightMode();
}
