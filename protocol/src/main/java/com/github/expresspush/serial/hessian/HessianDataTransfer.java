package com.github.expresspush.serial.hessian;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.github.expresspush.handler.RequestCommand;
import com.github.expresspush.serial.DataTransfer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.nio.ByteBuffer;

public class HessianDataTransfer implements DataTransfer<RequestCommand>{

    private Class <RequestCommand> entityClass;

    public HessianDataTransfer(){
        entityClass = (Class <RequestCommand>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public ByteBuffer encode(RequestCommand value) {
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

    public RequestCommand decode(ByteBuffer buffer) {
        ByteArrayInputStream bis = new ByteArrayInputStream(buffer.array());
        Hessian2Input input = new Hessian2Input(bis);
        try {
            return (RequestCommand)input.readObject(entityClass);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
