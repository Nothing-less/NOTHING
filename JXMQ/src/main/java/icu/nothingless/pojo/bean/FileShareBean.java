package icu.nothingless.pojo.bean;

import java.time.LocalDateTime;

/**
 * 文件分享（发送给好友）实体
 */
public class FileShareBean {

    private Long id;
    private Long senderId; // 发送者
    private Long receiverId; // 接收者
    private Long fileId; // 关联 file_user.id
    private LocalDateTime sendTime;
    private Integer isRevoked; // 0=正常 1=已撤回

    // 扩展字段（非数据库列），用于前端展示
    private String fileName;
    private Long fileSize;
    private String senderName;
    private String receiverName;

    // ========== Getters & Setters ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public LocalDateTime getSendTime() {
        return sendTime;
    }

    public void setSendTime(LocalDateTime sendTime) {
        this.sendTime = sendTime;
    }

    public Integer getIsRevoked() {
        return isRevoked;
    }

    public void setIsRevoked(Integer isRevoked) {
        this.isRevoked = isRevoked;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }
}
