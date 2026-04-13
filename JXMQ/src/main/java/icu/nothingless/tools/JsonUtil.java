package icu.nothingless.tools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonUtil {
    private static final Gson gson = new GsonBuilder()
        .setDateFormat("yyyy-MM-dd HH:mm:ss")
        .create();
    
    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }
    
    public static <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }
}