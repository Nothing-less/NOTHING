package icu.nothingless.dao.interfaces;

import java.util.List;

import icu.nothingless.commons.R;
import icu.nothingless.pojo.bean.FileUserBean;

/**
 * 用户文件 DAO
 */
public interface FileUserDao {

    /**
     * 插入文件记录
     */
    R<Long> insert(FileUserBean file) throws RuntimeException;

    /**
     * 根据 ID 查询
     */
    R<FileUserBean> findById(Long id) throws RuntimeException;

    /**
     * 根据用户 ID 查询所有文件（按上传时间倒序）
     */
    R<List<FileUserBean>> findByUserId(Long userId) throws RuntimeException;

    /**
     * 搜索：用户自己的文件中，按文件名模糊匹配
     */
    R<List<FileUserBean>> searchByUserAndName(Long userId, String keyword) throws RuntimeException;

    /**
     * 删除文件记录
     */
    R deleteById(Long id) throws RuntimeException;
}
