package com.github.expresspush.handler;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class TransferCommand {
    private static final Charset CHARSET_UTF8 = Charset.forName("utf-8");

    private Long reqId;//request id

    private Long respId = 0L;//response id

    private Long fromUid;//sent from uid

    private Long targetId;//sent to uid/groupId

    private Short type;//1.p2p 2.p2g

    private short oneway = 0;

    private String jsonData;//sent content

    public void markOneway(){
        this.oneway = 1;
    }

    public short length(){
        int contentLength = jsonData.getBytes(CHARSET_UTF8).length;
        return (short)(
            8  //reqId
            + 8  //respId
            + 8  //fromUid
            + 8  //targetId
            + 2  //type
            + 2  //oneway
            + 2  //content bytes length
            + contentLength);
    }

    public ByteBuffer encode(){
        ByteBuffer buf = ByteBuffer.allocate(length());
        buf.putLong(reqId  == null ? 0L : reqId);
        buf.putLong(respId == null ? 0L : respId);
        buf.putLong(fromUid== null ? 0L : fromUid);
        buf.putLong(targetId== null ? 0L : targetId);
        buf.putShort(type == null ? 1 : type);
        buf.putShort(oneway);//oneway default value 0
        buf.putShort((short) (jsonData.getBytes(CHARSET_UTF8).length));
        buf.put(jsonData.getBytes(CHARSET_UTF8));
        buf.flip();
        return buf;
    }

    public static TransferCommand decode(ByteBuffer buf){
        TransferCommand result = new TransferCommand();
        result.setReqId(buf.getLong());
        result.setRespId(buf.getLong());
        result.setFromUid(buf.getLong());
        result.setTargetId(buf.getLong());
        result.setType(buf.getShort());
        result.setOneway(buf.getShort());
        byte[] contentBytes = new byte[buf.getShort()];
        buf.get(contentBytes);
        result.setJsonData(new String(contentBytes, CHARSET_UTF8));
        return result;
    }

    @Override public String toString() {
        return "TransferCommand{" +
            "reqId=" + reqId +
            ", respId=" + respId +
            ", fromUid=" + fromUid +
            ", targetId=" + targetId +
            ", type=" + type +
            ", oneway=" + oneway +
            ", jsonData='" + jsonData + '\'' +
            '}';
    }

    public Long getReqId() {
        return reqId;
    }

    public void setReqId(Long reqId) {
        this.reqId = reqId;
    }

    public Long getRespId() {
        return respId;
    }

    public void setRespId(Long respId) {
        this.respId = respId;
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

    public short getOneway() {
        return oneway;
    }

    public void setOneway(short oneway) {
        this.oneway = oneway;
    }

    public String getJsonData() {
        return jsonData;
    }

    public void setJsonData(String jsonData) {
        this.jsonData = jsonData;
    }
}
