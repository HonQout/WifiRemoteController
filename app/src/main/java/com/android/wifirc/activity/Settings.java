package com.android.wifirc.activity;

import android.app.UiModeManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
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
        uiUtils = new UIUtils() {
            @Override
            public void nightAuto() {

            }

            @Override
            public void nightNo() {
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
                    binding.toolbar4.setNavigationIcon(R.drawable.baseline_arrow_back_black);
                    binding.toolbar4.setTitleTextColor(Color.BLACK);
                } else {
                    decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                    binding.toolbar4.setNavigationIcon(R.drawable.baseline_arrow_back_white);
                    binding.toolbar4.setTitleTextColor(Color.WHITE);
                }
                binding.getRoot().setBackgroundColor(Color.WHITE);
            }

            @Override
            public void nightYes() {
                getWindow().setStatusBarColor(Color.BLACK);
                getWindow().setNavigationBarColor(Color.BLACK);
                binding.toolbar4.setBackgroundColor(Color.BLACK);
                binding.toolbar4.setNavigationIcon(R.drawable.baseline_arrow_back_white);
                binding.getRoot().setBackgroundColor(Color.BLACK);
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
                        Log.i(TAG, "NIGHT_MODE changed to: " + nightMode);
                        UIUtils.setCurrentCustomNightMode(Settings.this, uiModeManager);
                        uiUtils.refresh(uiModeManager);
                        break;
                    case "theme_color":
                        String color = sharedPreferences.getString("theme_color", "white");
                        Log.i(TAG, "THEME_COLOR changed to" + color);
                        uiUtils.refresh(uiModeManager);
                        break;
                    case "immersion_status_bar":
                        String isb = sharedPreferences.getBoolean("immersion_status_bar", true) ? "On" : "Off";
                        Log.i(TAG, "IMMERSION_STATUS_BAR changed to " + isb);
                        uiUtils.refresh(uiModeManager);
                        break;
                    case "immersion_navigation_bar":
                        String inb = sharedPreferences.getBoolean("immersion_navigation_bar", true) ? "On" : "Off";
                        Log.i(TAG, "IMMERSION_NAVIGATION_BAR changed to " + inb);
                        uiUtils.refresh(uiModeManager);
                        break;
                    default:
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
                uiUtils.refresh(uiModeManager);
            }
        });
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }

        @Override
        public boolean onPreferenceTreeClick(@NonNull Preference preference) {
            String key = preference.getKey();
            Log.i(TAG, "Clicked preference " + key);
            switch (key) {
                case "github":
                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                    builder.setTitle(R.string.open_external_link);
                    builder.setMessage(String.format(getString(R.string.hint_open_link), getString(R.string.GitHub_address)));
                    builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(getString(R.string.GitHub_address)));
                            startActivity(intent);
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, null);
                    builder.create().show();
                    break;
                default:
                    break;
            }
            return super.onPreferenceTreeClick(preference);
        }
    }
}