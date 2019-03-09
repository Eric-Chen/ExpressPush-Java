package com.github.expresspush.protocol.handler;

import com.github.expresspush.protocol.basic.ResponseFuture;
import com.github.expresspush.protocol.route.RequestProcessService;
import com.github.expresspush.protocol.server.NettyServer;
import io.netty.channel.ChannelHandlerContext;
import java.util.Map;

public class ServerMessageHandler extends SimpleMessageHandler {

    private final NettyServer server;
    private final RequestProcessService requestProcessService;

    public ServerMessageHandler(NettyServer server) {
        this.server = server;
        this.requestProcessService = server.getRequestProcessService();
    }

    @Override protected void channelRead0(ChannelHandlerContext ctx, TransferCommand msg) throws Exception {
        super.channelRead0(ctx, msg);
        //todo handle request
        if(msg.getRespId() == null || msg.getRespId() == 0){
            //todo process to target service in async mode, not to block eventloop
            //todo Return from the service should send message to client through netty server directly
            Runnable reqTask = new Runnable() {
                @Override public void run() {
                    TransferCommand result = requestProcessService.process(msg);
                    //发送信息的接口在NettyServer，response的处理依靠server发送信息，业务有不必要的耦合
                    server.sendOneway(ctx.channel(), result);
                }
            };

            server.getRequestExecutor().submit(reqTask);
        }

    }

    @Override protected Map<Long, ResponseFuture> getResponseTable() {
        return server.getResponseTable();
    }
}
