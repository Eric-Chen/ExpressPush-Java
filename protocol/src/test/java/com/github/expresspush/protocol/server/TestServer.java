package com.github.expresspush.protocol.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class TestServer {

    public static void main(String[] args) throws InterruptedException {
        ServerBootstrap sbs = new ServerBootstrap();
        EventLoopGroup boss = new NioEventLoopGroup(3, new ThreadFactory() {
            private AtomicInteger count = new AtomicInteger(0);
            @Override public Thread newThread(Runnable r) {
                return new Thread(r, "server_boss_"+count.incrementAndGet());
            }
        });
        EventLoopGroup worker = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2, new ThreadFactory() {
            private static final String WORKER_THREAD_NAME_PREFIX = "NettyServer_worker_";
            private AtomicInteger indexer = new AtomicInteger(0);
            public Thread newThread(Runnable r) {
                return new Thread(r, WORKER_THREAD_NAME_PREFIX + indexer.incrementAndGet());
            }
        });

        try{
            sbs.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .localAddress(9090)
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast("log", new LogHandler());
                    }
                });
            sbs.bind().sync().channel().closeFuture().sync();
        } finally {
            worker.shutdownGracefully();
            boss.shutdownGracefully();
        }


    }

}
