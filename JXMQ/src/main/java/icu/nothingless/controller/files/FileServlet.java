package icu.nothingless.controller.files;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import icu.nothingless.commons.RespEntity;
import icu.nothingless.pojo.bean.FileShareBean;
import icu.nothingless.pojo.bean.FileUserBean;
import icu.nothingless.pojo.dto.UploadResultDTO;
import icu.nothingless.service.interfaces.IFileService;
import icu.nothingless.tools.FileDownloadUtil;
import icu.nothingless.tools.JsonUtil;
import icu.nothingless.tools.ServiceFactory;
import icu.nothingless.tools.ViewUtil;
import icu.nothingless.util.JsonResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet("/file/*")
@MultipartConfig(maxFileSize = 100 * 1024 * 1024, // 单文件 100MB
        maxRequestSize = 200 * 1024 * 1024, // 单次请求 200MB
        fileSizeThreshold = 1024 * 1024 // 1MB 以上写磁盘
)
public class FileServlet extends HttpServlet {

    private final IFileService fileService = ServiceFactory.getSingleton(IFileService.class);
    private static final Logger logger = LoggerFactory.getLogger(FileServlet.class);
    private static final Gson gson = new Gson();
    private Long userId = null;

    /* ====== 路由 ====== */

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        // init user ID
        init_requireLogin(req, resp);
        if (userId == null) {
            writeJson(resp, RespEntity.badRequest("You are not vaild"));
            return;
        }

        try {
            RespEntity response;
            switch (req.getPathInfo()) {
                case "/download" -> {
                    doDownload(req, resp);
                    return; // doDownload handles the response directly
                }
                case "/list" -> response = doList(req, resp);
                case "/search" -> response = doSearch(req, resp);
                case "/received" -> response = dolistReceived(req, resp);
                case "/sent" -> response = doSend(req, resp);
                default -> response = RespEntity.unauthorized("Unknown link");
            }
            if (response != null) {
                writeJson(resp, response);
            }
        } catch (IOException e) {
            logger.error("Error occurred: {}", e);
            ViewUtil.render(req, resp, "error_page",
                    Map.of("respEntity", RespEntity.internalError("There's some error occurred")));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        if (this.userId == null) {
            writeJson(resp, RespEntity.badRequest("You are not vaild"));
            return;
        }
        switch (req.getPathInfo()) {
            case "/upload" -> upload(req, resp);
            case "/delete" -> delete(req, resp);
            case "/send" -> send(req, resp);
            case "/revoke" -> revoke(req, resp);
            default -> writeJson(resp, RespEntity.badRequest("Unknown link!"));
        }
    }

    private void renderError(HttpServletRequest req, HttpServletResponse resp, RespEntity<?> respEntity) {
        try {
            ViewUtil.render(req, resp, "error_page", Map.of("respEntity", respEntity));
        } catch (ServletException | IOException e) {
            logger.error("Render error page failed", e);
        }
    }

    private void writeJson(HttpServletResponse resp, RespEntity<?> entity) throws IOException {
        resp.getWriter().write(JsonUtil.toJson(entity));
    }

    /* ====== Dispatch functions (for doGet)====== */

    /*
     * @params:
     * userId
     * 
     * @returns:
     * all files of this user
     */
    private RespEntity doList(HttpServletRequest req, HttpServletResponse resp) {
        RespEntity<List<FileUserBean>> listFiles = fileService.listFiles(this.userId);
        return RespEntity.success(listFiles);
    }

    /*
     * @params:
     * userId
     * keyword
     * 
     * @returns:
     * filter user's files with keyword
     */
    private RespEntity doSearch(HttpServletRequest req, HttpServletResponse resp) {
        String keyword = req.getParameter("keyword");
        RespEntity<List<FileUserBean>> listFiles = fileService.searchFiles(this.userId, keyword.trim());
        return RespEntity.success(listFiles);
    }

    /*
     * @params:
     * fileId
     * 
     * @returns:
     * provide this file to download
     */
    private void doDownload(HttpServletRequest req, HttpServletResponse resp) {
        Long fileId = parseLong(req.getParameter("fileId"));
        if (fileId == null) {
            renderError(req, resp, RespEntity.badRequest("Missing fileId"));
            return;
        }

        RespEntity<FileUserBean> ret = fileService.getDownloadableFile(this.userId, fileId);
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

    /*
     * @params:
     * userId
     * 
     * @returns:
     * files that this user received
     */
    private RespEntity dolistReceived(HttpServletRequest req, HttpServletResponse resp) {
        RespEntity<List<FileShareBean>> received_file_list = fileService.listReceived(this.userId);
        return RespEntity.success(received_file_list.getData());

    }

    private RespEntity doSend(HttpServletRequest req, HttpServletResponse resp) {
        RespEntity<List<FileShareBean>> sent_file_list = fileService.listSent(this.userId);
        return RespEntity.success(sent_file_list.getData());
    }

    /* ====== Dispatch functions (for doPost)====== */

    private void upload(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        Part part = req.getPart("file");
        if (part == null || part.getSize() == 0) {
            JsonResponse.fail(resp, "请选择文件");
            return;
        }

        RespEntity<UploadResultDTO> ret = fileService.upload(
                userId,
                part.getSubmittedFileName(),
                part.getContentType(),
                part.getInputStream());

        writeJson(resp, RespEntity.success(ret));
    }

    private void search(HttpServletRequest req, HttpServletResponse resp)
            throws Exception {
        String keyword = req.getParameter("keyword");
        RespEntity<List<FileUserBean>> list = (keyword == null || keyword.isBlank())
                ? fileService.listFiles(userId)
                : fileService.searchFiles(userId, keyword.trim());
        JsonResponse.ok(resp, list);
    }

    private void delete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Long fileId = parseLong(req.getParameter("fileId"));
        if (fileId == null) {
            JsonResponse.fail(resp, "缺少 fileId");
            return;
        }
        fileService.deleteFile(userId, fileId);
        JsonResponse.ok(resp, "删除成功", null);
    }

    private void send(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Long fileId = parseLong(req.getParameter("fileId"));
        Long friendId = parseLong(req.getParameter("friendId"));

        if (fileId == null || friendId == null) {
            JsonResponse.fail(resp, "参数缺失");
        }
    }

    private void revoke(HttpServletRequest req, HttpServletResponse resp)
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

    private void init_requireLogin(HttpServletRequest req, HttpServletResponse resp) {
        Object uid = req.getSession().getAttribute("userId");
        this.userId = uid == null ? null : (Long) uid;
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