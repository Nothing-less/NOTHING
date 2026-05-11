package icu.nothingless.commons;

/**
 * Daoo层 与 Service层的统一响应对象，包含状态码、状态信息(fixed)、详细信息和数据
 * 
 * @param code 状态码，1表示成功，0表示失败
 */
public record R<T>(
        int code,
        String description,
        String message,
        T data

) {
    private static final String DEFAULT_SUCCESS = "SUCCESS";
    private static final String DEFAULT_ERROR = "FAILED";

    public static <T> R<T> success(T data) {
        R<T> r = new R<>(1, DEFAULT_SUCCESS, "", data);
        return r;
    }

    public static <T> R<T> success(int code, String msg) {
        R<T> r = new R<>(code, DEFAULT_SUCCESS, msg, null);
        return r;
    }

    public static <T> R<T> success(String msg) {
        R<T> r = new R<>(1, DEFAULT_SUCCESS, msg, null);
        return r;
    }

    public static <T> R<T> error(String msg) {
        R<T> r = new R<>(0, DEFAULT_ERROR, msg, null);
        return r;
    }

    public int code() {
        return code;
    }

    public String message() {
        return message;
    }

    public T data() {
        return data;
    }

    public boolean isSuccess() {
        return code == 1 || DEFAULT_SUCCESS.equals(description);
    }

}
