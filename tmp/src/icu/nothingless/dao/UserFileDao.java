package icu.nothingless.dao;

import icu.nothingless.entity.UserFile;
import java.util.List;

/**
 * 用户文件 DAO
 */
public interface UserFileDao {

    /**
     * 插入文件记录
     */
    void insert(UserFile file);

    /**
     * 根据 ID 查询
     */
    UserFile findById(Long id);

    /**
     * 根据用户 ID 查询所有文件（按上传时间倒序）
     */
    List<UserFile> findByUserId(Long userId);

    /**
     * 搜索：用户自己的文件中，按文件名模糊匹配
     */
    List<UserFile> searchByUserAndName(Long userId, String keyword);

    /**
     * 删除文件记录
     */
    void deleteById(Long id);
}
