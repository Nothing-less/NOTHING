package icu.nothingless.pojo.bean;

import java.util.Date;

import icu.nothingless.pojo.adapter.iMSGAdapter;

public class Message implements java.io.Serializable, iMSGAdapter {
    public static final int TYPE_TEXT = 1;
    public static final int TYPE_IMAGE = 2;
    public static final int TYPE_FILE = 3;
    
    public static final int STATUS_UNREAD = 0;
    public static final int STATUS_READ = 1;
    public static final int STATUS_RECALLED = 2;
    
    private Long msgId;
    private Long senderId;
    private Long receiverId;
    private Integer msgType;
    private String content;
    private Integer status;
    private Date sendTime;
    private Date readTime;
    
    // 非持久化字段
    private String senderNickname;
    private String senderAvatar;
    public Long getMsgId() {
        return msgId;
    }
    public void setMsgId(Long msgId) {
        this.msgId = msgId;
    }
    public Long getSenderId() {
        return senderId;
    }
    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }
    public Long getReceiverId() {
        return receiverId;
    }
    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }
    public Integer getMsgType() {
        return msgType;
    }
    public void setMsgType(Integer msgType) {
        this.msgType = msgType;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public Integer getStatus() {
        return status;
    }
    public void setStatus(Integer status) {
        this.status = status;
    }
    public Date getSendTime() {
        return sendTime;
    }
    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }
    public Date getReadTime() {
        return readTime;
    }
    public void setReadTime(Date readTime) {
        this.readTime = readTime;
    }
    public String getSenderNickname() {
        return senderNickname;
    }
    public void setSenderNickname(String senderNickname) {
        this.senderNickname = senderNickname;
    }
    public String getSenderAvatar() {
        return senderAvatar;
    }
    public void setSenderAvatar(String senderAvatar) {
        this.senderAvatar = senderAvatar;
    }


}
