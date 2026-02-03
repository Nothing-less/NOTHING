package icu.nothingless.pojo.adapter;

import java.util.List;

public interface iSTAdapter<T> {

    // 查询
    List<T> query(T bean);

    // 插入或更新
    int save();

    // 删除 status -> false
    int delete();
}
