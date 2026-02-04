package icu.nothingless.pojo.engine;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

import icu.nothingless.pojo.adapter.iUserSTOAdapter;
import icu.nothingless.pojo.bean.UserSTO;
import icu.nothingless.tools.PDBUtil;

public class UserSTOEngine extends BaseEngine<iUserSTOAdapter,UserSTOEngine> {
    private static final String USERID = "USERID";
    private static final String ACCOUNT = "ACCOUNT";
    private static final String PWDSTRING = "PWDSTRING";
    private static final String NICKNAME = "NICKNAME";
    private static final String INFOS = "INFOS";
    private static final String REGISTERTIME = "REGISTERTIME";
    private static final String LASTLOGINTIME = "LASTLOGINTIME";
    private static final String LASTLOGINIPADDR = "LASTLOGINIPADDR";
    private static final String STATUS = "STATUS";
    private static final String ROLEID = "ROLEID";
    private static final String USERKEY1 = "USERKEY1";
    private static final String USERKEY2 = "USERKEY2";
    private static final String USERKEY3 = "USERKEY3";
    private static final String USERKEY4 = "USERKEY4";
    private static final String USERKEY5 = "USERKEY5";
    private static final String USERKEY6 = "USERKEY6";
    /* ---------------------------------------------------------------------- */
    private static final String TABLENAME = "USERS";

    @Override
    public int save(iUserSTOAdapter bean) {
        Map<String, Object> beanMap = toMap(bean);
        return (bean.getUserId() == null || bean.getUserId().isBlank())
            ? (insertOne(beanMap))
            : (updateOne(beanMap));
    }

    @Override
    public int delete(iUserSTOAdapter bean) {
        return 0;
    }

    @Override
    public List<iUserSTOAdapter> query(iUserSTOAdapter bean) {
        System.out.println("Hello from UserSTOEngine query method");
        return null;
    }
    private Map<String, Object> toMap(iUserSTOAdapter bean) {
        /**
         * Initializes a new {@link LinkedHashMap} with an initial capacity of 16.
         * At this point, the map contains no key-value mappings.
         * Therefore, calling {@code map.isEmpty()} will return {@code true}.
         */
        var map = new LinkedHashMap<String, Object>(16);
        if (bean == null) return map;

        String s;
        Object o;
        if ((s = bean.getUserId())             != null && !s.isBlank()) map.put(USERID, s);
        if ((s = bean.getAccount())            != null && !s.isBlank()) map.put(ACCOUNT, s);
        if ((s = bean.getPwdString())          != null && !s.isBlank()) map.put(PWDSTRING, s);
        if ((s = bean.getNickname())           != null && !s.isBlank()) map.put(NICKNAME, s);
        if ((s = bean.getInfos())              != null && !s.isBlank()) map.put(INFOS, s);
        if ((s = bean.getRegisterTime())       != null && !s.isBlank()) map.put(REGISTERTIME, s);
        if ((s = bean.getLastLoginTime())      != null && !s.isBlank()) map.put(LASTLOGINTIME, s);
        if ((s = bean.getLastLoginIpAddr())    != null && !s.isBlank()) map.put(LASTLOGINIPADDR, s);
        if ((s = bean.getRoleId())             != null && !s.isBlank()) map.put(ROLEID, s);
        if ((s = bean.getUserKey1())           != null && !s.isBlank()) map.put(USERKEY1, s);
        if ((s = bean.getUserKey2())           != null && !s.isBlank()) map.put(USERKEY2, s);
        if ((s = bean.getUserKey3())           != null && !s.isBlank()) map.put(USERKEY3, s);
        if ((s = bean.getUserKey4())           != null && !s.isBlank()) map.put(USERKEY4, s);
        if ((s = bean.getUserKey5())           != null && !s.isBlank()) map.put(USERKEY5, s);
        if ((s = bean.getUserKey6())           != null && !s.isBlank()) map.put(USERKEY6, s);
        if ((o = bean.getStatus())             != null) map.put(STATUS, o);
        return map;
    }

    private int insertOne(Map<String, Object> bean) {
        if(bean == null || bean.isEmpty()) {
            return -44;
        }
        
        StringBuffer sql = new StringBuffer();
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
            System.out.println("Executing SQL: \n" + sql.toString());
            System.out.println("With values: " );
            bean.values().forEach(v -> System.out.print(v + " | "));
            // return PDBUtil.executeUpdate(sql.toString(), bean.values().toArray());
        } catch (Exception e) {
        }
        return 0;
    }

    private int updateOne(Map<String, Object> bean) {
        if(bean == null || bean.isEmpty()) {
            return -22;
        }
        StringBuffer sql = new StringBuffer();
        Object[] params = new Object[bean.size()];
        sql.append("UPDATE ").append(TABLENAME).append(" SET ");
        int i = 0;
        for (String key : bean.keySet()) {
            if (!key.equals(USERID)) { // 主键是 WHERE 条件
                sql.append(key).append(" = ?, ");
                params[i++] = bean.get(key).toString(); // 参数有序化
            }
        }
        params[i] = bean.get(USERID).toString(); // 主键值放最后

        // 移除最後的逗號和空格
        sql.append("WHERE ").append(USERID).append(" = ? ");
        try {
            //  System.out.println("Executing SQL: \n" + sql.toString());
            //  System.out.println("With values: " );
            //     for (int j = 0; j < bean.size(); j++) {
            //         System.out.print(params[j] + " | ");
            //     }
            return PDBUtil.executeUpdate(sql.toString(), params);
        } catch (Exception e) {
        }

        return 0;
    }


    // /*------------------------------- Singleton Pattern -------------------------------*/

    // private UserSTOEngine() {
    //     if (Holder.INSTANCE != null) {
    //         throw new IllegalStateException("單例已存在，禁止通過反射創建！");
    //     }
    // }
    // private static class Holder {
    //     private static final UserSTOEngine INSTANCE = new UserSTOEngine();
    // }

    // public static UserSTOEngine getInstance() {
    //     return Holder.INSTANCE;
    // }

    // protected UserSTOEngine readResolve() {
    //     return getInstance();
    // }

    // @Override
    // protected Object clone() throws CloneNotSupportedException {
    //     super.clone();
    //     throw new CloneNotSupportedException("單例模式禁止克隆");
    // }

}
