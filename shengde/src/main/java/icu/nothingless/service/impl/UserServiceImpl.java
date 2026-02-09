package icu.nothingless.service.impl;

import java.util.List;
import java.util.Objects;

import icu.nothingless.pojo.adapter.iSTAdapter;
import icu.nothingless.pojo.adapter.iUserSTOAdapter;
import icu.nothingless.pojo.bean.UserSTO;
import icu.nothingless.pojo.commons.RespEntity;
import icu.nothingless.pojo.engine.BaseEngine;
import icu.nothingless.pojo.engine.UserSTOEngine;
import icu.nothingless.service.interfaces.iUserService;

public class UserServiceImpl implements iUserService<iUserSTOAdapter>{

    @Override
    public RespEntity<iUserSTOAdapter> doLogin(iUserSTOAdapter target) {
        /**
         * 
         * 默认传入的target对象有以下属性：
         *  UserAccount
         *  Password
         *  LastLoginTime
         *  LastLoginIpAddr
        */
        
        if(target == null || Objects.isNull(target.getUserAccount()) || Objects.isNull(target.getUserAccount()) ){
            // 传空的对象/内容
            return RespEntity.badRequest("illegal target");
        }

        iUserSTOAdapter result = queryOne(target.getUserAccount());
        if(result == null){
            // 未找到对应账号
            return RespEntity.unauthorized("your account or assword are not correct");
        }
        if(!Objects.equals(target.getUserPasswd(),result.getUserPasswd())){
            // 密码不一致
            return RespEntity.unauthorized("your account or assword are not correct");
        }else{// 密码一致
            // 更新登录信息
            target.setUserId(result.getUserId()); // 主键
            // 删除其他不应该更新的信息
            target.setUserPasswd(null); 
            target.save();
            return RespEntity.success(result);
        }
    }

    @Override
    public RespEntity<iUserSTOAdapter> doRegister(iUserSTOAdapter target) {
        
        throw new UnsupportedOperationException("Unimplemented method 'doRegister'");
    }

    private iUserSTOAdapter queryOne(String userAccount){
        iUserSTOAdapter tmp = new UserSTO();
        tmp.setUserAccount(userAccount);
        List<iUserSTOAdapter> results = tmp.query();
        if (results == null || results.isEmpty()) {
            return null;
        }
        return results.stream()
                .filter(v -> v != null && userAccount.equals(v.getUserAccount())) // 精确匹配
                .findFirst()
                .orElse(null);
    }
}
