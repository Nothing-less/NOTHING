package icu.nothingless.service.interfaces;

import java.util.List;

import icu.nothingless.commons.RespEntity;

public interface IUserService<T> {
    
    public RespEntity<T> doLogin(T target);
    public RespEntity<T> doRegister(T target);
    public RespEntity<List<T>> doSearch(T target);
    public RespEntity<T> doUpdate(T target);
    public RespEntity<T> doLogout(T target);

}
