package com.github.expresspush.config;

/**
 * Config object with static property names and default values
 */
public final class ServerConfig {

    public static final int WORKER_COUNT = Runtime.getRuntime().availableProcessors() * 2;

}
