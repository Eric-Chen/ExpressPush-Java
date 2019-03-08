package com.github.expresspush.handler;

import com.github.expresspush.basic.ResponseFuture;
import com.github.expresspush.client.NettyClient;
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
        if(msg.getRespId() == null){
            //todo process to target service in async mode, not to block eventloop
            //todo Return from the service should send message to client through netty server directly
            Runnable reqTask = new Runnable() {
                @Override public void run() {
                    TransferCommand result = client.getRequestProcessService().process(msg);
                    //发送信息的接口在NettyServer，response的处理依靠server发送信息，业务有不必要的耦合
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
