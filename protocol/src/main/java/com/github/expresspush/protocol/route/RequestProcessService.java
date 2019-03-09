package com.github.expresspush.protocol.route;

import com.github.expresspush.protocol.handler.TransferCommand;
import java.nio.channels.Channel;

public interface RequestProcessService {

    TransferCommand process(TransferCommand req);

    /**
     * channel的使用有两种场景，1）发送请求；2）发送响应
     * 发送请求的场景，需要使用业务ID，通过这个ID来获取需要使用的channel；
     * 发送响应，是通过客户端过来的请求ID找到返回的channel
     * @param id
     * @param channel
     */
    void register(Long id, Channel channel);

}
