package icu.nothingless.dao.impl;

import icu.nothingless.dao.FileShareDao;
import icu.nothingless.entity.FileShare;
import icu.nothingless.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FileShareDaoImpl implements FileShareDao {

    @Override
    public void insert(FileShare share) {
        String sql = "INSERT INTO file_share(sender_id, receiver_id, file_id, send_time, is_revoked) " +
                     "VALUES(?, ?, ?, NOW(), 0)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, share.getSenderId());
            ps.setLong(2, share.getReceiverId());
            ps.setLong(3, share.getFileId());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                share.setId(rs.getLong(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("插入文件分享记录失败", e);
        }
    }

    @Override
    public FileShare findById(Long id) {
        String sql =
            "SELECT fs.*, uf.file_name, uf.file_size, " +
            "       su.nickname AS sender_name, ru.nickname AS receiver_name " +
            "FROM file_share fs " +
            "JOIN user_file uf ON fs.file_id = uf.id " +
            "JOIN user su ON fs.sender_id = su.user_id " +
            "JOIN user ru ON fs.receiver_id = ru.user_id " +
            "WHERE fs.id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询文件分享失败", e);
        }
        return null;
    }

    @Override
    public List<FileShare> findReceivedByUserId(Long userId) {
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
    public List<FileShare> findSentByUserId(Long userId) {
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

    private List<FileShare> queryList(String sql, Long userId) {
        List<FileShare> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询文件分享列表失败", e);
        }
        return list;
    }

    private FileShare mapRow(ResultSet rs) throws SQLException {
        FileShare s = new FileShare();
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
