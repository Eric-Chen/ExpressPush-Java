package com.github.expresspush.protocol.handler;

import com.github.expresspush.protocol.basic.ResponseFuture;
import com.github.expresspush.protocol.client.NettyClient;
import io.netty.channel.ChannelHandlerContext;
import java.util.Map;

public class ClientMessageHandler extends SimpleMessageHandler {

    private final NettyClient client;

    public ClientMessageHandler(NettyClient client) {
        this.client = client;
    }

    @Override protected void channelRead0(ChannelHandlerContext ctx, TransferCommand msg) throws Exception {
        super.channelRead0(ctx, msg);
        //todo handle request
        if(msg.getRespId() == null
            || msg.getRespId() == 0){
            //todo process to target service in async mode, not to block eventloop
            //todo Return from the service should send message to client through netty server directly
            Runnable reqTask = new Runnable() {
                @Override public void run() {
                    TransferCommand result = client.getRequestProcessService().process(msg);
                    client.sendOneway(result);
                }
            };

            client.getRequestExecutor().submit(reqTask);
        }

    }

    @Override protected Map<Long, ResponseFuture> getResponseTable() {
        return client.getResponseTable();
    }
}
