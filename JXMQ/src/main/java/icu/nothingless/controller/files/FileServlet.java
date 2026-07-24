package icu.nothingless.controller.files;

import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import icu.nothingless.commons.RespEntity;
import icu.nothingless.pojo.bean.FileUserBean;
import icu.nothingless.pojo.dto.SendResultDTO;
import icu.nothingless.pojo.dto.UploadResultDTO;
import icu.nothingless.service.impl.FileServiceImpl;
import icu.nothingless.service.interfaces.IFileService;
import icu.nothingless.tools.FileDownloadUtil;
import icu.nothingless.tools.RedirectUtil;
import icu.nothingless.tools.ServiceFactory;
import icu.nothingless.tools.ViewUtil;
import icu.nothingless.util.JsonResponse;

@WebServlet("/file/*")
@MultipartConfig(maxFileSize = 100 * 1024 * 1024, // 单文件 100MB
        maxRequestSize = 200 * 1024 * 1024, // 单次请求 200MB
        fileSizeThreshold = 1024 * 1024 // 1MB 以上写磁盘
)
public class FileServlet extends HttpServlet {

    private final IFileService fileService = ServiceFactory.getSingleton(IFileService.class);
    private static final Logger logger = LoggerFactory.getLogger(FileServlet.class);
    private static final Gson gson = new Gson();

    /* ====== 路由 ====== */

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        PrintWriter out = resp.getWriter();

        Long userId = requireLogin(req, resp);
        if (userId == null)
            return;

        RespEntity response = null;
        switch (req.getPathInfo()) {
            case "/list" -> response = doList(req, resp);
            case "/search" -> response = doSearch(req, resp);
            case "/download" -> {
                doDownload(req, resp);
                return; // doDownload handles the response directly
            }
            case "/received" -> response = RespEntity.success(fileService.listReceived(userId));
            case "/sent" -> response = RespEntity.success(fileService.listSent(userId));
            default -> response = RespEntity.unauthorized("未知接口");
        }
        if (response != null) {
            out.print(gson.toJson(response));
        }

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // 支持CORS（如果需要）
        resp.setHeader("Access-Control-Allow-Origin", "*");
        PrintWriter out = resp.getWriter();

        Long userId = requireLogin(req, resp);
        if (userId == null)
            return;

        switch (req.getPathInfo()) {
            case "/upload" -> upload(req, resp, userId);
            case "/delete" -> delete(req, resp, userId);
            case "/send" -> send(req, resp, userId);
            case "/revoke" -> revoke(req, resp, userId);
            default -> JsonResponse.error(resp, 404, "未知接口");
        }
    }

    private RespEntity doList(HttpServletRequest req, HttpServletResponse resp) {
        Long userId = requireLogin(req, resp);
        RespEntity<List<FileUserBean>> files = fileService.listFiles(userId);
        return RespEntity.success(files);
    }

    private RespEntity doSearch(HttpServletRequest req, HttpServletResponse resp) {
        Long userId = requireLogin(req, resp);
        String keyword = req.getParameter("keyword");
        RespEntity<List<FileUserBean>> files = fileService.searchFiles(userId, keyword.trim());
        return RespEntity.success(files);
    }

    private void doDownload(HttpServletRequest req, HttpServletResponse resp) {
        Long fileId = parseLong(req.getParameter("fileId"));
        if (fileId == null) {
            renderError(req, resp, RespEntity.badRequest("Missing fileId"));
            return;
        }

        Long userId = requireLogin(req, resp);
        RespEntity<FileUserBean> ret = fileService.getDownloadableFile(userId, fileId);
        FileUserBean uf = ret.getData();
        File file = new File(uf.getFilePath());

        try {
            boolean ok = FileDownloadUtil.download(
                    req,
                    resp,
                    file,
                    uf.getFileName(),
                    uf.getMimeType());

            if (!ok) {
                renderError(req, resp, RespEntity.notFound("文件不存在或无法读取"));
            }
        } catch (IOException e) {
            logger.error("File download failed: {}", file.getAbsolutePath(), e);
            renderError(req, resp, RespEntity.internalError("文件下载失败"));
        }
    }

    private void renderError(HttpServletRequest req, HttpServletResponse resp, RespEntity<?> respEntity) {
        try {
            ViewUtil.render(req, resp, "error_page",
                    Map.of("respEntity", respEntity));
        } catch (Exception e) {
            logger.error("Render error page failed", e);
        }
    }

    /* ====== 业务方法 ====== */

    private void upload(HttpServletRequest req, HttpServletResponse resp, Long userId)
            throws IOException, ServletException {
        Part part = req.getPart("file");
        if (part == null || part.getSize() == 0) {
            JsonResponse.fail(resp, "请选择文件");
            return;
        }

        RespEntity<UploadResultDTO> dto = fileService.upload(
                userId,
                part.getSubmittedFileName(),
                part.getContentType(),
                part.getInputStream());

        JsonResponse.ok(resp, "上传成功", dto);
    }

    private void search(HttpServletRequest req, HttpServletResponse resp, Long userId)
            throws Exception {
        String keyword = req.getParameter("keyword");
        RespEntity<List<FileUserBean>> list = (keyword == null || keyword.isBlank())
                ? fileService.listFiles(userId)
                : fileService.searchFiles(userId, keyword.trim());
        JsonResponse.ok(resp, list);
    }

    private void delete(HttpServletRequest req, HttpServletResponse resp, Long userId)
            throws IOException {
        Long fileId = parseLong(req.getParameter("fileId"));
        if (fileId == null) {
            JsonResponse.fail(resp, "缺少 fileId");
            return;
        }
        fileService.deleteFile(userId, fileId);
        JsonResponse.ok(resp, "删除成功", null);
    }

    private void send(HttpServletRequest req, HttpServletResponse resp, Long userId)
            throws IOException {
        Long fileId = parseLong(req.getParameter("fileId"));
        Long friendId = parseLong(req.getParameter("friendId"));
        if (fileId == null || friendId == null) {
            JsonResponse.fail(resp, "参数缺失");
            return;
        }

        SendResultDTO dto = fileService.sendFile(userId, friendId, fileId);
        JsonResponse.ok(resp, "发送成功", dto.getShareId());
    }

    private void revoke(HttpServletRequest req, HttpServletResponse resp, Long userId)
            throws IOException {
        Long shareId = parseLong(req.getParameter("shareId"));
        if (shareId == null) {
            JsonResponse.fail(resp, "缺少 shareId");
            return;
        }
        fileService.revokeFile(userId, shareId);
        JsonResponse.ok(resp, "撤回成功", null);
    }

    /* ====== 工具 ====== */

    private Long requireLogin(HttpServletRequest req, HttpServletResponse resp) {
        Object uid = req.getSession().getAttribute("userId");
        return uid == null ? null : (Long) uid;
    }

    private Long parseLong(String s) {
        if (s == null)
            return null;
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}