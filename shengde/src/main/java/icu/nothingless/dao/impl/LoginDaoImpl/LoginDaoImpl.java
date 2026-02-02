package icu.nothingless.dao.impl.LoginDaoImpl;

import java.sql.SQLException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icu.nothingless.dao.interfaces.iLoginDao;
import icu.nothingless.entity.LoginTMPBean;
import icu.nothingless.tools.PDBUtil;

public class LoginDaoImpl implements iLoginDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginDaoImpl.class);

    @Override
    public boolean validateLogin(LoginTMPBean loginBean) {

        return true;
    }

    @Override
    public Optional<LoginTMPBean> findByUsername(String username) {
        String sql = "SELECT * FROM t_user WHERE username = ?";
        try {
            LoginTMPBean temp = PDBUtil.queryForObject(sql, LoginTMPBean.class, username);
            LOGGER.debug("" + temp);

        } catch (SQLException e) {
            LOGGER.error("Error finding user by username: {}", username, e);
        }

        return null;
    }

    @Override
    public boolean save(LoginTMPBean login) {

        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    public boolean updatePassword(String username, String newPassword) {

        throw new UnsupportedOperationException("Unimplemented method 'updatePassword'");
    }

}