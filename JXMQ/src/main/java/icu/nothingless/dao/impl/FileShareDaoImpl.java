package icu.nothingless.dao.impl;

import icu.nothingless.dao.interfaces.FileShareDao;
import icu.nothingless.pojo.bean.FileShareBean;
import icu.nothingless.tools.PDBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FileShareDaoImpl implements FileShareDao {

    @Override
    public void insert(FileShareBean sharedFile) {
        String sql = "INSERT INTO file_share(sender_id, receiver_id, file_id, send_time, is_revoked) " +
                     "VALUES(?, ?, ?, NOW(), 0)";
        try  {
            var generatedKeys = PDBUtil.executeInsert(sql, sharedFile.getSenderId(), sharedFile.getReceiverId(), sharedFile.getFileId());
            if (generatedKeys != null) {
                sharedFile.setId(generatedKeys);
            }
        } catch (SQLException e) {
            throw new RuntimeException("插入文件分享记录失败", e);
        }
    }

    @Override
    public FileShareBean findById(Long id) {
        String sql =
            "SELECT fs.*, uf.file_name, uf.file_size, " +
            "       su.nickname AS sender_name, ru.nickname AS receiver_name " +
            "FROM file_share fs " +
            "JOIN file_user uf ON fs.file_id = uf.id " +
            "JOIN users su ON fs.sender_id = su.user_id::bigint " +
            "JOIN users ru ON fs.receiver_id = ru.user_id::bigint " +
            "WHERE fs.file_status AND uf.file_status AND fs.id = ?";
        try {
            List<Map<String, Object>> results = PDBUtil.executeQuery(sql, id);
            if (!results.isEmpty()) {
                return map_to_bean(results.get(0));
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询文件分享失败", e);
        }
        return null;
    }

    @Override
    public List<FileShareBean> findReceivedByUserId(Long userId) {
        String sql =
            "SELECT fs.*, uf.file_name, uf.file_size, " +
            "       su.nickname AS sender_name " +
            "FROM file_share fs " +
            "JOIN file_user uf ON fs.file_id = uf.id " +
            "JOIN users su ON fs.sender_id = su.user_id::bigint " +
            "WHERE fs.file_status AND uf.file_status AND fs.receiver_id = ? AND fs.is_revoked = 0 " +
            "ORDER BY fs.send_time DESC";
        return queryList(sql, userId);
    }

    @Override
    public List<FileShareBean> findSentByUserId(Long userId) {
        String sql =
            "SELECT fs.*, uf.file_name, uf.file_size, " +
            "       ru.nickname AS receiver_name " +
            "FROM file_share fs " +
            "JOIN file_user uf ON fs.file_id = uf.id " +
            "JOIN users ru ON fs.receiver_id = ru.user_id::bigint " +
            "WHERE fs.file_status AND uf.file_status AND fs.sender_id = ? " +
            "ORDER BY fs.send_time DESC";
        return queryList(sql, userId);
    }

    @Override
    public void updateRevokeStatus(Long shareId, int revoked) {
        String sql = "UPDATE file_share SET is_revoked = ? WHERE file_status AND id = ?";
        try {
            PDBUtil.executeUpdate(sql, revoked, shareId);
        } catch (SQLException e) {
            throw new RuntimeException("更新撤回状态失败", e);
        }
    }

    private List<FileShareBean> queryList(String sql, Long userId) {
        List<FileShareBean> list = new ArrayList<>();
        try {
            List<Map<String, Object>> results = PDBUtil.executeQuery(sql, userId);
            for (Map<String, Object> row : results) {
                list.add(map_to_bean(row));
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询文件分享列表失败", e);
        }
        return list;
    }

    private FileShareBean map_to_bean(Map<String, Object> row) {
        FileShareBean s = new FileShareBean();
        s.setId(toLong(row.get("ID")));
        s.setSenderId(toLong(row.get("SENDER_ID")));
        s.setReceiverId(toLong(row.get("RECEIVER_ID")));
        s.setFileId(toLong(row.get("FILE_ID")));
        Timestamp ts = (Timestamp) row.get("SEND_TIME");
        if (ts != null) s.setSendTime(ts.toLocalDateTime());
        s.setIsRevoked(toInteger(row.get("IS_REVOKED")));

        // 扩展字段
        s.setFileName((String) row.get("FILE_NAME"));
        s.setFileSize(toLong(row.get("FILE_SIZE")));
        s.setSenderName((String) row.get("SENDER_NAME"));
        s.setReceiverName((String) row.get("RECEIVER_NAME"));
        return s;
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

    private Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.valueOf((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
