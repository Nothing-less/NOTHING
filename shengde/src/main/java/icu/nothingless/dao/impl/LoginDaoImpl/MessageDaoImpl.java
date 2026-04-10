package icu.nothingless.dao.impl.LoginDaoImpl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import icu.nothingless.dao.interfaces.IMessageDao;
import icu.nothingless.exceptions.EngineException;
import icu.nothingless.pojo.adapter.IMSGAdapter;
import icu.nothingless.pojo.bean.MessageBean;
import icu.nothingless.pojo.engine.MSGEngine;
import icu.nothingless.tools.PDBUtil;

public class MessageDaoImpl implements IMessageDao<IMSGAdapter> {
    
    private static final String MSG_ID = MSGEngine.getMsgId();
    private static final String SENDER_ID = MSGEngine.getSenderId();
    private static final String RECEIVER_ID = MSGEngine.getReceiverId();
    private static final String MSG_TYPE = MSGEngine.getMsgType();
    private static final String CONTENTS = MSGEngine.getContents();
    private static final String MSG_STATUS = MSGEngine.getMsgStatus();
    private static final String SEND_TIME = MSGEngine.getSendTime();
    private static final String READ_TIME = MSGEngine.getReadTime();
    /* ---------------------------------------------------------------------- */
    private static final String TABLE_NAME = MSGEngine.getTableName();

    // 保存消息
    public Long saveMessage(MessageBean msg) throws Exception {
        // "INSERT INTO t_message (sender_id, receiver_id, msg_type, content, send_time) VALUES (?, ?, ?, ?, NOW())";
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(TABLE_NAME).append(" (")
                .append(SENDER_ID).append(", ")
                .append(RECEIVER_ID).append(", ")
                .append(MSG_TYPE).append(", ")
                .append(CONTENTS).append(", ")
                .append(SEND_TIME)
                .append(") VALUES (?, ?, ?, ?, NOW())");

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
    public Boolean markAsRead(Long userId, Long friendId) throws Exception {
        // UPDATE t_message SET status = 1, read_time = NOW() 
        // WHERE receiver_id = ? AND sender_id = ? AND status = 0";
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
    public List<IMSGAdapter> getChatHistory(Long userId, Long friendId, Long lastMsgId, int limit) throws Exception {
        /*
         * String sql = "SELECT m.*, u.nickname as sender_nickname " +
         * "FROM t_message m " +
         * "JOIN users u ON m.sender_id = u.user_id " +
         * "WHERE m.receiver_id = ? AND m.status = 0 " +
         * "ORDER BY m.send_time";
         */
        List<IMSGAdapter> list = new ArrayList<>();
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
                MessageBean m = mapMessage(row);
                m.setSenderNickname((String) row.get("SENDER_NICKNAME"));
                list.add(m);
            }
        } catch (SQLException e) {
            throw new EngineException("Error occurred while executing function <getChatHistory> : ", e);
        }
        return list;
    }

    // 获取未读消息列表
    public List<IMSGAdapter> getUnreadMessages(Long userId) throws Exception {
        List<IMSGAdapter> list = new ArrayList<>();
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
                MessageBean m = mapMessage(row);
                m.setSenderNickname((String) row.get("SENDER_NICKNAME"));
                list.add(m);
            }
        } catch (SQLException e) {
            throw new EngineException("Error occurred while executing function <getUnreadMessages> : ", e);
        }
        return list;
    }

    // 撤回消息
    public Boolean recallMessage(Long msgId, Long userId) throws Exception {
        StringBuilder sql = new StringBuilder();
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

    private MessageBean mapMessage(Map<String, Object> row) throws Exception {
        MessageBean m = new MessageBean();
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

    @Override
    public Long saveMessage(IMSGAdapter msg) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'saveMessage'");
    }
}