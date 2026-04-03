package icu.nothingless.commons;

public record Result<T>(
    int code,
    String message,
    T data

) {
    private static final String DEFAULT_SUCCESS_MESSAGE = "SUCCESS";
    private static final String DEFAULT_ERROR_MESSAGE = "FAILED";

        public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>(0, DEFAULT_SUCCESS_MESSAGE, data);
        return r;
    }
    
    public static <T> Result<T> error(String msg) {
        Result<T> r = new Result<>(1, DEFAULT_ERROR_MESSAGE, null);
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

}
