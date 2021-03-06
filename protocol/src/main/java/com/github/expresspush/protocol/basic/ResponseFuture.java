package com.github.expresspush.protocol.basic;

import com.github.expresspush.protocol.handler.TransferCommand;
import io.netty.channel.ChannelFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用途同 @InnerOnewaySemaphore
 */
public class ResponseFuture {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseFuture.class);

    private ChannelFuture chf;

    private Semaphore semaphoreAsync;

    private AtomicBoolean once = new AtomicBoolean(false);

    private long tickStart;

    private long timeoutMillisec;

    /* 执行状态 */
    private volatile boolean succeed;

    private TransferCommand resp;

    private CountDownLatch latch = new CountDownLatch(1);

    private RemoteAsyncCallback callback;

    public ResponseFuture(final ChannelFuture chf,
        final Semaphore semaphoreAsync,
        final long tickStart,
        final long timeoutMillisec,
        RemoteAsyncCallback callback){
        this.chf = chf;
        this.semaphoreAsync = semaphoreAsync;
        this.tickStart = tickStart;
        this.timeoutMillisec = timeoutMillisec;
        this.callback = callback;
    }

    public void release(){
        if(this.semaphoreAsync != null
            && once.compareAndSet(false, true)){
            this.semaphoreAsync.release();
        }
    }

    public void executeCallback(){
        if(callback != null){
            try {
                callback.actionCompleted(this);
            } catch (Exception e){
                LOGGER.error("Exception on response callback ", e);
            }
        }
    }

    public boolean isTimeout(){
        long now = System.nanoTime();
        long elapse = (now - this.tickStart)/1000000;
        return elapse > this.timeoutMillisec;
    }


    public void setSucceed(boolean succeed){
        this.succeed = succeed;
    }

    public void setResp(TransferCommand resp){
        this.resp = resp;
        this.latch.countDown();
    }

    public boolean await(long timeoutInMillisec) throws InterruptedException {
        return this.latch.await(timeoutInMillisec, TimeUnit.MILLISECONDS);
    }

    public boolean isSucceed() {
        return succeed;
    }

    public TransferCommand getResp() {
        return resp;
    }

    public long getTickStart() {
        return tickStart;
    }

    public long getTimeoutMillisec() {
        return timeoutMillisec;
    }
}
