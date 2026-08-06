package icu.nothingless.tools;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class FileDownloadUtil {

    private static final int BUFFER_SIZE = 8192;

    private FileDownloadUtil() {}

    /**
     * 下载文件（支持断点续传）
     *
     * @param req       HttpServletRequest
     * @param resp      HttpServletResponse
     * @param file      实际文件
     * @param fileName  下载时显示的文件名（支持中文）
     * @param mimeType  MIME 类型（可为 null，内部自动兜底）
     * @return true  = 成功处理（已写出响应）
     *         false = 未处理（如文件不存在，由调用方决定返回 404 / 错误页）
     */
    public static boolean download(
            HttpServletRequest req,
            HttpServletResponse resp,
            File file,
            String fileName,
            String mimeType
    ) throws IOException {

        if (file == null || !file.exists() || !file.canRead()) {
            return false;
        }

        long fileLength = file.length();
        String encodedName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);

        resp.reset();
        resp.setHeader("Accept-Ranges", "bytes");
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setHeader("Content-Disposition",
                "attachment; filename*=UTF-8''" + encodedName);

        // MIME 兜底
        if (mimeType == null || mimeType.isBlank()) {
            mimeType = req.getServletContext().getMimeType(fileName);
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }
        }
        resp.setContentType(mimeType);

        String range = req.getHeader("Range");

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {

            // ===== 断点续传 =====
            if (range != null && range.startsWith("bytes=")) {
                return handleRange(req, raf, resp, fileLength);
            }

            // ===== 全量下载 =====
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentLengthLong(fileLength);

            byte[] buffer = new byte[BUFFER_SIZE];
            OutputStream out = resp.getOutputStream();

            int len;
            while ((len = raf.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
            return true;
        } catch (IOException e) {
            /*
             * 客户端主动断开连接（暂停 / 取消下载）
             * 属于正常行为，不向上抛异常
             */
            if (!resp.isCommitted()) {
                throw e;
            }
            return true;
        }
    }

    /**
     * 处理 Range 请求
     */
    private static boolean handleRange(
            HttpServletRequest req,
            RandomAccessFile raf,
            HttpServletResponse resp,
            long fileLength
    ) throws IOException {

        String range = req.getHeader("Range");
        long start;
        long end;

        try {
            String[] parts = range.replace("bytes=", "").split("-");
            start = Long.parseLong(parts[0]);
            end = parts.length > 1 && !parts[1].isEmpty()
                    ? Long.parseLong(parts[1])
                    : fileLength - 1;
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
            return false;
        }

        if (start >= fileLength || start > end) {
            resp.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
            resp.setHeader("Content-Range", "bytes */" + fileLength);
            return false;
        }

        end = Math.min(end, fileLength - 1);
        long contentLength = end - start + 1;

        resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        resp.setHeader("Content-Range",
                "bytes " + start + "-" + end + "/" + fileLength);
        resp.setContentLengthLong(contentLength);

        raf.seek(start);

        byte[] buffer = new byte[BUFFER_SIZE];
        OutputStream out = resp.getOutputStream();

        long remaining = contentLength;
        while (remaining > 0) {
            int len = raf.read(buffer, 0,
                    (int) Math.min(buffer.length, remaining));
            if (len == -1) {
                break;
            }
            out.write(buffer, 0, len);
            remaining -= len;
        }
        out.flush();
        return true;
    }
}