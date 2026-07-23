package icu.nothingless.controller;

import icu.nothingless.dao.FileShareDao;
import icu.nothingless.dao.UserFileDao;
import icu.nothingless.dao.impl.FileShareDaoImpl;
import icu.nothingless.dao.impl.UserFileDaoImpl;
import icu.nothingless.entity.FileShare;
import icu.nothingless.entity.UserFile;
import icu.nothingless.util.JsonResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.*;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@WebServlet("/file/*")
@MultipartConfig(
    maxFileSize = 100 * 1024 * 1024,       // 单文件 100MB
    maxRequestSize = 200 * 1024 * 1024,     // 单次请求 200MB
    fileSizeThreshold = 1024 * 1024           // 1MB 以上写磁盘
)
public class FileServlet extends HttpServlet {

    private static final String UPLOAD_ROOT =
        System.getProperty("user.home") + "/MuSong/files/";

    private final UserFileDao userFileDao = new UserFileDaoImpl();
    private final FileShareDao fileShareDao = new FileShareDaoImpl();

    /* ================= 路由分发 ================= */

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        Long userId = getUserId(req);

        if (userId == null) { JsonResponse.error(resp, 401, "未登录"); return; }

        if ("/list".equals(path)) {
            listFiles(resp, userId);
        } else if ("/search".equals(path)) {
            searchFiles(req, resp, userId);
        } else if ("/download".equals(path)) {
            downloadFile(req, resp, userId);
        } else if ("/received".equals(path)) {
            listReceived(resp, userId);
        } else if ("/sent".equals(path)) {
            listSent(resp, userId);
        } else {
            JsonResponse.error(resp, 404, "未知接口");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        Long userId = getUserId(req);

        if (userId == null) { JsonResponse.error(resp, 401, "未登录"); return; }

        switch (path == null ? "" : path) {
            case "/upload"  -> uploadFile(req, resp, userId);
            case "/delete"  -> deleteFile(req, resp, userId);
            case "/send"    -> sendFile(req, resp, userId);
            case "/revoke"  -> revokeFile(req, resp, userId);
            default         -> JsonResponse.error(resp, 404, "未知接口");
        }
    }

    /* ================= 1. 上传 ================= */

    private void uploadFile(HttpServletRequest req, HttpServletResponse resp, Long userId)
            throws IOException, ServletException {
        Part part = req.getPart("file");
        if (part == null || part.getSize() == 0) {
            JsonResponse.fail(resp, "请选择文件");
            return;
        }

        String originalName = part.getSubmittedFileName();
        String storedName = UUID.randomUUID().toString() + getExtension(originalName);

        File userDir = new File(UPLOAD_ROOT + userId + "/");
        userDir.mkdirs();

        File target = new File(userDir, storedName);
        part.write(target.getAbsolutePath());

        UserFile uf = new UserFile();
        uf.setUserId(userId);
        uf.setFileName(originalName);
        uf.setStoredName(storedName);
        uf.setFilePath(target.getAbsolutePath());
        uf.setFileSize(target.length());
        uf.setMimeType(part.getContentType());
        userFileDao.insert(uf);

        JsonResponse.ok(resp, "上传成功", uf);
    }

    /* ================= 2. 列表 ================= */

    private void listFiles(HttpServletResponse resp, Long userId) throws IOException {
        List<UserFile> files = userFileDao.findByUserId(userId);
        JsonResponse.ok(resp, files);
    }

    /* ================= 3. 搜索 ================= */

    private void searchFiles(HttpServletRequest req, HttpServletResponse resp, Long userId)
            throws IOException {
        String keyword = req.getParameter("keyword");
        if (keyword == null || keyword.trim().isEmpty()) {
            listFiles(resp, userId);
            return;
        }
        List<UserFile> files = userFileDao.searchByUserAndName(userId, keyword.trim());
        JsonResponse.ok(resp, files);
    }

    /* ================= 4. 下载 ================= */

