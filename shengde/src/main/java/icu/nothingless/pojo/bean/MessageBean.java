package icu.nothingless.pojo.bean;

import java.time.LocalDateTime;

import icu.nothingless.pojo.adapter.IMSGAdapter;

public class MessageBean implements java.io.Serializable, IMSGAdapter {

    // ==================== 字段（与数据库表一一对应）====================

    private Long msgId; // 消息ID（自增主键）
    private Long senderId; // 发送者ID（必填）
    private Long receiverId; // 接收者ID（必填）
    private Integer msgType; // 消息类型（1=文本，2=图片等）
    private String contents; // 消息内容
    private Integer msgStatus; // 消息状态（0=未读，1=已读）
    private LocalDateTime sendTime; // 发送时间（默认当前时间）
    private LocalDateTime readTime; // 读取时间（可为空）

    // ==================== 构造方法 ====================

    public MessageBean() {
    }

    private MessageBean(Builder builder) {
        this.msgId = builder.msgId;
        this.senderId = builder.senderId;
        this.receiverId = builder.receiverId;
        this.msgType = builder.msgType;
        this.contents = builder.contents;
        this.msgStatus = builder.msgStatus;
        this.sendTime = builder.sendTime;
        this.readTime = builder.readTime;
    }

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

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public Integer getMsgStatus() {
        return msgStatus;
    }

    public void setMsgStatus(Integer msgStatus) {
        this.msgStatus = msgStatus;
    }

    public LocalDateTime getSendTime() {
        return sendTime;
    }

    public void setSendTime(LocalDateTime sendTime) {
        this.sendTime = sendTime;
    }

    public LocalDateTime getReadTime() {
        return readTime;
    }

    public void setReadTime(LocalDateTime readTime) {
        this.readTime = readTime;
    }

    // ==================== Builder 模式 ====================

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long msgId;
        private Long senderId;
        private Long receiverId;
        private Integer msgType = 1; // 默认文本消息
        private String contents;
        private Integer msgStatus = 0; // 默认未读
        private LocalDateTime sendTime;
        private LocalDateTime readTime;

        private Builder() {
        }

        public Builder msgId(Long msgId) {
            this.msgId = msgId;
            return this;
        }

        public Builder senderId(Long senderId) {
            this.senderId = senderId;
            return this;
        }

        public Builder receiverId(Long receiverId) {
            this.receiverId = receiverId;
            return this;
        }

        public Builder msgType(Integer msgType) {
            this.msgType = msgType;
            return this;
        }

        public Builder contents(String contents) {
            this.contents = contents;
            return this;
        }

        public Builder msgStatus(Integer msgStatus) {
            this.msgStatus = msgStatus;
            return this;
        }

        public Builder sendTime(LocalDateTime sendTime) {
            this.sendTime = sendTime;
            return this;
        }

        public Builder readTime(LocalDateTime readTime) {
            this.readTime = readTime;
            return this;
        }

        public MessageBean build() {
            if (senderId == null)
                throw new IllegalStateException("senderId is required");
            if (receiverId == null)
                throw new IllegalStateException("receiverId is required");
            return new MessageBean(this);
        }
    }

    // ==================== 业务方法 ====================

    /**
     * 标记为已读
     */
    public void markAsRead() {
        this.msgStatus = 1;
        this.readTime = LocalDateTime.now();
    }

    /**
     * 是否为已读
     */
    public boolean isRead() {
        return Integer.valueOf(1).equals(this.msgStatus);
    }

    // ==================== Object 方法 ====================

    @Override
    public String toString() {
        return "MessageEntity{" +
                "msgId=" + msgId +
                ", senderId=" + senderId +
                ", receiverId=" + receiverId +
                ", msgType=" + msgType +
                ", contents='" + (contents != null && contents.length() > 20
                        ? contents.substring(0, 20) + "..."
                        : contents)
                + '\'' +
                ", msgStatus=" + msgStatus +
                ", sendTime=" + sendTime +
                ", readTime=" + readTime +
                '}';
    }
}
