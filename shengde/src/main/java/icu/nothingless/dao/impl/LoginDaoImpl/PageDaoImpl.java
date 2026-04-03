package icu.nothingless.dao.impl.LoginDaoImpl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icu.nothingless.dao.interfaces.iPageDao;
import icu.nothingless.exceptions.PageItemException;
import icu.nothingless.pojo.adapter.iPageItemAdpter;
import icu.nothingless.pojo.bean.PageItem;

public class PageDaoImpl implements iPageDao<iPageItemAdpter> {
    private static final Logger logger = LoggerFactory.getLogger(PageDaoImpl.class);

    @Override
    public List<iPageItemAdpter> getKidPages(String pageName) throws Exception {
        iPageItemAdpter bean = new PageItem(
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
    public List<iPageItemAdpter> getKidPages(iPageItemAdpter page) throws Exception {
        try {
            return getKidPages(page.parent());
        } catch (Exception e) {
            logger.error("Error occurred in iPageDao.getKidPages(iPageItemAdpter page) : ", e);
            throw new PageItemException("Error occurred in iPageDao.getKidPages(iPageItemAdpter page) : ", e);
        }

    }

    @Override
    public iPageItemAdpter getParentPage(String pageName) {
        logger.error("");
        return null;
    }

}
