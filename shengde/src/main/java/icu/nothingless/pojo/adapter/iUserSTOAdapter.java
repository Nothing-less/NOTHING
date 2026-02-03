package icu.nothingless.pojo.adapter;

import java.util.List;

import icu.nothingless.pojo.bean.UserSTO;
import icu.nothingless.pojo.engine.UserSTOEngine;

public interface iUserSTOAdapter extends iSTAdapter<UserSTO> {
    @Override
    default int save() {
        UserSTOEngine.getInstance().save((UserSTO) this);
        return 0;
    }

    @Override
    default int delete() {
        UserSTOEngine.getInstance().delete((UserSTO) this);
        return 0;
    }

    @Override
    default List<UserSTO> query(UserSTO bean) {
        return UserSTOEngine.getInstance().query(bean);
    }

}
