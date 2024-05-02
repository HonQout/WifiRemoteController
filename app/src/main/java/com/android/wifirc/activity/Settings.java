package com.android.wifirc.activity;

import android.app.UiModeManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.android.wifirc.R;
import com.android.wifirc.databinding.ActivitySettingsBinding;
import com.android.wifirc.utils.ColorUtils;
import com.android.wifirc.utils.UIUtils;

public class Settings extends AppCompatActivity {
    private static final String TAG = Settings.class.getSimpleName();
    private ActivitySettingsBinding binding;
    private SharedPreferences sharedPreferences;
    private UiModeManager uiModeManager;
    private UIUtils uiUtils;
    private SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        // 初始化binding
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // 初始化sharedPreferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // 初始化uiModeManager
        uiModeManager = (UiModeManager) getSystemService(Context.UI_MODE_SERVICE);
        // 设置界面
        uiUtils = new UIUtils(this) {
            @Override
            public void whenEnabledNightMode() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }
            }

            @Override
            public void whenDisabledNightMode() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_NO);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                int color = ColorUtils.analyzeColor(Settings.this,
                        sharedPreferences.getString("theme_color", "white"));
                if (sharedPreferences.getBoolean("immersion_status_bar", true)) {
                    getWindow().setStatusBarColor(color);
                } else {
                    getWindow().setStatusBarColor(Color.GRAY);
                }
                if (sharedPreferences.getBoolean("immersion_navigation_bar", true)) {
                    getWindow().setNavigationBarColor(color);
                } else {
                    getWindow().setNavigationBarColor(Color.TRANSPARENT);
                }
                binding.toolbar4.setBackgroundColor(color);
                View decorView = getWindow().getDecorView();
                if (color == Color.WHITE) {
                    decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                    binding.toolbar4.setTitleTextColor(Color.BLACK);
                } else {
                    decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                    binding.toolbar4.setTitleTextColor(Color.WHITE);
                }
                binding.getRoot().setBackgroundColor(color);
            }
        };
        // 注册共享偏好改变监听器
        onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                Log.i(TAG, "Received shared preference change");
                switch (key) {
                    case "theme":
                        String nightMode = sharedPreferences.getString("theme", "follow_system");
                        Log.i(TAG, "NIGHT_MODE changed to" + nightMode);
                        switch (nightMode) {
                            case "light":
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_NO);
                                } else {
                                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                                }
                                break;
                            case "dark":
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_YES);
                                } else {
                                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                                }
                                break;
                            default:
                                break;
                        }
                        onResume();
                        break;
                    case "theme_color":
                        String color = sharedPreferences.getString("theme_color", "white");
                        Log.i(TAG, "THEME_COLOR changed to" + color);
                        uiUtils.refresh(Settings.this);
                        break;
                    case "immersion_status_bar":
                        String isb = sharedPreferences.getBoolean("immersion_status_bar", true) ? "On" : "Off";
                        Log.i(TAG, "IMMERSION_STATUS_BAR changed to " + isb);
                        uiUtils.refresh(Settings.this);
                        break;
                    case "immersion_navigation_bar":
                        String inb = sharedPreferences.getBoolean("immersion_navigation_bar", true) ? "On" : "Off";
                        Log.i(TAG, "IMMERSION_NAVIGATION_BAR changed to " + inb);
                        uiUtils.refresh(Settings.this);
                        break;
                }
            }
        };
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
        // 设置控件
        // ActionBar
        setSupportActionBar(binding.toolbar4);
        binding.toolbar4.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
        // SettingsFragment
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();
        // 重绘界面
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                uiUtils.refresh(Settings.this);
            }
        });
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
    }
}