package icu.nothingless.pojo.engine;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import icu.nothingless.exceptions.EngineException;
import icu.nothingless.pojo.adapter.iFSAdapter;
import icu.nothingless.pojo.bean.Friendship;
import icu.nothingless.pojo.bean.UserSTO;
import icu.nothingless.tools.PDBUtil;

public class FSEngine extends BaseEngine<iFSAdapter, FSEngine> {

    private static final String FS_ID = "FS_ID";
    private static final String USER_ID = "USER_ID";
    private static final String FRIEND_ID = "FRIEND_ID";
    private static final String FS_STATUS = "FS_STATUS";
    private static final String REMARK = "REMARK";
    private static final String GROUP_NAME = "GROUP_NAME";
    private static final String APPLY_MSG = "APPLY_MSG";
    private static final String CREATE_TIME = "CREATE_TIME";
    private static final String AGREE_TIME = "AGREE_TIME";
    /* ---------------------------------------------------------------------- */
    private static final String TABLENAME = "T_FRIENDSHIP";

    // save means insert, no update, only insert
    @Override
    public Long save(iFSAdapter bean) throws Exception {
        return null;
    }

    @Override
    public Long delete(iFSAdapter bean) throws Exception {
        return null;
    }

    @Override
    public java.util.List<iFSAdapter> query(iFSAdapter bean) throws Exception {
        return null;
    }

    // 申请添加好友
    public boolean applyFriend(Long userId, Long friendId, String applyMsg) throws Exception {
        // 检查是否已存在关系
        List<Friendship> existing = getFriendship(userId, friendId);
        if (existing != null && !existing.isEmpty()) {
            return false; // 已存在关系
        }

        // 检查对方是否已申请自己
        List<Friendship> reverse = getFriendship(friendId, userId);
        if (reverse != null && !reverse.isEmpty()) {
            Friendship req = reverse.get(0);
            if (req.getFsStatus() == 0) {
                // 对方已申请，直接同意
                try {
                    return agreeFriend(userId, friendId, "", "我的好友");
                } catch (EngineException e) {
                    return false; // 同意失败
                }
            }
            return false; // 已经是好友或已拒绝/删除
        }

        // String sql = "INSERT INTO t_friendship (user_id, friend_id, fs_status,
        // apply_msg, create_time) VALUES (?, ?, 0, ?, NOW())";
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(TABLENAME).append(" (")
                .append(USER_ID).append(", ")
                .append(FRIEND_ID).append(", ")
                .append(FS_STATUS).append(", ")
                .append(APPLY_MSG).append(", ")
                .append(CREATE_TIME).append(") VALUES (?, ?, 0, ?, NOW())");
        try {
            Long id = PDBUtil.executeInsert(sql.toString(), userId, friendId, applyMsg);
            return id != null && id > 0;
        } catch (SQLException e) {
            if (e.getMessage().toLowerCase().contains("duplicate") ||
                    e.getSQLState().equals("23000") || // SQLState for integrity constraint violation
                    e.getSQLState().equals("23505")) { // PostgreSQL unique violation
                return false;
            }
            throw new EngineException("Error occurred while executing function <applyFriend> : ", e);
        }
    }

