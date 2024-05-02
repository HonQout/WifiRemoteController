package com.android.wifirc.room.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.android.wifirc.room.dao.ControllerDao;
import com.android.wifirc.room.entity.ControllerInfo;

@Database(entities = {ControllerInfo.class}, version = 1, exportSchema = false)
public abstract class ControllerDatabase extends RoomDatabase {
    public abstract ControllerDao controllerDao();
}