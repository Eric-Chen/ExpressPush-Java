package com.github.expresspush.serial.simple;

import com.github.expresspush.handler.RequestCommand;
import com.github.expresspush.serial.DataTransfer;
import java.nio.ByteBuffer;

public class SimpleDataTransfer implements DataTransfer<RequestCommand> {

    /**
     * 先写入字段长度参数
     * lengh field decoder: length + all bytes
     * 再写入内容
     * @param msg
     * @return
     */
    @Override
    public ByteBuffer encode(RequestCommand msg) {
        int contentLength = msg.length();
        short fullLength = (short) (2 + contentLength);
        ByteBuffer msgBuf = msg.encode();
        ByteBuffer buf = ByteBuffer.allocate(fullLength);
        buf.putShort((short) contentLength);//设置length field，
                                            // 要搞清楚是 1）整个message的长度[ 2(length field) + content length ],还是 2）只有[content length]
                                            // 如果是1）则需要设置LengthFieldBasedFrameDecoder.lengthAdjustment=-2 （2是short类型的length），以截除length字段的长度
                                            // 如果是2）设置LengthFieldBasedFrameDecoder.lengthAdjustment=0即可
        buf.put(msgBuf);
        buf.flip();//keep it in mind
        return buf;
    }

    @Override
    public RequestCommand decode(ByteBuffer buffer) {
        return RequestCommand.decode(buffer);
    }

}