    // 获取单向好友关系
    public List<Friendship> getFriendship(Long userId, Long friendId) throws Exception {
        // String sql = "SELECT * FROM t_friendship WHERE user_id = ? AND friend_id = ?
        // ";
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ")
                .append(FS_ID).append(", ")
                .append(USER_ID).append(", ")
                .append(FRIEND_ID).append(", ")
                .append(FS_STATUS).append(", ")
                .append(REMARK).append(", ")
                .append(GROUP_NAME).append(", ")
                .append(APPLY_MSG).append(", ")
                .append(CREATE_TIME).append(", ")
                .append(AGREE_TIME)
                .append(" FROM ").append(TABLENAME)
                .append(" WHERE ").append(USER_ID).append(" = ? AND ")
                .append(FRIEND_ID).append(" = ? ");

        List<Friendship> result = new ArrayList<>();
        try {
            List<Map<String, Object>> queryResult = PDBUtil.executeQuery(
                    sql.toString(), userId, friendId);

            for (Map<String, Object> map : queryResult) {
                Friendship f = new Friendship();
                f.setFsId(String.valueOf(map.get(FS_ID)));
                f.setUserId(String.valueOf(map.get(USER_ID)));
                f.setFriendId(String.valueOf(map.get(FRIEND_ID)));

                Object statusObj = map.get(FS_STATUS);
                f.setFsStatus(statusObj instanceof Number ? ((Number) statusObj).intValue() : null);

                f.setRemark((String) map.get(REMARK));
                f.setGroupName((String) map.get(GROUP_NAME));
                f.setApplyMsg((String) map.get(APPLY_MSG));

                // 安全处理时间
                Object createTime = map.get(CREATE_TIME);
                f.setCreateTime(createTime != null ? createTime.toString() : null);

                Object agreeTime = map.get(AGREE_TIME);
                f.setAgreeTime(agreeTime != null ? agreeTime.toString() : null);

                result.add(f);
            }
            return result.isEmpty() ? null : result;
        } catch (SQLException e) {
            throw new EngineException("Error occurred while executing function <getFriendship> : ", e);
        }
    }

    // 获取双向好友关系(检查是否已经是好友)
    public boolean isFriend(Long userId, Long friendId) throws Exception {
        /*
         * String sql =
         * "SELECT * FROM t_friendship WHERE user_id = ? AND friend_id = ? AND status = 1 "
         * + "UNION "
         * +
         * "SELECT * FROM t_friendship WHERE user_id = ? AND friend_id = ? AND status = 1"
         * ;
         */

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM ").append(TABLENAME)
                .append(" WHERE ((")
                .append(USER_ID).append(" = ? AND ")
                .append(FRIEND_ID).append(" = ? AND ")
                .append(FS_STATUS).append(" = 1 ) OR (")
                .append(USER_ID).append(" = ? AND ")
                .append(FRIEND_ID).append(" = ?)) AND ")
                .append(FS_STATUS).append(" = 1");

        try {
            Long count = PDBUtil.queryForObject(sql.toString(), Long.class,
                    userId, friendId, friendId, userId);
            return count != null && count == 2;
        } catch (SQLException e) {
            throw new EngineException("Error occurred while executing function <isFriend> : ", e);
        }
    }

    // 获取好友列表(包含好友信息)
    public List<Friendship> getFriendList(Long userId, String groupName, String keyword) throws Exception {
        List<Friendship> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();
        sql.append("SELECT f.fs_id, f.user_id, f.friend_id, f.fs_status, f.remark, ")
                .append("f.group_name, f.apply_msg, f.create_time, f.agree_time, ")
                .append("u.user_id as friend_user_id, u.useraccount, u.nickname, u.user_status ")
                .append("FROM t_friendship f ")
                .append("JOIN users u ON f.friend_id = u.user_id ")
                .append("WHERE f.user_id = ? AND f.fs_status = 1 ");
        params.add(userId);

        if (groupName != null && !groupName.isEmpty()) {
            sql.append("AND f.group_name = ? ");
            params.add(groupName);
        }
        if (keyword != null && !keyword.isEmpty()) {
            sql.append("AND (u.nickname LIKE ? OR f.remark LIKE ?) ");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }
        sql.append("ORDER BY f.group_name, u.nickname");

        try {

            List<Map<String, Object>> queryResult = PDBUtil.executeQuery(
                    sql.toString(),
                    params.toArray(new Object[0]));
            if (queryResult != null && !queryResult.isEmpty()) {

                for (Map<String, Object> map : queryResult) {
                    Friendship f = new Friendship();
                    UserSTO applicant = new UserSTO();

                    f.setFsId(String.valueOf(map.get("FS_ID")));
                    f.setUserId(String.valueOf(map.get("USER_ID")));
                    f.setFriendId(String.valueOf(map.get("FRIEND_ID")));

                    // TINYINT 转 Integer
                    Object statusObj = map.get("FS_STATUS");
                    f.setFsStatus(statusObj instanceof Number ? ((Number) statusObj).intValue() : null);

                    f.setRemark((String) map.get("REMARK"));
                    f.setGroupName((String) map.get("GROUP_NAME"));
                    f.setApplyMsg((String) map.get("APPLY_MSG"));

                    // 安全处理时间字段
                    Object createTime = map.get("CREATE_TIME");
                    f.setCreateTime(createTime != null ? createTime.toString() : null);

                    Object agreeTime = map.get("AGREE_TIME");
                    f.setAgreeTime(agreeTime != null ? agreeTime.toString() : null);

                    applicant.setUserId(String.valueOf(map.get("FRIEND_USER_ID")));
                    applicant.setUserAccount((String) map.get("USERACCOUNT"));
                    applicant.setNickname((String) map.get("NICKNAME"));

                    f.setFriendInfo(applicant);
                    list.add(f);
                }
            }
        } catch (SQLException e) {
            throw new EngineException("Error occurred while executing function <getFriendList> : ", e);
        }
        return list;
    }

