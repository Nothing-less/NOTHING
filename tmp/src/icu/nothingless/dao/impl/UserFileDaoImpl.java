package icu.nothingless.dao.impl;

import icu.nothingless.dao.UserFileDao;
import icu.nothingless.entity.UserFile;
import icu.nothingless.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserFileDaoImpl implements UserFileDao {

    @Override
    public void insert(UserFile file) {
        String sql = "INSERT INTO user_file(user_id, file_name, stored_name, file_path, file_size, mime_type, upload_time) " +
                     "VALUES(?, ?, ?, ?, ?, ?, NOW())";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, file.getUserId());
            ps.setString(2, file.getFileName());
            ps.setString(3, file.getStoredName());
            ps.setString(4, file.getFilePath());
            ps.setLong(5, file.getFileSize());
            ps.setString(6, file.getMimeType());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                file.setId(rs.getLong(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("插入文件记录失败", e);
        }
    }

    @Override
    public UserFile findById(Long id) {
        String sql = "SELECT * FROM user_file WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询文件失败", e);
        }
        return null;
    }

    @Override
    public List<UserFile> findByUserId(Long userId) {
        String sql = "SELECT * FROM user_file WHERE user_id = ? ORDER BY upload_time DESC";
        List<UserFile> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询用户文件列表失败", e);
        }
        return list;
    }

    @Override
    public List<UserFile> searchByUserAndName(Long userId, String keyword) {
        String sql = "SELECT * FROM user_file WHERE user_id = ? AND file_name LIKE ? ORDER BY upload_time DESC";
        List<UserFile> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("搜索文件失败", e);
        }
        return list;
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM user_file WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("删除文件记录失败", e);
        }
    }

    private UserFile mapRow(ResultSet rs) throws SQLException {
        UserFile f = new UserFile();
        f.setId(rs.getLong("id"));
        f.setUserId(rs.getLong("user_id"));
        f.setFileName(rs.getString("file_name"));
        f.setStoredName(rs.getString("stored_name"));
        f.setFilePath(rs.getString("file_path"));
        f.setFileSize(rs.getLong("file_size"));
        f.setMimeType(rs.getString("mime_type"));
        Timestamp ts = rs.getTimestamp("upload_time");
        if (ts != null) f.setUploadTime(ts.toLocalDateTime());
        return f;
    }
}
