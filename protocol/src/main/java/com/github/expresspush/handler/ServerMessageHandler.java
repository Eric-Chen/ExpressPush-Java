package com.github.expresspush.handler;

import io.netty.channel.ChannelHandlerContext;

public class ServerMessageHandler extends SimpleMessageHandler {

    @Override protected void channelRead0(ChannelHandlerContext ctx, TransferCommand msg) throws Exception {
        super.channelRead0(ctx, msg);
    }
}
