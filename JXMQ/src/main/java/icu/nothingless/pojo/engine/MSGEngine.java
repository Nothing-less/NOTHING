package icu.nothingless.pojo.engine;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import icu.nothingless.exceptions.EngineException;
import icu.nothingless.pojo.adapter.IMSGAdapter;
import icu.nothingless.pojo.bean.MessageBean;
import icu.nothingless.tools.PDBUtil;

public class MSGEngine extends BaseEngine<IMSGAdapter, MSGEngine> {

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

    // insert
    @Override
    public Long save(IMSGAdapter bean) throws Exception {
        if (bean == null) {
            throw new EngineException("Function <save> null entering");
        }
        if (bean.getSendTime() == null) {
            bean.setSendTime(LocalDateTime.now());
        }

        Map<String, Object> beanMap = toMap(bean);
        if (beanMap == null || beanMap.isEmpty()) {
            throw new EngineException("Function <save> empty bean map");
        }

        beanMap.remove(MSG_ID);
        beanMap.remove(MSG_STATUS);

        StringBuilder sql = new StringBuilder();
        List<Object> paramsList = new ArrayList<>();

        sql.append("INSERT INTO ").append(TABLE_NAME).append(" (");

        for (String key : beanMap.keySet()) {
            Object value = beanMap.get(key);
            // 只处理非空值
            if (value != null) {
                sql.append(key).append(", ");
                paramsList.add(value);
            }
        }
        // 移除最后一个逗号和空格
        if (sql.toString().endsWith(", ")) {
            sql.setLength(sql.length() - 2);
        }
        sql.append(") VALUES (");

        for (int i = 0; i < paramsList.size(); i++) {
            sql.append("?, ");
        }
        // 移除最后一个逗号和空格
        if (sql.toString().endsWith(", ")) {
            sql.setLength(sql.length() - 2);
        }
        sql.append(")");

        if (paramsList.isEmpty()) {
            throw new EngineException("No non-null fields to insert");
        }

        try {
            // INSERT INTO T_MESSAGE (SENDER_ID, RECEIVER_ID, MSG_TYPE, CONTENTS, SEND_TIME)
            // VALUES (?, ?, ?, ?, NOW())
            return Long.valueOf("" + PDBUtil.executeUpdate(sql.toString(), paramsList.toArray()));
        } catch (SQLException e) {
            throw new EngineException("Error occurred while executing function <save> : ", e);
        }
    }

    // delete by setting MSG_STATUS to STATUS_RECALLED
    @Override
    public Long delete(IMSGAdapter bean) throws Exception {
        if (bean == null) {
            throw new EngineException("Function <delete> entering null");
        }

        // 检查MSG_ID是否存在
        if (bean.getMsgId() == null) {
            throw new EngineException("Function <delete> MSG_ID required");
        }

        StringBuilder sql = new StringBuilder();
        List<Object> paramsList = new ArrayList<>();

        sql.append("UPDATE ").append(TABLE_NAME)
                .append(" SET ").append(MSG_STATUS)
                .append(" = ? WHERE ").append(MSG_ID).append(" = ?")
                .append(" AND ").append(MSG_STATUS).append(" != ").append(IMSGAdapter.STATUS_RECALLED)
                .append(" AND ").append(SENDER_ID).append(" = ?")
                .append(" AND ").append(RECEIVER_ID).append(" = ?");
        // .append(" AND ").append(SEND_TIME).append(" >= NOW() - INTERVAL '2 minutes'")

        paramsList.add(IMSGAdapter.STATUS_RECALLED);
        paramsList.add(bean.getMsgId());
        paramsList.add(bean.getSenderId());
        paramsList.add(bean.getReceiverId());

        try {
            // UPDATE T_MESSAGE
            // SET MSG_STATUS = 2
            // WHERE MSG_ID = ? AND SENDER_ID = ? AND RECEIVER_ID = ? AND MSG_STATUS != 2
            // **AND SEND_TIME >= NOW() - INTERVAL '2 minutes'
            return Long.valueOf(PDBUtil.executeUpdate(sql.toString(), paramsList.toArray()));
        } catch (SQLException e) {
            throw new EngineException("Error occurred while executing function <delete> : ", e);
        }
    }

