package icu.nothingless.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icu.nothingless.commons.R;
import icu.nothingless.commons.RespEntity;
import icu.nothingless.dao.interfaces.FileShareDao;
import icu.nothingless.dao.interfaces.FileUserDao;
import icu.nothingless.pojo.bean.FileShareBean;
import icu.nothingless.pojo.bean.FileUserBean;
import icu.nothingless.pojo.dto.SendResultDTO;
import icu.nothingless.pojo.dto.UploadResultDTO;
import icu.nothingless.service.interfaces.IFileService;
import icu.nothingless.tools.Fmt;
import icu.nothingless.tools.ServiceFactory;

public class FileServiceImpl implements IFileService {

    private final FileUserDao userFileDao = ServiceFactory.getSingleton(FileUserDao.class);
    private final FileShareDao fileShareDao = ServiceFactory.getSingleton(FileShareDao.class);
    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    private static final String UPLOAD_ROOT = "C:\\Shengde\\Repo\\NOTHING\\JXMQ\\upload\\repo\\";

    @Override
    public RespEntity upload(Long userId, String originalName,
            String contentType, InputStream inputStream) {

        String storedName = UUID.randomUUID() + getExtension(originalName);
        File userDir = new File(UPLOAD_ROOT + userId + "/");
        boolean created = userDir.mkdirs();
        if (!created && !userDir.exists()) {
            logger.error("Unable to create directory: {}", userDir.getAbsolutePath());
        }

        File target = new File(userDir, storedName);
        try  {
            FileOutputStream out = new FileOutputStream(target);
            inputStream.transferTo(out);
        }catch (IOException e) {
            logger.error("Failed to save uploaded file: {}", target.getAbsolutePath(), e);
            return RespEntity.error("Failed to save uploaded file");
        }

        FileUserBean uf = new FileUserBean();
        uf.setUserId(userId);
        uf.setFileName(originalName);
        uf.setStoredName(storedName);
        uf.setFilePath(target.getAbsolutePath());
        uf.setFileSize(target.length());
        uf.setMimeType(contentType);

        R ret = userFileDao.insert(uf);
        if (!ret.isSuccess()) {
            return RespEntity.error(ret.message());
        }
        return RespEntity.success(new UploadResultDTO(uf.getId(), uf.getFileName()));
    }

    @Override
    public RespEntity<List<FileUserBean>> listFiles(Long userId) {
        if (userId == null) {
            throw new RuntimeException("Unknown userId");
        }
        R<List<FileUserBean>> ret = userFileDao.findByUserId(userId);
        if (!ret.isSuccess()) {
            return RespEntity.error(ret.message());
        }
        return RespEntity.success((List<FileUserBean>)ret.data());
    }

    @Override
    public RespEntity<List<FileUserBean>> searchFiles(Long userId, String keyword) {
        if(userId == null) {
            throw new RuntimeException("Unknown userId");
        }
        if(Fmt.isStrictEmpty(keyword)) {
            throw new RuntimeException("Keyword cannot be empty");
        }
        R<List<FileUserBean>> ret = userFileDao.searchByUserAndName(userId, keyword);
        if (!ret.isSuccess()) {
            return RespEntity.error(ret.message());
        }
        return RespEntity.success((List<FileUserBean>)ret.data());
    }

    @Override
    public RespEntity<FileUserBean> getDownloadableFile(Long userId, Long fileId) {
        R<FileUserBean> uf = userFileDao.findById(fileId);
        if (!uf.isSuccess()) {
            throw new RuntimeException("无权限或文件不存在");
        }
        File f = new File(uf.data().getFilePath());
        if (!f.exists()) {
            throw new RuntimeException("文件不存在");
        }
        return RespEntity.success(uf.data());
    }

    @Override
    public RespEntity deleteFile(Long userId, Long fileId) {
        R<FileUserBean> uf = userFileDao.findById(fileId);
        if (!uf.isSuccess()) {
            throw new RuntimeException("文件不存在");
        }
        if (!uf.data().getUserId().equals(userId)) {
            throw new RuntimeException("无权限或文件不存在");
        }

        new File(uf.data().getFilePath()).delete();
        userFileDao.deleteById(fileId);
        return RespEntity.success();
    }

    @Override
    public RespEntity<SendResultDTO> sendFile(Long senderId, Long receiverId, Long fileId) {
        R<FileUserBean> uf = userFileDao.findById(fileId);
        if (!uf.isSuccess()) {
            throw new RuntimeException("文件不存在或无权限");
        }

        FileShareBean share = new FileShareBean();
        share.setSenderId(senderId);
        share.setReceiverId(receiverId);
        share.setFileId(fileId);
        share.setIsRevoked(0);

        fileShareDao.insert(share);
        return RespEntity.success(new SendResultDTO(share.getId()));
    }

    @Override
    public RespEntity revokeFile(Long userId, Long shareId) {
        FileShareBean share = fileShareDao.findById(shareId);
        if (share == null) {
            throw new RuntimeException("记录不存在");
        }
        if (!share.getSenderId().equals(userId)) {
            throw new RuntimeException("只能撤回自己发送的文件");
        }

        long hours = ChronoUnit.HOURS.between(share.getSendTime(), LocalDateTime.now());
        if (hours > 24) {
            throw new RuntimeException("超过 24 小时，无法撤回");
        }

        fileShareDao.updateRevokeStatus(shareId, 1);
        return RespEntity.success();
    }

    @Override
    public RespEntity<List<FileShareBean>> listReceived(Long userId) {
        return RespEntity.success(fileShareDao.findReceivedByUserId(userId));
    }

    @Override
    public RespEntity<List<FileShareBean>> listSent(Long userId) {
        return RespEntity.success(fileShareDao.findSentByUserId(userId));
    }

    private String getExtension(String name) {
        if (name == null)
            return "";
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(dot) : "";
    }
}
