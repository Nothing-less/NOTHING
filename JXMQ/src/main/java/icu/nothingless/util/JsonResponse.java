package icu.nothingless.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class JsonResponse {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void ok(HttpServletResponse resp, Object data) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.print("{\"code\":200,\"message\":\"操作成功\",\"data\":" + toJson(data) + "}");
        out.flush();
    }

    public static void ok(HttpServletResponse resp, String message, Object data) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.print("{\"code\":200,\"message\":\"" + message + "\",\"data\":" + toJson(data) + "}");
        out.flush();
    }

    public static void fail(HttpServletResponse resp, String message) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.print("{\"code\":400,\"message\":\"" + message + "\",\"data\":null}");
        out.flush();
    }

    public static void error(HttpServletResponse resp, int code, String message) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.print("{\"code\":" + code + ",\"message\":\"" + message + "\",\"data\":null}");
        out.flush();
    }

    private static String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            return "null";
        }
    }
}
