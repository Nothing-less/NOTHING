package icu.nothingless.dao.interfaces;

import java.util.Map;
import java.util.Optional;

import icu.nothingless.pojo.adapter.iSTAdapter;
import icu.nothingless.pojo.adapter.iUserSTOAdapter;
import icu.nothingless.pojo.bean.UserSTO;

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