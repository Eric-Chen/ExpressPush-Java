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

public class NettyClient extends NettyBasicAction {

    private static final long DEFAULT_REQUEST_TIMEOUT_MILLISECS = 3000;
    public static final int DEFAULT_CONNECT_TIMEOUT_MILLISECS = 3000;

    private volatile Status status = Status.INITIALIZING;

    private String serverAddr;

    private int port;

    /* 不可用时，会切换channel*/
    private volatile Channel channel;

    EventLoopGroup group;

    Bootstrap bootstrap;

    private final ScheduledExecutorService refreshTimeoutResponseScheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory(){
        @Override public Thread newThread(Runnable r) {
            logger.info("[Client] result schedule request new thread, new name: client_result_check_schedule");
            return new Thread(r, "client_result_check_schedule");
        }
    });

    private final ScheduledExecutorService checkChannelScheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        @Override public Thread newThread(Runnable r) {
            logger.info("[Client] check channel schedule request new thread, new name: client_result_check_schedule");
            return new Thread(r, "client_channel_check_schedule");
        }
    });

    public NettyClient(String serverAddr, int port){
        this.serverAddr = serverAddr;
        this.port = port;
    }

    private InetSocketAddress routeToServer(){
        //todo get server config through http??
        return new InetSocketAddress(serverAddr, port);
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
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline()
                        .addLast("encoder", new SimpleEncoder())
                        .addLast("decoder", new SimpleDecoder())
                        .addLast("idle", new IdleStateHandler(0, 0, 60, TimeUnit.SECONDS))
                        .addLast("messageHandler", new ClientMessageHandler(NettyClient.this));
                }
            });

        register();

        //注册检查返回结果的schedule task
        refreshTimeoutResponseScheduler.scheduleAtFixedRate(() -> refreshResponseTable(), 3000, 1000, TimeUnit.MILLISECONDS);
    }

    void register(){
        logger.info("[Client] start to connect to server");
        ChannelFuture channelFuture = this.bootstrap.connect(routeToServer());
        try {
            if(channelFuture.await(DEFAULT_CONNECT_TIMEOUT_MILLISECS, TimeUnit.MILLISECONDS)){
                if(channelFuture.channel() != null && channelFuture.channel().isActive()){
                    this.channel = channelFuture.channel();
                    logger.info("[Client] connected to server");
                }
            }
        } catch (InterruptedException e) {
            logger.info("[Client] got exception", e);
        }
    }

    void releaseChannel(){
        if(!this.channel.isActive()){
            //todo release the inactive channel
            //fixme should do some cleaning job on the channel?
            this.channel.close();
        }
    }

    public void requestOneway(TransferCommand req){
        sendOneway(this.channel, req, DEFAULT_REQUEST_TIMEOUT_MILLISECS);
    }

    public TransferCommand requestSync(TransferCommand req){
        return remoteSendSync(this.channel, req,DEFAULT_REQUEST_TIMEOUT_MILLISECS);
    }

}
