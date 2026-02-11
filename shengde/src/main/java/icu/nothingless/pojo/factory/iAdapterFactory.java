package icu.nothingless.pojo.factory;

import icu.nothingless.pojo.adapter.iSTAdapter2;
import icu.nothingless.pojo.engine.iEngine;

/**
 * 抽象工厂接口
 */
public interface iAdapterFactory<T extends iSTAdapter2<T>> {
    
    /**
     * 创建新的适配器实例（实现接口实例化）
     */
    T createAdapter();
    
    /**
     * 获取对应的引擎
     */
    iEngine<T> getEngine();
    
    /**
     * 注册到框架
     */
    void register();
}