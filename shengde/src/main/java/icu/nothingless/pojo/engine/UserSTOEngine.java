package icu.nothingless.pojo.engine;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icu.nothingless.pojo.adapter.iUserSTOAdapter;
import icu.nothingless.pojo.bean.UserSTO;
import icu.nothingless.tools.PDBUtil;

public class UserSTOEngine extends BaseEngine<iUserSTOAdapter, UserSTOEngine> {
    private static final String USERID = "user_id";
    private static final String USERACCOUNT = "useraccount";
    private static final String USERPASSWD = "userpasswd";
    private static final String NICKNAME = "nickname";
    private static final String USER_INFOS = "user_infos";
    private static final String REGISTER_TIME = "register_time";
    private static final String LAST_LOGIN_TIME = "last_login_time";
    private static final String LAST_LOGIN_IP_ADDR = "last_login_ip_addr";
    private static final String USER_STATUS = "user_status";
    private static final String ROLEID = "role_id";
    private static final String USERKEY1 = "user_key1";
    private static final String USERKEY2 = "user_key2";
    private static final String USERKEY3 = "user_key3";
    private static final String USERKEY4 = "user_key4";
    private static final String USERKEY5 = "user_key5";
    private static final String USERKEY6 = "user_key6";
    /* ---------------------------------------------------------------------- */
    private static final String TABLENAME = "USERS";
    private static final Logger logger = LoggerFactory.getLogger(UserSTOEngine.class);

    @Override
    public Long save(iUserSTOAdapter bean) {
        Map<String, Object> beanMap = toMap(bean);
        return (bean.getUserId() == null || bean.getUserId().isBlank())
                ? (insertOne(beanMap))
                : (updateOne(beanMap));
    }

    @Override
    public Long delete(iUserSTOAdapter bean) {
        Map<String, Object> beanMap = toMap(bean);
        if (beanMap.isEmpty() || !beanMap.containsKey(USERID)) {
            logger.error("delete方法传入值为null");
            return -64L;
        }
        StringBuilder sql = new StringBuilder();
        Object[] params = new Object[2];
        sql.append("UPDATE ").append(TABLENAME).append(" SET ");
        sql.append(USER_STATUS).append(" = ? ");
        params[0] = false;
        params[1] = beanMap.get(USERID).toString();
        try {
            return Long.valueOf(PDBUtil.executeUpdate(sql.toString(), params));
        } catch (SQLException e) {
            logger.error("Error executing delete (soft): ", e);
            logger.error("SQL: {}", sql.toString());
            logger.error("Parameters: {}", java.util.Arrays.toString(params));
        }
        return -65L;
    }

    @Override
    public List<iUserSTOAdapter> query(iUserSTOAdapter bean) {
        if(bean == null){
            logger.error("query方法传入值为null");
            return null;
        }
        return fuzzyQuery(bean);
    }

    private iUserSTOAdapter toBean(Map<String, Object> map) {
        if (map == null || map.isEmpty()){
            logger.error("toBean方法传入值为null");
            return null;
        }

        iUserSTOAdapter bean = new UserSTO();

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
        Optional.ofNullable(map.get(USER_STATUS)).ifPresent(v -> bean.setUserStatus(Boolean.valueOf(String.valueOf(v))));

        return bean;
    }

    private Map<String, Object> toMap(iUserSTOAdapter bean) {
        /**
         * Initializes a new {@link LinkedHashMap} with an initial capacity of 16.
         * At this point, the map contains no key-value mappings.
         * Therefore, calling {@code map.isEmpty()} will return {@code true}.
         */
        var map = new LinkedHashMap<String, Object>(16);
        if (bean == null){
            logger.error("toMap方法传入值为null");
            return map;
        }
        String s;
        Object o;
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
    }

