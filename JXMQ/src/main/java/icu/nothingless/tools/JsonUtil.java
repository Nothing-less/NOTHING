package icu.nothingless.tools;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class JsonUtil {

    private static final Gson gson = new GsonBuilder()
            // 处理 java.util.Date
            .setDateFormat("yyyy-MM-dd HH:mm:ss")

            // 处理 java.time
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .setPrettyPrinting()
            .serializeNulls()
            .create();

    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    public static <T> T fromJson(String json, Type type) {
        return gson.fromJson(json, type);
    }

    /* ========== Adapter 实现 ========== */

    static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
        private static final DateTimeFormatter FORMATTER =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        @Override
        public void write(JsonWriter out, LocalDateTime value) throws java.io.IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(FORMATTER.format(value));
            }
        }

        @Override
        public LocalDateTime read(JsonReader in) throws java.io.IOException {
            String str = in.nextString();
            return str == null || str.isEmpty()
                    ? null
                    : LocalDateTime.parse(str, FORMATTER);
        }
    }

    static class LocalDateAdapter extends TypeAdapter<LocalDate> {
        private static final DateTimeFormatter FORMATTER =
                DateTimeFormatter.ofPattern("yyyy-MM-dd");

        @Override
        public void write(JsonWriter out, LocalDate value) throws java.io.IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(FORMATTER.format(value));
            }
        }

        @Override
        public LocalDate read(JsonReader in) throws java.io.IOException {
            String str = in.nextString();
            return str == null || str.isEmpty()
                    ? null
                    : LocalDate.parse(str, FORMATTER);
        }
    }
}