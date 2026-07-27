package icu.nothingless.controller.files;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icu.nothingless.commons.RespEntity;
import icu.nothingless.dao.interfaces.FileShareDao;
import icu.nothingless.pojo.bean.FileShareBean;
import icu.nothingless.pojo.bean.FileUserBean;
import icu.nothingless.pojo.dto.SendResultDTO;
import icu.nothingless.pojo.dto.UploadResultDTO;
import icu.nothingless.service.interfaces.IFileService;
import icu.nothingless.tools.FileDownloadUtil;
import icu.nothingless.tools.JsonUtil;
import icu.nothingless.tools.ServiceFactory;
import icu.nothingless.tools.ViewUtil;
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
    private final FileShareDao fileShareDao = ServiceFactory.getSingleton(FileShareDao.class);
    private static final Logger logger = LoggerFactory.getLogger(FileServlet.class);

    /* ====== 路由 ====== */

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        // init user ID
        Long userId = init_requireLogin(req, resp);
        if (userId == null) {
            writeJson(resp, RespEntity.badRequest("You are not valid"));
            return;
        }
        req.getSession(false).setAttribute("UID", userId);
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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        // init user ID
        Long userId = init_requireLogin(req, resp);
        if (userId == null) {
            writeJson(resp, RespEntity.badRequest("You are not valid"));
            return;
        }
        req.getSession(false).setAttribute("UID", userId);
        try {
            RespEntity response;
            switch (req.getPathInfo()) {
                case "/upload" -> response = upload(req, resp);
                case "/delete" -> response = delete(req, resp);
                case "/send" -> response = send(req, resp);
                case "/revoke" -> response = revoke(req, resp);
                default -> response = RespEntity.unauthorized("Unknown link!");
            }
            if (response != null) {
                writeJson(resp, response);
            }
        } catch (ServletException | IOException e) {
            logger.error("Error occurred: {}", e);
            ViewUtil.render(req, resp, "error_page",
                    Map.of("respEntity", RespEntity.internalError("There's some error occurred!")));
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
        Long userId = getCurrentUserId(req);
        RespEntity<List<FileUserBean>> listFiles = fileService.listFiles(userId);
        return RespEntity.success((List<FileUserBean>)listFiles.getData());
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
        Long userId = getCurrentUserId(req);
        RespEntity<List<FileUserBean>> listFiles = fileService.searchFiles(userId, keyword.trim());
        return RespEntity.success((List<FileUserBean>)listFiles.getData());
    }

    /*
     * @params:
     * fileId
     * 
     * @returns:
     * provide this file to download
     */
    private void doDownload(HttpServletRequest req, HttpServletResponse resp) {
        Long shareId = parseLong(req.getParameter("shareId"));
        Long fileId = parseLong(req.getParameter("fileId"));
        Long userId = getCurrentUserId(req);

        FileUserBean uf = null;

        if (shareId != null) {
            FileShareBean share = fileShareDao.findById(shareId);
            if (share == null || share.getIsRevoked() != null && share.getIsRevoked() == 1) {
                renderError(req, resp, RespEntity.notFound("文件不存在或已撤回"));
                return;
            }
            if (!share.getReceiverId().equals(userId)) {
                renderError(req, resp, RespEntity.forbidden("无权限下载该文件"));
                return;
            }

            RespEntity<List<FileUserBean>> fileResp = fileService.findFileById(share.getFileId());
            if (!fileResp.isSuccess() || fileResp.getData() == null || fileResp.getData().isEmpty()) {
                renderError(req, resp, RespEntity.notFound("文件不存在"));
                return;
            }
            uf = fileResp.getData().get(0);

        } else if (fileId != null) {
            RespEntity<List<FileUserBean>> fileResp = fileService.findFileById(fileId);
            if (!fileResp.isSuccess() || fileResp.getData() == null || fileResp.getData().isEmpty()) {
                renderError(req, resp, RespEntity.notFound("文件不存在"));
                return;
            }

            uf = fileResp.getData().get(0);
            if (!uf.getUserId().equals(userId)) {
                renderError(req, resp, RespEntity.forbidden("无权限下载该文件"));
                return;
            }

        } else {
            renderError(req, resp, RespEntity.badRequest("Missing fileId or shareId"));
            return;
        }

        if (uf == null || uf.getFilePath() == null || uf.getFilePath().isBlank()) {
            renderError(req, resp, RespEntity.notFound("文件不存在或无法读取"));
            return;
        }

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
     * files that this user have received
     */
    private RespEntity dolistReceived(HttpServletRequest req, HttpServletResponse resp) {
        Long userId = getCurrentUserId(req);
        RespEntity<List<FileShareBean>> received_file_list = fileService.listReceived(userId);
        return RespEntity.success((List<FileShareBean>)received_file_list.getData());

    }

    /*
     * @params:
     * userId
     * 
     * @returns:
     * files that this user have sent
     */
    private RespEntity doSend(HttpServletRequest req, HttpServletResponse resp) {
        Long userId = getCurrentUserId(req);
        RespEntity<List<FileShareBean>> sent_file_list = fileService.listSent(userId);
        return RespEntity.success((List<FileShareBean>)sent_file_list.getData());
    }

    /* ====== Dispatch functions (for doPost)====== */

    /*
     * @params:
     * fileId
     * 
     * @returns:
     * provide this file to download
     */
    private RespEntity upload(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        Part part = req.getPart("file");
        if (part == null || part.getSize() == 0) {
            return RespEntity.badRequest("Please select upload file");
        }
        Long userId = getCurrentUserId(req);

        RespEntity<UploadResultDTO> ret = fileService.upload(
                userId,
                part.getSubmittedFileName(),
                part.getContentType(),
                part.getInputStream());

        return RespEntity.success((UploadResultDTO)ret.getData());
    }

    private RespEntity search(HttpServletRequest req, HttpServletResponse resp) {
        String keyword = req.getParameter("keyword");
        Long userId = getCurrentUserId(req);
        RespEntity<List<FileUserBean>> list = (keyword == null || keyword.isBlank())
                ? fileService.listFiles(userId)
                : fileService.searchFiles(userId, keyword.trim());
        return RespEntity.success((List<FileUserBean>)list.getData());
    }

    private RespEntity delete(HttpServletRequest req, HttpServletResponse resp) {
        Long fileId = parseLong(req.getParameter("fileId"));
        Long userId = getCurrentUserId(req);
        if (fileId == null) {
            return RespEntity.badRequest("Missing file ID");
        }
        if(userId == null){
            return RespEntity.badRequest("Missing user ID");
        }
        return fileService.deleteFile(userId, fileId);
    }

    private RespEntity send(HttpServletRequest req, HttpServletResponse resp) {
        Long fileId = parseLong(req.getParameter("fileId"));
        Long friendId = parseLong(req.getParameter("friendId"));
        Long userId = getCurrentUserId(req);

        if (fileId == null || friendId == null) {
            return RespEntity.error("Missing file ID / friend ID");
        }
        // 校验文件归属
        RespEntity<List<FileUserBean>> uf = fileService.findFileById(fileId);

        if (!uf.isSuccess()) {
            return RespEntity.badRequest("The file that you point does not exist!");
        }
        List<FileUserBean> fu = uf.getData();

        if (fu == null || fu.isEmpty() || !fu.get(0).getUserId().equals(userId)) {
            return RespEntity.badRequest("File does not exist or no authorization");
        }

        RespEntity<SendResultDTO> ret = fileService.sendFile(userId, friendId, fileId);
        if (ret.isSuccess()) {
            return RespEntity.success((SendResultDTO) ret.getData());
        } else {
            return RespEntity.error(ret.getMessage());
        }

    }

    private RespEntity revoke(HttpServletRequest req, HttpServletResponse resp) {
        Long shareId = parseLong(req.getParameter("shareId"));
        Long userId = getCurrentUserId(req);
        if (shareId == null) {
            return RespEntity.error("missing sharedId");
        }
        fileService.revokeFile(userId, shareId);
        return RespEntity.success("撤回成功");
    }

    private Long init_requireLogin(HttpServletRequest req, HttpServletResponse resp) {
        // 1. 优先从 Session 取
        Object uid = req.getSession().getAttribute("CURRENT_USER_ID");
        if (uid == null) {
            uid = req.getSession().getAttribute("userId");
        }
        if (uid != null) {
            return toLong(uid);
        }

        // 2. 尝试普通参数（适用于 GET 请求、query string、application/x-www-form-urlencoded）
        String uidStr = req.getParameter("userId");
        if (uidStr != null && !uidStr.isEmpty()) {
            return toLong(uidStr);
        }

        // 3. 只有 multipart 请求才调用 getPart，否则抛 InvalidContentTypeException
        String contentType = req.getContentType();
        if (contentType != null && contentType.toLowerCase().startsWith("multipart/")) {
            try {
                Part userIdPart = req.getPart("userId");
                if (userIdPart != null) {
                    String partValue = readPartString(userIdPart);
                    // 过滤前端 undefined/null 字符串
                    if (partValue != null && !partValue.isEmpty()
                            && !"undefined".equals(partValue)
                            && !"null".equalsIgnoreCase(partValue)) {
                        logger.debug("Got userId from multipart part: {}", partValue);
                        return toLong(partValue);
                    }
                }
            } catch (ServletException | IOException e) {
                logger.warn("Failed to read userId from multipart part", e);
            }
        }

        return null;
    }

    private String readPartString(Part part) throws IOException {
        if (part == null)
            return null;
        try (InputStream is = part.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString().trim();
        }
    }

    /* ====== 工具 ====== */

    private Long getCurrentUserId(HttpServletRequest req) {
        return toLong(req.getSession(false).getAttribute("UID"));
    }

    private Long parseLong(String s) {
        if (s == null)
            return null;
        try {
            return Long.valueOf(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    static long toLong(Object obj) {
        return switch (obj) {
            case Long l -> l; // 直接拆箱
            case Integer i -> i.longValue(); // 安全拓宽
            case Number n -> n.longValue(); // 覆盖 Double/Float/Short/Byte
            case String s -> Long.parseLong(s); // 字符串解析
            case null -> 0L; // 显式处理 null
            default -> throw new IllegalArgumentException(
                    "Unsupported type: " + obj.getClass());
        };
    }
}