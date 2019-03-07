package com.github.expresspush.basic;

public class InvalidChannelStatusException extends IllegalStateException {

    public InvalidChannelStatusException(String errMsg){
        super(errMsg);
    }

    public InvalidChannelStatusException(String errMsg, Throwable throwable){
        super(errMsg, throwable);
    }

}
