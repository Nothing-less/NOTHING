package icu.nothingless.exceptions;

public class EngineException extends Exception {
    public EngineException(String message) {
        super(message);
    }
    
    public EngineException(String message, Throwable cause) {
        super(message, cause);
    }

    public EngineException(Throwable cause) {
        super(cause);
    }

}
