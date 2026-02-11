package icu.nothingless.service.interfaces;

import icu.nothingless.pojo.adapter.iSTAdapter;
import icu.nothingless.pojo.adapter.iUserSTOAdapter;
import icu.nothingless.pojo.commons.RespEntity;
import icu.nothingless.pojo.engine.BaseEngine;

public interface iUserService<T> {
    
    public RespEntity<T> doLogin(T target);
    public RespEntity<T> doRegister(T target);

}
