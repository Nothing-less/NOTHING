package icu.nothingless.pojo.engine;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import icu.nothingless.exceptions.EngineException;
import icu.nothingless.pojo.adapter.IFSAdapter;
import icu.nothingless.tools.PDBUtil;

public class FSEngine extends BaseEngine<IFSAdapter, FSEngine> {

    private static final String FS_ID = "FS_ID";
    private static final String USER_ID = "USER_ID";
    private static final String FRIEND_ID = "FRIEND_ID";
    private static final String FS_STATUS = "FS_STATUS";
    private static final String REMARK = "REMARK";
    private static final String GROUP_NAME = "GROUP_NAME";
    private static final String APPLY_MSG = "APPLY_MSG";
    private static final String CREATE_TIME = "CREATE_TIME";
    private static final String AGREE_TIME = "AGREE_TIME";
    /***************************************************************************/
    private static final String TABLENAME = "T_FRIENDSHIP";

    // Insert
    @Override
    public Long save(IFSAdapter bean) throws Exception {
        if (bean == null) {
            throw new EngineException("Function <save> null entering");
        }

        Map<String, Object> beanMap = toMap(bean);
        if (beanMap == null || beanMap.isEmpty()) {
            throw new EngineException("Function <save> empty bean map");
        }

        beanMap.remove(FS_ID);
        beanMap.remove(FS_STATUS);

        StringBuilder sql = new StringBuilder();
        List<Object> paramsList = new ArrayList<>();

        sql.append("INSERT INTO ").append(TABLENAME).append(" (");

        for (String key : beanMap.keySet()) {
            Object value = beanMap.get(key);
            // 只处理非空值
            if (value != null && !value.toString().trim().isEmpty()) {
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
            // INSERT INTO
            // T_FRIENDSHIP (USER_ID, FRIEND_ID, FS_STATUS, REMARK, GROUP_NAME, APPLY_MSG,
            // CREATE_TIME, AGREE_TIME)
            // VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            return Long.valueOf("" + PDBUtil.executeUpdate(sql.toString(), paramsList.toArray()));
        } catch (SQLException e) {
            throw new EngineException("Error occurred while executing function <save> : ", e);
        }
    }

    /**
     * FS_ID is required as primary key for update, other non-null fields will be
     * updated
     */
    @Override
    public Long update(IFSAdapter bean) throws Exception {
        if (bean == null) {
            throw new EngineException("Function <update> null entering");
        }

        Map<String, Object> beanMap = toMap(bean);
        if (beanMap == null || beanMap.isEmpty()) {
            throw new EngineException("Function <update> empty bean map");
        }

        // 检查主键FS_ID是否存在
        Object primaryKey = beanMap.get(FS_ID);
        if (primaryKey == null) {
            throw new EngineException("Function <update> FS_ID null entering");
        }
        // 移除主键，不参与SET
        beanMap.remove(FS_ID);

        StringBuilder sql = new StringBuilder();
        List<Object> paramsList = new ArrayList<>();

        sql.append("UPDATE ").append(TABLENAME).append(" SET ");
        if (beanMap.isEmpty()) {
            throw new EngineException("Function <update> no non-null fields to update");
        }

        for (String key : beanMap.keySet()) {
            Object value = beanMap.get(key);
            // 只处理非空值
            if (value != null && !value.toString().trim().isEmpty()) {
                sql.append(key).append(" = ?, ");
                paramsList.add(value);
            }
        }

        // 添加WHERE条件
        sql.append(" WHERE ").append(FS_ID).append(" = ?");
        paramsList.add(primaryKey);

        try {
            /*
             * update
             * T_FRIENDSHIP
             * set
             * USER_ID = ?,
             * FRIEND_ID = ?,
             * FS_STATUS = ?,
             * REMARK = ?,
             * GROUP_NAME = ?,
             * APPLY_MSG = ?,
             * CREATE_TIME = ?,
             * AGREE_TIME = ?
             * where
             * FS_ID = ?
             */

            return Long.valueOf("" + PDBUtil.executeUpdate(sql.toString(), paramsList.toArray()));
        } catch (SQLException e) {
            throw new EngineException("Error occurred while executing function <update> : ", e);
        }
    }

    // delete by setting FS_STATUS to STATUS_DELETED
    @Override
    public Long delete(IFSAdapter bean) throws Exception {
        if (bean == null) {
            throw new EngineException("Function <delete> entering null");
        }

        // 检查FS_ID是否存在
        if (bean.getFsId() == null
                || bean.getUserId() == null
                || bean.getFriendId() == null) {
            throw new EngineException("Function <delete> required keys empty");
        }

        StringBuilder sql = new StringBuilder();
        List<Object> paramsList = new ArrayList<>();

        sql.append("UPDATE ").append(TABLENAME)
                .append(" SET ").append(FS_STATUS)
                .append(" = ? WHERE ").append(FS_ID).append(" = ?")
                .append(" AND ").append(FS_STATUS).append(" != 0")
                .append(" AND ").append(USER_ID).append(" = ?")
                .append(" AND ").append(FRIEND_ID).append(" = ?");

        paramsList.add(IFSAdapter.STATUS_DELETED);
        paramsList.add(bean.getFsId());
        paramsList.add(bean.getUserId());
        paramsList.add(bean.getFriendId());

        try {
            // UPDATE T_FRIENDSHIP SET FS_STATUS = 0 WHERE FS_ID = ? AND FS_STATUS != 0 AND
            // USER_ID = ? AND FRIEND_ID = ?
            return Long.valueOf(PDBUtil.executeUpdate(sql.toString(), paramsList.toArray()));
        } catch (SQLException e) {
            throw new EngineException("Error occurred while executing function <delete> : ", e);
        }
    }

    // fyzzy query
    @Override
    public List<IFSAdapter> query(IFSAdapter bean) throws Exception {
        if (bean == null) {
            throw new EngineException("Function <query> null entering");
        }
        Map<String, Object> beanMap = toMap(bean);
        if (beanMap == null || beanMap.isEmpty()) {
            throw new EngineException("Function <query> empty bean map");
        }

        StringBuilder sql = new StringBuilder();
        List<Object> paramsList = new ArrayList<>();

        sql.append("SELECT * FROM ").append(TABLENAME).append(" WHERE 1=1");

        for (String key : beanMap.keySet()) {
            Object value = beanMap.get(key);
            if (value != null && !value.toString().trim().isEmpty()) {
                // 模糊查询使用LIKE
                sql.append(" AND ").append(key).append(" LIKE ?");
                paramsList.add("%" + value.toString() + "%");
            }
        }

        // 默认按创建时间倒序排列
        sql.append(" ORDER BY ").append(CREATE_TIME).append(" DESC");

        try {
            // SELECT * FROM T_FRIENDSHIP WHERE USER_ID LIKE ? AND FRIEND_ID LIKE ? ORDER BY
            // CREATE_TIME DESC
            List<Map<String, Object>> resultMaps = PDBUtil.executeQuery(sql.toString(), paramsList.toArray());
            List<IFSAdapter> resultList = new ArrayList<>();
            if (!(resultMaps == null || resultMaps.isEmpty())) {
                for (Map<String, Object> map : resultMaps)
                    resultList.add(toBean(map));
            }
            return resultList;
        } catch (SQLException e) {
            throw new EngineException("Error occurred while executing function <query> : ", e);
        }
    }

    /**
     * Bean to Map
     */
    @Override
    public Map<String, Object> toMap(IFSAdapter bean) throws Exception {
        if (bean == null) {
            return null;
        }
        java.util.HashMap<String, Object> map = new java.util.LinkedHashMap<>();

        if (bean.getFsId() != null) {
            map.put(FS_ID, bean.getFsId());
        }
        if (bean.getUserId() != null) {
            map.put(USER_ID, bean.getUserId());
        }
        if (bean.getFriendId() != null) {
            map.put(FRIEND_ID, bean.getFriendId());
        }
        if (bean.getFsStatus() != null) {
            map.put(FS_STATUS, bean.getFsStatus());
        }
        if (bean.getRemark() != null) {
            map.put(REMARK, bean.getRemark());
        }
        if (bean.getGroupName() != null) {
            map.put(GROUP_NAME, bean.getGroupName());
        }
        if (bean.getApplyMsg() != null) {
            map.put(APPLY_MSG, bean.getApplyMsg());
        }
        if (bean.getCreateTime() != null) {
            map.put(CREATE_TIME, bean.getCreateTime());
        }
        if (bean.getAgreeTime() != null) {
            map.put(AGREE_TIME, bean.getAgreeTime());
        }

        return map;
    }

    // Map to Bean
    @Override
    public IFSAdapter toBean(Map<String, Object> map) throws Exception {
        if (map == null || map.isEmpty()) {
            return null;
        }

        IFSAdapter bean = new icu.nothingless.pojo.bean.FriendshipBean();

        // 从map中获取值并设置到bean
        if (map.containsKey(FS_ID)) {
            Object val = map.get(FS_ID);
            if (val != null) {
                bean.setFsId(val instanceof Number ? ((Number) val).longValue() : Long.valueOf(val.toString()));
            }
        }
        if (map.containsKey(USER_ID)) {
            Object val = map.get(USER_ID);
            if (val != null) {
                bean.setUserId(val instanceof Number ? ((Number) val).longValue() : Long.valueOf(val.toString()));
            }
        }
        if (map.containsKey(FRIEND_ID)) {
            Object val = map.get(FRIEND_ID);
            if (val != null) {
                bean.setFriendId(val.toString());
            }
        }
        if (map.containsKey(FS_STATUS)) {
            Object val = map.get(FS_STATUS);
            if (val != null) {
                bean.setFsStatus(val instanceof Number ? ((Number) val).intValue() : Integer.valueOf(val.toString()));
            }
        }
        if (map.containsKey(REMARK)) {
            bean.setRemark(String.valueOf(map.get(REMARK)));
        }
        if (map.containsKey(GROUP_NAME)) {
            bean.setGroupName(String.valueOf(map.get(GROUP_NAME)));
        }
        if (map.containsKey(APPLY_MSG)) {
            bean.setApplyMsg(String.valueOf(map.get(APPLY_MSG)));
        }
        if (map.containsKey(CREATE_TIME)) {
            bean.setCreateTime(String.valueOf(map.get(CREATE_TIME)));
        }
        if (map.containsKey(AGREE_TIME)) {
            bean.setAgreeTime(String.valueOf(map.get(AGREE_TIME)));
        }

        return bean;
    }

    public static String getFsId() {
        return FS_ID;
    }

    public static String getUserId() {
        return USER_ID;
    }

    public static String getFriendId() {
        return FRIEND_ID;
    }

    public static String getFsStatus() {
        return FS_STATUS;
    }

    public static String getRemark() {
        return REMARK;
    }

    public static String getGroupName() {
        return GROUP_NAME;
    }

    public static String getApplyMsg() {
        return APPLY_MSG;
    }

    public static String getCreateTime() {
        return CREATE_TIME;
    }

    public static String getAgreeTime() {
        return AGREE_TIME;
    }

    public static String getTablename() {
        return TABLENAME;
    }

    
}