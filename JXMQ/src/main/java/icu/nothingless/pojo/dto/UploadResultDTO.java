package icu.nothingless.pojo.dto;

public class UploadResultDTO {
    private Long fileId;
    private String fileName;

    public UploadResultDTO(Long fileId, String fileName) {
        this.fileId = fileId;
        this.fileName = fileName;
    }

    public Long getFileId() { return fileId; }
    public String getFileName() { return fileName; }
}
