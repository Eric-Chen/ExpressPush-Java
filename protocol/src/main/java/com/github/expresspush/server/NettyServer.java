package com.github.expresspush.server;

import com.github.expresspush.Status;
import com.github.expresspush.config.ServerConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.expresspush.config.ServerConfig.PORT_CONFIG_KEY;

public class NettyServer implements RemotingServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyServer.class);

    private static final String BOSS_THREAD_NAME = "NettyServer_boss_1";
    private static final String WORKER_THREAD_NAME_PREFIX = "NettyServer_worker_";
    private static final String DEFAULT_PORT = "52025";

    private AtomicReference<Status> status
            = new AtomicReference<>(Status.INITIALIZING);

    private ServerBootstrap bootstrap;

    private NioEventLoopGroup bossGroup;

    private NioEventLoopGroup workerGroup;

    public void start() {
        //todo setup config properties
        int workerCount = ServerConfig.WORKER_COUNT;

        //todo mix properties from startup args
        bootstrap = new ServerBootstrap();

        bossGroup = new NioEventLoopGroup(3, new ThreadFactory() {
            public Thread newThread(Runnable r) {
                return new Thread(r, BOSS_THREAD_NAME);
            }
        });

        workerGroup = new NioEventLoopGroup(workerCount, new ThreadFactory() {

            private AtomicInteger indexer = new AtomicInteger(0);

            public Thread newThread(Runnable r) {
                return new Thread(r, WORKER_THREAD_NAME_PREFIX + indexer.incrementAndGet());
            }
        });

        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_RCVBUF, 32*1024) //32k
                .childOption(ChannelOption.SO_SNDBUF, 32*1024) //32k

                //linux backlog default value: 1024; somaxconn default 128;
                //The impact of these 2 values is the accept queue = min(backlog, somaxconn)
                //if connections count exceed the limit, server won't accept any new connection.
//                .childOption(ChannelOption.SO_BACKLOG)
                .childHandler(new DefaultChannelInitializer());

        String port = ServerConfig.getSystemProperty(PORT_CONFIG_KEY, DEFAULT_PORT);

        try {
            if(!status.compareAndSet(Status.INITIALIZING, Status.RUNNING)){
                LOGGER.error("[Server] error when changing status: {}", status.get());
            }

            ChannelFuture future = bootstrap.bind(Integer.valueOf(port)).sync();
            LOGGER.info("[Server] started...");
            //fixme listen to close action, but this could block the following actions, being quit....
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            LOGGER.error("[Server] bind/close interrupted error", e);
        } finally {
            shutdown();
        }

        //todo register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            shutdown();
        }));
    }

    public void shutdown() {
        if(!status.compareAndSet(Status.RUNNING, Status.STOPPED)){
            LOGGER.error("[Server] shutdown error when changing status: {}", status.get());
        }
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    public String status() {
        return status.get().name();
    }
}
