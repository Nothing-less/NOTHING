package icu.nothingless.service.impl;

import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icu.nothingless.dao.interfaces.iPageDao;
import icu.nothingless.pojo.adapter.iPageItemAdpter;
import icu.nothingless.service.interfaces.iPageService;
import icu.nothingless.tools.ServiceFactory;

public class PageServiceImpl implements iPageService {
    private static final Logger logger = LoggerFactory.getLogger(PageServiceImpl.class);
    private static final iPageDao<iPageItemAdpter> pageDao = ServiceFactory.getSingleton(iPageDao.class);

    @Override
    public Set<Map<String,String>> getPages(String pageName) {
        try {
            var pageList = pageDao.getKidPages(pageName);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
       return null;

    }
}
