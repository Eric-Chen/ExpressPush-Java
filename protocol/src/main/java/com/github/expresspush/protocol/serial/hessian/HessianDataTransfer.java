package com.github.expresspush.protocol.serial.hessian;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.github.expresspush.protocol.handler.TransferCommand;
import com.github.expresspush.protocol.serial.DataTransfer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.nio.ByteBuffer;

public class HessianDataTransfer implements DataTransfer<TransferCommand>{

    private Class <TransferCommand> entityClass;

    public HessianDataTransfer(){
        entityClass = (Class <TransferCommand>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public ByteBuffer encode(TransferCommand value) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Hessian2Output output = new Hessian2Output(bos);
        try {
            output.writeObject(value);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ByteBuffer.wrap(bos.toByteArray());
    }

    public TransferCommand decode(ByteBuffer buffer) {
        ByteArrayInputStream bis = new ByteArrayInputStream(buffer.array());
        Hessian2Input input = new Hessian2Input(bis);
        try {
            return (TransferCommand)input.readObject(entityClass);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
