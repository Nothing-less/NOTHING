package icu.nothingless.tools;

public class Fmt {
    public static String of(String template, Object... args) {
        if (template == null || args == null || args.length == 0) {
            return template;
        }
        return String.format(
                template.replace("%", "%%").replace("{}", "%s"),
                args);
    }

    public static Boolean isEmpty(String str) {
        return StrUtil.isEmpty(str,false);
    }

    public static boolean isAnyEmpty(String... strs) {
        return StrUtil.isAnyEmpty(strs);
    }

    public static boolean isAllEmpty(String... strs) {
        return StrUtil.isAllEmpty(strs);
    }
}

class StrUtil {

    /**
     * 判断字符串是否为空（最严格模式）
     * 为空的情况包括：null、空字符串""、纯空白字符、字符串"null"、"undefined"、"nil"、"none"
     */
    public static boolean isEmpty(String str) {
        return isEmpty(str, true);
    }

    /**
     * 判断字符串是否为空（可配置模式）
     * 
     * @param str    待判断字符串
     * @param strict 是否严格模式（true: 包含"null"/"undefined"等字面量也视为空）
     */
    public static boolean isEmpty(String str, boolean strict) {
        // 1. 基础null判断
        if (str == null) {
            return true;
        }

        // 2. 去除首尾空白后判断
        String trimmed = str.trim();
        if (trimmed.isEmpty()) {
            return true;
        }

        // 3. 严格模式：检查无意义字面量（忽略大小写）
        if (strict) {
            String lower = trimmed.toLowerCase();
            return lower.equals("null")
                    || lower.equals("undefined")
                    || lower.equals("nil")
                    || lower.equals("none")
                    || lower.equals("nan")
                    || lower.equals("null")
                    || lower.equals("void")
                    || lower.equals("empty")
                    || lower.equals("blank");
        }

        return false;
    }

    /**
     * 判断字符串是否不为空
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 判断字符串是否纯空白（包括各种Unicode空白）
     */
    public static boolean isBlank(String str) {
        if (str == null || str.isEmpty()) {
            return true;
        }
        // 使用Java 11+的isBlank()，或自定义实现
        return str.trim().isEmpty();
    }

    /**
     * 检查多个字符串是否全部为空
     */
    public static boolean isAllEmpty(String... strs) {
        if (strs == null || strs.length == 0) {
            return true;
        }
        for (String str : strs) {
            if (!isEmpty(str)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查多个字符串是否有任意一个为空
     */
    public static boolean isAnyEmpty(String... strs) {
        if (strs == null) {
            return true;
        }
        for (String str : strs) {
            if (isEmpty(str)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取字符串有效值（为空时返回默认值）
     */
    public static String defaultIfEmpty(String str, String defaultVal) {
        return isEmpty(str) ? defaultVal : str;
    }

    /**
     * 获取字符串有效值（为空时返回空字符串）
     */
    public static String defaultString(String str) {
        return defaultIfEmpty(str, "");
    }
}