package com.github.expresspush.client;

import com.github.expresspush.Status;
import com.github.expresspush.basic.NettyBasicAction;
import com.github.expresspush.handler.ClientMessageHandler;
import com.github.expresspush.handler.TransferCommand;
import com.github.expresspush.handler.local.SimpleDecoder;
import com.github.expresspush.handler.local.SimpleEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
//todo 单独用client来控制连接不够，因为一个client只有一个连接可用。应该提供一个factory来保证有活着的client, 为此就仍然需要给client定义生命周期和状态
public class NettyClient extends NettyBasicAction {

    private static final long DEFAULT_REQUEST_TIMEOUT_MILLISECS = 3000;

    private volatile Status status = Status.INITIALIZING;

    //todo 这是点对点的处理方式，不利于分布式的管理。需要换成路由模式
    private String saddr;

    private int port;

    //todo 确保唯一channel vs 确保唯一client
    private Channel channel;

    EventLoopGroup group;

    Bootstrap bootstrap;

    private final ScheduledExecutorService refreshTimeoutResponseScheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory(){

        private static final String THREAD_NAME_PREFIX = "client_result_check_schedule_";
        private AtomicInteger count = new AtomicInteger(0);

        @Override public Thread newThread(Runnable r) {
            String tn = THREAD_NAME_PREFIX + count.incrementAndGet();
            logger.info("[Client] result schedule request new thread, new name: [{}]", tn);
            return new Thread(r, tn);
        }
    });

    public NettyClient(String saddr, int port){
        this.saddr = saddr;
        this.port = port;
    }

    public void shutdown(){
        group.shutdownGracefully();
    }

    public void start() {
        if(this.status != Status.INITIALIZING) {
            throw new IllegalStateException("client has already initialized");
        }

        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group)
            .channel(NioSocketChannel.class)
            .remoteAddress(new InetSocketAddress(saddr, port))
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline()
                        .addLast("encoder", new SimpleEncoder())
                        .addLast("decoder", new SimpleDecoder())
                        .addLast("idle", new IdleStateHandler(0, 0, 60, TimeUnit.SECONDS))
                        .addLast("messageHandler", new ClientMessageHandler());
                }
            });

        register();
        //注册检查返回结果的schedule task
        refreshTimeoutResponseScheduler.scheduleAtFixedRate(() -> refreshResponseTable(), 1000, 1000, TimeUnit.MILLISECONDS);
    }

    void register(){
        logger.info("[Client] start to connect to server");
        ChannelFuture channelFuture = this.bootstrap.connect();
        try {
            if(channelFuture.await(3000, TimeUnit.MILLISECONDS)){
                if(channelFuture.channel() != null && channelFuture.channel().isActive()){
                    this.channel = channelFuture.channel();
                    logger.info("[Client] connected to server");
                }
            }
        } catch (InterruptedException e) {
            logger.info("[Client] got exception", e);
        }

    }

    public void sendMessageOneway(TransferCommand req){
        sendOneway(this.channel, req, DEFAULT_REQUEST_TIMEOUT_MILLISECS);
    }



}
