package icu.nothingless.pojo.adapter;

import java.util.List;

import icu.nothingless.pojo.engine.BaseEngine;
import icu.nothingless.pojo.engine.PageItemEngine;

public interface iPageItemAdpter extends iSTAdapter<iPageItemAdpter> {

    @Override
    default Long save() throws Exception{
       return BaseEngine.getInstance(PageItemEngine.class).save(this);
    }

    @Override
    default Long delete() throws Exception{
        return BaseEngine.getInstance(PageItemEngine.class).delete(this);
    }

    @Override
    default List<iPageItemAdpter> query() throws Exception{
        return BaseEngine.getInstance(PageItemEngine.class).query(this);
    }

        public String page_id();
        public String page();
        public String page_name();
        public String page_order();
        public String parent();
        public Boolean page_status();


}