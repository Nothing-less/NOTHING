package icu.nothingless.controller.user;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icu.nothingless.commons.RespEntity;
import icu.nothingless.config.GlobalConfig;
import icu.nothingless.config.GlobalParams;
import icu.nothingless.pojo.dto.User;
import icu.nothingless.service.interfaces.IUserService;
import icu.nothingless.tools.JsonUtil;
import icu.nothingless.tools.RedirectUtil;
import icu.nothingless.tools.ServiceFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

/**
 * 用户头像上传 API
 */
@WebServlet("/upload/avatar")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 20 * 1024 * 1024, maxRequestSize = 25 * 1024 * 1024)
public class UserAvatarServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(UserAvatarServlet.class);
    private final IUserService<User> userService = ServiceFactory.getSingleton(IUserService.class);

    // 外部固定目录，避免重启丢失
    private static final String AVATAR_DIR;
    private static final String AVATAR_URL_PREFIX = "/user/avatar/";

    static {
        // 自动检测操作系统，选择合适的路径
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            // Windows
            AVATAR_DIR = GlobalConfig.CONFIG_MAP.get("avatar.dir");
        } else {
            // Linux/Mac: /data/MuSong/upload/avatar 或临时目录
            String dataDir = "/data/MuSong/upload/avatar/";
            File dir = new File(dataDir);
            if (dir.exists() || dir.mkdirs()) {
                AVATAR_DIR = dataDir;
            } else {
                // fallback 到临时目录
                AVATAR_DIR = System.getProperty("java.io.tmpdir") + "/MuSong/avatar/";
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null) {
            writeJson(resp, RespEntity.unauthorized("未登录"));
            return;
        }

        User currentUser = (User) icu.nothingless.tools.RedirectUtil.getFlash(req, GlobalParams.CURRENT_USER);
        if (currentUser == null) {
            writeJson(resp, RespEntity.unauthorized("未登录"));
            return;
        }

        try {
            Part filePart = req.getPart("avatar");
            if (filePart == null || filePart.getSize() == 0) {
                writeJson(resp, RespEntity.badRequest("请选择图片文件"));
                return;
            }

            String contentType = filePart.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                writeJson(resp, RespEntity.badRequest("只能上传图片文件"));
                return;
            }

            // 使用外部固定目录
            File uploadDir = new File(AVATAR_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            String originalName = filePart.getSubmittedFileName();
            String extension = getExtension(originalName);
            String fileName = UUID.randomUUID().toString().replace("-", "") + extension;

            Path filePath = Paths.get(AVATAR_DIR, fileName);
            Files.copy(filePart.getInputStream(), filePath);

            // 构建访问 URL
            String avatarUrl = req.getContextPath() + AVATAR_URL_PREFIX + fileName;

            // 更新数据库
            User updateUser = User.builder()
                    .userId(currentUser.userId())
                    .userKey2(avatarUrl)
                    .build();
            var result = userService.doUpdate(updateUser);

            if (result.isSuccess() && result.getData() != null) {
                User freshUser = result.getData();
                session.setAttribute(RedirectUtil.PREFIX + GlobalParams.CURRENT_USER, freshUser);
                writeJson(resp, RespEntity.success(avatarUrl));
            } else {
                Files.deleteIfExists(filePath);
                writeJson(resp, RespEntity.error("头像保存失败"));
            }

        } catch (Exception e) {
            logger.error("Upload avatar failed for user [{}]", currentUser.userId(), e);
            writeJson(resp, RespEntity.error("上传失败: " + e.getMessage()));
        }
    }

    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return ".jpg";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    private void writeJson(HttpServletResponse resp, RespEntity<?> entity) throws IOException {
        resp.getWriter().write(JsonUtil.toJson(entity));
    }
}