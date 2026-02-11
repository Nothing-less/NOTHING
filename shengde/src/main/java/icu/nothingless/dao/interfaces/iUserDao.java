package icu.nothingless.dao.interfaces;

import java.util.Optional;

import icu.nothingless.pojo.adapter.iSTAdapter;
import icu.nothingless.pojo.adapter.iUserSTOAdapter;
import icu.nothingless.pojo.bean.UserSTO;

public interface iUserDao {

    // 查询用户名
    <T> T findByUsername(String username);

    // 登录
    Boolean doLogin(iUserSTOAdapter login);

    // 注册
    Boolean doRegister(iUserSTOAdapter register);

    // 更新密码
    Boolean updatePwd(String username, String newPassword);


}