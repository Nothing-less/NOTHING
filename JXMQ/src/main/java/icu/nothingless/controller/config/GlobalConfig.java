package icu.nothingless.controller.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局配置 - config.properties
 */
public class GlobalConfig {
    private static final String CONFIG_FILE = "config.properties";

    public static final Map<String, String> CONFIG_MAP;
    static {
        Map<String, String> rawProps = new ConcurrentHashMap<>();

        try (InputStream in = GlobalConfig.class
                .getClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {

            Properties props = new Properties();
            props.load(in);

            for (String name : props.stringPropertyNames()) {
                rawProps.put(name, props.getProperty(name));
            }
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }

        Map<String, String> resolved = new ConcurrentHashMap<>();
        for (Map.Entry<String, String> entry : rawProps.entrySet()) {
            resolved.put(
                    entry.getKey(),
                    PlaceholderResolver.resolve(entry.getValue(), rawProps));
        }

        CONFIG_MAP = Collections.unmodifiableMap(resolved);
    }
}

class PlaceholderResolver {

    /**
     * 解析 ${key} 占位符
     */
    public static String resolve(String value, Map<String, String> props) {
        if (value == null) {
            return null;
        }

        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < value.length()) {
            int start = value.indexOf("${", i);
            if (start == -1) {
                result.append(value, i, value.length());
                break;
            }
            result.append(value, i, start);

            int end = value.indexOf("}", start);
            if (end == -1) {
                throw new IllegalArgumentException("非法占位符: " + value);
            }

            String placeholder = value.substring(start + 2, end);
            String resolved;

            if (placeholder.startsWith("env.")) {
                // 支持系统环境变量
                resolved = System.getenv(placeholder.substring(4));
            } else {
                // 支持配置文件内的 key
                resolved = props.get(placeholder);
            }

            if (resolved == null) {
                throw new IllegalArgumentException("未定义的占位符: " + placeholder);
            }

            result.append(resolved);
            i = end + 1;
        }
        return result.toString();
    }
}