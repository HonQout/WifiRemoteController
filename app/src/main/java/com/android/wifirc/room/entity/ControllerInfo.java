package com.android.wifirc.room.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.android.wifirc.room.typeconverter.StringHashMapConverter;

import java.util.HashMap;

@Entity
@TypeConverters(StringHashMapConverter.class)
public class ControllerInfo {
    @PrimaryKey
    @NonNull
    private String timeAdded;
    @NonNull
    private String title;
    private String description;
    private HashMap<String, String> content;

    public ControllerInfo(@NonNull String timeAdded, @NonNull String title, String description,
                          HashMap<String, String> content) {
        this.timeAdded = timeAdded;
        this.title = title;
        this.description = description;
        this.content = content;
    }

    public void setTimeAdded(@NonNull String timeAdded) {
        this.timeAdded = timeAdded;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setContent(HashMap<String, String> content) {
        this.content = content;
    }

    @NonNull
    public String getTimeAdded() {
        return timeAdded;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public HashMap<String, String> getContent() {
        return content;
    }
}