    // fuzzy query
    @Override
    public List<IMSGAdapter> query(IMSGAdapter bean) throws Exception {
        if (bean == null) {
            throw new EngineException("Function <query> null entering");
        }
        Map<String, Object> beanMap = toMap(bean);
        if (beanMap == null || beanMap.isEmpty()) {
            throw new EngineException("Function <query> empty bean map");
        }
        beanMap.remove(MSG_ID);
        beanMap.remove(MSG_STATUS);
        beanMap.remove(SEND_TIME);
        beanMap.remove(READ_TIME);

        StringBuilder sql = new StringBuilder();
        List<Object> paramsList = new ArrayList<>();

        sql.append("SELECT * FROM ").append(TABLE_NAME).append(" WHERE 1=1");

        for (String key : beanMap.keySet()) {
            Object value = beanMap.get(key);
            if (value != null) {
                // 模糊查询使用LIKE
                sql.append(" AND ").append(key).append(" LIKE ?");
                paramsList.add("%" + value.toString() + "%");
            }
        }

        // 默认按发送时间倒序排列
        sql.append(" ORDER BY ").append(SEND_TIME).append(" DESC");

        try {
            List<Map<String, Object>> resultMaps = PDBUtil.executeQuery(sql.toString(), paramsList.toArray());
            List<IMSGAdapter> resultList = new ArrayList<>();
            if (!(resultMaps == null || resultMaps.isEmpty())) {
                for (Map<String, Object> map : resultMaps)
                    resultList.add(toBean(map));
            }
            return resultList;
        } catch (SQLException e) {
            throw new EngineException("Error occurred while executing function <query> : ", e);
        }
    }

    // update
    @Override
    public Long update(IMSGAdapter bean) throws Exception {
        if (bean == null) {
            throw new EngineException("Function <update> null entering");
        }

        Map<String, Object> beanMap = toMap(bean);
        if (beanMap == null || beanMap.isEmpty()) {
            throw new EngineException("Function <update> empty bean map");
        }

        // 检查主键MSG_ID是否存在
        Object primaryKey = beanMap.get(MSG_ID);
        if (primaryKey == null) {
            throw new EngineException("Function <update> MSG_ID null entering");
        }
        // 移除主键，不参与SET
        beanMap.remove(MSG_ID);
        beanMap.remove(SEND_TIME);
        beanMap.remove(READ_TIME);

        StringBuilder sql = new StringBuilder();
        List<Object> paramsList = new ArrayList<>();

        sql.append("UPDATE ").append(TABLE_NAME).append(" SET ");
        if (beanMap.isEmpty()) {
            throw new EngineException("Function <update> no non-null fields to update");
        }

        for (String key : beanMap.keySet()) {
            Object value = beanMap.get(key);
            // 只处理非空值
            if (value != null) {
                sql.append(key).append(" = ?, ");
                paramsList.add(value);
            }
        }
        // 移除最后一个逗号和空格
        if (sql.toString().endsWith(", ")) {
            sql.setLength(sql.length() - 2);
        }

        // 添加WHERE条件
        sql.append(" WHERE ").append(MSG_ID).append(" = ?");
        paramsList.add(primaryKey);

        try {
            // UPDATE T_MESSAGE SET SENDER_ID = ?, CONTENTS = ? WHERE MSG_ID = ?
            return Long.valueOf("" + PDBUtil.executeUpdate(sql.toString(), paramsList.toArray()));
        } catch (SQLException e) {
            throw new EngineException("Error occurred while executing function <update> : ", e);
        }
    }

