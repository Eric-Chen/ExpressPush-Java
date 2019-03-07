package com.github.expresspush.handler;

import com.github.expresspush.basic.ResponseFuture;
import com.github.expresspush.client.NettyClient;
import java.util.Map;

public class ClientMessageHandler extends SimpleMessageHandler {

    private final NettyClient client;

    public ClientMessageHandler(NettyClient client) {
        this.client = client;
    }

    @Override protected Map<Long, ResponseFuture> getResponseTable() {
        return client.getResponseTable();
    }
}
