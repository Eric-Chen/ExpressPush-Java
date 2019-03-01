package com.github.expresspush.server;

public interface RemotingServer {

    void start();

    void shutdown();

    String status();
}
