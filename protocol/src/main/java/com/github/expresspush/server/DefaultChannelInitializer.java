package com.github.expresspush.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public class DefaultChannelInitializer extends ChannelInitializer<SocketChannel> {

    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
            .addLast("log", new LogHandler());
//            .addLast("encoder", new SimpleEncoder())
//            .addLast("decoder", new SimpleDecoder())
//            .addLast("idle", new IdleStateHandler(0, 0, 3, TimeUnit.SECONDS))
//            .addLast("messageHandler", new SimpleMessageHandler());
    }
}
