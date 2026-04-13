package icu.nothingless.commons;

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
        return code == 1 && DEFAULT_SUCCESS.equals(description);
    }

}
