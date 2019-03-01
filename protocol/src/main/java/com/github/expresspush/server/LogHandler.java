package com.github.expresspush.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogHandler.class);

    @Override public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        LOGGER.info("new channel registering... channel id: {}", ctx.channel().id());
    }

    @Override public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        LOGGER.info("channel is active, {}", ctx.channel().id());
    }

    @Override public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        LOGGER.info("show messge: >>>>>> {}", ((ByteBuf)msg).toString(CharsetUtil.UTF_8));
    }

    @Override public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        LOGGER.error("got exception", cause);
    }
}
