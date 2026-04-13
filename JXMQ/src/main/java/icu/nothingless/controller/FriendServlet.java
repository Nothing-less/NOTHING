package icu.nothingless.controller;

import java.io.IOException;
import java.util.List;

import icu.nothingless.commons.R;
import icu.nothingless.pojo.bean.FriendshipBean;
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
    private IFriendService friendService = new FriendServiceImpl();
    
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        
        String path = req.getPathInfo();
        HttpSession session = req.getSession();
        Long userId = (Long) session.getAttribute("userId");
        
        if ("/list".equals(path)) {
            // 获取好友列表
            String group = req.getParameter("group");
            String keyword = req.getParameter("keyword");
            List<FriendshipBean> list = friendService.getFriendList(userId, group, keyword);
            resp.getWriter().write(JsonUtil.toJson(R.success(list)));
            
        } else if ("/search".equals(path)) {
            // 搜索用户
            String keyword = req.getParameter("keyword");
            List<User> list = friendService.searchUsers(userId, keyword);
            resp.getWriter().write(JsonUtil.toJson(R.success(list)));
            
        } else if ("/requests".equals(path)) {
            // 获取好友申请列表
            List<FriendshipBean> list = friendService.getPendingRequests(userId);
            resp.getWriter().write(JsonUtil.toJson(R.success(list)));
            
        } else if ("/groups".equals(path)) {
            // 获取分组列表
            List<String> groups = friendService.getGroups(userId);
            resp.getWriter().write(JsonUtil.toJson(R.success(groups)));
        }
    }
    
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        
        String path = req.getPathInfo();
        HttpSession session = req.getSession();
        Long userId = (Long) session.getAttribute("userId");
        
        if ("/apply".equals(path)) {
            // 申请添加好友
            Long friendId = Long.parseLong(req.getParameter("friendId"));
            String applyMsg = req.getParameter("applyMsg");
            boolean success = friendService.applyFriend(userId, friendId, applyMsg);
            resp.getWriter().write(JsonUtil.toJson(success ? R.success(null) : R.error("申请失败")));
            
        } else if ("/agree".equals(path)) {
            // 同意申请
            Long friendId = Long.parseLong(req.getParameter("friendId"));
            String remark = req.getParameter("remark");
            String groupName = req.getParameter("groupName");
            boolean success = friendService.agreeFriend(userId, friendId, remark, groupName);
            resp.getWriter().write(JsonUtil.toJson(success ? R.success(null) : R.error("操作失败")));
            
        } else if ("/reject".equals(path)) {
            // 拒绝申请
            Long friendId = Long.parseLong(req.getParameter("friendId"));
            boolean success = friendService.rejectFriend(userId, friendId);
            resp.getWriter().write(JsonUtil.toJson(success ? R.success(null) : R.error("操作失败")));
            
        } else if ("/delete".equals(path)) {
            // 删除好友
            Long friendId = Long.parseLong(req.getParameter("friendId"));
            boolean success = friendService.deleteFriend(userId, friendId);
            resp.getWriter().write(JsonUtil.toJson(success ? R.success(null) : R.error("删除失败")));
            
        } else if ("/update".equals(path)) {
            // 修改备注/分组
            Long friendId = Long.parseLong(req.getParameter("friendId"));
            String remark = req.getParameter("remark");
            String groupName = req.getParameter("groupName");
            boolean success = friendService.updateFriendInfo(userId, friendId, remark, groupName);
            resp.getWriter().write(JsonUtil.toJson(success ? R.success(null) : R.error("修改失败")));
        }
    }
}