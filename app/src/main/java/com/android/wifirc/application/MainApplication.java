package com.android.wifirc.application;

import android.app.Application;

import androidx.room.Room;

import com.android.wifirc.room.database.ControllerDatabase;

public class MainApplication extends Application {
    private static MainApplication mainApplication;
    private ControllerDatabase controllerDatabase;

    public static MainApplication getInstance() {
        return mainApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mainApplication = this;
        controllerDatabase = Room.databaseBuilder(mainApplication, ControllerDatabase.class,
                        "ControllerInfo")
                .addMigrations()
                .allowMainThreadQueries()
                .build();
    }

    public ControllerDatabase getControllerDatabase() {
        return controllerDatabase;
    }
}
