package com.github.expresspush.handler.local;

import com.github.expresspush.handler.RequestCommand;
import com.github.expresspush.serial.simple.SimpleDataTransfer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class SimpleTransferTest {

    @Test
    public void test_0(){
        int l = 65510;

        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.putInt(l);
        buf.flip();
        System.out.println(Arrays.toString(buf.array()));


        byte[] b = new byte[4];
        b[0] = (byte) 1;
        b[1] = (byte) ((l >> 16) & 0xFF);
        b[2] = (byte) ((l >> 8) & 0xFF);
        b[3] = (byte) ( l & 0xFF);
        System.out.println(Arrays.toString(b));
        System.out.println(0xFF);
    }

    @Test
    public void test_bytebuffer(){
        ByteBuffer buf = ByteBuffer.allocate(4 + 4 + 10);//length + headerlength
        buf.putInt(4 + 10 + 20);
        buf.putInt(10);
        byte[] headerData = new String("Helloworld").getBytes();
        buf.put(headerData);
        buf.flip();
        System.out.println(buf.array().length);
    }

    @Test
    public void test_handlers_order(){
        EmbeddedChannel channel = new EmbeddedChannel(new SimpleChannelInboundHandler<Object>() {
            @Override protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                System.out.println("order 1");
                ctx.fireChannelRead(msg);
            }
        },
            new SimpleChannelInboundHandler<Object>() {
                @Override protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                    System.out.println("order 2");
                }
            },
            new ChannelOutboundHandlerAdapter(){
                @Override
                public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                    super.write(ctx, msg, promise);
                    System.out.println("order 3");
                }
            },
            new ChannelOutboundHandlerAdapter(){
                @Override
                public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                    super.write(ctx, msg, promise);
                    System.out.println("order 4");
                }
            });


        channel.writeInbound("test");
        channel.readInbound();

        channel.writeOutbound("ttt");
        channel.readOutbound();
    }


    @Test
    public void test_codecs(){

        EmbeddedChannel channel = new EmbeddedChannel(new ByteToMessageDecoder() {
            @Override protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
                while(in.readableBytes() >= 4){
                    out.add( in.readInt() );
                }
                System.out.println("not enough bytes");
            }
        });

        ByteBuf buf = Unpooled.buffer(8);
//        for(int i = 0; i < 10; i++ ){
//            buf.writeInt(i);
//        }

        buf.writeShort((short) 1);
        buf.writeShort((short) 1);

        channel.writeInbound(buf);

        for(Integer out = channel.readInbound(); out != null; out = channel.readInbound()) {
            System.out.println(out);
        }


    }

    @Test
    public void test_simple_encode_and_decode(){

        RequestCommand req = new RequestCommand();
        req.setRid(1L);
        req.setFromUid(2L);
        req.setTargetId(3L);
        req.setType((short)11);
        req.setJsonData("test");
        System.out.println(">>>>>>>> length:" + req.length());

        ByteBuf buf = Unpooled.buffer();
        SimpleDataTransfer transfer = new SimpleDataTransfer();
        buf.writeBytes(transfer.encode(req));


        EmbeddedChannel channel = new EmbeddedChannel(new SimpleEncoder(),
            new SimpleDecoder());
        channel.writeOutbound(req);
        ByteBuf r = channel.readOutbound();
        System.out.println("=========="+ r.getClass().getCanonicalName() + "===============");
        System.out.println("=========="+ r.readableBytes() + "===============");
        //use encoded data as inbound data
        channel.writeInbound(r);
        RequestCommand rs = channel.readInbound();
        System.out.println(rs);
    }


}