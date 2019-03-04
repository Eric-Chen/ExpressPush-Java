package com.github.expresspush.handler.local;

import com.github.expresspush.handler.RequestCommand;
import com.github.expresspush.serial.simple.SimpleDataTransfer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleEncoder extends MessageToByteEncoder<RequestCommand>{

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleEncoder.class);

    private SimpleDataTransfer dataTransfer = new SimpleDataTransfer();

    @Override
    protected void encode(ChannelHandlerContext ctx, RequestCommand msg, ByteBuf out) throws Exception {
        out.writeBytes(dataTransfer.encode(msg));
    }

    @Override public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("exception.......", cause);
        ctx.channel().close();
    }
}
