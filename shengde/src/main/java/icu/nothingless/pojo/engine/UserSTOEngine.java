package icu.nothingless.pojo.engine;

import java.sql.Connection;
import java.util.List;

import icu.nothingless.pojo.bean.UserSTO;

public class UserSTOEngine extends BaseEngine<UserSTO> {
    private static final String userId          = "USERID";
    private static final String account         = "ACCOUNT";
    private static final String pwdString       = "PWDSTRING";
    private static final String nickname        = "NICKNAME";
    private static final String infos           = "INFOS";
    private static final String registerTime    = "REGISTERTIME";
    private static final String lastLoginTime   = "LASTLOGINTIME";
    private static final String lastLoginIpAddr = "LASTLOGINIPADDR";
    private static final String status          = "STATUS";
    private static final String roleId          = "ROLEID";
    private static final String userKey1        = "USERKEY1";
    private static final String userKey2        = "USERKEY2";
    private static final String userKey3        = "USERKEY3";
    private static final String userKey4        = "USERKEY4";
    private static final String userKey5        = "USERKEY5";
    private static final String userKey6        = "USERKEY6";
    
    private UserSTOEngine() {
        this.beanClass = UserSTO.class;
        this.tableName = "Users";
    }
    public UserSTOEngine(Connection connection) {
        this();
        this.connection = connection;
    }

    @Override
    public int save() {
        return 0;
    }

    @Override
    public int delete() {
        return 0;
    }

    @Override
    public List<UserSTO> query(UserSTO bean) {

        return null;
    }

}
