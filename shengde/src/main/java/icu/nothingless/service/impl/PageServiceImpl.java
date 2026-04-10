package icu.nothingless.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icu.nothingless.dao.interfaces.IPageDao;
import icu.nothingless.pojo.adapter.IPageItemAdpter;
import icu.nothingless.service.interfaces.IPageService;
import icu.nothingless.tools.ServiceFactory;

public class PageServiceImpl implements IPageService {
    private static final Logger logger = LoggerFactory.getLogger(PageServiceImpl.class);
    private static final IPageDao<IPageItemAdpter> pageDao = ServiceFactory.createInstance(IPageDao.class,
            "CachePageDaoImpl");

    @Override
    public Set<Map<String, String>> getPages(String pageName) {
        Set<Map<String, String>> resulSet = new java.util.HashSet<>();
        try {
            List pageList = pageDao.getKidPages(pageName);
            if (pageList == null || pageList.isEmpty()) {
                logger.warn("No pages found for pageName={}", pageName);
                return Set.of();
            } else {
                logger.error("Actual type: {}, class: {}", pageList, pageList.getClass().getName());
                for (Object item : pageList) {
                    if (item instanceof IPageItemAdpter) {
                        IPageItemAdpter pageItem = (IPageItemAdpter) item;
                         Map<String, String> pageMap = new HashMap<>();
                                pageMap.put("page_id", pageItem.page_id());
                                pageMap.put("page_link", pageItem.page_link());
                                pageMap.put("page_name", pageItem.page_name());
                                pageMap.put("page_order", pageItem.page_order());
                                pageMap.put("parent", pageItem.parent());
                                resulSet.add(pageMap);
                    } else if(item instanceof Map) {
                        Map<String, String> pageMap = (Map<String, String>) item;
                        resulSet.add(pageMap);
                    } else {
                        logger.warn("Unexpected item type: {}, value: {}", item.getClass().getName(), item);
                    }
                }
                return resulSet;
            }

        } catch (Exception e) {
            logger.error("Error occurred while executing function <getPages>: ", e);
        }
        return Set.of();
    }
}
