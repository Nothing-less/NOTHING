package icu.nothingless.controller.server;

import java.io.IOException;
import java.util.List;

import icu.nothingless.commons.RespEntity;
import icu.nothingless.pojo.dto.Friendship;
import icu.nothingless.pojo.dto.User;
import icu.nothingless.service.impl.FriendServiceImpl;
import icu.nothingless.service.interfaces.IFriendService;
import icu.nothingless.tools.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/friend/*")
public class FriendServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final IFriendService<Friendship, User> friendService = new FriendServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        prepareResponse(resp);
        Long userId = getCurrentUserId(req);
        if (userId == null) {
            writeJson(resp, RespEntity.error("未登录或会话已过期"));
            return;
        }

        String path = req.getPathInfo();
        if (path == null) {
            writeJson(resp, RespEntity.error("无效请求路径"));
            return;
        }

        switch (path) {
            case "/list" -> handleFriendList(req, resp, userId);
            case "/search" -> handleSearchUsers(req, resp, userId);
            case "/requests" -> handlePendingRequests(resp, userId);
            case "/groups" -> handleGroups(resp, userId);
            default -> writeJson(resp, RespEntity.error("无效请求路径"));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        prepareResponse(resp);
        Long userId = getCurrentUserId(req);
        if (userId == null) {
            writeJson(resp, RespEntity.error("未登录或会话已过期"));
            return;
        }

        String path = req.getPathInfo();
        if (path == null) {
            writeJson(resp, RespEntity.error("无效请求路径"));
            return;
        }

        switch (path) {
            case "/apply" -> handleApplyFriend(req, resp, userId);
            case "/agree" -> handleAgreeFriend(req, resp, userId);
            case "/reject" -> handleRejectFriend(req, resp, userId);
            case "/delete" -> handleDeleteFriend(req, resp, userId);
            case "/update" -> handleUpdateFriend(req, resp, userId);
            default -> writeJson(resp, RespEntity.error("无效请求路径"));
        }
    }

    private void handleFriendList(HttpServletRequest req, HttpServletResponse resp, Long userId) throws IOException {
        String group = req.getParameter("group");
        String keyword = req.getParameter("keyword");
        RespEntity<List<Friendship>> respEntity = friendService.getFriendList(userId, group, keyword);
        writeRespEntity(resp, respEntity);
    }

    private void handleSearchUsers(HttpServletRequest req, HttpServletResponse resp, Long userId) throws IOException {
        String keyword = req.getParameter("keyword");
        RespEntity<List<User>> respEntity = friendService.searchUsers(userId, keyword);
        writeRespEntity(resp, respEntity);
    }

    private void handlePendingRequests(HttpServletResponse resp, Long userId) throws IOException {
        RespEntity<List<Friendship>> respEntity = friendService.getPendingRequests(userId);
        writeRespEntity(resp, respEntity);
    }

    private void handleGroups(HttpServletResponse resp, Long userId) throws IOException {
        RespEntity<List<String>> respEntity = friendService.getGroups(userId);
        writeRespEntity(resp, respEntity);
    }

    private void handleApplyFriend(HttpServletRequest req, HttpServletResponse resp, Long userId) throws IOException {
        Long friendId = parseLongParameter(req, "friendId");
        String applyMsg = req.getParameter("applyMsg");
        if (friendId == null) {
            writeJson(resp, RespEntity.error("缺少 friendId 参数"));
            return;
        }
        RespEntity<Void> respEntity = friendService.applyFriend(userId, friendId, applyMsg);
        writeRespEntity(resp, respEntity);
    }

    private void handleAgreeFriend(HttpServletRequest req, HttpServletResponse resp, Long userId) throws IOException {
        Long friendId = parseLongParameter(req, "friendId");
        String remark = req.getParameter("remark");
        String groupName = req.getParameter("groupName");
        if (friendId == null) {
            writeJson(resp, RespEntity.error("缺少 friendId 参数"));
            return;
        }
        RespEntity<Void> respEntity = friendService.agreeFriend(userId, friendId, remark, groupName);
        writeRespEntity(resp, respEntity);
    }

    private void handleRejectFriend(HttpServletRequest req, HttpServletResponse resp, Long userId) throws IOException {
        Long friendId = parseLongParameter(req, "friendId");
        if (friendId == null) {
            writeJson(resp, RespEntity.error("缺少 friendId 参数"));
            return;
        }
        RespEntity<Void> respEntity = friendService.rejectFriend(userId, friendId);
        writeRespEntity(resp, respEntity);
    }

    private void handleDeleteFriend(HttpServletRequest req, HttpServletResponse resp, Long userId) throws IOException {
        Long friendId = parseLongParameter(req, "friendId");
        if (friendId == null) {
            writeJson(resp, RespEntity.error("缺少 friendId 参数"));
            return;
        }
        RespEntity<Void> respEntity = friendService.deleteFriend(userId, friendId);
        writeRespEntity(resp, respEntity);
    }

    private void handleUpdateFriend(HttpServletRequest req, HttpServletResponse resp, Long userId) throws IOException {
        Long friendId = parseLongParameter(req, "friendId");
        String remark = req.getParameter("remark");
        String groupName = req.getParameter("groupName");
        if (friendId == null) {
            writeJson(resp, RespEntity.error("缺少 friendId 参数"));
            return;
        }
        RespEntity<Void> respEntity = friendService.updateFriendInfo(userId, friendId, remark, groupName);
        writeRespEntity(resp, respEntity);
    }

    private Long getCurrentUserId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return null;
        }
        Object userIdObj = session.getAttribute("CURRENT_USER_ID");
        if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        }
        if (userIdObj instanceof String) {
            try {
                return Long.parseLong((String) userIdObj);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private Long parseLongParameter(HttpServletRequest req, String name) {
        String value = req.getParameter(name);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void prepareResponse(HttpServletResponse resp) {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
    }

    private void writeRespEntity(HttpServletResponse resp, RespEntity<?> respEntity) throws IOException {
        if (respEntity == null) {
            writeJson(resp, RespEntity.error("No response"));
            return;
        }
        if (respEntity.isError()) {
            writeJson(resp, RespEntity.error(respEntity.getMessage()));
            return;
        }
        writeJson(resp, RespEntity.success(respEntity.getData()));
    }

    private void writeJson(HttpServletResponse resp, Object body) throws IOException {
        resp.getWriter().write(JsonUtil.toJson(body));
    }
}
