package icu.nothingless.pojo.engine;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.LoggerFactory;

import icu.nothingless.exceptions.EngineException;
import icu.nothingless.pojo.adapter.IUserAdapter;

import icu.nothingless.tools.PDBUtil;

public class UserEngine extends BaseEngine<IUserAdapter, UserEngine> {
    private static final String USERID = "USER_ID";
    private static final String USERACCOUNT = "USERACCOUNT";
    private static final String USERPASSWD = "USERPASSWD";
    private static final String NICKNAME = "NICKNAME";
    private static final String USER_INFOS = "USER_INFOS";
    private static final String REGISTER_TIME = "REGISTER_TIME";
    private static final String LAST_LOGIN_TIME = "LAST_LOGIN_TIME";
    private static final String LAST_LOGIN_IP_ADDR = "LAST_LOGIN_IP_ADDR";
    private static final String USER_STATUS = "USER_STATUS";
    private static final String ROLEID = "ROLE_ID";
    private static final String USERKEY1 = "USER_KEY1";
    private static final String USERKEY2 = "USER_KEY2";
    private static final String USERKEY3 = "USER_KEY3";
    private static final String USERKEY4 = "USER_KEY4";
    private static final String USERKEY5 = "USER_KEY5";
    private static final String USERKEY6 = "USER_KEY6";
    /* ---------------------------------------------------------------------- */
    private static final String TABLENAME = "USERS";

    @Override
    public Long save(IUserAdapter bean) throws Exception {
        if (bean == null) {
            throw new EngineException("Function <save> entering null");
        }

        Map<String, Object> beanMap = toMap(bean);
        return (bean.getUserId() == null || bean.getUserId().isBlank())
                ? insertOne(beanMap)
                : updateOne(beanMap);
    }

    @Override
    public Long delete(IUserAdapter bean) throws Exception {
        if (bean == null) {
            throw new EngineException("Function <delete> entering null");
        }

        Map<String, Object> beanMap = toMap(bean);
        if (beanMap.isEmpty() || !beanMap.containsKey(USERID)) {
            throw new EngineException("Function <delete> entering null");
        }

        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(TABLENAME).append(" SET ")
                .append(USER_STATUS).append(" = ? ")
                .append("WHERE ").append(USERID).append(" = ?");

        try {
            return Long.valueOf(PDBUtil.executeUpdate(sql.toString(), IUserAdapter.STATUS_INACTIVE,
                    beanMap.get(USERID).toString()));
        } catch (SQLException e) {
            throw new EngineException("Error occurred while executing function <delete> : ", e);
        }
    }

    @Override
    public List<IUserAdapter> query(IUserAdapter bean) throws Exception {
        if (bean == null) {
            throw new EngineException("Function <query> entering null");
        }
        return fuzzyQuery(bean);
    }

    @Override
    public Long update(IUserAdapter bean) throws Exception {
        if (bean == null) {
            throw new EngineException("Function <update> entering null");
        }

        Map<String, Object> beanMap = toMap(bean);
        if (beanMap.isEmpty() || !beanMap.containsKey(USERID)) {
            throw new EngineException("Function <update> entering null");
        }
        return updateOne(beanMap);
    }