    // 获取待处理的好友申请列表
    public List<Friendship> getPendingRequests(Long userId) throws Exception {
        List<Friendship> list = new ArrayList<>();

        /*
         * String sql = "SELECT f.*, u.user_id, u.account, u.nickname "
         * + "FROM t_friendship f "
         * + "JOIN users u ON f.user_id = u.user_id "
         * + "WHERE f.friend_id = ? AND f.status = 0 "
         * + "ORDER BY f.create_time DESC";
         */

        // 🔴 u.account -> u.useraccount，f.status -> f.fs_status
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT f.").append(FS_ID.toLowerCase()).append(", ")
                .append("f.").append(USER_ID.toLowerCase()).append(", ")
                .append("f.").append(FRIEND_ID.toLowerCase()).append(", ")
                .append("f.").append(FS_STATUS.toLowerCase()).append(", ")
                .append("f.").append(REMARK.toLowerCase()).append(", ")
                .append("f.").append(GROUP_NAME.toLowerCase()).append(", ")
                .append("f.").append(APPLY_MSG.toLowerCase()).append(", ")
                .append("f.").append(CREATE_TIME.toLowerCase()).append(", ")
                .append("f.").append(AGREE_TIME.toLowerCase()).append(", ")
                .append("u.user_id as applicant_id, u.useraccount, u.nickname ")
                .append("FROM ").append(TABLENAME).append(" f ")
                .append("JOIN users u ON f.").append(USER_ID.toLowerCase()).append(" = u.user_id ")
                .append("WHERE f.").append(FRIEND_ID.toLowerCase()).append(" = ? AND f.")
                .append(FS_STATUS.toLowerCase()).append(" = 0 ")
                .append("ORDER BY f.").append(CREATE_TIME.toLowerCase()).append(" DESC");

        try {
            List<Map<String, Object>> rs = PDBUtil.executeQuery(sql.toString(), userId);

            for (Map<String, Object> map : rs) {
                Friendship f = new Friendship();
                UserSTO applicant = new UserSTO();

                f.setFsId(String.valueOf(map.get(FS_ID)));
                f.setUserId(String.valueOf(map.get(USER_ID)));
                f.setFriendId(String.valueOf(map.get(FRIEND_ID)));

                Object statusObj = map.get(FS_STATUS);
                f.setFsStatus(statusObj instanceof Number ? ((Number) statusObj).intValue() : null);

                f.setRemark((String) map.get(REMARK));
                f.setGroupName((String) map.get(GROUP_NAME));
                f.setApplyMsg((String) map.get(APPLY_MSG));

                Object createTime = map.get(CREATE_TIME);
                f.setCreateTime(createTime != null ? createTime.toString() : null);

                Object agreeTime = map.get(AGREE_TIME);
                f.setAgreeTime(agreeTime != null ? agreeTime.toString() : null);

                // 🔴使用别名 applicant_id 避免混淆
                applicant.setUserId(String.valueOf(map.get("APPLICANT_ID")));
                applicant.setUserAccount((String) map.get("USERACCOUNT"));
                applicant.setNickname((String) map.get("NICKNAME"));

                f.setFriendInfo(applicant);
                list.add(f);
            }
        } catch (SQLException e) {
            throw new EngineException("Error occurred while executing function <getPendingRequests> : ", e);
        }
        return list;
    }

