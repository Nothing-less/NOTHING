package icu.nothingless.dao.interfaces;

import java.util.Optional;

import icu.nothingless.pojo.bean.UserSTO;

public interface iLoginDao {

    // 查询用户名
    Optional<UserSTO> findByUsername(String username);

    // 注册用户
    boolean save(UserSTO login);

    // 验证登录密码
    boolean validateLogin(UserSTO login);

    // 更新密码
    boolean updatePassword(String username, String newPassword);

}