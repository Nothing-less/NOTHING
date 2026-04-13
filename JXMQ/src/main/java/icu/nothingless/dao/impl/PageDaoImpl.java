package icu.nothingless.dao.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icu.nothingless.dao.interfaces.IPageDao;
import icu.nothingless.exceptions.PageItemException;
import icu.nothingless.pojo.adapter.IPageItemAdpter;
import icu.nothingless.pojo.bean.PageItemBean;

public class PageDaoImpl implements IPageDao<IPageItemAdpter> {
    private static final Logger logger = LoggerFactory.getLogger(PageDaoImpl.class);

    @Override
    public List<IPageItemAdpter> getKidPages(String pageName) throws Exception {
        IPageItemAdpter bean = new PageItemBean(
                null,
                null,
                null,
                null,
                pageName,
                true);
        try {
            return bean.query();
        } catch (Exception e) {
            logger.error("Error occurred in iPageDao.getKidPages(String pageName) : ", e);
            throw new PageItemException("Error occurred in iPageDao.getKidPages(String pageName) : ", e);
        }
    }

    @Override
    public List<IPageItemAdpter> getKidPages(IPageItemAdpter page) throws Exception {
        try {
            return getKidPages(page.parent());
        } catch (Exception e) {
            logger.error("Error occurred in iPageDao.getKidPages(iPageItemAdpter page) : ", e);
            throw new PageItemException("Error occurred in iPageDao.getKidPages(iPageItemAdpter page) : ", e);
        }

    }

    @Override
    public IPageItemAdpter getParentPage(String pageName) {
        logger.error("");
        return null;
    }

}
