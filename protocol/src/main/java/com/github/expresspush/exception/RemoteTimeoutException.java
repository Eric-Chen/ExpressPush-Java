package com.github.expresspush.exception;

public class RemoteTimeoutException extends RuntimeException {

    public RemoteTimeoutException(String msg){
        super(msg);
    }

    public RemoteTimeoutException(String msg, Throwable t){
        super(msg, t);
    }

}
