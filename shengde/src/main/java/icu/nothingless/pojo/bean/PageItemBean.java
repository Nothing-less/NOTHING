package icu.nothingless.pojo.bean;

import icu.nothingless.pojo.adapter.IPageItemAdpter;

public record PageItemBean(
        String page_id,
        String page_link,
        String page_name,
        String page_order,
        String parent,
        Boolean page_status) implements java.io.Serializable, IPageItemAdpter {
                
}
