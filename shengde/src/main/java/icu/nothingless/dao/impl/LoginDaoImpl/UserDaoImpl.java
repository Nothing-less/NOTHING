package icu.nothingless.dao.impl.LoginDaoImpl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icu.nothingless.dao.interfaces.iUserDao;
import icu.nothingless.exceptions.UserSTOException;
import icu.nothingless.pojo.adapter.iUserSTOAdapter;
import icu.nothingless.pojo.bean.UserSTO;

public class UserDaoImpl implements iUserDao {
    private static final Logger logger = LoggerFactory.getLogger(UserDaoImpl.class);

    @SuppressWarnings("unchecked")
    @Override
    public iUserSTOAdapter findByUsername(String username) throws Exception{

        if (username == null || username.trim().isEmpty()) {
            throw new UserSTOException("username is empty");
        }
        iUserSTOAdapter tmp = new UserSTO();
        tmp.setUserAccount(username);

        List<iUserSTOAdapter> results;
        try {
            results = tmp.query();
            if (results == null || results.isEmpty()) {
            throw new UserSTOException(" not found the exact user account");
            }
            for (iUserSTOAdapter one : results) {
                if (one != null && username.equals(one.getUserAccount())) {
                    logger.info("successfully found the user account");
                    return one;
                }
            }
        } catch (Exception e) {
            throw new UserSTOException("Error occurred in iUserDao.findByUsername : ",e);
        }

        logger.info("user not found : "+username);
        return null;
    }

    @Override
    public Boolean doLogin(iUserSTOAdapter login) throws Exception{
        if(login == null) return false;
        if(login.getUserId() == null || Objects.equals("", login.getUserId())){
            throw new UserSTOException("user ID is empty");

        }
        if(login.getLastLoginTime() == null || Objects.equals("", login.getLastLoginTime())){
            throw new UserSTOException("can not get last login time");

        }
        if(login.getLastLoginIpAddr() == null || Objects.equals("", login.getLastLoginIpAddr())){
            throw new UserSTOException("can not get last login IP");
        }
        iUserSTOAdapter tmp = new UserSTO();
        tmp.setUserId(login.getUserId());
        tmp.setLastLoginTime(login.getLastLoginTime());
        tmp.setLastLoginIpAddr(login.getLastLoginIpAddr());
        long result = -1L;
        try {
            result = tmp.save();
        } catch (Exception e) {
            throw new UserSTOException("Error occurred in iUserDao.doLogin : ",e);
        }
        if(result > 0L){
            logger.info("successfully saved the new login messages");
            return true;
        }
        logger.error("failed to update new login messages");
        logger.error("Last Login Time :<{}> ",login.getLastLoginTime());
        logger.error("Last Login IP   : "+login.getLastLoginIpAddr());
        return false;
    }

    @Override
    public Boolean doRegister(Map<String, String> register) throws Exception{
        if(register == null || register.size() == 0){
            throw new UserSTOException("register is empty");
        }
        String username = Optional.ofNullable(register.get("username"))
                                    .map(Object::toString)
                                    .filter(s -> !s.trim().isEmpty())
                                    .orElse("");
        String password = Optional.ofNullable(register.get("password"))
                                    .map(Object::toString)
                                    .filter(s -> !s.trim().isEmpty())
                                    .orElse("");
        String last_login_time = Optional.ofNullable(register.get("last_login_time"))
                                    .map(Object::toString)
                                    .filter(s -> !s.trim().isEmpty())
                                    .orElse("");
        String last_login_ip = Optional.ofNullable(register.get("last_login_ip"))
                                    .map(Object::toString)
                                    .filter(s -> !s.trim().isEmpty())
                                    .orElse("");
        if(
            Objects.equals("", last_login_ip) 
            || Objects.equals("", last_login_time)
            || Objects.equals("", password) 
            || Objects.equals("", username)
        ){
            throw new UserSTOException("register information are missing");
        }
        iUserSTOAdapter tmp = new UserSTO();
        tmp.setUserAccount(username);
        tmp.setUserPasswd(password);
        tmp.setLastLoginIpAddr(last_login_ip);
        tmp.setLastLoginTime(last_login_time);
        tmp.setRegisterTime(last_login_time);
        Long result = tmp.save();
        if(result > 0L){
            logger.info("register successfully!");
            return true;
        }
        logger.error("failed to regisiter");
        return false;
    }

    @Override
    public Boolean updatePwd(String username, String newPassword) throws Exception{

        throw new UnsupportedOperationException("Unimplemented method 'updatePassword'");
    }

}