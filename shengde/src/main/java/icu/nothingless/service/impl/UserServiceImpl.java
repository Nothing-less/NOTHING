package icu.nothingless.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icu.nothingless.dao.impl.LoginDaoImpl.UserDaoImpl;
import icu.nothingless.dao.interfaces.iUserDao;
import icu.nothingless.dto.UserDTO;
import icu.nothingless.pojo.adapter.iUserSTOAdapter;
import icu.nothingless.pojo.commons.RespEntity;
import icu.nothingless.service.interfaces.iUserService;
/**
 * 
 * 默认传入的UserDTO对象有以下属性：
 *  UserAccount 账号
 *  Password    密码
 *  LastLoginTime   登录时间
 *  LastLoginIpAddr 登录地址
*/
public class UserServiceImpl implements iUserService<UserDTO>{
    private static final iUserDao userDao = new UserDaoImpl();
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    public RespEntity doLogin(final UserDTO target) {
        
        if(target == null || Objects.isNull(target.getUserAccount()) 
                          || Objects.isNull(target.getUserPasswd()) 
        ){
            // 传空的对象/内容
            return RespEntity.badRequest("illegal target");
        }

        iUserSTOAdapter result;
        try {
            result = UserServiceImpl.userDao.findByUsername(target.getUserAccount());

            if(result == null){
                // 未找到对应账号
                return RespEntity.unauthorized("your account or password are not correct");
            }

            if(!Objects.equals(target.getUserPasswd(),result.getUserPasswd())){
                // 密码不一致
                return RespEntity.unauthorized("your account or password are not correct");
            }
            
            /* *------------------------ 密码一致 ------------------------* */ 
            // 更新登录信息(通过主键更新登录时间和登录地址)
            final Boolean b_result = UserServiceImpl.userDao.doLogin(result);
            if(Boolean.TRUE.equals(b_result)){
                final UserDTO ret = this.bean_to_dto(result);
                return RespEntity.success(ret);
            }
        } catch (final Exception e) {
            UserServiceImpl.logger.error("Error occurred in iUserService.doLogin :",e);
        }
        return RespEntity.error("Login Failed 〒▽〒");
    }

    @Override
    public RespEntity doRegister(final UserDTO target) {
        if(target == null 
            || Objects.isNull(target.getUserAccount()) 
            || Objects.isNull(target.getUserPasswd())
        ){
            // 传空的对象/内容
            return RespEntity.badRequest("illegal target");
        }
        iUserSTOAdapter result;
        try {
            result = UserServiceImpl.userDao.findByUsername(target.getUserAccount());

            if(result != null){
                // 当前账号已被注册
                return RespEntity.badRequest("The current username is already in use");
            }
            final Map<String,String> params = new HashMap<>(){{
                this.put("username", target.getUserAccount());
                this.put("password",target.getUserPasswd());
                this.put("last_login_time",target.getLastLoginTime());
                this.put("last_login_ip",target.getLastLoginIpAddr());
            }};
            Boolean ret = UserServiceImpl.userDao.doRegister(params);
            if(Boolean.TRUE.equals(ret)){
                // TODO 
            }

        } catch (final Exception e) {
            UserServiceImpl.logger.error("Error occurred in iUserService.doRegister :",e);
        }

        return RespEntity.error("Register Failed 〒▽〒");
    }


    private UserDTO bean_to_dto(final iUserSTOAdapter bean){
        return UserDTO.builder()
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


}
