package icu.nothingless.service.interfaces;

import icu.nothingless.pojo.commons.RespEntity;

public interface iUserService<T> {
    
    public RespEntity<T> doLogin(T target);
    public RespEntity<T> doRegister(T target);

}
