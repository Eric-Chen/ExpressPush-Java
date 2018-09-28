package com.github.expresspush.server;

import com.github.expresspush.config.ServerConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NettyRemotingServer implements RemotingServer {

    private static final String BOSS_THREAD_NAME_PREFIX = "NettyServer_boss_1";
    private static final String WORKER_THREAD_NAME_PREFIX = "NettyServer_worker_";


    public void start() {
        //todo setup config properties
        int workerCount = ServerConfig.WORKER_COUNT;

        //todo mix properties from startup args
        ServerBootstrap bootstrap = new ServerBootstrap();

        NioEventLoopGroup boss = new NioEventLoopGroup(1, new ThreadFactory() {
            public Thread newThread(Runnable r) {
                return new Thread(r, BOSS_THREAD_NAME_PREFIX);
            }
        });

        NioEventLoopGroup worker = new NioEventLoopGroup(workerCount, new ThreadFactory() {

            private AtomicInteger indexer = new AtomicInteger(0);

            public Thread newThread(Runnable r) {
                return new Thread(r, WORKER_THREAD_NAME_PREFIX + indexer.incrementAndGet());
            }
        });

        bootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_RCVBUF, 32*1024) //32k
                .childOption(ChannelOption.SO_SNDBUF, 32*1024) //32k

                //linux backlog default value: 1024; somaxconn default 128;
                //The impact of these 2 values is the accept queue = min(backlog, somaxconn)
                //if connections count exceed the limit, server won't accept any new connection.
//                .childOption(ChannelOption.SO_BACKLOG)
                .childHandler(new ChannelInitializer<Channel>() {
                    protected void initChannel(Channel ch) throws Exception {
                        //todo init pipeline handlers
                    }
                });
        //todo register shutdown hook
    }

    public void shutdown() {

    }

    public void status() {

    }
}
