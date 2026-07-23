package icu.nothingless.dao.impl;

import icu.nothingless.commons.R;
import icu.nothingless.dao.interfaces.FileUserDao;
import icu.nothingless.pojo.bean.FileUserBean;
import icu.nothingless.tools.PDBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FileUserDaoImpl implements FileUserDao {

    @Override
    public R insert(FileUserBean file) throws RuntimeException {
        String sql = "INSERT INTO file_user(user_id, file_name, stored_name, file_path, file_size, mime_type) "
                + "VALUES(?, ?, ?, ?, ?, ?)";
        try {
            Long generatedKeys = PDBUtil.executeInsert(sql, file.getUserId(), file.getFileName(), file.getStoredName(),
                    file.getFilePath(), file.getFileSize(), file.getMimeType());
            if (generatedKeys != null) {
                file.setId(generatedKeys);
                return R.success(generatedKeys);
            }else{
                return R.error("插入文件记录失败");
            }
        } catch (SQLException e) {
            throw new RuntimeException("插入文件记录失败", e);
        }
    }

    @Override
    public R<FileUserBean> findById(Long id) throws RuntimeException {
        String sql = "SELECT * FROM file_user WHERE file_status AND id = ? ";
        try {
            List<Map<String, Object>> results = PDBUtil.executeQuery(sql, id);
            if (!results.isEmpty()) {
                return R.success(map_to_bean(results.get(0)));
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询文件失败", e);
        }
        return R.error("文件未找到");
    }

    @Override
    public R<List<FileUserBean>> findByUserId(Long userId) throws RuntimeException {
        String sql = "SELECT * FROM file_user WHERE file_status AND user_id = ? ORDER BY upload_time DESC";
        List<FileUserBean> list = new ArrayList<>();
        try {
            List<Map<String, Object>> results = PDBUtil.executeQuery(sql, userId);
            for (Map<String, Object> row : results) {
                list.add(map_to_bean(row));
            }
            return R.success(list);
        } catch (SQLException e) {
            throw new RuntimeException("查询用户文件列表失败", e);
        }
    }

    @Override
    public R<List<FileUserBean>> searchByUserAndName(Long userId, String keyword) throws RuntimeException {
        String sql = "SELECT * FROM file_user WHERE file_status AND user_id = ? AND file_name LIKE ? "
                + "ORDER BY upload_time DESC";
        try {
            List<Map<String, Object>> results = PDBUtil.executeQuery(sql, userId, "%" + keyword + "%");
            List<FileUserBean> list = new ArrayList<>();
            for (Map<String, Object> row : results) {
                list.add(map_to_bean(row));
            }
            return R.success(list);
        } catch (SQLException e) {
            throw new RuntimeException("搜索文件失败", e);
        }
    }

    @Override
    public R deleteById(Long id) throws RuntimeException {
        String sql = "UPDATE file_user SET file_status = FALSE WHERE id = ?";
        try {
            var result = PDBUtil.executeUpdate(sql, id);
            if(result > 0) {
                return R.success(result);
            } else {
                return R.error("文件未找到或已删除");
            }
        } catch (SQLException e) {
            throw new RuntimeException("删除文件记录失败", e);
        }
    }

    private FileUserBean map_to_bean(Map<String, Object> row) {
        FileUserBean file = new FileUserBean();
        file.setId(toLong(row.get("ID")));
        file.setUserId(toLong(row.get("USER_ID")));
        file.setFileName((String) row.get("FILE_NAME"));
        file.setStoredName((String) row.get("STORED_NAME"));
        file.setFilePath((String) row.get("FILE_PATH"));
        file.setFileSize(toLong(row.get("FILE_SIZE")));
        file.setMimeType((String) row.get("MIME_TYPE"));
        Timestamp ts = (Timestamp) row.get("UPLOAD_TIME");
        if (ts != null) {
            file.setUploadTime(ts.toLocalDateTime());
        }
        return file;
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.valueOf((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
