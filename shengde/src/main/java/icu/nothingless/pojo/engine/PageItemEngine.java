package icu.nothingless.pojo.engine;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

import icu.nothingless.exceptions.EngineException;
import icu.nothingless.pojo.adapter.iPageItemAdpter;
import icu.nothingless.pojo.bean.PageItem;
import icu.nothingless.tools.PDBUtil;


public class PageItemEngine extends BaseEngine<iPageItemAdpter, PageItemEngine> {

    private static final String PAGE_ID = "PAGE_ID";
    private static final String PAGE_LINK = "PAGE_LINK";
    private static final String PAGE_NAME = "PAGE_NAME";
    private static final String PAGE_ORDER = "PAGE_ORDER";
    private static final String PARENT = "PARENT";
    private static final String PAGE_STATUS = "PAGE_STATUS";
    /* ---------------------------------------------------------------------- */
    private static final String TABLENAME = "PAGES";


    @Override
    public List<iPageItemAdpter> query(iPageItemAdpter bean) throws Exception {
        List<iPageItemAdpter> results = new ArrayList<>();

        Map<String, Object> beanMap = toMap(bean);
        beanMap.remove(PAGE_STATUS);

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ").append(TABLENAME).append(" WHERE PAGE_STATUS = TRUE");
        beanMap.forEach((key, value) -> {
            sql.append(" AND ").append(key).append(" LIKE ? ");
        });
        try {
            Object[] params = beanMap.values().stream()
                    .map(v -> v == null ? null : "%" + v.toString() + "%")
                    .toArray();
            List<Map<String, Object>> queryResults = PDBUtil.executeQuery(sql.toString(), params);
            queryResults.forEach(row -> {
                iPageItemAdpter resultBean;
                try {
                    resultBean = toBean(row);
                    results.add(resultBean);
                }catch(Exception e){
                    LoggerFactory.getLogger(PageItemEngine.class).error("Error occurred while executing function <toBean> in function <query>: ", e);
                }
            });
            return results;
        } catch (Exception e) {
            throw new EngineException("Error occurred while executing function <fuzzyQuery> : ", e);
        }

    }

    private iPageItemAdpter toBean(Map<String,Object> map) throws Exception {
        if (map == null || map.isEmpty()){
            throw new EngineException("Function <toBean> entering null");
        }

        return new PageItem(map.get(PAGE_ID).toString(), 
                            map.get(PAGE_LINK).toString(),
                            map.get(PAGE_NAME).toString(),
                            map.get(PAGE_ORDER).toString(),
                            map.get(PARENT).toString(),
                            Boolean.valueOf(map.get(PAGE_STATUS).toString()));
    }

    private Map<String, Object> toMap(iPageItemAdpter bean) throws Exception{
        var map = new LinkedHashMap<String, Object>(6);
        if (bean == null){
            throw new EngineException("Function <toMap> entering null");
        }
        String s;
        Object o;
        try {
            if ((s = bean.page_id()) != null && !s.isBlank())
                map.put(PAGE_ID, s);
            if ((s = bean.page_link()) != null && !s.isBlank())
                map.put(PAGE_LINK, s);
            if ((s = bean.page_name()) != null && !s.isBlank())
                map.put(PAGE_NAME, s);
            if ((s = bean.page_order()) != null && !s.isBlank())
                map.put(PAGE_ORDER, s);
            if ((s = bean.parent()) != null && !s.isBlank())
                map.put(PARENT, s);
            if ((o = bean.page_status()) != null)
                map.put(PAGE_STATUS, o);
            return map; 
        }catch (Exception e) {
            throw new EngineException("Error occurred while executing function <toMap> : ", e);
        }

    }

    @Override
    public Long save(iPageItemAdpter bean) throws Exception {
         throw new EngineException("You Can't Storage New Pages");
    }

    @Override
    public Long delete(iPageItemAdpter bean) throws Exception {
         throw new EngineException("You Can't Delete Pages");
    }
}
