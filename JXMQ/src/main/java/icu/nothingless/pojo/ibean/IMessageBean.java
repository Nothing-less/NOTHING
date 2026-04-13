package icu.nothingless.pojo.ibean;

import java.time.LocalDateTime;

public interface IMessageBean {

    public void markAsRead();
    public boolean isRead();

    public Long getMsgId();

    public void setMsgId(Long msgId);

    public Long getSenderId();

    public void setSenderId(Long senderId);

    public Long getReceiverId();

    public void setReceiverId(Long receiverId);

    public Integer getMsgType();

    public void setMsgType(Integer msgType);

    public String getContents();

    public void setContents(String contents);

    public Integer getMsgStatus();

    public void setMsgStatus(Integer msgStatus);

    public LocalDateTime getSendTime();

    public void setSendTime(LocalDateTime sendTime);

    public LocalDateTime getReadTime();

    public void setReadTime(LocalDateTime readTime);

}
