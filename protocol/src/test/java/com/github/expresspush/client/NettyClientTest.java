package com.github.expresspush.client;

import com.github.expresspush.handler.TransferCommand;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class NettyClientTest {

    private NettyClient client;

    @Before
    public void init() throws Exception {
        client = new NettyClient();
        client.start();

//        EchoClient nc = new EchoClient("127.0.0.1", 9090);
//        nc.start();
    }

    @After
    public void release(){
//        client.shutdown();
    }

    /**
     * 基本通信，数据压缩
     * */
    @Test
    public void test_client() throws IOException {
        TransferCommand req = new TransferCommand();
        req.setRid(1L);
        req.setFromUid(2L);
        req.setTargetId(3L);
        req.setType((short)11);
        req.setJsonData("test");

        for (int i = 0; i < 5; i++) {
            client.sendMessageOneway(req);
        }

        System.in.read();
    }

}

class EchoClient {
    private final String host;
    private final int port;

    public EchoClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                .channel(NioSocketChannel.class)
//                .remoteAddress(new InetSocketAddress(host, port))
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(
                            new EchoClientHandler());
                    }
                });
            ChannelFuture f = b.connect(host, port).sync();
            if(f.channel().isActive()){
                for (int i = 0; i < 5; i++) {
                    f.channel().writeAndFlush(Unpooled.copiedBuffer("Netty rocks! ==="+i,
                        CharsetUtil.UTF_8));
                }
            }

//            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }

}
class EchoClientHandler extends SimpleChannelInboundHandler<ByteBuf> {
    @Override
    public void channelActive(ChannelHandlerContext ctx) {

    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf in) {
        System.out.println(
            "Client received: " + in.toString(CharsetUtil.UTF_8));
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,
        Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}