package com.android.wifirc.room.typeconverter;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;

public class StringHashMapConverter {
    @TypeConverter
    public String objectToString(HashMap<String, String> hashMap) {
        return new Gson().toJson(hashMap);
    }

    @TypeConverter
    public HashMap<String, String> stringToObject(String s) {
        Type type = new TypeToken<HashMap<String, String>>() {
        }.getType();
        return new Gson().fromJson(s, type);
    }
}
