package com.github.expresspush.client;

import com.github.expresspush.Status;
import com.github.expresspush.handler.RequestCommand;
import com.github.expresspush.handler.SimpleMessageHandler;
import com.github.expresspush.handler.local.SimpleDecoder;
import com.github.expresspush.handler.local.SimpleEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 *
 */
//todo 单独用client来控制连接不够，因为一个client只有一个连接可用。应该提供一个factory来保证有活着的client, 为此就仍然需要给client定义生命周期和状态
public class NettyClient {

    private volatile Status status;

    private String saddr;

    private int port;

    private final OnlyOneChannel oneChannel;

    EventLoopGroup group;

    Bootstrap bootstrap;

    public NettyClient(String saddr, int port){
        this.saddr = saddr;
        this.port = port;
        oneChannel = new OnlyOneChannel(this);
    }

    public void shutdown(){
        oneChannel.releaseChannel();
        group.shutdownGracefully();
    }

    public void start() {
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group)
            .channel(NioSocketChannel.class)
            .remoteAddress(new InetSocketAddress(saddr, port))
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline()
                        .addLast("idle", new IdleStateHandler(0, 0, 60, TimeUnit.SECONDS))
                        .addLast("encoder", new SimpleEncoder())
                        .addLast("decoder", new SimpleDecoder())
                        .addLast("messageHandler", new SimpleMessageHandler());
                }
            });
        oneChannel.register();
    }


    public boolean sendMessage(RequestCommand req){
        //fixme 定义sendOneway的返回结果
        this.oneChannel.sendOneway(req);
        return true;//fixme
    }

}
