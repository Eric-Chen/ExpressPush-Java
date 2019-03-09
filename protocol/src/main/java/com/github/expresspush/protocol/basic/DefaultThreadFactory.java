package com.github.expresspush.protocol.basic;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultThreadFactory implements ThreadFactory {

    private final String threadNamePrefix;
    private AtomicInteger indexer = new AtomicInteger(0);
    private boolean enableIndex;

    public DefaultThreadFactory(String threadNamePrefix, boolean enableIndex){
        this.threadNamePrefix = threadNamePrefix;
        this.enableIndex = enableIndex;
    }

    public Thread newThread(Runnable r) {
        String threadName = this.threadNamePrefix;
        if(this.enableIndex){
            threadName += indexer.incrementAndGet();
        }
        return new Thread(r,  threadName);
    }
}
