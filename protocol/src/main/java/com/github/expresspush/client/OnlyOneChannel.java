package com.github.expresspush.client;

import com.github.expresspush.handler.RequestCommand;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.CharsetUtil;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public class OnlyOneChannel {

    private volatile Channel channel;

    private AtomicBoolean flag = new AtomicBoolean(false);

    private ConcurrentMap<Long/* requestId */, ChannelFuture/* waitFuture */> requestTable = new ConcurrentHashMap<>(1024);

    private ReentrantLock lock = new ReentrantLock();

    private NettyClient client;

    public OnlyOneChannel(NettyClient client){
        this.client = client;
    }

    public void register(){
        try {
            if (lock.tryLock()) {
                if (flag.compareAndSet(false, true)) {
                    ChannelFuture channelFuture = client.bootstrap.connect();
                    //fixme 暂不做任何检查
                    if(channelFuture.await(3000, TimeUnit.MILLISECONDS)){
                        if(channelFuture.channel() != null && channelFuture.channel().isActive()){
                            this.channel = channelFuture.channel();
                        }
                    }

                } else {
                    throw new IllegalStateException("channel is already set");
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

    }

    public void releaseChannel(){
        try {
            if (lock.tryLock()){
                if(flag.compareAndSet(true, false)){
                    channel.close();
                    channel = null;
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void sendOneway(RequestCommand request){
        this.channel.writeAndFlush(request);
    }
//fixme delete
    public void testsendOneway(String request){
        this.channel.writeAndFlush(Unpooled.copiedBuffer(request, CharsetUtil.UTF_8));
    }

}
