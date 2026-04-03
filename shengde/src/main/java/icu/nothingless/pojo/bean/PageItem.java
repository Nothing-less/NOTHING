package icu.nothingless.pojo.bean;

import icu.nothingless.pojo.adapter.iPageItemAdpter;

public record PageItem(
        String page_id,
        String page_link,
        String page_name,
        String page_order,
        String parent,
        Boolean page_status) implements iPageItemAdpter {
                
}
