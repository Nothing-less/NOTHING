package icu.nothingless.exceptions;

public class PageItemException extends Exception{
    public PageItemException(String message){
        super(message);
    }

    public PageItemException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public PageItemException(Throwable cause) {
        super(cause);
    }


}
