package icu.nothingless.controller.user;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import icu.nothingless.config.GlobalConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 头像图片访问 Servlet
 * URL: /user/avatar/*
 */
@WebServlet("/user/avatar/*")
public class AvatarAccessServlet extends HttpServlet {

    // 和 UserAvatarServlet 完全一致
    private static final String AVATAR_DIR = GlobalConfig.CONFIG_MAP.get("avatar.dir");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String fileName = pathInfo.substring(1);
        Path filePath = Paths.get(AVATAR_DIR, fileName);

        File file = filePath.toFile();
        if (!file.exists() || !file.isFile()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String fileNameLower = fileName.toLowerCase();
        if (fileNameLower.endsWith(".png")) {
            resp.setContentType("image/png");
        } else if (fileNameLower.endsWith(".jpg") || fileNameLower.endsWith(".jpeg")) {
            resp.setContentType("image/jpeg");
        } else if (fileNameLower.endsWith(".gif")) {
            resp.setContentType("image/gif");
        } else {
            resp.setContentType("application/octet-stream");
        }

        Files.copy(filePath, resp.getOutputStream());
        resp.getOutputStream().flush();
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }
}
