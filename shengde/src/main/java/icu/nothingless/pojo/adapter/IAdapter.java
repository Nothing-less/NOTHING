package icu.nothingless.pojo.adapter;

import java.util.List;

public interface IAdapter<T> {

    // 查询
    List<T> query() throws Exception;

    // 插入
    Long save() throws Exception;

    // 更新
    Long update() throws Exception;

    // 删除 status -> false
    Long delete() throws Exception;
}
