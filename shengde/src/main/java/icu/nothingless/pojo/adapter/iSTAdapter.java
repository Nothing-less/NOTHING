package icu.nothingless.pojo.adapter;

import java.util.List;

public interface iSTAdapter<T> {

    // 查询
    List<T> query();

    // 插入或更新
    Long save();

    // 删除 status -> false
    Long delete();
}
