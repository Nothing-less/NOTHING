package icu.nothingless.dao.interfaces;

import java.util.Map;
import icu.nothingless.pojo.adapter.iUserSTOAdapter;

public interface iUserDao {

    // 查询用户名
    <T> T findByUsername(String username) throws Exception;

    // 登录
    Boolean doLogin(iUserSTOAdapter login)throws Exception;

    // 注册
    Boolean doRegister(Map<String, String> register)throws Exception;

    // 更新密码
    Boolean updatePwd(String username, String newPassword)throws Exception;


}