    @Override
    public Map<String, Object> toMap(IUserAdapter bean) throws Exception {
        if (bean == null) {
            throw new EngineException("Function <toMap> entering null");
        }

        LinkedHashMap<String, Object> map = new LinkedHashMap<>(16);
        String s;
        Object o;
        try {
            if ((s = bean.getUserId()) != null && !s.isBlank())
                map.put(USERID, s);
            if ((s = bean.getUserAccount()) != null && !s.isBlank())
                map.put(USERACCOUNT, s);
            if ((s = bean.getUserPasswd()) != null && !s.isBlank())
                map.put(USERPASSWD, s);
            if ((s = bean.getNickname()) != null && !s.isBlank())
                map.put(NICKNAME, s);
            if ((s = bean.getUserInfos()) != null && !s.isBlank())
                map.put(USER_INFOS, s);
            if ((s = bean.getRegisterTime()) != null && !s.isBlank())
                map.put(REGISTER_TIME, s);
            if ((s = bean.getLastLoginTime()) != null && !s.isBlank())
                map.put(LAST_LOGIN_TIME, s);
            if ((s = bean.getLastLoginIpAddr()) != null && !s.isBlank())
                map.put(LAST_LOGIN_IP_ADDR, s);
            if ((s = bean.getRoleId()) != null && !s.isBlank())
                map.put(ROLEID, s);
            if ((s = bean.getUserKey1()) != null && !s.isBlank())
                map.put(USERKEY1, s);
            if ((s = bean.getUserKey2()) != null && !s.isBlank())
                map.put(USERKEY2, s);
            if ((s = bean.getUserKey3()) != null && !s.isBlank())
                map.put(USERKEY3, s);
            if ((s = bean.getUserKey4()) != null && !s.isBlank())
                map.put(USERKEY4, s);
            if ((s = bean.getUserKey5()) != null && !s.isBlank())
                map.put(USERKEY5, s);
            if ((s = bean.getUserKey6()) != null && !s.isBlank())
                map.put(USERKEY6, s);
            if ((o = bean.getUserStatus()) != null)
                map.put(USER_STATUS, o);

            return map;
        } catch (Exception e) {
            throw new EngineException("Error occurred while executing function <toMap> : ", e);
        }
    }

    @Override
    public IUserAdapter toBean(Map<String, Object> map) throws Exception {
        if (map == null || map.isEmpty()) {
            throw new EngineException("Function <toBean> entering null");
        }

        IUserAdapter bean = new icu.nothingless.pojo.bean.UserBean();
        try {
            Optional.ofNullable(map.get(USERID)).ifPresent(v -> bean.setUserId(String.valueOf(v)));
            Optional.ofNullable(map.get(USERACCOUNT)).ifPresent(v -> bean.setUserAccount(String.valueOf(v)));
            Optional.ofNullable(map.get(USERPASSWD)).ifPresent(v -> bean.setUserPasswd(String.valueOf(v)));
            Optional.ofNullable(map.get(NICKNAME)).ifPresent(v -> bean.setNickname(String.valueOf(v)));
            Optional.ofNullable(map.get(USER_INFOS)).ifPresent(v -> bean.setUserInfos(String.valueOf(v)));
            Optional.ofNullable(map.get(REGISTER_TIME)).ifPresent(v -> bean.setRegisterTime(String.valueOf(v)));
            Optional.ofNullable(map.get(LAST_LOGIN_TIME)).ifPresent(v -> bean.setLastLoginTime(String.valueOf(v)));
            Optional.ofNullable(map.get(LAST_LOGIN_IP_ADDR)).ifPresent(v -> bean.setLastLoginIpAddr(String.valueOf(v)));
            Optional.ofNullable(map.get(ROLEID)).ifPresent(v -> bean.setRoleId(String.valueOf(v)));
            Optional.ofNullable(map.get(USERKEY1)).ifPresent(v -> bean.setUserKey1(String.valueOf(v)));
            Optional.ofNullable(map.get(USERKEY2)).ifPresent(v -> bean.setUserKey2(String.valueOf(v)));
            Optional.ofNullable(map.get(USERKEY3)).ifPresent(v -> bean.setUserKey3(String.valueOf(v)));
            Optional.ofNullable(map.get(USERKEY4)).ifPresent(v -> bean.setUserKey4(String.valueOf(v)));
            Optional.ofNullable(map.get(USERKEY5)).ifPresent(v -> bean.setUserKey5(String.valueOf(v)));
            Optional.ofNullable(map.get(USERKEY6)).ifPresent(v -> bean.setUserKey6(String.valueOf(v)));
            Optional.ofNullable(map.get(USER_STATUS))
                    .ifPresent(v -> bean.setUserStatus(Boolean.valueOf(String.valueOf(v))));
            return bean;
        } catch (Exception e) {
            throw new EngineException("Error occurred while executing function <toBean> : ", e);
        }
    }

