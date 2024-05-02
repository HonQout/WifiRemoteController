package com.android.wifirc.room.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.android.wifirc.room.entity.ControllerInfo;

import java.util.List;

@Dao
public interface ControllerDao {
    @Query("SELECT * FROM ControllerInfo")
    List<ControllerInfo> getAllControllers();

    @Query("SELECT * FROM ControllerInfo WHERE timeAdded = :timeAdded")
    ControllerInfo getControllerByTimeAdded(String timeAdded);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertController(ControllerInfo info);

    @Insert
    void insertControllers(List<ControllerInfo> infoList);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateController(ControllerInfo info);

    @Delete
    void deleteController(ControllerInfo info);

    @Query("DELETE FROM ControllerInfo WHERE 1=1")
    void deleteAllControllers();
}