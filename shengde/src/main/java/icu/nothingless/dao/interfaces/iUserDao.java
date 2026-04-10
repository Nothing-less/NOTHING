package icu.nothingless.dao.interfaces;

import java.util.List;
import java.util.Map;

public interface IUserDao<T> {

    // 查询用户名
    T findByUsername(String username) throws Exception;

    List<T> fuzzyQuery(String keyword) throws Exception;

    // 登录
    Boolean doLogin(T login)throws Exception;

    // 注册
    Boolean doRegister(Map<String, String> register)throws Exception;

    // 更新密码
    Boolean updatePwd(String username, String newPassword)throws Exception;

    // 注销
    Boolean doLogout(T currentUser)throws Exception;


}