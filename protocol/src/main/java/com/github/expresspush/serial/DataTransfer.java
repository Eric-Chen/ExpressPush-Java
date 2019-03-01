package com.github.expresspush.serial;

import java.nio.ByteBuffer;

public interface DataTransfer<T> {

    ByteBuffer encode(T obj);

    T decode(ByteBuffer buffer);

}
