package com.android.wifirc.activity;

import android.app.UiModeManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.wifirc.R;
import com.android.wifirc.adapter.ControllerRecyclerAdapter;
import com.android.wifirc.application.MainApplication;
import com.android.wifirc.databinding.ActivityMainBinding;
import com.android.wifirc.room.dao.ControllerDao;
import com.android.wifirc.utils.ColorUtils;
import com.android.wifirc.utils.UIUtils;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private ActivityMainBinding binding;
    private SharedPreferences sharedPreferences;
    private UiModeManager uiModeManager;
    private UIUtils uiUtils;
    private ControllerRecyclerAdapter controllerRecyclerAdapter;
    private ControllerDao controllerDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        // 初始化binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
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
                int color = ColorUtils.analyzeColor(MainActivity.this,
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
                binding.toolbar.setBackgroundColor(color);
                View decorView = getWindow().getDecorView();
                if (color == Color.WHITE) {
                    decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                    binding.toolbar.setTitleTextColor(Color.BLACK);
                } else {
                    decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                    binding.toolbar.setTitleTextColor(Color.WHITE);
                }
                binding.getRoot().setBackgroundColor(color);
            }
        };
        // 设置控件
        // ActionBar
        setSupportActionBar(binding.toolbar);
        // RecyclerView
        LinearLayoutManager manager = new LinearLayoutManager(this, RecyclerView.VERTICAL,
                false);
        binding.rvMain.setLayoutManager(manager);
        controllerRecyclerAdapter = new ControllerRecyclerAdapter(this, null);
        binding.rvMain.setAdapter(controllerRecyclerAdapter);
        // FloatingActionButton
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditController.class);
                intent.putExtra("option", EditController.CREATE_CONTROLLER);
                startActivity(intent);
            }
        });
        // 初始化Dao
        controllerDao = MainApplication.getInstance().getControllerDatabase().controllerDao();
    }

    @Override
    protected void onResume() {
        Log.v(TAG, "onResume");
        super.onResume();
        // 重绘界面02
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                uiUtils.refresh(MainActivity.this);
            }
        });
        // 设置遥控器列表
        controllerRecyclerAdapter.setInfoList(controllerDao.getAllControllers());
        controllerRecyclerAdapter.setItemClickListener(new ControllerRecyclerAdapter.ItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Log.v(TAG, "Clicked BillRecyclerAdapter's item #" + position);
                Intent intent = new Intent(MainActivity.this, Controller.class);
                intent.putExtra("position", position);
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(int position) {
                Log.v(TAG, "Long clicked BillRecyclerAdapter's item #" + position);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.operation);
                builder.setItems(R.array.controller_menu, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                Intent intent = new Intent(MainActivity.this, EditController.class);
                                intent.putExtra("option", EditController.EDIT_CONTROLLER);
                                intent.putExtra("position", position);
                                startActivity(intent);
                                break;
                            case 1:
                                controllerDao.deleteController(controllerRecyclerAdapter.getControllerList().get(position));
                                controllerRecyclerAdapter.deleteItem(position);
                                break;
                            default:
                                break;
                        }
                    }
                });
                builder.create().show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, Settings.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}