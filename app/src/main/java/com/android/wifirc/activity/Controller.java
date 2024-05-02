package com.android.wifirc.activity;

import android.app.UiModeManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;

import com.android.wifirc.R;
import com.android.wifirc.application.MainApplication;
import com.android.wifirc.databinding.ActivityControllerBinding;
import com.android.wifirc.event.MessageEvent;
import com.android.wifirc.room.dao.ControllerDao;
import com.android.wifirc.room.entity.ControllerInfo;
import com.android.wifirc.service.WifiCommunicationService;
import com.android.wifirc.utils.ColorUtils;
import com.android.wifirc.utils.IPUtils;
import com.android.wifirc.utils.UIUtils;
import com.android.wifirc.viewmodel.ControllerViewModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class Controller extends AppCompatActivity {
    private static final String TAG = Controller.class.getSimpleName();
    private ActivityControllerBinding binding;
    private SharedPreferences sharedPreferences;
    private UiModeManager uiModeManager;
    private UIUtils uiUtils;
    private ControllerViewModel viewModel;
    private ControllerDao controllerDao;
    private WifiCommunicationService bindService;
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bindService = ((WifiCommunicationService.MyBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bindService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        // 初始化binding
        binding = ActivityControllerBinding.inflate(getLayoutInflater());
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
                int color = ColorUtils.analyzeColor(Controller.this,
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
                binding.toolbar3.setBackgroundColor(color);
                View decorView = getWindow().getDecorView();
                if (color == Color.WHITE) {
                    decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                    binding.toolbar3.setTitleTextColor(Color.BLACK);
                } else {
                    decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                    binding.toolbar3.setTitleTextColor(Color.WHITE);
                }
                binding.getRoot().setBackgroundColor(color);
            }
        };
        // 初始化ViewModel
        viewModel = new ControllerViewModel();
        viewModel.getHost().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                binding.tvHost.setText(s);
            }
        });
        viewModel.getPort().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                binding.tvPort.setText(String.valueOf(integer));
            }
        });
        // 注册EventBus
        EventBus.getDefault().register(this);
        // 初始化Dao
        controllerDao = MainApplication.getInstance().getControllerDatabase().controllerDao();
        // 初始化控件
        setSupportActionBar(binding.toolbar3);
        binding.toolbar3.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
        binding.lineCommunicationObject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Controller.this);
                builder.setTitle(R.string.set_communication_object);
                final View view = View.inflate(Controller.this, R.layout.dialog_set_communication_object, null);
                final EditText input_host = view.findViewById(R.id.input_host);
                final TextView illegal_host = view.findViewById(R.id.tv_illegal_host);
                final EditText input_port = view.findViewById(R.id.input_port);
                final TextView illegal_port = view.findViewById(R.id.tv_illegal_port);
                final String host = viewModel.getHost().getValue();
                final String port = String.valueOf(viewModel.getPort().getValue());
                input_host.setText(host);
                input_port.setText(port);
                builder.setView(view);
                builder.setPositiveButton(R.string.confirm, null);
                builder.setNegativeButton(R.string.cancel, null);
                builder.setCancelable(false);
                AlertDialog dialog = builder.create();
                dialog.show();
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String host = input_host.getText().toString();
                        String port = input_port.getText().toString();
                        boolean isValidIPAddress = IPUtils.isValidIPAddress(host);
                        boolean isValidPortNumber = IPUtils.isValidPortNumber(port);
                        illegal_host.setVisibility(isValidIPAddress ? View.GONE : View.VISIBLE);
                        illegal_port.setVisibility(isValidPortNumber ? View.GONE : View.VISIBLE);
                        if (isValidIPAddress && isValidPortNumber) {
                            viewModel.setHost(host);
                            viewModel.setPort(Integer.parseInt(port));
                            dialog.dismiss();
                        }
                    }
                });
            }
        });
        setButtons();
        // 初始化数据
        Intent intent = getIntent();
        int position = intent.getIntExtra("position", -1);
        if (position == -1) {
            Toast.makeText(this, R.string.wrong_argument, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            viewModel.setPosition(position);
            viewModel.setHost("0.0.0.0");
            viewModel.setPort(0);
            ControllerInfo controllerInfo = controllerDao.getAllControllers().get(position);
            viewModel.setControllerInfo(controllerInfo);
            binding.toolbar3.setTitle(controllerInfo.getTitle());
        }
        // 绑定服务
        Intent intent1 = new Intent(this, WifiCommunicationService.class);
        bindService(intent1, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_controller, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_help) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.help);
            builder.setMessage(R.string.help_controller);
            builder.setPositiveButton(R.string.confirm, null);
            builder.create().show();
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
                uiUtils.refresh(Controller.this);
            }
        });
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
        unbindService(serviceConnection);
        bindService = null;
        // 反注册EventBus
        EventBus.getDefault().unregister(this);
    }

    private void setButton(@NonNull Button button, @NonNull String name) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ControllerInfo info = viewModel.getControllerInfo().getValue();
                if (info == null) {
                    Toast.makeText(Controller.this, R.string.non_existent_controller_data, Toast.LENGTH_SHORT).show();
                } else {
                    String data = info.getContent().get(name);
                    if (data == null) {
                        Toast.makeText(Controller.this, R.string.cmd_not_set, Toast.LENGTH_SHORT).show();
                    } else {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String host = viewModel.getHost().getValue();
                                int port = viewModel.getPort().getValue() == null ?
                                        -1 : viewModel.getPort().getValue();
                                bindService.sendData(host, port, data);
                            }
                        }).start();
                    }
                }
            }
        });
        button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ControllerInfo info = viewModel.getControllerInfo().getValue();
                if (info == null) {
                    Toast.makeText(Controller.this, R.string.non_existent_controller_data, Toast.LENGTH_SHORT).show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Controller.this);
                    builder.setTitle(R.string.edit_cmd);
                    final EditText input = new EditText(Controller.this);
                    String data = info.getContent().get(name);
                    if (data == null) {
                        input.setText("");
                    } else {
                        input.setText(data);
                    }
                    builder.setView(input);
                    builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            info.getContent().put(name, input.getText().toString());
                            viewModel.setControllerInfo(info);
                            controllerDao.updateController(info);
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, null);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            builder.create().show();
                        }
                    });
                    Log.i(TAG, "Long clicked button");
                }
                return true;
            }
        });
    }

    private void setImageButton(@NonNull ImageButton imageButton, @NonNull String name) {
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ControllerInfo info = viewModel.getControllerInfo().getValue();
                if (info == null) {
                    Toast.makeText(Controller.this, R.string.non_existent_controller_data, Toast.LENGTH_SHORT).show();
                } else {
                    String data = info.getContent().get(name);
                    if (data == null) {
                        Toast.makeText(Controller.this, R.string.cmd_not_set, Toast.LENGTH_SHORT).show();
                    } else {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String host = viewModel.getHost().getValue();
                                int port = viewModel.getPort().getValue() == null ?
                                        -1 : viewModel.getPort().getValue();
                                bindService.sendData(host, port, data);
                            }
                        }).start();
                    }
                }
            }
        });
        imageButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ControllerInfo info = viewModel.getControllerInfo().getValue();
                if (info == null) {
                    Toast.makeText(Controller.this, R.string.non_existent_controller_data, Toast.LENGTH_SHORT).show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Controller.this);
                    builder.setTitle(R.string.edit_cmd);
                    final EditText input = new EditText(Controller.this);
                    String data = info.getContent().get(name);
                    if (data == null) {
                        input.setText("");
                    } else {
                        input.setText(data);
                    }
                    builder.setView(input);
                    builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            info.getContent().put(name, input.getText().toString());
                            viewModel.setControllerInfo(info);
                            controllerDao.updateController(info);
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, null);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            builder.create().show();
                        }
                    });
                    Log.i(TAG, "Long clicked button");
                }
                return true;
            }
        });
    }

    private void setButtons() {
        setImageButton(binding.btnPower, "power");
        setImageButton(binding.btnUp, "up");
        setImageButton(binding.btnDown, "down");
        setImageButton(binding.btnLeft, "left");
        setImageButton(binding.btnRight, "right");
        setButton(binding.btnOk, "ok");
        setButton(binding.btnDigit0, "0");
        setButton(binding.btnDigit1, "1");
        setButton(binding.btnDigit2, "2");
        setButton(binding.btnDigit3, "3");
        setButton(binding.btnDigit4, "4");
        setButton(binding.btnDigit5, "5");
        setButton(binding.btnDigit6, "6");
        setButton(binding.btnDigit7, "7");
        setButton(binding.btnDigit8, "8");
        setButton(binding.btnDigit9, "9");
        setButton(binding.btnBack, "back");
        setButton(binding.btnMenu, "menu");
    }

    // 处理EventBus事件
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleEvent(MessageEvent messageEvent) {
        String message = messageEvent.getMessage();
        if (message == null) {
            return;
        }
        Log.i(TAG, "Received Message: " + message);
        switch (message) {
            case WifiCommunicationService.CONNECTION_FAILED:
                Toast.makeText(this, R.string.connection_failed, Toast.LENGTH_SHORT).show();
                break;
            case WifiCommunicationService.CONNECTION_SUCCEED:
                Toast.makeText(this, R.string.connection_succeed, Toast.LENGTH_SHORT).show();
                break;
            case WifiCommunicationService.SENDING_SUCCEED:
                Toast.makeText(this, R.string.sending_succeed, Toast.LENGTH_SHORT).show();
                break;
            case WifiCommunicationService.CLOSED:
                Toast.makeText(this, R.string.connection_closed, Toast.LENGTH_SHORT).show();
                break;
        }
    }
}