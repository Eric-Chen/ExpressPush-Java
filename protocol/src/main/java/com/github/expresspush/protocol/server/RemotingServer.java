package com.github.expresspush.protocol.server;

public interface RemotingServer {

    void start();

    void shutdown();

    String status();
}
