package icu.nothingless.commons;

public record ResultEntity<T>(
    int code,
    String description,
    String message,
    T data

) {
    private static final String DEFAULT_SUCCESS = "SUCCESS";
    private static final String DEFAULT_ERROR = "FAILED";

        public static <T> ResultEntity<T> success(T data) {
        ResultEntity<T> r = new ResultEntity<>(1, DEFAULT_SUCCESS, "", data);
        return r;
    }
    
    public static <T> ResultEntity<T> error(String msg) {
        ResultEntity<T> r = new ResultEntity<>(0, DEFAULT_ERROR, msg, null);
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
