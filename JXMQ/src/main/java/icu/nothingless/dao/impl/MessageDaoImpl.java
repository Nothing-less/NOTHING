package icu.nothingless.dao.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import icu.nothingless.commons.R;
import icu.nothingless.dao.interfaces.IMessageDao;
import icu.nothingless.exceptions.EngineException;
import icu.nothingless.pojo.adapter.IMSGAdapter;
import icu.nothingless.pojo.engine.BaseEngine;
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
    private static final String SELECT_COLUMNS = MSG_ID + ", " + SENDER_ID + ", " + RECEIVER_ID + ", " + MSG_TYPE + ", "
            + CONTENTS + ", " + MSG_STATUS + ", " + SEND_TIME + ", " + READ_TIME;

    private static final String SELECT_BASE = "SELECT " + SELECT_COLUMNS + " FROM " + TABLE_NAME;

    // 保存消息
    @Override
    public R<Long> saveMessage(IMSGAdapter msg) throws Exception {
        String sql = "INSERT INTO " + TABLE_NAME + " ("
                + SENDER_ID + ", "
                + RECEIVER_ID + ", "
                + MSG_TYPE + ", "
                + CONTENTS + ", "
                + SEND_TIME + ") VALUES (?, ?, ?, ?, NOW())";

        try {
            return R.success(PDBUtil.executeInsert(sql,
                    msg.getSenderId(),
                    msg.getReceiverId(),
                    msg.getMsgType(),
                    msg.getContents()));
        } catch (SQLException e) {
            throw new EngineException("Error occurred while executing function <saveMessage> : ", e);
        }
    }

    // 标记消息为已读
    @Override
    public R<Boolean> markAsRead(Long userId, Long friendId) throws Exception {
        String sql = "UPDATE " + TABLE_NAME + " SET "
                + MSG_STATUS + " = 1, "
                + READ_TIME + " = NOW() "
                + "WHERE "
                + RECEIVER_ID + " = ? AND "
                + SENDER_ID + " = ? AND "
                + MSG_STATUS + " = 0";

        try {
            return R.success(PDBUtil.executeUpdate(sql, userId, friendId) > 0);
        } catch (SQLException e) {
            throw new EngineException("Error occurred while executing function <markAsRead> : ", e);
        }
    }

    // 获取聊天记录(分页)
    @Override
    public R<List<IMSGAdapter>> getChatHistory(Long userId, Long friendId, Long lastMsgId, int limit) throws Exception {
        StringBuilder sql = new StringBuilder(SELECT_BASE);
        sql.append(" WHERE (")
                .append(SENDER_ID).append(" = ? AND ")
                .append(RECEIVER_ID).append(" = ?")
                .append(" OR ")
                .append(SENDER_ID).append(" = ? AND ")
                .append(RECEIVER_ID).append(" = ?")
                .append(")");

        List<Object> params = new ArrayList<>();
        params.add(userId);
        params.add(friendId);
        params.add(friendId);
        params.add(userId);

        if (lastMsgId != null) {
            sql.append(" AND ").append(MSG_ID).append(" < ?");
            params.add(lastMsgId);
        }

        int pageSize = limit > 0 ? limit : 20;
        sql.append(" ORDER BY ").append(MSG_ID).append(" DESC LIMIT ?");
        params.add(pageSize);

        try {
            return R.success(queryMessages(sql.toString(), params.toArray()));
        } catch (SQLException e) {
            throw new EngineException("Error occurred while executing function <getChatHistory> : ", e);
        }
    }

    // 获取未读消息列表
    @Override
    public R<List<IMSGAdapter>> getUnreadMessages(Long userId) throws Exception {
        String sql = SELECT_BASE
                + " WHERE " + RECEIVER_ID + " = ? AND " + MSG_STATUS + " = ?"
                + " ORDER BY " + SEND_TIME + " ASC, " + MSG_ID + " ASC";

        try {
            return R.success(queryMessages(sql, userId, IMSGAdapter.STATUS_UNREAD));
        } catch (SQLException e) {
            throw new EngineException("Error occurred while executing function <getUnreadMessages> : ", e);
        }
    }

    // 撤回消息
    @Override
    public R<Boolean> recallMessage(Long msgId, Long userId) throws Exception {
        String sql = "UPDATE " + TABLE_NAME + " SET "
                + MSG_STATUS + " = 2 "
                + "WHERE "
                + MSG_ID + " = ? AND "
                + SENDER_ID + " = ? AND "
                + SEND_TIME + " >= NOW() - INTERVAL '2 minutes'";

        try {
            return R.success(PDBUtil.executeUpdate(sql, msgId, userId) > 0);
        } catch (SQLException e) {
            throw new EngineException("Error occurred while executing function <recallMessage> : ", e);
        }
    }

    private List<IMSGAdapter> queryMessages(String sql, Object... params) throws SQLException {
        List<Map<String, Object>> rows = PDBUtil.executeQuery(sql, params);
        List<IMSGAdapter> list = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            try {
                list.add(toBean(row));
            } catch (Exception e) {
                throw new SQLException("Failed to map message row", e);
            }
        }
        return list;
    }

    private IMSGAdapter toBean(Map<String, Object> map) throws Exception {
        return BaseEngine.getInstance(MSGEngine.class).toBean(map);
    }

}