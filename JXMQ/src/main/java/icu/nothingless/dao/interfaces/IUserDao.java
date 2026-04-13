package icu.nothingless.dao.interfaces;

import java.util.List;
import java.util.Map;

import icu.nothingless.pojo.adapter.IUserAdapter;

public interface IUserDao<T extends IUserAdapter> {

    // 查询用户名
    public T findByUsername(String username) throws Exception;

    public List<T> fuzzyQuery(String keyword) throws Exception;

    // 登录
    public Boolean doLogin(T login)throws Exception;

    // 注册
    public Boolean doRegister(Map<String, String> register)throws Exception;

    // 更新密码
    public Boolean updatePwd(String username, String newPassword)throws Exception;

    // 注销
    public Boolean doLogout(T currentUser)throws Exception;


}