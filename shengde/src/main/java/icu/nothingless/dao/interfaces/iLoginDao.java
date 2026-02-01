package icu.nothingless.dao.interfaces;

import java.util.Optional;

import icu.nothingless.entity.LoginTMPBean;

public interface iLoginDao {

    // 查询用户名
    Optional<LoginTMPBean> findByUsername(String username);

    // 注册用户
    boolean save(LoginTMPBean login);

    // 验证登录密码
    boolean validateLogin(LoginTMPBean login);

    // 更新密码
    boolean updatePassword(String username, String newPassword);

}