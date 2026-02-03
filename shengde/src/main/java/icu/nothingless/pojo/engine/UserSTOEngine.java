package icu.nothingless.pojo.engine;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import icu.nothingless.pojo.bean.UserSTO;
import icu.nothingless.tools.PDBPoolManager;

public class UserSTOEngine extends BaseEngine<UserSTO> {
    private static final String userId = "USERID";
    private static final String account = "ACCOUNT";
    private static final String pwdString = "PWDSTRING";
    private static final String nickname = "NICKNAME";
    private static final String infos = "INFOS";
    private static final String registerTime = "REGISTERTIME";
    private static final String lastLoginTime = "LASTLOGINTIME";
    private static final String lastLoginIpAddr = "LASTLOGINIPADDR";
    private static final String status = "STATUS";
    private static final String roleId = "ROLEID";
    private static final String userKey1 = "USERKEY1";
    private static final String userKey2 = "USERKEY2";
    private static final String userKey3 = "USERKEY3";
    private static final String userKey4 = "USERKEY4";
    private static final String userKey5 = "USERKEY5";
    private static final String userKey6 = "USERKEY6";

    @Override
    public int save(UserSTO bean) {
        String sql = null;
        Connection conn = null;
        try {
            conn = PDBPoolManager.getConnection();

        } catch (SQLException e) {
        } finally {
            PDBPoolManager.closeConnection(conn);
        }
        return 0;
    }

    @Override
    public int delete(UserSTO bean) {
        return 0;
    }

    @Override
    public List<UserSTO> query(UserSTO bean) {

        return null;
    }

    /*------------------------------- Singleton Pattern -------------------------------*/

    private UserSTOEngine() {
        if (Holder.INSTANCE != null) {
            throw new IllegalStateException("單例已存在，禁止通過反射創建！");
        }
    }

    private static class Holder {
        private static final UserSTOEngine INSTANCE = new UserSTOEngine();
    }

    public static UserSTOEngine getInstance() {
        return Holder.INSTANCE;
    }

    protected UserSTOEngine readResolve() {
        return getInstance();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        super.clone();
        throw new CloneNotSupportedException("單例模式禁止克隆");
    }

}
