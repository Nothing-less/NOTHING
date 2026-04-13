package icu.nothingless.pojo.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import icu.nothingless.pojo.bean.MessageBean;

/**
 * Message DTO 类
 */
public record Message(
        Long msgId,
        Long senderId,
        Long receiverId,
        Integer msgType,
        String contents,
        Integer msgStatus,
        String sendTime,
        String readTime,
        String senderNickname,
        String senderAvatar,
        String receiverNickname,
        Boolean isSelf) implements java.io.Serializable {

    // ==================== 常量 ====================

    // 消息类型
    public static final int TYPE_TEXT = 1;
    public static final int TYPE_IMAGE = 2;
    public static final int TYPE_FILE = 3;
    public static final int TYPE_VOICE = 4;

    // 消息状态
    public static final int STATUS_UNREAD = 0;
    public static final int STATUS_READ = 1;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ==================== 紧凑构造器（校验 & 默认值）====================

    public Message {
        if (msgType == null)
            msgType = TYPE_TEXT;
        if (msgStatus == null)
            msgStatus = STATUS_UNREAD;
        if (isSelf == null)
            isSelf = false;
    }

    // ==================== 便捷方法 ====================

    /**
     * 是否为已读
     */
    public boolean isRead() {
        return STATUS_READ == this.msgStatus;
    }

    /**
     * 是否为文本消息
     */
    public boolean isText() {
        return TYPE_TEXT == this.msgType;
    }

    /**
     * 获取内容摘要（用于列表展示）
     */
    public String getSummary(int maxLength) {
        if (contents == null)
            return "";
        if (contents.length() <= maxLength)
            return contents;
        return contents.substring(0, maxLength) + "...";
    }

    // ==================== Builder 模式 ====================

    public static Builder builder() {
        return new Builder();
    }

    /**
     * 从当前对象创建 Builder（用于修改副本）
     */
    public Builder toBuilder() {
        return new Builder()
                .msgId(this.msgId)
                .senderId(this.senderId)
                .receiverId(this.receiverId)
                .msgType(this.msgType)
                .contents(this.contents)
                .msgStatus(this.msgStatus)
                .sendTime(this.sendTime)
                .readTime(this.readTime)
                .senderNickname(this.senderNickname)
                .senderAvatar(this.senderAvatar)
                .receiverNickname(this.receiverNickname)
                .isSelf(this.isSelf);
    }

    public static class Builder {
        private Long msgId;
        private Long senderId;
        private Long receiverId;
        private Integer msgType;
        private String contents;
        private Integer msgStatus;
        private String sendTime;
        private String readTime;
        private String senderNickname;
        private String senderAvatar;
        private String receiverNickname;
        private Boolean isSelf;

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

        public Builder sendTime(String sendTime) {
            this.sendTime = sendTime;
            return this;
        }

        public Builder readTime(String readTime) {
            this.readTime = readTime;
            return this;
        }

        public Builder senderNickname(String senderNickname) {
            this.senderNickname = senderNickname;
            return this;
        }

        public Builder senderAvatar(String senderAvatar) {
            this.senderAvatar = senderAvatar;
            return this;
        }

        public Builder receiverNickname(String receiverNickname) {
            this.receiverNickname = receiverNickname;
            return this;
        }

        public Builder isSelf(Boolean isSelf) {
            this.isSelf = isSelf;
            return this;
        }

        /**
         * 从 Entity 构建（自动转换时间格式）
         */
        public Builder from(MessageBean entity) {
            this.msgId = entity.getMsgId();
            this.senderId = entity.getSenderId();
            this.receiverId = entity.getReceiverId();
            this.msgType = entity.getMsgType();
            this.contents = entity.getContents();
            this.msgStatus = entity.getMsgStatus();
            this.sendTime = formatTime(entity.getSendTime());
            this.readTime = formatTime(entity.getReadTime());
            return this;
        }

        /**
         * 设置当前用户ID，自动计算 isSelf
         */
        public Builder withCurrentUser(Long currentUserId) {
            this.isSelf = currentUserId != null && currentUserId.equals(this.senderId);
            return this;
        }

        /**
         * 标记为已读状态
         */
        public Builder markAsRead() {
            this.msgStatus = STATUS_READ;
            this.readTime = LocalDateTime.now().format(FORMATTER);
            return this;
        }

        public Message build() {
            if (senderId == null)
                throw new IllegalStateException("senderId is required");
            if (receiverId == null)
                throw new IllegalStateException("receiverId is required");
            return new Message(msgId, senderId, receiverId, msgType, contents,
                    msgStatus, sendTime, readTime, senderNickname, senderAvatar,
                    receiverNickname, isSelf);
        }

        private String formatTime(LocalDateTime time) {
            return time != null ? time.format(FORMATTER) : null;
        }
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 从 Entity 快速创建（基础字段）
     */
    public static Message fromEntity(MessageBean entity) {
        return Message.builder().from(entity).build();
    }

    /**
     * 从 Entity 创建（带当前用户视角）
     */
    public static Message fromEntity(MessageBean entity, Long currentUserId) {
        return Message.builder()
                .from(entity)
                .withCurrentUser(currentUserId)
                .build();
    }

    /**
     * 创建文本消息（快捷方法）
     */
    public static Message text(Long senderId, Long receiverId, String contents) {
        return Message.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .contents(contents)
                .msgType(TYPE_TEXT)
                .build();
    }

    // ==================== Object 方法 ====================

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Message that = (Message) o;
        return Objects.equals(msgId, that.msgId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(msgId);
    }

    @Override
    public String toString() {
        return "Message{" +
                "msgId=" + msgId +
                ", senderId=" + senderId +
                ", receiverId=" + receiverId +
                ", msgType=" + msgType +
                ", contents='" + getSummary(20) + '\'' +
                ", msgStatus=" + msgStatus +
                ", isSelf=" + isSelf +
                '}';
    }
}