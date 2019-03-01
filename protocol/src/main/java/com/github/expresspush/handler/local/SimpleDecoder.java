package com.github.expresspush.handler.local;

import com.github.expresspush.handler.RequestCommand;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import java.nio.ByteBuffer;

public class SimpleDecoder extends LengthFieldBasedFrameDecoder {

    private static final int DEFAULT_MAX_LENGTH = 65535;

    public SimpleDecoder(){
        super(
            DEFAULT_MAX_LENGTH,
            0,
            2,
            0,
            2
        );
    }

    @Override protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = null;
        try {
            frame = (ByteBuf) super.decode(ctx, in);
            if (null == frame) {
                return null;
            }

            ByteBuffer byteBuffer = frame.nioBuffer();

            return RequestCommand.decode(byteBuffer);
        } catch (Exception e) {
            e.printStackTrace();
            ctx.channel().close();
        } finally {
            if (null != frame) {
                frame.release();
            }
        }

        return null;
    }
}
