package icu.nothingless.controller;

import icu.nothingless.dao.UserFileDao;
import icu.nothingless.dao.impl.FileShareDaoImpl;
import icu.nothingless.dao.impl.UserFileDaoImpl;
import icu.nothingless.dao.interfaces.FileShareDao;
import icu.nothingless.entity.FileShare;
import icu.nothingless.entity.UserFile;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;

/**
 * 补充说明：
 * 在 FileServlet.downloadFile() 中增加对 shareId 的支持，
 * 让接收方可以通过 shareId 下载发送方分享给自己的文件。
 *
 * 将以下逻辑合并到 FileServlet 的 doGet /download 分支即可。
 */
public class FileServletPatch {

    private final UserFileDao userFileDao = new UserFileDaoImpl();
    private final FileShareDao fileShareDao = new FileShareDaoImpl();

    /**
     * 改进后的 downloadFile，同时支持 fileId（自己下载）和 shareId（接收方下载）
     */
    protected void downloadFile(HttpServletRequest req, HttpServletResponse resp, Long userId)
            throws Exception {

        String shareIdStr = req.getParameter("shareId");
        String fileIdStr  = req.getParameter("fileId");

        UserFile uf = null;

        if (shareIdStr != null) {
            // ===== 通过 shareId 下载（接收方） =====
            Long shareId = Long.parseLong(shareIdStr);
            FileShare share = fileShareDao.findById(shareId);

            if (share == null || share.getIsRevoked() == 1) {
                resp.sendError(404, "文件不存在或已撤回");
                return;
            }

            // 只能下载发给自己的
            if (!share.getReceiverId().equals(userId)) {
                resp.sendError(403, "无权限");
                return;
            }

            uf = userFileDao.findById(share.getFileId());

        } else if (fileIdStr != null) {
            // ===== 通过 fileId 下载（自己） =====
            Long fileId = Long.parseLong(fileIdStr);
            uf = userFileDao.findById(fileId);

            if (uf == null || !uf.getUserId().equals(userId)) {
                resp.sendError(403, "无权限或文件不存在");
                return;
            }
        } else {
            resp.sendError(400, "缺少参数");
            return;
        }

        if (uf == null) { resp.sendError(404, "文件不存在"); return; }

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
}