    // 同意好友申请(双向建立关系)
    public boolean agreeFriend(Long userId, Long friendId, String remark, String groupName) throws Exception {
        try {
            PDBUtil.executeTransaction(conn -> {
                // 1. 更新申请记录状态
                String updateSql = "UPDATE " + TABLENAME + " SET " + FS_STATUS + " = 1, "
                        + AGREE_TIME + " = NOW() WHERE " + USER_ID + " = ? AND "
                        + FRIEND_ID + " = ? AND " + FS_STATUS + " = 0";

                try (PreparedStatement ps1 = conn.prepareStatement(updateSql)) {
                    ps1.setLong(1, friendId);
                    ps1.setLong(2, userId);
                    int updated = ps1.executeUpdate();

                    if (updated == 0) {
                        throw new SQLException("No pending friend request found");
                    }
                }

                // 🔴 PostgreSQL 兼容性：ON CONFLICT 代替 ON DUPLICATE KEY UPDATE
                String insertSql;
                boolean isPostgreSQL = conn.getMetaData().getDatabaseProductName()
                        .toLowerCase().contains("postgresql");

                if (isPostgreSQL) {
                    // PostgreSQL 语法
                    insertSql = "INSERT INTO " + TABLENAME + " ("
                            + USER_ID + ", " + FRIEND_ID + ", " + FS_STATUS + ", "
                            + REMARK + ", " + GROUP_NAME + ", " + AGREE_TIME + ") "
                            + "VALUES (?, ?, 1, ?, ?, NOW()) "
                            + "ON CONFLICT (" + USER_ID + ", " + FRIEND_ID + ") "
                            + "DO UPDATE SET " + FS_STATUS + " = 1, "
                            + REMARK + " = EXCLUDED." + REMARK + ", "
                            + GROUP_NAME + " = EXCLUDED." + GROUP_NAME + ", "
                            + AGREE_TIME + " = NOW()";
                } else {
                    // MySQL 语法
                    insertSql = "INSERT INTO " + TABLENAME + " ("
                            + USER_ID + ", " + FRIEND_ID + ", " + FS_STATUS + ", "
                            + REMARK + ", " + GROUP_NAME + ", " + AGREE_TIME + ") "
                            + "VALUES (?, ?, 1, ?, ?, NOW()) "
                            + "ON DUPLICATE KEY UPDATE " + FS_STATUS + " = 1, "
                            + REMARK + " = VALUES(" + REMARK + "), "
                            + GROUP_NAME + " = VALUES(" + GROUP_NAME + "), "
                            + AGREE_TIME + " = NOW()";
                }

                try (PreparedStatement ps2 = conn.prepareStatement(insertSql)) {
                    ps2.setLong(1, userId);
                    ps2.setLong(2, friendId);
                    ps2.setString(3, remark);
                    ps2.setString(4, groupName);
                    ps2.executeUpdate();
                }
            });
            return true;
        } catch (SQLException e) {
            throw new EngineException("Error occurred while executing function <agreeFriend> : ", e);
        }
    }

