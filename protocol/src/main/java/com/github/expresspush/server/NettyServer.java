package com.github.expresspush.server;

import com.github.expresspush.Status;
import com.github.expresspush.basic.NettyBasicAction;
import com.github.expresspush.basic.RemoteAsyncCallback;
import com.github.expresspush.config.ServerConfig;
import com.github.expresspush.handler.ServerMessageHandler;
import com.github.expresspush.handler.TransferCommand;
import com.github.expresspush.handler.local.SimpleDecoder;
import com.github.expresspush.handler.local.SimpleEncoder;
import com.github.expresspush.route.RequestProcessService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.expresspush.config.ServerConfig.PORT_CONFIG_KEY;

public class NettyServer extends NettyBasicAction implements RemotingServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyServer.class);

    private static final long DEFAULT_REMOTE_TIMEOUT_MILLISECS = 3000;
    private static final String BOSS_THREAD_NAME = "NettyServer_boss_";
    private static final String WORKER_THREAD_NAME_PREFIX = "NettyServer_worker_";
    private static final String REQUEST_THREAD_NAME_PREFIX = "NettyServer_request_";
    private static final String DEFAULT_PORT = "52025";


    private AtomicReference<Status> status
            = new AtomicReference<>(Status.INITIALIZING);

    private ServerBootstrap bootstrap;

    private NioEventLoopGroup bossGroup;

    private NioEventLoopGroup workerGroup;

    /**
     * 主要服务请求的处理分发
     */
    private ExecutorService requestExecutor;

    private RequestProcessService requestProcessService;

    public void start() {
        //todo setup config properties
        int workerCount = ServerConfig.WORKER_COUNT;

        requestExecutor = Executors.newFixedThreadPool(ServerConfig.DEFAULT_REQUESTS_CONCURRENCY, new DefaultThreadFactory(REQUEST_THREAD_NAME_PREFIX, true));

        //todo mix properties from startup args
        bootstrap = new ServerBootstrap();

        bossGroup = new NioEventLoopGroup(3, new DefaultThreadFactory(BOSS_THREAD_NAME, true));

        workerGroup = new NioEventLoopGroup(workerCount, new DefaultThreadFactory(WORKER_THREAD_NAME_PREFIX, true));

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
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                            .addLast("encoder", new SimpleEncoder())
                            .addLast("decoder", new SimpleDecoder())
                            .addLast("idle", new IdleStateHandler(0, 0, 60, TimeUnit.SECONDS))
                            .addLast("messageHandler", new ServerMessageHandler(NettyServer.this));
                    }
                });

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

        this.requestExecutor.shutdown();
    }

    public void sendOneway(Channel channel, TransferCommand cmd){
        this.remoteSendOneway(channel, cmd, DEFAULT_REMOTE_TIMEOUT_MILLISECS);
    }

    public TransferCommand sendSync(Channel channel, TransferCommand cmd){
        return this.remoteSendSync(channel, cmd, DEFAULT_REMOTE_TIMEOUT_MILLISECS);
    }

    public void sendAsync(Channel channel, TransferCommand cmd, RemoteAsyncCallback callback) {
        this.remoteSendAsync(channel, cmd, DEFAULT_REMOTE_TIMEOUT_MILLISECS, callback);
    }

    private static class DefaultThreadFactory implements ThreadFactory {

        private final String threadNamePrefix;
        private AtomicInteger indexer = new AtomicInteger(0);
        private boolean enableIndex;

        public DefaultThreadFactory(String threadNamePrefix, boolean enableIndex){
            this.threadNamePrefix = threadNamePrefix;
            this.enableIndex = enableIndex;
        }

        public Thread newThread(Runnable r) {
            String threadName = this.threadNamePrefix;
            if(this.enableIndex){
                threadName += indexer.incrementAndGet();
            }
            return new Thread(r,  threadName);
        }
    }

    public String status() {
        return status.get().name();
    }

    public void setRequestProcessService(RequestProcessService requestProcessService){
        this.requestProcessService = requestProcessService;
    }

    public RequestProcessService getRequestProcessService() {
        return requestProcessService;
    }

    public ExecutorService getRequestExecutor() {
        return requestExecutor;
    }
}
