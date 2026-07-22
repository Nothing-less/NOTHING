package icu.nothingless.dao.interfaces;

import java.util.List;

import icu.nothingless.pojo.bean.UserFileBean;

/**
 * 用户文件 DAO
 */
public interface FileUserDao {

    /**
     * 插入文件记录
     */
    void insert(UserFileBean file);

    /**
     * 根据 ID 查询
     */
    UserFileBean findById(Long id);

    /**
     * 根据用户 ID 查询所有文件（按上传时间倒序）
     */
    List<UserFileBean> findByUserId(Long userId);

    /**
     * 搜索：用户自己的文件中，按文件名模糊匹配
     */
    List<UserFileBean> searchByUserAndName(Long userId, String keyword);

    /**
     * 删除文件记录
     */
    void deleteById(Long id);
}
