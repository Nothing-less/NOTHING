package icu.nothingless.pojo.engine;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import icu.nothingless.pojo.adapter.iUserSTOAdapter;
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
    @Override
    public int save(iUserSTOAdapter bean) {
        String sql = null;
        Map<String, Object> valueMap = toMap(bean);
        try {
            if (bean.getUserId() == null || bean.getUserId().isBlank()) {
                // Insert
                
                
            } else {
                // Update
                
            }
            PDBUtil.executeUpdate(sql);
        } catch (SQLException e) {

        }

        return 0;
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
        if (bean == null) return Map.of();
    
        var map = new HashMap<String, Object>(16);
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
