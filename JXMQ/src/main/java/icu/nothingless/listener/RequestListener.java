package icu.nothingless.listener;

import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;

public class RequestListener implements ServletRequestListener {
    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        // 每个请求进入时触发
    }

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        // 每个请求结束时触发
    }
}