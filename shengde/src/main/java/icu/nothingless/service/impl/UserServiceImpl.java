package icu.nothingless.service.impl;

import java.util.List;
import java.util.Objects;

import icu.nothingless.dao.impl.LoginDaoImpl.UserDaoImpl;
import icu.nothingless.dao.interfaces.iUserDao;
import icu.nothingless.pojo.adapter.iSTAdapter;
import icu.nothingless.pojo.adapter.iUserSTOAdapter;
import icu.nothingless.pojo.bean.UserSTO;
import icu.nothingless.pojo.commons.RespEntity;
import icu.nothingless.pojo.engine.BaseEngine;
import icu.nothingless.pojo.engine.UserSTOEngine;
import icu.nothingless.service.interfaces.iUserService;

public class UserServiceImpl implements iUserService<iUserSTOAdapter>{
    private static final iUserDao userDao = new UserDaoImpl();

    @Override
    public RespEntity<iUserSTOAdapter> doLogin(iUserSTOAdapter target) {
        /**
         * 
         * 默认传入的target对象有以下属性：
         *  UserAccount 账号
         *  Password    密码
         *  LastLoginTime   登录时间
         *  LastLoginIpAddr 登录地址
        */
        
        if(target == null || Objects.isNull(target.getUserAccount()) || Objects.isNull(target.getUserAccount()) ){
            // 传空的对象/内容
            return RespEntity.badRequest("illegal target");
        }

        iUserSTOAdapter result = userDao.findByUsername(target.getUserAccount());
        if(result == null){
            // 未找到对应账号
            return RespEntity.unauthorized("your account or password are not correct");
        }
        if(!Objects.equals(target.getUserPasswd(),result.getUserPasswd())){
            // 密码不一致
            return RespEntity.unauthorized("your account or password are not correct");
        }else{// 密码一致

            // 更新登录信息（通过主键更新登录时间和登录地址）
            target.setUserId(result.getUserId());
            // 隐藏密码
            target.setUserPasswd(null);
            result.setUserPasswd(null);

            if(userDao.save(target)){
                return RespEntity.success(result);
            }else{
                return RespEntity.error("Login failed");
            }
        }
    }

    @Override
    public RespEntity<iUserSTOAdapter> doRegister(iUserSTOAdapter target) {
        
        throw new UnsupportedOperationException("Unimplemented method 'doRegister'");
    }

}
