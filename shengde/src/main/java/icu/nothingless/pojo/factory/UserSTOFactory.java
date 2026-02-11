package icu.nothingless.pojo.factory;

import icu.nothingless.pojo.adapter.iUserSTOAdapter2;
import icu.nothingless.pojo.bean.UserSTO2;
import icu.nothingless.pojo.core.FrameworkRegistry;
import icu.nothingless.pojo.engine.GenericEngine;
import icu.nothingless.pojo.engine.iEngine;
import icu.nothingless.pojo.proxy.AdapterProxy;

import java.util.HashMap;
import java.util.Map;

/**
 * User 模块工厂 - 简化版，使用通用引擎
 */
public class UserSTOFactory implements iAdapterFactory<iUserSTOAdapter2> {
    
    private static final UserSTOFactory INSTANCE = new UserSTOFactory();
    private final iEngine<iUserSTOAdapter2> engine;
    
    private UserSTOFactory() {
        this.engine = createEngine();
    }
    
    public static UserSTOFactory getInstance() {
        return INSTANCE;
    }
    
    @Override
    public iUserSTOAdapter2 createAdapter() {
        return AdapterProxy.create(iUserSTOAdapter2.class, UserSTO2.class, engine);
    }
    
    @Override
    public iEngine<iUserSTOAdapter2> getEngine() {
        return engine;
    }
    
    @Override
    public void register() {
        FrameworkRegistry registry = FrameworkRegistry.getInstance();
        
        // 注册工厂
        registry.register(iUserSTOAdapter2.class, this);
        
        // 注册表名
        registry.registerTableName(iUserSTOAdapter2.class, "USERS");
        
        // 注册字段映射（Java字段名 -> 数据库列名）
        Map<String, String> columnMapping = new HashMap<>();
        columnMapping.put("userId", "USER_ID");
        columnMapping.put("userAccount", "USERACCOUNT");
        columnMapping.put("userPasswd", "USERPASSWD");
        columnMapping.put("nickname", "NICKNAME");
        columnMapping.put("userInfos", "USER_INFOS");
        columnMapping.put("registerTime", "REGISTER_TIME");
        columnMapping.put("lastLoginTime", "LAST_LOGIN_TIME");
        columnMapping.put("lastLoginIpAddr", "LAST_LOGIN_IP_ADDR");
        columnMapping.put("userStatus", "USER_STATUS");
        columnMapping.put("roleId", "ROLE_ID");
        columnMapping.put("userKey1", "USER_KEY1");
        columnMapping.put("userKey2", "USER_KEY2");
        columnMapping.put("userKey3", "USER_KEY3");
        columnMapping.put("userKey4", "USER_KEY4");
        columnMapping.put("userKey5", "USER_KEY5");
        columnMapping.put("userKey6", "USER_KEY6");
        
        registry.registerColumnMapping(iUserSTOAdapter2.class, columnMapping);
    }
    
    private iEngine<iUserSTOAdapter2> createEngine() {
        GenericEngine engine = new GenericEngine();
        engine.setTableName("USERS");
        engine.setPrimaryKey("userId");
        
        // 这里可以设置数据库连接，实际项目中从连接池获取
        // engine.setConnection(dataSource.getConnection());
        
        return engine;
    }
}