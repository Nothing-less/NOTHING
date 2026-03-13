package icu.nothingless.service.impl;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icu.nothingless.service.interfaces.iPageService;
import icu.nothingless.tools.PDBUtil;

public class PageServiceImpl implements iPageService {
    private static final Logger logger = LoggerFactory.getLogger(PageServiceImpl.class);

    @Override
    public Set<Map<String,String>> getPages(String currentPage) {
        String sql = "SELECT page, page_name FROM pages WHERE status = 1 AND parent = ?";
        /*
        
        try {
            List<Map<String, Object>> retList = PDBUtil.executeQuery(sql, currentPage);
            return retList.stream()
                    .map(row -> String.valueOf(""+row.get("PAGE")))
                    .filter(s -> !s.isEmpty() && !"null".equals(s))
                    .collect(Collectors.toSet());
        } catch (SQLException e) {
            logger.error("Failed Execute SQL: {}, Params: {}", sql, currentPage, e);
            return null;
        }
        */
       return null;

    }
}
