package icu.nothingless.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icu.nothingless.commons.R;
import icu.nothingless.commons.RespEntity;
import icu.nothingless.dao.interfaces.IUserDao;
import icu.nothingless.pojo.dto.User;
import icu.nothingless.service.interfaces.IUserService;
import icu.nothingless.tools.ServiceFactory;

/**
 * 
 * 默认传入的UserDTO对象有以下属性：
 * UserAccount 账号
 * Password 密码
 * LastLoginTime 登录时间
 * LastLoginIpAddr 登录地址
 */
public class UserServiceImpl implements IUserService<User> {
    private static final IUserDao userDao = ServiceFactory.createInstance(IUserDao.class, "cacheUserDaoImpl");
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    public RespEntity<List<User>> doSearch(String target) {
        if (target == null || Objects.isNull(target)
                || Objects.isNull(target)) {
            // 传空的对象/内容
            return RespEntity.badRequest("illegal target");
        }
        List<User> resultList = new ArrayList<>();

        IUserDao userDaoImpl = ServiceFactory.createInstance(IUserDao.class, "userDaoImpl");
        try {
            R result = userDaoImpl.doSearch(target);
            if(result.isSuccess()){
                resultList = (List<User>) result.data();
            }
            if (resultList != null && !resultList.isEmpty()) {
                List<User> returnList = new ArrayList<>();
                for (User user : resultList) {
                    returnList.add(user.withoutPasswd());
                }
                return RespEntity.success(returnList);
            }

        } catch (Exception e) {
            logger.error("Error occurred in iUserService.doSerch :", e);
        }
        
        return RespEntity.error("No users found");
    }

    @Override
    public RespEntity<User> doLogin(User target) {

        if (target == null || Objects.isNull(target.userAccount())
                || Objects.isNull(target.userPasswd())) {
            // 传空的对象/内容
            return RespEntity.badRequest("illegal target");
        }

        try {
            R ret = userDao.findByUsername(target.userAccount());

            if (!ret.isSuccess()) {
                // 未找到对应账号
                return RespEntity.unauthorized("your account or password are not correct");
            }
            User tmp = (User)ret.data();
            User target_copy = User.forLogin(target,tmp.userId());
            ret = userDao.doLogin(target_copy);
            if(ret.isSuccess()){
                User user = (User)ret.data();
                return RespEntity.success(user);
            }
        } catch (final Exception e) {
            logger.error("Error occurred in iUserService.doLogin :", e);
        }
        return RespEntity.error("Login Failed 〒▽〒");
    }

    @Override
    public RespEntity<User> doRegister(User target) {
        if (target == null
                || Objects.isNull(target.userAccount())
                || Objects.isNull(target.userPasswd())) {
            // 传空的对象/内容
            return RespEntity.badRequest("illegal target");
        }
        try {
            R result = userDao.findByUsername(target.userAccount());

            if (result.isSuccess()) {
                // 当前账号已被注册
                return RespEntity.badRequest("The current username is already in use");
            }
            R ret = userDao.doRegister(target);
            if (ret.isSuccess()) {

                return RespEntity.success(""+ret.data(),target);
            }

        } catch (final Exception e) {
            logger.error("Error occurred in iUserService.doRegister :", e);
        }

        return RespEntity.error("Register Failed 〒▽〒");
    }


    @Override
    public RespEntity<User> doLogout(User target) {

        if (target == null) {
            // 传空的对象/内容
            return RespEntity.badRequest("illegal target");
        }
        try {
            R result = userDao.doLogout((target));
            if (result.isSuccess()) {
                return RespEntity.success(target);
            }
        } catch (final Exception e) {
            logger.error("Error occurred in iUserService.doLogout :", e);
        }
        return RespEntity.error("Error occurred in Logout 〒▽〒");
    }

    @Override
    public RespEntity<User> doUpdate(User target) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'doUpdate'");
    }

}
