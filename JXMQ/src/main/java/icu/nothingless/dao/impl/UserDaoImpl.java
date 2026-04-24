package icu.nothingless.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icu.nothingless.commons.R;
import icu.nothingless.dao.interfaces.IUserDao;
import icu.nothingless.exceptions.UserSTOException;
import icu.nothingless.pojo.adapter.IUserAdapter;
import icu.nothingless.pojo.bean.UserBean;
import icu.nothingless.pojo.dto.User;
import icu.nothingless.tools.Fmt;

public class UserDaoImpl implements IUserDao<User> {
    private static final Logger logger = LoggerFactory.getLogger(UserDaoImpl.class);

    @Override
    public R findByUsername(String username) throws Exception {
        if (username == null || "".equals(username)) {
            return R.error("Empty UserName");
        }
        IUserAdapter tmp = new icu.nothingless.pojo.bean.UserBean();
        tmp.setUserAccount(username);
        try {
            List<IUserAdapter> results = tmp.query();
            if (results == null || results.isEmpty()) {
                logger.error("User({}) Not Found!", username);
                throw new UserSTOException(Fmt.of("User({}) Not Found!", username));
            }
            for (IUserAdapter one : results) {
                if (one != null && username.equals(one.getUserAccount())) {
                    logger.info("User({}) Found!", username);
                    return R.success(User.from(one));
                }
            }
            return R.error("");
        } catch (Exception e) {
            logger.error("Error occurred in iUserDao.findByUsername : ", e);
            throw new UserSTOException("Error occurred in iUserDao.findByUsername : ", e);
        }
    }

    @Override
    public R doSearch(String str)throws Exception{
                if (str == null || "".equals(str)) {
            return R.error("Empty Search");
        }
        IUserAdapter tmp1 = new icu.nothingless.pojo.bean.UserBean();
        tmp1.setUserAccount(str);
        IUserAdapter tmp2 = new icu.nothingless.pojo.bean.UserBean();
        tmp2.setNickname(str);
        try {
            List<IUserAdapter> results_1 = tmp1.query();
            List<IUserAdapter> results_2 = tmp2.query();
            List<IUserAdapter> results = new ArrayList<>(results_1);
            results.addAll(results_2);
            if (results == null || results.isEmpty()) {
                logger.error("Keyword({}) Not Found!", str);
                throw new UserSTOException(Fmt.of("Keyword({}) Not Found!", str));
            }
            List<User> ret = new ArrayList<>();
            for(IUserAdapter one: results){
                ret.add(User.from(one));
            }
            return R.success(ret);
        } catch (Exception e) {
            logger.error("Error occurred in iUserDao.findByUsername : ", e);
            throw new UserSTOException("Error occurred in iUserDao.findByUsername : ", e);
        }
    }

    @Override
    public R doLogin(User login_user) throws Exception {
        if (login_user == null) {
            logger.error("Empty User!");
            throw new UserSTOException("Empty User!");
        }
        R query = findByUsername(login_user.userAccount());
        if (!query.isSuccess()) {
            logger.error("User({}) Not Found", login_user.userAccount());
            throw new UserSTOException(Fmt.of("User({}) Not Found", login_user.userAccount()));
        }
        User queryResult = (User) query.data();
        if (!Objects.equals(queryResult.userPasswd(), login_user.userPasswd())) {
            logger.error("User({}) Login Failed!", login_user.userAccount());
            return R.error(Fmt.of("User({}) Login Failed!", login_user.userAccount()));
        }

        IUserAdapter tmp = new UserBean();
        tmp.setUserId(login_user.userId());
        tmp.setLastLoginTime(login_user.lastLoginTime());
        tmp.setLastLoginIpAddr(login_user.lastLoginIpAddr());
        tmp.setUserKey1(UserBean.STATUS_ONLINE);
        try {
            long result = tmp.save();
            if (result > 0L) {
                logger.info("User(ID:{}) Login!", tmp.getUserId());
                return R.success(login_user);
            }
            logger.info("User(ID:{}) Login Can't Update!", tmp.getUserId());
            return R.error(Fmt.of("User(ID:{}) Login Can't Update!", tmp.getUserId()));
        } catch (Exception e) {
            logger.error("User({}) Login Failed", login_user.userAccount());
            logger.error("Last Login Time :<{}> ", login_user.lastLoginTime());
            logger.error("Last Login IP :<{}> ", login_user.lastLoginIpAddr());
            logger.error("Error occurred in iUserDao.doLogin : ", e);
            throw new UserSTOException("Error occurred in iUserDao.doLogin : ", e);
        }

    }

    @Override
    public R doRegister(User register) throws Exception {
        if (register == null) {
            logger.error("Empty Register!");
            throw new UserSTOException("Empty Register!");
        }
        String username = Optional.ofNullable(register.userAccount())
                .map(Object::toString)
                .filter(s -> !s.trim().isEmpty())
                .orElse("");
        String password = Optional.ofNullable(register.userPasswd())
                .map(Object::toString)
                .filter(s -> !s.trim().isEmpty())
                .orElse("");
        String last_login_time = Optional.ofNullable(register.lastLoginTime())
                .map(Object::toString)
                .filter(s -> !s.trim().isEmpty())
                .orElse("");
        String last_login_ip = Optional.ofNullable(register.lastLoginIpAddr())
                .map(Object::toString)
                .filter(s -> !s.trim().isEmpty())
                .orElse("");
        if (Objects.equals("", last_login_ip)
                || Objects.equals("", last_login_time)
                || Objects.equals("", password)
                || Objects.equals("", username)) {
            throw new UserSTOException("register information are missing");
        }
        IUserAdapter tmp = new UserBean();
        tmp.setUserAccount(username);
        tmp.setUserPasswd(password);
        tmp.setLastLoginIpAddr(last_login_ip);
        tmp.setLastLoginTime(last_login_time);
        tmp.setRegisterTime(last_login_time);
        long result = tmp.save();
        if (result > 0L) {
            logger.info("User({}) Regisiter Successful!", username);
            return R.success(Fmt.of("User({}) Regisiter Successful!", username));
        }
        logger.error("User({}) Can't Regisiter!", username);
        return R.error(Fmt.of("User({}) Can't Regisiter!", username));
    }

    @Override
    public R doUpdate(User newTarget) throws Exception {
        // TODO doUpdate
        return null;
    }

    @Override
    public R doLogout(User currentUser) throws Exception {
        if (currentUser == null || currentUser.userId() == null || Objects.equals("", currentUser.userId())) {
            logger.error("Empty User!");
            throw new UserSTOException("Empty User!");
        }
        IUserAdapter tmp = new UserBean();
        tmp.setUserId(currentUser.userId());
        tmp.setUserKey1(UserBean.STATUS_OFFLINE);
        try {
            long result = tmp.save();
            if (result > 0L) {
                logger.info("User({}) Logout!", currentUser.userAccount());
                return R.success(Fmt.of("User({}) Logout!", currentUser.userAccount()));
            }
            logger.error("User({}) Can't Logout!", currentUser.userAccount());
            return R.error(Fmt.of("User({}) Can't Logout!", currentUser.userAccount()));
        } catch (Exception e) {
            logger.error("Error occurred in iUserDao.doLogout : ", e);
            throw new UserSTOException("Error occurred in iUserDao.doLogout : ", e);
        }
    }
}