package icu.nothingless.pojo.engine;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import icu.nothingless.exceptions.EngineException;
import icu.nothingless.pojo.adapter.iMSGAdapter;
import icu.nothingless.pojo.bean.Message;
import icu.nothingless.tools.PDBUtil;

public class MSGEngine extends BaseEngine<iMSGAdapter, MSGEngine> {

    private static final String MSG_ID = "MSG_ID";
    private static final String SENDER_ID = "SENDER_ID";
    private static final String RECEIVER_ID = "RECEIVER_ID";
    private static final String MSG_TYPE = "MSG_TYPE";
    private static final String CONTENTS = "CONTENTS";
    private static final String MSG_STATUS = "MSG_STATUS";
    private static final String SEND_TIME = "SEND_TIME";
    private static final String READ_TIME = "READ_TIME";
    /* ---------------------------------------------------------------------- */
    private static final String TABLE_NAME = "T_MESSAGE";

    @Override
    public Long save(iMSGAdapter bean) throws Exception {
        return null;
    }

    @Override
    public Long delete(iMSGAdapter bean) throws Exception {
        return null;
    }

    @Override
    public java.util.List<iMSGAdapter> query(iMSGAdapter bean) throws Exception {
        return null;
    }

    // 保存消息
    public Long saveMessage(Message msg) throws Exception {
        // String sql = "INSERT INTO t_message (sender_id, receiver_id, msg_type,
        // content, status, send_time) VALUES (?, ?, ?, ?, ?, NOW())";
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(TABLE_NAME).append(" (")
                .append(SENDER_ID).append(", ")
                .append(RECEIVER_ID).append(", ")
                .append(MSG_TYPE).append(", ")
                .append(CONTENTS).append(", ")
                .append(MSG_STATUS).append(", ")
                .append(SEND_TIME)
                .append(") VALUES (?, ?, ?, ?, ?, NOW())");

        try {
            return PDBUtil.executeInsert(sql.toString(),
                    msg.getSenderId(),
                    msg.getReceiverId(),
                    msg.getMsgType(),
                    msg.getContent(),
                    msg.getStatus());
        } catch (SQLException e) {
            throw new EngineException("Error occurred while executing function <saveMessage> : ", e);
        }
    }

    // 标记消息为已读
    public boolean markAsRead(Long userId, Long friendId) throws Exception {
        // String sql = "UPDATE t_message SET status = 1, read_time = NOW() WHERE
        // receiver_id = ? AND sender_id = ? AND status = 0";
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(TABLE_NAME).append(" SET ")
                .append(MSG_STATUS).append(" = 1, ")
                .append(READ_TIME).append(" = NOW() ")
                .append("WHERE ")
                .append(RECEIVER_ID).append(" = ? AND ")
                .append(SENDER_ID).append(" = ? AND ")
                .append(MSG_STATUS).append(" = 0");
        try {
            return PDBUtil.executeUpdate(sql.toString(), userId, friendId) > 0;
        } catch (SQLException e) {
            throw new EngineException("Error occurred while executing function <markAsRead> : ", e);
        }
    }

