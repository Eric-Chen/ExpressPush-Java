package com.github.expresspush.protocol.handler;

import com.github.expresspush.protocol.basic.ResponseFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.util.Map;

public abstract class SimpleMessageHandler extends SimpleChannelInboundHandler<TransferCommand> {


    @Override protected void channelRead0(ChannelHandlerContext ctx, TransferCommand msg) throws Exception {
        processResponseCommand(msg);
    }

    protected void processResponseCommand(TransferCommand msg) {
        Long respId = msg.getRespId();
        if(respId != null && respId > 0){
            ResponseFuture responseFuture = getResponseTable().get(respId);
            if(responseFuture != null){
                responseFuture.setResp(msg);
                responseFuture.release();
                responseFuture.executeCallback();

                getResponseTable().remove(respId);
            }
        }

    }

    protected abstract Map<Long, ResponseFuture> getResponseTable();

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
