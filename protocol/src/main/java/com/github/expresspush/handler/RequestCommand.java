package com.github.expresspush.handler;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class RequestCommand {
    private static final Charset CHARSET_UTF8 = Charset.forName("utf-8");

    private Long rid;//request id

    private Long fromUid;//sent from uid

    private Long targetId;//sent to uid/groupId

    private Short type;//1.p2p 2.p2g

    private String jsonData;//sent content

    public short length(){
        int contentLength = jsonData.getBytes(CHARSET_UTF8).length;
        return (short)(
              8  //rid
            + 8  //fromUid
            + 8  //targetId
            + 2  //type
            + 2  //content bytes length
            + contentLength);
    }

    public ByteBuffer encode(){
        ByteBuffer buf = ByteBuffer.allocate(length());
        buf.putLong(rid);
        buf.putLong(fromUid);
        buf.putLong(targetId);
        buf.putShort(type);
        buf.putShort((short) (jsonData.getBytes(CHARSET_UTF8).length));
        buf.put(jsonData.getBytes(CHARSET_UTF8));
        buf.flip();
        return buf;
    }

    public static RequestCommand decode(ByteBuffer buf){
        RequestCommand result = new RequestCommand();
        result.setRid(buf.getLong());
        result.setFromUid(buf.getLong());
        result.setTargetId(buf.getLong());
        result.setType(buf.getShort());
        byte[] contentBytes = new byte[buf.getShort()];
        buf.get(contentBytes);
        result.setJsonData(new String(contentBytes, CHARSET_UTF8));
        return result;
    }

    @Override public String toString() {
        return "RequestCommand{" +
            "rid=" + rid +
            ", fromUid=" + fromUid +
            ", targetId=" + targetId +
            ", type=" + type +
            ", jsonData='" + jsonData + '\'' +
            '}';
    }

    public Long getRid() {
        return rid;
    }

    public void setRid(Long rid) {
        this.rid = rid;
    }

    public Long getFromUid() {
        return fromUid;
    }

    public void setFromUid(Long fromUid) {
        this.fromUid = fromUid;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public Short getType() {
        return type;
    }

    public void setType(Short type) {
        this.type = type;
    }

    public String getJsonData() {
        return jsonData;
    }

    public void setJsonData(String jsonData) {
        this.jsonData = jsonData;
    }
}
