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
            "JOIN user_file uf ON fs.file_id = uf.id " +
            "JOIN user su ON fs.sender_id = su.user_id " +
            "JOIN user ru ON fs.receiver_id = ru.user_id " +
            "WHERE fs.id = ?";
        try {
            List<Map<String, Object>> results = PDBUtil.executeQuery(sql, id);
            if (!results.isEmpty()) {
                Map<String, Object> row = results.get(0);
                FileShareBean s = new FileShareBean();
                s.setId((Long) row.get("id"));
                s.setSenderId((Long) row.get("sender_id"));
                s.setReceiverId((Long) row.get("receiver_id"));
                s.setFileId((Long) row.get("file_id"));
                Timestamp ts = (Timestamp) row.get("send_time");
                if (ts != null) s.setSendTime(ts.toLocalDateTime());
                s.setIsRevoked((Integer) row.get("is_revoked"));
                s.setFileName((String) row.get("file_name"));
                s.setFileSize((Long) row.get("file_size"));
                s.setSenderName((String) row.get("sender_name"));
                s.setReceiverName((String) row.get("receiver_name"));
                return s;
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
            "JOIN user_file uf ON fs.file_id = uf.id " +
            "JOIN user su ON fs.sender_id = su.user_id " +
            "WHERE fs.receiver_id = ? AND fs.is_revoked = 0 " +
            "ORDER BY fs.send_time DESC";
        return queryList(sql, userId);
    }

    @Override
    public List<FileShareBean> findSentByUserId(Long userId) {
        String sql =
            "SELECT fs.*, uf.file_name, uf.file_size, " +
            "       ru.nickname AS receiver_name " +
            "FROM file_share fs " +
            "JOIN user_file uf ON fs.file_id = uf.id " +
            "JOIN user ru ON fs.receiver_id = ru.user_id " +
            "WHERE fs.sender_id = ? " +
            "ORDER BY fs.send_time DESC";
        return queryList(sql, userId);
    }

    @Override
    public void updateRevokeStatus(Long shareId, int revoked) {
        String sql = "UPDATE file_share SET is_revoked = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, revoked);
            ps.setLong(2, shareId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("更新撤回状态失败", e);
        }
    }

    private List<FileShareBean> queryList(String sql, Long userId) {
        List<FileShareBean> list = new ArrayList<>();
        try  {
            PDBUtil.executeQuery(sql, userId, rs -> {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException("查询文件分享列表失败", e);
        }
        return list;
    }

    private FileShareBean mapRow(ResultSet rs) throws SQLException {
        FileShareBean s = new FileShareBean();
        s.setId(rs.getLong("id"));
        s.setSenderId(rs.getLong("sender_id"));
        s.setReceiverId(rs.getLong("receiver_id"));
        s.setFileId(rs.getLong("file_id"));
        Timestamp ts = rs.getTimestamp("send_time");
        if (ts != null) s.setSendTime(ts.toLocalDateTime());
        s.setIsRevoked(rs.getInt("is_revoked"));

        // 扩展字段
        s.setFileName(rs.getString("file_name"));
        s.setFileSize(rs.getLong("file_size"));
        try { s.setSenderName(rs.getString("sender_name")); } catch (SQLException ignored) {}
        try { s.setReceiverName(rs.getString("receiver_name")); } catch (SQLException ignored) {}
        return s;
    }
}