    private long insertOne(Map<String, Object> bean) {
        if (bean == null || bean.isEmpty()) {
            logger.error("insertOne方法传入值为null");
            return -44L;
        }
        bean.remove(USERID); // 自增主键, 无需手动设值
        bean.put(USER_STATUS,true);

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(TABLENAME).append(" (");
        StringBuffer valuesPart = new StringBuffer("VALUES (");
        for (String key : bean.keySet()) {
            sql.append(key).append(", ");
            valuesPart.append("?, ");
        }
        // 移除最後的逗號和空格
        sql.setLength(sql.length() - 2);
        valuesPart.setLength(valuesPart.length() - 2);
        sql.append(") ").append(valuesPart).append(")");
        try {
            // System.out.println("Executing SQL: \n" + sql.toString());
            // System.out.println("With values: ");
            // bean.values().forEach(v -> System.out.print(v + " | "));
            return PDBUtil.executeInsert(sql.toString(), bean.values().toArray());
        } catch (SQLException e) {
            logger.error("Error executing insert: ", e);
            logger.error("SQL: {}", sql.toString());
            logger.error("Parameters: {}", bean.values());
        }
        return -45L;
    }

    private long updateOne(Map<String, Object> bean) {
        if (bean == null || bean.isEmpty()) {
            logger.error("updateOne方法传入值为null/没有主键");
            return -24L;
        }

        bean.remove(USER_STATUS);

        StringBuilder sql = new StringBuilder();
        Object[] params = new Object[bean.size()];
        sql.append("UPDATE ").append(TABLENAME).append(" SET ");
        int i = 0;
        boolean firstField = true;
        for (String key : bean.keySet()) {
            if (!key.equals(USERID)) { // 主键是 WHERE 条件
                if (!firstField) {
                sql.append(", ");   // 先加逗号，除了第一个字段
            }
                sql.append(key).append(" = ? ");
                params[i++] = bean.get(key).toString(); // 参数有序化
                firstField = false;
            }
        }
        params[i] =Long.valueOf( String.valueOf(bean.get(USERID)));

        sql.append("WHERE ").append(USERID).append(" = ? ");
        try {
            // System.out.println("Executing SQL: \n" + sql.toString());
            // System.out.println("With values: " );
            // for (int j = 0; j < bean.size(); j++) {
            //     System.out.print(params[j] + " | ");
            // }
            return Long.valueOf(PDBUtil.executeUpdate(sql.toString(), params));
        } catch (SQLException e) {
            logger.error("Error executing update: ", e);
            logger.error("SQL: {}", sql.toString());
            logger.error("Parameters: {}", java.util.Arrays.toString(params));
        }

        return -25L;
    }

    private List<iUserSTOAdapter> fuzzyQuery(iUserSTOAdapter bean) {
        Map<String, Object> beanMap = toMap(bean);
        List<iUserSTOAdapter> results = new ArrayList<>();
        if (bean == null || beanMap.isEmpty()) {
            logger.error("fuzzyQuery方法传入值为null");
            return null;
        }
        beanMap.remove(USER_STATUS); // 不參與模糊查詢條件
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ").append(TABLENAME).append(" WHERE USER_STATUS = TRUE ");
        beanMap.forEach((key, value) -> {
            sql.append(" AND ").append(key).append(" LIKE ? ");
        });
        try {
            Object[] params = beanMap.values().stream()
                    .map(v -> v == null ? null : "%" + v.toString() + "%")
                    .toArray();
            List<Map<String, Object>> queryResults = PDBUtil.executeQuery(sql.toString(), params);
            queryResults.forEach(row -> {
                iUserSTOAdapter resultBean = toBean(row);
                if (resultBean != null) {
                    results.add(resultBean);
                }
            });
        } catch (SQLException e) {
            logger.error("Error executing fuzzy query: ", e);
            logger.error("SQL: {}", sql.toString());
            logger.error("Parameters: {}", java.util.Arrays.asList(params));
        }
        return results;
    }


    // /*------------------------------- Singleton Pattern
    // -------------------------------*/

    // private UserSTOEngine() {
    // if (Holder.INSTANCE != null) {
    // throw new IllegalStateException("單例已存在，禁止通過反射創建！");
    // }
    // }
    // private static class Holder {
    // private static final UserSTOEngine INSTANCE = new UserSTOEngine();
    // }

    // public static UserSTOEngine getInstance() {
    // return Holder.INSTANCE;
    // }

    // protected UserSTOEngine readResolve() {
    // return getInstance();
    // }

    // @Override
    // protected Object clone() throws CloneNotSupportedException {
    // super.clone();
    // throw new CloneNotSupportedException("單例模式禁止克隆");
    // }

}
