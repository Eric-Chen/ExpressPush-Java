package com.github.expresspush.handler;

import com.github.expresspush.basic.ResponseFuture;
import com.github.expresspush.server.NettyServer;
import java.util.Map;

public class ServerMessageHandler extends SimpleMessageHandler {

    private final NettyServer server;

    public ServerMessageHandler(NettyServer server) {
        this.server = server;
    }

    @Override protected Map<Long, ResponseFuture> getResponseTable() {
        return server.getResponseTable();
    }
}
