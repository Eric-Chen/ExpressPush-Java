package com.github.expresspush.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.charset.Charset;
import org.junit.Test;

public class TestBasics {

    private static final Charset CHARSET_UTF8 = Charset.forName("utf-8");

    @Test
    public void test_buffers(){
        String c = "this is a test";
        byte[] bytes = c.getBytes(CHARSET_UTF8);
        ByteBuf nbuf = Unpooled.buffer();
        nbuf.writeBytes(bytes);
        System.out.println(nbuf.readerIndex());
        System.out.println(nbuf.writerIndex());
        System.out.println(nbuf.readableBytes());
        ByteBuf newB = nbuf.copy();
        byte[] b = new byte[12];
        nbuf.readBytes(b);
        System.out.println(nbuf.readerIndex());
        System.out.println(nbuf.writerIndex());
        System.out.println(nbuf.readableBytes());

//        ByteBuf newB = nbuf.copy();
        System.out.println(newB.readableBytes());
        ByteBuf dbuf = nbuf.duplicate();
        dbuf.setByte(0, (byte)'K');
        System.out.println(dbuf.getByte(0) == nbuf.getByte(0));
        System.out.println(dbuf.getByte(0));

    }

}
