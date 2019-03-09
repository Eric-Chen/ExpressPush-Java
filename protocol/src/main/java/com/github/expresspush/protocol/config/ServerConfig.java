package com.github.expresspush.protocol.config;

/**
 * Config object with static property names and default values
 */
public final class ServerConfig {

    public static final int WORKER_COUNT = Runtime.getRuntime().availableProcessors() * 2;

    public static final int DEFAULT_REQUESTS_CONCURRENCY = Runtime.getRuntime().availableProcessors() * 2;

    public static final String PORT_CONFIG_KEY = "sys.port";

    public static final String getSystemProperty(String key, String defaultValue){
        return System.getProperty(key, defaultValue);
    }

}
