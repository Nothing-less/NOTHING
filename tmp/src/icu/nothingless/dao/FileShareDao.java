package icu.nothingless.dao;

import icu.nothingless.entity.FileShare;
import java.util.List;

/**
 * 文件分享 DAO
 */
public interface FileShareDao {

    /**
     * 插入分享记录
     */
    void insert(FileShare share);

    /**
     * 根据 ID 查询
     */
    FileShare findById(Long id);

    /**
     * 查询某人收到的文件（含文件信息 + 发送者昵称）
     */
    List<FileShare> findReceivedByUserId(Long userId);

    /**
     * 查询某人发出的文件（含文件信息 + 接收者昵称）
     */
    List<FileShare> findSentByUserId(Long userId);

    /**
     * 更新撤回状态
     */
    void updateRevokeStatus(Long shareId, int revoked);
}
