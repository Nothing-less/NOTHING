package icu.nothingless.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import icu.nothingless.tools.PDBUtil;

public class UserDaoImpl implements IUserDao<User> {
    private static final Logger logger = LoggerFactory.getLogger(UserDaoImpl.class);

    @Override
    public R findByUsername(String username) throws Exception {
        IUserAdapter tmp = new icu.nothingless.pojo.bean.UserBean();
        tmp.setUserAccount(username);
        try {
            List<IUserAdapter> results = tmp.query();
            if (results == null || results.isEmpty()) {
                logger.error("User({}) Not Found!", username);
                return new R<>(0, "User Not Found", Fmt.of("User({}) Not Found!", username), null);
            }
            for (IUserAdapter one : results) {
                if (one != null && username.equals(one.getUserAccount())) {
                    logger.info("User({}) Found!", username);
                    return R.success(User.from(one));
                }
            }
            return R.error(Fmt.of("User({}) Not Found!", username));
        } catch (Exception e) {
            logger.error("Error occurred in iUserDao.findByUsername : ", e);
            throw new UserSTOException("Error occurred in iUserDao.findByUsername : ", e);
        }
    }

    @Override
    public R doSearch(String str) throws Exception {
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
                return R.error(Fmt.of("Keyword({}) Not Found!", str));
            }
            List<User> ret = new ArrayList<>();
            for (IUserAdapter one : results) {
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
                return R.success(queryResult.withoutPasswd());
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
                .orElse(Fmt.getCurrentTime());
        String last_login_ip = Optional.ofNullable(register.lastLoginIpAddr())
                .map(Object::toString)
                .filter(s -> !s.trim().isEmpty())
                .orElse("192.168.0.1");
        if (Fmt.isAnyEmpty(last_login_ip, last_login_time, password, username)) {
            throw new UserSTOException("register information are missing");
        }
        IUserAdapter tmp = new UserBean();
        tmp.setUserAccount(username);
        tmp.setUserPasswd(password);
        tmp.setLastLoginIpAddr(last_login_ip);
        tmp.setLastLoginTime(last_login_time);
        tmp.setRegisterTime(Fmt.getCurrentTime());
        if (!Fmt.isEmpty(register.roleId())) {
            tmp.setRoleId(register.roleId());
        }
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

        IUserAdapter tmp = new UserBean();
        tmp.setUserId(newTarget.userId());

        /*
         * if(!Fmt.isEmpty(newTarget.userAccount())){
         * tmp.setUserAccount(newTarget.userAccount());
         * }
         * if(!Fmt.isEmpty(newTarget.userPasswd())){
         * tmp.setUserPasswd(newTarget.userPasswd());
         * }
         * 
         */

        if (!Fmt.isEmpty(newTarget.nickname())) {
            tmp.setNickname(newTarget.nickname());
        }
        if (!Fmt.isEmpty(newTarget.userInfos())) {
            tmp.setUserInfos(newTarget.userInfos());
        }
        if (!Fmt.isEmpty(newTarget.userKey2())) {
            tmp.setUserKey2(newTarget.userKey2()); // userKey2 is used for avatar URL
        }
        /*
         * if (!Fmt.isEmpty(newTarget.userKey3())) {
         * tmp.setUserKey3(newTarget.userKey3());
         * }
         * if (!Fmt.isEmpty(newTarget.userKey4())) {
         * tmp.setUserKey4(newTarget.userKey4());
         * }
         * if (!Fmt.isEmpty(newTarget.userKey5())) {
         * tmp.setUserKey5(newTarget.userKey5());
         * }
         * if (!Fmt.isEmpty(newTarget.userKey6())) {
         * tmp.setUserKey6(newTarget.userKey6());
         * }
         * 
         */
        if (Fmt.isAllEmpty(
                newTarget.userAccount(),
                newTarget.userPasswd(),
                newTarget.nickname(),
                newTarget.userInfos(),
                newTarget.lastLoginTime(),
                newTarget.lastLoginIpAddr(),
                newTarget.registerTime(),
                newTarget.roleId(),
                newTarget.userKey1(),
                newTarget.userKey2(),
                newTarget.userKey3(),
                newTarget.userKey4(),
                newTarget.userKey5(),
                newTarget.userKey6())) {
            return R.error("No fields to update");
        }
        try {
            long result = tmp.save();
            if (result > 0L) {
                logger.info("User({}) Update Successful!", newTarget.userId());
                IUserAdapter updatedUser = new UserBean();
                updatedUser.setUserId(tmp.getUserId());
                IUserAdapter fetchedUser = updatedUser.query().stream()
                        .filter(user -> Objects.equals(user.getUserId(), tmp.getUserId()))
                        .findFirst()
                        .orElse(null);

                return R.success(User.from(fetchedUser));
            }
            logger.error("User({}) Can't Update!", newTarget.userId());
            return R.error(Fmt.of("User({}) Can't Update!", newTarget.userId()));
        } catch (Exception e) {
            logger.error("Error occurred in iUserDao.doUpdate : ", e);
            throw new UserSTOException("Error occurred in iUserDao.doUpdate : ", e);
        }
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

    @Override
    public R doLogoutForAll() throws Exception {

        try {
            String sql = "SELECT * FROM users WHERE 1=1 AND user_status = ? AND user_key1 = ? ";
            List<Map<String, Object>> results = PDBUtil.executeQuery(sql, UserBean.STATUS_ACTIVE,
                    UserBean.STATUS_ONLINE);
            List<Map<String, String>> ret = new ArrayList<>();
            for (Map<String, Object> one : results) {
                ret.add(Map.of(
                        "userId", String.valueOf(one.get("USER_ID")),
                        "userAccount", String.valueOf(one.get("USER_ACCOUNT"))));
            }
            String sql_updateStatus = "UPDATE users SET user_key1 = ? WHERE 1=1 AND user_status = ?  AND user_key1 = ? ";
            PDBUtil.executeUpdate(sql_updateStatus, UserBean.STATUS_OFFLINE, UserBean.STATUS_ACTIVE,
                    UserBean.STATUS_ONLINE);

            return R.success(ret);
        } catch (Exception e) {
            logger.error("Error occurred in iUserDao.queryAll : ", e);
            throw new UserSTOException("Error occurred in iUserDao.queryAll : ", e);
        }
    }
}