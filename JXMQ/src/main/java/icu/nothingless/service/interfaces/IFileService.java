package icu.nothingless.service.interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import icu.nothingless.commons.RespEntity;
import icu.nothingless.pojo.bean.FileShareBean;
import icu.nothingless.pojo.bean.FileUserBean;
import icu.nothingless.pojo.dto.SendResultDTO;
import icu.nothingless.pojo.dto.UploadResultDTO;

public interface IFileService {

    RespEntity<UploadResultDTO> upload(Long userId, String originalName, String contentType,
                           InputStream inputStream) throws IOException;

    RespEntity<List<FileUserBean>> listFiles(Long userId);

    RespEntity<List<FileUserBean>> searchFiles(Long userId, String keyword);

    RespEntity<FileUserBean> getDownloadableFile(Long userId, Long fileId);

    RespEntity deleteFile(Long userId, Long fileId);

    RespEntity<SendResultDTO> sendFile(Long senderId, Long receiverId, Long fileId);

    RespEntity revokeFile(Long userId, Long shareId);

    RespEntity<List<FileShareBean>> listReceived(Long userId);

    RespEntity<List<FileShareBean>> listSent(Long userId);
}