    // 拒绝好友申请
    public boolean rejectFriend(Long userId, Long friendId) throws Exception {
        /*
         * String sql = "UPDATE t_friendship SET fs_status = 2 " +
         * "WHERE user_id = ? AND friend_id = ? AND fs_status = 0";
         */
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(TABLENAME).append(" SET ")
                .append(FS_STATUS).append(" = 2 ")
                .append("WHERE ")
                .append(USER_ID).append(" = ? AND ")
                .append(FRIEND_ID).append(" = ? AND ")
                .append(FS_STATUS).append(" = 0");

        try {
            int updated = PDBUtil.executeUpdate(sql.toString(), friendId, userId);
            return updated > 0;
        } catch (SQLException e) {
            throw new EngineException("Error occurred while executing function <rejectFriend> : ", e);
        }
    }

    // 删除好友(双向删除)
    public boolean deleteFriend(Long userId, Long friendId) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(TABLENAME).append(" SET ")
                .append(FS_STATUS).append(" = 3 ")
                .append("WHERE ((")
                .append(USER_ID).append(" = ? AND ")
                .append(FRIEND_ID).append(" = ?) OR (")
                .append(USER_ID).append(" = ? AND ")
                .append(FRIEND_ID).append(" = ?)) AND ")
                .append(FS_STATUS).append(" = 1");

        try {
            int updated = PDBUtil.executeUpdate(sql.toString(),
                    userId, friendId, friendId, userId);
            if (updated == 0) {
                throw new EngineException("Failed to delete: No relationship found");
            }
            if (updated != 2) {
                throw new EngineException("Failed to delete: Inconsistent data - updated " + updated + " records");
            }
            return true;
        } catch (SQLException e) {
            throw new EngineException("Error occurred while executing function <deleteFriend> : ", e);
        }
    }

    // 修改备注和分组
    public boolean updateFriendInfo(Long userId, Long friendId, String remark, String groupName) throws Exception {
        if (remark == null && groupName == null) {
            throw new IllegalArgumentException("REMARK and GROUPNAME cannot be empty at the same time");
        }

        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(TABLENAME).append(" SET ");

        List<Object> params = new ArrayList<>();
        if (remark != null) {
            sql.append(REMARK).append(" = ? ");
            params.add(remark);
        }
        if (groupName != null) {
            if (!params.isEmpty())
                sql.append(", ");
            sql.append(GROUP_NAME).append(" = ? ");
            params.add(groupName);
        }

        sql.append(" WHERE ")
                .append(USER_ID).append(" = ? AND ")
                .append(FRIEND_ID).append(" = ? AND ")
                .append(FS_STATUS).append(" = 1");
        params.add(userId);
        params.add(friendId);

        try {
            int updated = PDBUtil.executeUpdate(sql.toString(), params.toArray(new Object[0]));
            if (updated == 0) {
                throw new EngineException(
                        String.format(
                                "Failed to modify relationship information: No relationship found. userId={%d}, friendId={%d}",
                                userId, friendId));
            }
            return true;
        } catch (SQLException e) {
            throw new EngineException("Error occurred while executing function <updateFriendInfo> : ", e);
        }
    }

    // 获取分组列表
    public List<String> getGroups(Long userId) throws Exception {
        List<String> groups = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT ").append(GROUP_NAME)
                .append(" FROM ").append(TABLENAME)
                .append(" WHERE ")
                .append(USER_ID).append(" = ? AND ")
                .append(FS_STATUS).append(" = 1 ")
                .append("ORDER BY ").append(GROUP_NAME);

        try {
            List<Map<String, Object>> queryResult = PDBUtil.executeQuery(sql.toString(), userId);
            for (Map<String, Object> map : queryResult) {
                String groupName = (String) map.get(GROUP_NAME); // 使用常量
                if (groupName != null && !groupName.isEmpty()) {
                    groups.add(groupName);
                }
            }
            return groups;
        } catch (SQLException e) {
            throw new EngineException("Error occurred while executing function <getGroups> : ", e);
        }
    }
}
