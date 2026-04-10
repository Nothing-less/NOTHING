package icu.nothingless.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icu.nothingless.commons.RespEntity;
import icu.nothingless.dao.interfaces.IUserDao;
import icu.nothingless.pojo.adapter.IUserAdapter;
import icu.nothingless.pojo.bean.UserBean;
import icu.nothingless.pojo.dto.User;
import icu.nothingless.pojo.ibean.IUserBean;
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
    public RespEntity<List<User>> doSearch(User target) {
        if (target == null || Objects.isNull(target.getUserAccount())
                || Objects.isNull(target.getUserPasswd())) {
            // 传空的对象/内容
            return RespEntity.badRequest("illegal target");
        }
        List<User> resultList = new ArrayList<>();

        IUserDao userDaoImpl = ServiceFactory.createInstance(IUserDao.class, "userDaoImpl");
        try {
            List<IUserAdapter> result = userDaoImpl.fuzzyQuery(target.getNickname());
            if (result != null && !result.isEmpty()) {
                result.forEach(
                    one ->{
                        if(!Objects.equals(one.getUserId(), target.getUserId())) {
                            // 不返回自己的信息
                        resultList.add(this.bean_to_dto(one));
                        }
                    }
                );
                return RespEntity.success(resultList);
            }

        } catch (Exception e) {
            logger.error("Error occurred in iUserService.doSerch :", e);
        }
        
        return RespEntity.error("No users found");
    }

    @Override
    public RespEntity<User> doLogin(User target) {

        if (target == null || Objects.isNull(target.getUserAccount())
                || Objects.isNull(target.getUserPasswd())) {
            // 传空的对象/内容
            return RespEntity.badRequest("illegal target");
        }

        IUserBean result;
        try {
            result = (IUserBean)userDao.findByUsername(target.getUserAccount());

            if (result == null) {
                // 未找到对应账号
                return RespEntity.unauthorized("your account or password are not correct");
            }

            if (!Objects.equals(target.getUserPasswd(), result.getUserPasswd())) {
                // 密码不一致
                return RespEntity.unauthorized("your account or password are not correct");
            }

            /* *------------------------ 密码一致 ------------------------* */
            // 更新登录信息(通过主键更新登录时间和登录地址)
            result.setLastLoginIpAddr(target.getLastLoginIpAddr());
            result.setLastLoginTime(target.getLastLoginTime());
            
            final Boolean b_result = userDao.doLogin(result);
            if (Boolean.TRUE.equals(b_result)) {
                final User ret = this.bean_to_dto(result);
                return RespEntity.success(ret);
            }
        } catch (final Exception e) {
            logger.error("Error occurred in iUserService.doLogin :", e);
        }
        return RespEntity.error("Login Failed 〒▽〒");
    }

    @Override
    public RespEntity<User> doRegister(final User target) {
        if (target == null
                || Objects.isNull(target.getUserAccount())
                || Objects.isNull(target.getUserPasswd())) {
            // 传空的对象/内容
            return RespEntity.badRequest("illegal target");
        }
        IUserBean result;
        try {
            result = (IUserBean)userDao.findByUsername(target.getUserAccount());

            if (result != null) {
                // 当前账号已被注册
                return RespEntity.badRequest("The current username is already in use");
            }
            final Map<String, String> params = new HashMap<>() {
                {
                    this.put("username", target.getUserAccount());
                    this.put("password", target.getUserPasswd());
                    this.put("last_login_time", target.getLastLoginTime());
                    this.put("last_login_ip", target.getLastLoginIpAddr());
                }
            };
            Boolean ret = userDao.doRegister(params);
            if (Boolean.TRUE.equals(ret)) {
                // TODO register new user
            }

        } catch (final Exception e) {
            logger.error("Error occurred in iUserService.doRegister :", e);
        }

        return RespEntity.error("Register Failed 〒▽〒");
    }

    private User bean_to_dto(final IUserBean bean) {
        return User.builder()
                .userId(bean.getUserId())
                .userAccount(bean.getUserAccount())
                .userPasswd(null)
                .nickname(bean.getNickname())
                .registerTime(bean.getRegisterTime())
                .lastLoginIpAddr(bean.getLastLoginIpAddr())
                .lastLoginTime(bean.getLastLoginTime())
                .userStatus(bean.getUserStatus())
                .roleId(bean.getRoleId())
                .userKey1(bean.getUserKey1())
                .userKey2(bean.getUserKey2())
                .userKey3(bean.getUserKey3())
                .userKey4(bean.getUserKey4())
                .userKey5(bean.getUserKey5())
                .userKey6(bean.getUserKey6())
                .build();
    }

    @Override
    public RespEntity<User> doLogout(User target) {

        if (target == null || Objects.isNull(target.getUserAccount())) {
            // 传空的对象/内容
            return RespEntity.badRequest("illegal target");
        }
        try {
            Boolean result = (Boolean)userDao.doLogout(dto_to_bean(target));
            if (Boolean.TRUE.equals(result)) {
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
    private IUserBean dto_to_bean(final User dto) {
        IUserBean bean = new UserBean();
        bean.setUserId(dto.getUserId());
        bean.setUserAccount(dto.getUserAccount());
        bean.setUserPasswd(dto.getUserPasswd());
        bean.setNickname(dto.getNickname());
        bean.setRegisterTime(dto.getRegisterTime());
        bean.setLastLoginIpAddr(dto.getLastLoginIpAddr());
        bean.setLastLoginTime(dto.getLastLoginTime());
        bean.setUserStatus(dto.getUserStatus());
        bean.setRoleId(dto.getRoleId());
        bean.setUserKey1(dto.getUserKey1());
        bean.setUserKey2(dto.getUserKey2());
        bean.setUserKey3(dto.getUserKey3());
        bean.setUserKey4(dto.getUserKey4());
        bean.setUserKey5(dto.getUserKey5());
        bean.setUserKey6(dto.getUserKey6());
        return bean;
    }

}
