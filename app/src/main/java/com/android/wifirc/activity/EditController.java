package com.android.wifirc.activity;

import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.android.wifirc.R;
import com.android.wifirc.application.MainApplication;
import com.android.wifirc.databinding.ActivityEditControllerBinding;
import com.android.wifirc.room.dao.ControllerDao;
import com.android.wifirc.room.entity.ControllerInfo;
import com.android.wifirc.utils.ColorUtils;
import com.android.wifirc.utils.DateTimeUtils;
import com.android.wifirc.utils.UIUtils;
import com.android.wifirc.viewmodel.EditControllerViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EditController extends AppCompatActivity {
    private static final String TAG = EditController.class.getSimpleName();
    public static final int CREATE_CONTROLLER = 0;
    public static final int EDIT_CONTROLLER = 1;
    private ActivityEditControllerBinding binding;
    private SharedPreferences sharedPreferences;
    private UiModeManager uiModeManager;
    private UIUtils uiUtils;
    private EditControllerViewModel viewModel;
    private ControllerDao controllerDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        // 初始化binding
        binding = ActivityEditControllerBinding.inflate(getLayoutInflater());
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
                int color = ColorUtils.analyzeColor(EditController.this,
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
                binding.toolbar2.setBackgroundColor(color);
                View decorView = getWindow().getDecorView();
                if (color == Color.WHITE) {
                    decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                    binding.toolbar2.setNavigationIcon(R.drawable.baseline_arrow_back_black);
                    binding.toolbar2.setTitleTextColor(Color.BLACK);
                } else {
                    decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                    binding.toolbar2.setNavigationIcon(R.drawable.baseline_arrow_back_white);
                    binding.toolbar2.setTitleTextColor(Color.WHITE);
                }
                binding.getRoot().setBackgroundColor(Color.WHITE);
            }

            @Override
            public void nightYes() {
                getWindow().setStatusBarColor(Color.BLACK);
                getWindow().setNavigationBarColor(Color.BLACK);
                binding.toolbar2.setBackgroundColor(Color.BLACK);
                binding.toolbar2.setNavigationIcon(R.drawable.baseline_arrow_back_white);
                binding.getRoot().setBackgroundColor(Color.BLACK);
            }
        };
        // 设置控件
        // ActionBar
        setSupportActionBar(binding.toolbar2);
        binding.toolbar2.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
        // 初始化ViewModel
        viewModel = new EditControllerViewModel();
        // 初始化Dao
        controllerDao = MainApplication.getInstance().getControllerDatabase().controllerDao();
        // 获取选择
        Intent intent = getIntent();
        int option = intent.getIntExtra("option", -1);
        switch (option) {
            case -1:
                Toast.makeText(this, R.string.wrong_argument, Toast.LENGTH_SHORT).show();
                finish();
                break;
            case CREATE_CONTROLLER:
                viewModel.setOption(option);
                binding.toolbar2.setTitle(R.string.create_controller);
                break;
            case EDIT_CONTROLLER:
                viewModel.setOption(option);
                binding.toolbar2.setTitle(R.string.edit_controller);
                int position = intent.getIntExtra("position", -1);
                if (position == -1) {
                    Toast.makeText(this, R.string.wrong_argument, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    viewModel.setPosition(position);
                    initControllerInfo(position);
                }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_controller, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        int option = viewModel.getOption();
        if (id == R.id.action_reset_edit) {
            if (option == CREATE_CONTROLLER) {
                binding.inputTitle.setText("");
                binding.inputDescription.setText("");
            } else if (option == EDIT_CONTROLLER) {
                initControllerInfo(viewModel.getPosition());
            }
        } else if (id == R.id.action_complete_edit) {
            List<String> errList = new ArrayList<String>();
            // 处理添加时间
            String timeAdded;
            if (option == CREATE_CONTROLLER) {
                timeAdded = DateTimeUtils.getFormattedDateTime();
            } else {
                timeAdded = controllerDao.getAllControllers().get(viewModel.getPosition()).getTimeAdded();
            }
            // 处理标题
            String title = null;
            if (!TextUtils.isEmpty(binding.inputTitle.getText().toString())) {
                title = binding.inputTitle.getText().toString();
            } else {
                errList.add(getString(R.string.err_null_title));
            }
            // 处理描述
            String description = binding.inputDescription.getText().toString();
            if (TextUtils.isEmpty(description)) {
                description = "";
            }
            // 显示错误
            if (!errList.isEmpty()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.error);
                StringBuilder sb = new StringBuilder();
                sb.append(getString(R.string.hint_following_error_occurred));
                for (String err : errList) {
                    sb.append(String.format("\n%s", err));
                }
                builder.setMessage(sb.toString());
                builder.setPositiveButton(R.string.confirm, null);
                builder.create().show();
            } else if (title != null) {
                ControllerInfo info = new ControllerInfo(timeAdded, title, description,
                        new HashMap<String, String>());
                if (option == CREATE_CONTROLLER) {
                    controllerDao.insertController(info);
                    Toast.makeText(EditController.this, R.string.create_controller_success,
                            Toast.LENGTH_SHORT).show();
                } else if (option == EDIT_CONTROLLER) {
                    controllerDao.updateController(info);
                    Toast.makeText(EditController.this, R.string.edit_controller_success,
                            Toast.LENGTH_SHORT).show();
                }
                getOnBackPressedDispatcher().onBackPressed();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        Log.v(TAG, "onResume");
        super.onResume();
        // 重绘界面
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                uiUtils.refresh(uiModeManager);
            }
        });
    }

    private void initControllerInfo(int position) {
        ControllerInfo info = controllerDao.getAllControllers().get(position);
        binding.inputTitle.setText(info.getTitle());
        binding.inputDescription.setText(info.getDescription());
    }
}