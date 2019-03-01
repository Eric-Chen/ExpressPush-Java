package com.github.expresspush.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class Message implements Serializable {

    private String id;

    private String from = "";

    private String to = "";

    //json
    private String content = "";


    public int byteLength() {
        return id.getBytes().length
                + from.getBytes().length
                + to.getBytes().length
                + content.getBytes().length;
    }

}
