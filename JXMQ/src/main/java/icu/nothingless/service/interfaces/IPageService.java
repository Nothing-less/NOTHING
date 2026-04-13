package icu.nothingless.service.interfaces;

import java.util.Map;
import java.util.Set;

public interface IPageService {
    public Set<Map<String,String>> getPages(String pageName);
}