    // Bean to Map
    @Override
    public Map<String, Object> toMap(IMSGAdapter bean) throws Exception {
        if (bean == null) {
            return null;
        }
        java.util.HashMap<String, Object> map = new java.util.LinkedHashMap<>();

        if (bean.getMsgId() != null) {
            map.put(MSG_ID, bean.getMsgId());
        }
        if (bean.getSenderId() != null) {
            map.put(SENDER_ID, bean.getSenderId());
        }
        if (bean.getReceiverId() != null) {
            map.put(RECEIVER_ID, bean.getReceiverId());
        }
        if (bean.getMsgType() != null) {
            map.put(MSG_TYPE, bean.getMsgType());
        }
        if (bean.getContents() != null) {
            map.put(CONTENTS, bean.getContents());
        }
        if (bean.getMsgStatus() != null) {
            map.put(MSG_STATUS, bean.getMsgStatus());
        }
        if (bean.getSendTime() != null) {
            map.put(SEND_TIME, Timestamp.valueOf(bean.getSendTime()));
        }
        if (bean.getReadTime() != null) {
            map.put(READ_TIME, bean.getReadTime());
        }

        return map;
    }

    // Map to Bean
    @Override
    public IMSGAdapter toBean(Map<String, Object> map) throws Exception {
        if (map == null || map.isEmpty()) {
            return null;
        }

        MessageBean bean = new icu.nothingless.pojo.bean.MessageBean();

        // 从map中获取值并设置到bean
        if (map.containsKey(MSG_ID)) {
            Object val = map.get(MSG_ID);
            if (val != null) {
                bean.setMsgId(val instanceof Number ? ((Number) val).longValue() : Long.valueOf(val.toString()));
            }
        }
        if (map.containsKey(SENDER_ID)) {
            Object val = map.get(SENDER_ID);
            if (val != null) {
                bean.setSenderId(val instanceof Number ? ((Number) val).longValue() : Long.valueOf(val.toString()));
            }
        }
        if (map.containsKey(RECEIVER_ID)) {
            Object val = map.get(RECEIVER_ID);
            if (val != null) {
                bean.setReceiverId(val instanceof Number ? ((Number) val).longValue() : Long.valueOf(val.toString()));
            }
        }
        if (map.containsKey(MSG_TYPE)) {
            Object val = map.get(MSG_TYPE);
            if (val != null) {
                bean.setMsgType(val instanceof Number ? ((Number) val).intValue() : Integer.valueOf(val.toString()));
            }
        }
        if (map.containsKey(CONTENTS)) {
            bean.setContents(String.valueOf(map.get(CONTENTS)));
        }
        if (map.containsKey(MSG_STATUS)) {
            Object val = map.get(MSG_STATUS);
            if (val != null) {
                bean.setMsgStatus(val instanceof Number ? ((Number) val).intValue() : Integer.valueOf(val.toString()));
            }
        }
        if (map.containsKey(SEND_TIME)) {
            Object val = map.get(SEND_TIME);
            if (val != null) {
                if (val instanceof java.sql.Timestamp) {
                    bean.setSendTime(((Timestamp) val).toLocalDateTime());
                } else if (val instanceof java.util.Date) {
                    bean.setSendTime(new Timestamp(((java.util.Date) val).getTime()).toLocalDateTime());
                } else {
                    // 如果是字符串或其他，尝试解析
                    bean.setSendTime(java.sql.Timestamp.valueOf(val.toString()).toLocalDateTime());
                }
            }
        }
        if (map.containsKey(READ_TIME)) {
            Object val = map.get(READ_TIME);
            if (val != null) {
                if (val instanceof java.sql.Timestamp) {
                    bean.setReadTime(((Timestamp) val).toLocalDateTime());
                } else if (val instanceof java.util.Date) {
                    bean.setReadTime(((Timestamp) val).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
                } else {
                    // 如果是字符串或其他，尝试解析
                    bean.setReadTime(java.sql.Timestamp.valueOf(val.toString()).toLocalDateTime());
                }
            }
        }

        return bean;
    }

    public static String getMsgId() {
        return MSG_ID;
    }

    public static String getSenderId() {
        return SENDER_ID;
    }

    public static String getReceiverId() {
        return RECEIVER_ID;
    }

    public static String getMsgType() {
        return MSG_TYPE;
    }

    public static String getContents() {
        return CONTENTS;
    }

    public static String getMsgStatus() {
        return MSG_STATUS;
    }

    public static String getSendTime() {
        return SEND_TIME;
    }

    public static String getReadTime() {
        return READ_TIME;
    }

    public static String getTableName() {
        return TABLE_NAME;
    }

}