package com.github.expresspush.server;

import com.github.expresspush.handler.SimpleMessageHandler;
import com.github.expresspush.handler.local.SimpleDecoder;
import com.github.expresspush.handler.local.SimpleEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import java.util.concurrent.TimeUnit;

public class DefaultChannelInitializer extends ChannelInitializer<SocketChannel> {

    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
            .addLast("idle", new IdleStateHandler(0, 0, 60, TimeUnit.SECONDS))
            .addLast("encoder", new SimpleEncoder())
            .addLast("decoder", new SimpleDecoder())
            .addLast("messageHandler", new SimpleMessageHandler());
    }
}
