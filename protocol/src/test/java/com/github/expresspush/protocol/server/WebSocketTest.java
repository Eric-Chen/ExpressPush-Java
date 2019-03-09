package com.github.expresspush.protocol.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.junit.Test;

public class WebSocketTest {

    /**
     * websocket server端
     * 可以通过在线websocket客户端测试：http://www.blue-zero.com/WebSocket/
     * 访问地址：ws://127.0.0.1:9900/ws
     * @throws InterruptedException
     */
    @Test
    public void test_websocket_server() throws InterruptedException {
        EventLoopGroup b = new NioEventLoopGroup();
        EventLoopGroup w = new NioEventLoopGroup();

        try {
            ServerBootstrap sbs = new ServerBootstrap();
            sbs.group(b, w)
                .channel(NioServerSocketChannel.class)
                .localAddress(9900)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast("codec", new HttpServerCodec())
                            .addLast("aggregator", new HttpObjectAggregator(2 * 1024)) // 2k
                            .addLast("websocket", new WebSocketServerProtocolHandler("/ws"))
                            .addLast("my", new ShowMessageHandler());
                    }
                });
            ChannelFuture f = sbs.bind().sync();
            f.channel().closeFuture().sync();
        } finally {
            w.shutdownGracefully();
            b.shutdownGracefully();
        }
    }


    static class ShowMessageHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
        @Override protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
            System.out.println(msg.text());
            ctx.channel().writeAndFlush(msg.retain());
        }
    }
}
