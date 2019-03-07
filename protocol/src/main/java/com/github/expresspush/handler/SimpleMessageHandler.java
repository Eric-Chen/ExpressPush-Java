package com.github.expresspush.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleMessageHandler extends SimpleChannelInboundHandler<TransferCommand> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleMessageHandler.class);

    @Override protected void channelRead0(ChannelHandlerContext ctx, TransferCommand msg) throws Exception {
        System.out.println("=======================");
        LOGGER.info("show input {}", msg);
    }

//    @Override
//    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
//        if (evt instanceof IdleStateEvent) {
//            IdleStateEvent event = (IdleStateEvent) evt;
//            if (event.state().equals(IdleState.ALL_IDLE)) {
//                ctx.channel().close();
//            }
//        }
//
//        ctx.fireUserEventTriggered(evt);
//    }
}
