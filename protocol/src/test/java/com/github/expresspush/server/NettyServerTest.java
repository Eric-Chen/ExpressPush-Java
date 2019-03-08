package com.github.expresspush.server;

import com.github.expresspush.handler.TransferCommand;
import com.github.expresspush.route.RequestProcessService;
import java.nio.channels.Channel;
import org.junit.Test;

public class NettyServerTest {
    private NettyServer server;

    @Test
    public void test_server(){
        server = new NettyServer();

        RequestProcessService rps = new RequestProcessService() {

            @Override public TransferCommand process(TransferCommand req) {
                TransferCommand result = new TransferCommand();
                result.setRespId(req.getReqId());
                result.setJsonData("jsondata");
                return result;
            }

            @Override public void register(Long id, Channel channel) {

            }
        };

        server.setRequestProcessService(rps);
        server.start();
    }
}