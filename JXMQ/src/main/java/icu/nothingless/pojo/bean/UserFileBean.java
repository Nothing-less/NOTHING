package icu.nothingless.pojo.bean;

import java.time.LocalDateTime;

/**
 * 用户文件仓库实体
 */
public class UserFileBean {

    private Long id;
    private Long userId;
    private String fileName;       // 原始文件名
    private String storedName;     // 磁盘上的 UUID 文件名
    private String filePath;       // 磁盘绝对路径
    private Long fileSize;         // 字节数
    private String mimeType;       // MIME 类型
    private LocalDateTime uploadTime;

    // ========== Getters & Setters ==========

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getStoredName() { return storedName; }
    public void setStoredName(String storedName) { this.storedName = storedName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public LocalDateTime getUploadTime() { return uploadTime; }
    public void setUploadTime(LocalDateTime uploadTime) { this.uploadTime = uploadTime; }
}
