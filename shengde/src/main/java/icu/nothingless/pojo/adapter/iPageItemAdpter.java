package icu.nothingless.pojo.adapter;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import icu.nothingless.pojo.bean.PageItem;
import icu.nothingless.pojo.engine.BaseEngine;
import icu.nothingless.pojo.engine.PageItemEngine;

@JsonDeserialize(as = PageItem.class)
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
        public String page_link();
        public String page_name();
        public String page_order();
        public String parent();
        public Boolean page_status();


}