package icu.nothingless.pojo.engine;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

import icu.nothingless.exceptions.EngineException;
import icu.nothingless.pojo.adapter.IPageItemAdpter;
import icu.nothingless.tools.PDBUtil;

public class PageItemEngine extends BaseEngine<IPageItemAdpter, PageItemEngine> {

    private static final String PAGE_ID = "PAGE_ID";
    private static final String PAGE_LINK = "PAGE_LINK";
    private static final String PAGE_NAME = "PAGE_NAME";
    private static final String PAGE_ORDER = "PAGE_ORDER";
    private static final String PARENT = "PARENT";
    private static final String PAGE_STATUS = "PAGE_STATUS";
    /* ---------------------------------------------------------------------- */
    private static final String TABLE_NAME = "PAGES";

    @Override
    public List<IPageItemAdpter> query(IPageItemAdpter bean) throws Exception {
        List<IPageItemAdpter> results = new ArrayList<>();

        Map<String, Object> beanMap = toMap(bean);
        beanMap.remove(PAGE_STATUS);

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ").append(TABLE_NAME).append(" WHERE PAGE_STATUS = TRUE");
        beanMap.forEach((key, value) -> {
            sql.append(" AND ").append(key).append(" LIKE ? ");
        });
        try {
            Object[] params = beanMap.values().stream()
                    .map(v -> v == null ? null : "%" + v.toString() + "%")
                    .toArray();
            List<Map<String, Object>> queryResults = PDBUtil.executeQuery(sql.toString(), params);
            queryResults.forEach(row -> {
                IPageItemAdpter resultBean;
                try {
                    resultBean = toBean(row);
                    results.add(resultBean);
                } catch (Exception e) {
                    LoggerFactory.getLogger(PageItemEngine.class)
                            .error("Error occurred while executing function <toBean> in function <query>: ", e);
                }
            });
            return results;
        } catch (Exception e) {
            throw new EngineException("Error occurred while executing function <fuzzyQuery> : ", e);
        }

    }

    public IPageItemAdpter toBean(Map<String, Object> map) throws Exception {
        if (map == null || map.isEmpty()) {
            throw new EngineException("Function <toBean> entering null");
        }

        return new icu.nothingless.pojo.bean.PageItemBean(map.get(PAGE_ID).toString(),
                map.get(PAGE_LINK).toString(),
                map.get(PAGE_NAME).toString(),
                map.get(PAGE_ORDER).toString(),
                map.get(PARENT).toString(),
                Boolean.valueOf(map.get(PAGE_STATUS).toString()));
    }

    public Map<String, Object> toMap(IPageItemAdpter bean) throws Exception {
        var map = new LinkedHashMap<String, Object>(6);
        if (bean == null) {
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
        } catch (Exception e) {
            throw new EngineException("Error occurred while executing function <toMap> : ", e);
        }

    }

    @Override
    public Long save(IPageItemAdpter bean) throws Exception {
        if (bean == null) {
            throw new EngineException("Function <save> null entering");
        }
        Map<String, Object> beanMap = toMap(bean);
        if (beanMap == null || beanMap.isEmpty()) {
            throw new EngineException("Function <save> empty entering");
        }

        StringBuilder sql = new StringBuilder();
        StringBuilder placeholders = new StringBuilder();
        List<Object> paramsList = new ArrayList<>();
        sql.append("INSERT INTO ").append(TABLE_NAME).append(" (");
        beanMap.forEach((key, value) -> {
            sql.append(key).append(", ");
            placeholders.append("?, ");
            paramsList.add(value);
        });
        // 移除最后的逗号和空格
        sql.setLength(sql.length() - 2);
        placeholders.setLength(placeholders.length() - 2);
        sql.append(") VALUES (").append(placeholders).append(")");

        try {
            return Long.valueOf("" + PDBUtil.executeUpdate(sql.toString(), paramsList.toArray()));
        } catch (Exception e) {
            throw new EngineException("Error occurred while executing function <save> : ", e);
        }
    }

    @Override
    public Long delete(IPageItemAdpter bean) throws Exception {
        if (bean == null) {
            throw new EngineException("Function <delete> null entering");
        }
        Map<String, Object> beanMap = toMap(bean);
        if (beanMap == null || beanMap.isEmpty()) {
            throw new EngineException("Function <delete> empty entering");
        }

        StringBuilder sql = new StringBuilder();
        List<Object> paramsList = new ArrayList<>();
        sql.append("UPDATE ").append(TABLE_NAME).append(" SET PAGE_STATUS = FALSE WHERE 1=1");
        beanMap.forEach((key, value) -> {
            sql.append(" AND ").append(key).append(" = ? ");
            paramsList.add(value);
        });

        try {
            return Long.valueOf("" + PDBUtil.executeUpdate(sql.toString(), paramsList.toArray()));
        } catch (Exception e) {
            throw new EngineException("Error occurred while executing function <delete> : ", e);
        }
    }

    @Override
    public Long update(IPageItemAdpter bean) throws Exception {
        if (bean == null) {
            throw new EngineException("Function <update> null entering");
        }
        Map<String, Object> beanMap = toMap(bean);
        if (beanMap == null || beanMap.isEmpty()) {
            throw new EngineException("Function <update> empty entering");
        }

        StringBuilder sql = new StringBuilder();
        List<Object> paramsList = new ArrayList<>();
        sql.append("UPDATE ").append(TABLE_NAME).append(" SET ");
        beanMap.forEach((key, value) -> {
            sql.append(key).append(" = ? , ");
            paramsList.add(value);
        });
        // 移除最后的逗号和空格
        sql.setLength(sql.length() - 2);
        sql.append(" WHERE 1=1");
        beanMap.forEach((key, value) -> {
            sql.append(" AND ").append(key).append(" = ? ");
            paramsList.add(value);
        });

        try {
            return Long.valueOf("" + PDBUtil.executeUpdate(sql.toString(), paramsList.toArray()));
        } catch (Exception e) {
            throw new EngineException("Error occurred while executing function <update> : ", e);
        }
    }

}
