package com.github.expresspush.client;

import com.github.expresspush.Status;
import com.github.expresspush.handler.RequestCommand;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetSocketAddress;

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
                    //fixme add handlers

                }
            });
        oneChannel.register();
    }


    public boolean sendMessage(RequestCommand req){
        //fixme 定义sendOneway的返回结果
        this.oneChannel.sendOneway(req);
        return true;//fixme
    }

    public boolean sendMessage1(String content){
        //fixme 定义sendOneway的返回结果
        this.oneChannel.testsendOneway(content);
        return true;//fixme
    }

}