    private void downloadFile(HttpServletRequest req, HttpServletResponse resp, Long userId)
            throws IOException {
        Long fileId = parseLong(req.getParameter("fileId"));
        if (fileId == null) { resp.sendError(400, "缺少 fileId"); return; }

        UserFile uf = userFileDao.findById(fileId);
        if (uf == null || !uf.getUserId().equals(userId)) {
            resp.sendError(403, "无权限或文件不存在");
            return;
        }

        File f = new File(uf.getFilePath());
        if (!f.exists()) { resp.sendError(404, "文件不存在"); return; }

        resp.setContentType(uf.getMimeType() != null ? uf.getMimeType() : "application/octet-stream");
        resp.setHeader("Content-Length", String.valueOf(f.length()));
        resp.setHeader("Content-Disposition",
            "attachment; filename=\"" + URLEncoder.encode(uf.getFileName(), "UTF-8") + "\"");

        try (InputStream in = new FileInputStream(f);
             OutputStream out = resp.getOutputStream()) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        }
    }

    /* ================= 5. 删除 ================= */

    private void deleteFile(HttpServletRequest req, HttpServletResponse resp, Long userId)
            throws IOException {
        Long fileId = parseLong(req.getParameter("fileId"));
        if (fileId == null) { JsonResponse.fail(resp, "缺少 fileId"); return; }

        UserFile uf = userFileDao.findById(fileId);
        if (uf == null || !uf.getUserId().equals(userId)) {
            JsonResponse.fail(resp, "无权限或文件不存在");
            return;
        }

        // 删磁盘
        File f = new File(uf.getFilePath());
        if (f.exists()) f.delete();

        // 删 DB
        userFileDao.deleteById(fileId);
        JsonResponse.ok(resp, "删除成功", null);
    }

    /* ================= 6. 发送给好友 ================= */

    private void sendFile(HttpServletRequest req, HttpServletResponse resp, Long userId)
            throws IOException {
        Long fileId   = parseLong(req.getParameter("fileId"));
        Long friendId = parseLong(req.getParameter("friendId"));

        if (fileId == null || friendId == null) {
            JsonResponse.fail(resp, "参数缺失");
            return;
        }

        // 校验文件归属
        UserFile uf = userFileDao.findById(fileId);
        if (uf == null || !uf.getUserId().equals(userId)) {
            JsonResponse.fail(resp, "文件不存在或无权限");
            return;
        }

        // TODO: 校验好友关系（调用你的 FriendDao.isFriend）
        // if (!friendDao.isFriend(userId, friendId)) {
        //     JsonResponse.fail(resp, "不是好友");
        //     return;
        // }

        FileShare share = new FileShare();
        share.setSenderId(userId);
        share.setReceiverId(friendId);
        share.setFileId(fileId);
        share.setIsRevoked(0);
        fileShareDao.insert(share);

        JsonResponse.ok(resp, "发送成功", share.getId());
    }

    /* ================= 7. 撤回（24h 窗口） ================= */

    private void revokeFile(HttpServletRequest req, HttpServletResponse resp, Long userId)
            throws IOException {
        Long shareId = parseLong(req.getParameter("shareId"));
        if (shareId == null) { JsonResponse.fail(resp, "缺少 shareId"); return; }

        FileShare share = fileShareDao.findById(shareId);
        if (share == null) {
            JsonResponse.fail(resp, "记录不存在");
            return;
        }

        // 只能撤回自己发的
        if (!share.getSenderId().equals(userId)) {
            JsonResponse.fail(resp, "只能撤回自己发送的文件");
            return;
        }

        // 24 小时窗口
        long hours = ChronoUnit.HOURS.between(share.getSendTime(), LocalDateTime.now());
        if (hours > 24) {
            JsonResponse.fail(resp, "超过 24 小时，无法撤回");
            return;
        }

        fileShareDao.updateRevokeStatus(shareId, 1);
        JsonResponse.ok(resp, "撤回成功", null);
    }

    /* ================= 8. 收到的文件列表 ================= */

    private void listReceived(HttpServletResponse resp, Long userId) throws IOException {
        List<FileShare> list = fileShareDao.findReceivedByUserId(userId);
        JsonResponse.ok(resp, list);
    }

    /* ================= 9. 发出的文件列表（用于撤回） ================= */

    private void listSent(HttpServletResponse resp, Long userId) throws IOException {
        List<FileShare> list = fileShareDao.findSentByUserId(userId);
        JsonResponse.ok(resp, list);
    }

    /* ================= 工具方法 ================= */

    private Long getUserId(HttpServletRequest req) {
        Object uid = req.getSession().getAttribute("userId");
        return uid == null ? null : (Long) uid;
    }

    private Long parseLong(String s) {
        try { return s == null ? null : Long.parseLong(s); }
        catch (NumberFormatException e) { return null; }
    }

    private String getExtension(String name) {
        if (name == null) return "";
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(dot) : "";
    }
}