    // 获取聊天记录(分页)
    public List<Message> getChatHistory(Long userId, Long friendId, Long lastMsgId, int limit) throws Exception {
        /*
         * String sql = "SELECT m.*, u.nickname as sender_nickname " +
         * "FROM t_message m " +
         * "JOIN users u ON m.sender_id = u.user_id " +
         * "WHERE m.receiver_id = ? AND m.status = 0 " +
         * "ORDER BY m.send_time";
         */
        List<Message> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        sql.append("SELECT m.*, u.nickname as sender_nickname ")
                .append("FROM ").append(TABLE_NAME).append(" m ")
                .append("JOIN users u ON m.").append(SENDER_ID).append(" = u.user_id ")
                .append("WHERE ((m.").append(SENDER_ID).append(" = ? AND m.")
                .append(RECEIVER_ID).append(" = ?) OR (m.")
                .append(SENDER_ID).append(" = ? AND m.")
                .append(RECEIVER_ID).append(" = ?)) ")
                .append("AND m.").append(MSG_STATUS).append(" != 2 "); // 排除已撤回的

        params.add(userId);
        params.add(friendId);
        params.add(friendId);
        params.add(userId);

        if (lastMsgId != null && lastMsgId > 0) {
            sql.append("AND m.").append(MSG_ID).append(" < ? ");
            params.add(lastMsgId);
        }

        // ⚠️ PostgreSQL 用 LIMIT，Oracle 用 FETCH FIRST，SQL Server 用 TOP
        sql.append("ORDER BY m.").append(MSG_ID).append(" DESC LIMIT ?");
        params.add(limit);

        try {
            List<Map<String, Object>> queryResult = PDBUtil.executeQuery(
                    sql.toString(), params.toArray(new Object[0]));

            for (Map<String, Object> row : queryResult) {
                Message m = mapMessage(row);
                m.setSenderNickname((String) row.get("SENDER_NICKNAME"));
                list.add(m);
            }
        } catch (SQLException e) {
            throw new EngineException("Error occurred while executing function <getChatHistory> : ", e);
        }
        return list;
    }

    // 获取未读消息列表
    public List<Message> getUnreadMessages(Long userId) throws Exception {
        List<Message> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT m.*, u.nickname as sender_nickname ")
                .append("FROM ").append(TABLE_NAME).append(" m ")
                .append("JOIN users u ON m.").append(SENDER_ID).append(" = u.user_id ")
                .append("WHERE m.").append(RECEIVER_ID).append(" = ? AND m.")
                .append(MSG_STATUS).append(" = 0 ")
                .append("ORDER BY m.").append(SEND_TIME);

        try {
            List<Map<String, Object>> queryResult = PDBUtil.executeQuery(sql.toString(), userId);
            for (Map<String, Object> row : queryResult) {
                Message m = mapMessage(row);
                m.setSenderNickname((String) row.get("SENDER_NICKNAME"));
                list.add(m);
            }
        } catch (SQLException e) {
            throw new EngineException("Error occurred while executing function <getUnreadMessages> : ", e);
        }
        return list;
    }

    // 撤回消息
    public boolean recallMessage(Long msgId, Long userId) throws Exception {
        StringBuilder sql = new StringBuilder();

        // 🔴 方案 1：通用方案（推荐）- 在 Java 中计算时间
        // 先查询消息时间，再判断是否可撤回
        /*
         * String checkSql = "SELECT " + SEND_TIME + " FROM " + TABLE_NAME +
         * " WHERE " + MSG_ID + " = ? AND " + SENDER_ID + " = ?";
         * // 然后在 Java 中判断时间差
         */

        // 🔴 方案 2：数据库判断
        sql.append("UPDATE ").append(TABLE_NAME).append(" SET ")
                .append(MSG_STATUS).append(" = 2 ")
                .append("WHERE ")
                .append(MSG_ID).append(" = ? AND ")
                .append(SENDER_ID).append(" = ? AND ");

        sql.append(SEND_TIME).append(" >= NOW() - INTERVAL '2 minutes'");

        try {
            return PDBUtil.executeUpdate(sql.toString(), msgId, userId) > 0;
        } catch (SQLException e) {
            throw new EngineException("Error occurred while executing function <recallMessage> : ", e);
        }
    }

    private Message mapMessage(Map<String, Object> row) throws Exception {
        Message m = new Message();
        Object msgId = row.get(MSG_ID);
        if (msgId instanceof Number) {
            m.setMsgId(((Number) msgId).longValue());
        }
        m.setSenderId((Long) row.get(SENDER_ID));
        m.setReceiverId((Long) row.get(RECEIVER_ID));
        m.setMsgType((Integer) row.get(MSG_TYPE));
        m.setContent((String) row.get(CONTENTS));
        m.setStatus((Integer) row.get(MSG_STATUS));
        m.setSendTime((java.sql.Timestamp) row.get(SEND_TIME));
        m.setReadTime((java.sql.Timestamp) row.get(READ_TIME));
        return m;
    }
}
