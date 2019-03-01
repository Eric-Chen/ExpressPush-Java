package com.github.expresspush.server;

import org.junit.Test;

public class NettyServerTest {
    private NettyServer server;

    @Test
    public void test_server(){
        server = new NettyServer();
        server.start();
    }
}