package icu.nothingless.dao.impl.LoginDaoImpl;

import java.util.Optional;

import icu.nothingless.dao.interfaces.iLoginDao;
import icu.nothingless.entity.LoginTMPBean;

public class LoginDaoImpl implements iLoginDao {
    @Override
    public boolean validateLogin(LoginTMPBean loginBean) {

        return true;
    }

    @Override
    public Optional<LoginTMPBean> findByUsername(String username) {

        throw new UnsupportedOperationException("Unimplemented method 'findByUsername'");
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