    private long insertOne(Map<String, Object> bean) throws Exception {
        if (bean == null || bean.isEmpty()) {
            throw new EngineException("Function <insertOne> entering null");
        }

        bean.remove(USERID); // 自增主键, 无需手动设值
        bean.put(USER_STATUS, IUserAdapter.STATUS_ACTIVE);

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(TABLENAME).append(" (");
        StringBuilder valuesPart = new StringBuilder("VALUES (");
        for (String key : bean.keySet()) {
            sql.append(key).append(", ");
            valuesPart.append("?, ");
        }
        sql.setLength(sql.length() - 2);
        valuesPart.setLength(valuesPart.length() - 2);
        sql.append(") ").append(valuesPart).append(")");

        try {
            return PDBUtil.executeInsert(sql.toString(), bean.values().toArray());
        } catch (SQLException e) {
            throw new EngineException("Error occurred while executing function <insertOne> : ", e);
        }
    }

    private long updateOne(Map<String, Object> bean) throws Exception {
        if (bean == null || bean.isEmpty()) {
            throw new EngineException("Function <updateOne> entering null");
        }

        bean.remove(USER_STATUS);

        if (!bean.containsKey(USERID)) {
            throw new EngineException("Function <updateOne> requires USER_ID");
        }
        if (bean.size()<2) {
            throw new EngineException("Function <updateOne> no fields to update");
        }

        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();
        sql.append("UPDATE ").append(TABLENAME).append(" SET ");

        for (String key : bean.keySet()) {
            if (USERID.equals(key)) {
                continue; // 主键不参与SET
            }
            sql.append(key).append(" = ?, ");
            params.add(bean.get(key));
        }

        if (params.isEmpty()) {
            throw new EngineException("Function <updateOne> no fields to update");
        }

        sql.setLength(sql.length() - 2); // 移除最后一个逗号和空格
        sql.append(" WHERE ").append(USERID).append(" = ?");
        params.add(String.valueOf(bean.get(USERID)));

        try {
            return Long.valueOf(PDBUtil.executeUpdate(sql.toString(), params.toArray()));
        } catch (SQLException e) {
            throw new EngineException("Error occurred while executing function <updateOne> : ", e);
        }
    }

    private List<IUserAdapter> fuzzyQuery(IUserAdapter bean) throws Exception {
        if (bean == null) {
            throw new EngineException("Function <fuzzyQuery> entering null");
        }

        Map<String, Object> beanMap = toMap(bean);
        if (beanMap.isEmpty()) {
            throw new EngineException("Function <fuzzyQuery> entering null");
        }

        beanMap.remove(USER_STATUS); // 不参与模糊查询条件

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ").append(TABLENAME).append(" WHERE USER_STATUS = TRUE");
        beanMap.forEach((key, value) -> sql.append(" AND ").append(key).append(" LIKE ?"));

        try {
            Object[] params = beanMap.values().stream()
                    .map(v -> v == null ? null : "%" + v.toString() + "%")
                    .toArray();
            List<Map<String, Object>> queryResults = PDBUtil.executeQuery(sql.toString(), params);
            List<IUserAdapter> results = new ArrayList<>();
            for (Map<String, Object> row : queryResults) {
                try {
                    results.add(toBean(row));
                } catch (Exception e) {
                    LoggerFactory.getLogger(UserEngine.class)
                            .error("Error occurred while executing function <toBean> in function <fuzzyQuery>: ", e);
                }
            }
            return results;
        } catch (Exception e) {
            throw new EngineException("Error occurred while executing function <fuzzyQuery> : ", e);
        }
    }
}
