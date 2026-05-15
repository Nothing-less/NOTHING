package icu.nothingless.dao.interfaces;

import icu.nothingless.commons.R;

public interface IUserDao<T> {

    // 查询用户名
    public R findByUsername(String username) throws Exception;

    // 登录
    public R doLogin(T login)throws Exception;

    // 注册
    public R doRegister(T register)throws Exception;

    // 更新
    public R doUpdate(T newTarget)throws Exception;

    // 登出
    public R doLogout(T currentUser)throws Exception;

    public R doLogoutForAll()throws Exception;

    // 查找
    public R doSearch(String str)throws Exception;


}