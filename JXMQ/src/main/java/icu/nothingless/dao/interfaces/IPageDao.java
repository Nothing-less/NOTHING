package icu.nothingless.dao.interfaces;

import java.util.List;

import icu.nothingless.pojo.adapter.IPageItemAdpter;

public interface IPageDao<T extends IPageItemAdpter> {
    // get children pages form currentPageName
    List<T> getKidPages(String pageName) throws Exception;

    List<T> getKidPages(T page) throws Exception;

    // get parent page from pageName
    T getParentPage(String pageName) throws Exception;
}
