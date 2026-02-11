package icu.nothingless.dao.impl.LoginDaoImpl;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icu.nothingless.dao.interfaces.iUserDao;
import icu.nothingless.pojo.adapter.iSTAdapter;
import icu.nothingless.pojo.adapter.iUserSTOAdapter;
import icu.nothingless.pojo.bean.UserSTO;
import icu.nothingless.tools.PDBUtil;

public class UserDaoImpl implements iUserDao {
    private static final Logger logger = LoggerFactory.getLogger(UserDaoImpl.class);

    @Override
    public <T> T findByUsername(String username) {

        if (username == null || username.trim().isEmpty()) {
            logger.error("username is empty");
            return null;
        }
        iUserSTOAdapter tmp = new UserSTO();
        tmp.setUserAccount(username);

        List<iUserSTOAdapter> results = tmp.query();
        if (results == null || results.isEmpty()) {
            logger.error(" not found the exact user account");
            return null;
        }
        for (iUserSTOAdapter one : results) {
            if (one != null && username.equals(one.getUserAccount())) {
                logger.info("successfully found the user account");
                return (T) one;
            }
        }
        logger.info("user not found : "+username);
        return null;
    }

    @Override
    public Boolean doLogin(iUserSTOAdapter login) {
        if(login == null) return false;
        if(login.getUserId() == null || Objects.equals("", login.getUserId())){
            logger.error("user ID is empty");
           return false; 
        }
        if(login.getLastLoginTime() == null || Objects.equals("", login.getLastLoginTime())){
            logger.error("can not get last login time");
           return false; 
        }
        if(login.getLastLoginIpAddr() == null || Objects.equals("", login.getLastLoginIpAddr())){
            logger.error("can not get last login IP");
           return false; 
        }
        iUserSTOAdapter tmp = new UserSTO();
        tmp.setUserId(login.getUserId());
        tmp.setLastLoginTime(login.getLastLoginTime());
        tmp.setLastLoginIpAddr(login.getLastLoginIpAddr());
        long result = -1L;
        result = tmp.save();
        if(result > 0L){
            logger.info("successfully saved the new login messages");
            return true;
        }
        logger.info("failed to save the new login messages");
        logger.info("Last Login Time :<{}> ",login.getLastLoginTime());
        logger.info("Last Login IP   : "+login.getLastLoginIpAddr());
        return false;
    }

    @Override
    public Boolean doRegister(iUserSTOAdapter register) {
        
        return false;
    }

    @Override
    public Boolean updatePwd(String username, String newPassword) {

        throw new UnsupportedOperationException("Unimplemented method 'updatePassword'");
    }

}