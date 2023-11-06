package com.andy.vmt_app;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class WordTypeConverter {

    @TypeConverter
    public static List<Word> fromString(String value) {
        Type listType = new TypeToken<ArrayList<Word>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromList(List<Word> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        return json;
    }
}

