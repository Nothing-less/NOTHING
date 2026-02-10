package icu.nothingless.dao.interfaces;

import java.util.Optional;

import icu.nothingless.pojo.adapter.iSTAdapter;
import icu.nothingless.pojo.adapter.iUserSTOAdapter;
import icu.nothingless.pojo.bean.UserSTO;

public interface iUserDao {

    // 查询用户名
    <T> T findByUsername(String username);

    // 注册用户
    boolean save(iUserSTOAdapter login);

    // 验证登录密码
    boolean validateLogin(iUserSTOAdapter login);

    // 更新密码
    boolean updatePassword(String username, String newPassword);

}