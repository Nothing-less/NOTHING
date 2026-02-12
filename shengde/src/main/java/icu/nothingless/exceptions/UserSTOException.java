package icu.nothingless.exceptions;

public class UserSTOException extends MyException {
    public UserSTOException(String message) {
        super(message);
    }
    
    public UserSTOException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserSTOException(Throwable cause) {
        super(cause);
    }

}
