<!-- file_repository.jsp - 个人文件仓库页面 -->
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page import="icu.nothingless.commons.RespEntity" %>
<%@ page import="icu.nothingless.pojo.dto.User" %>
<%@ page import="icu.nothingless.tools.ViewUtil" %>

<link rel="stylesheet" href="<c:url value='/static/css/pages.css' />">
<link rel="stylesheet" href="<c:url value='/static/css/file_repository.css' />">

<%
    User currentUser = (User) session.getAttribute("CURRENT_USER");
    if (currentUser == null) {
        request.setAttribute("respEntity", RespEntity.error("错误！系统出现异常！"));
        ViewUtil.render(request, response, "error_page");
        return;
    }
    Object currentUser_ID = currentUser.userId();
    session.setAttribute("CURRENT_USER_ID", currentUser_ID);
    Object currentUser_Nickname = currentUser.nickname();
    session.setAttribute("CURRENT_USER_NICKNAME", currentUser_Nickname);
    String contextPath = request.getContextPath();
%>

<!-- 全局配置，必须在所有 JS 之前 -->
<script>
    window.APP = {
        contextPath: '${pageContext.request.contextPath}',
        currentUser: {
            userId: '${sessionScope.CURRENT_USER_ID}',
            nickname: '${sessionScope.CURRENT_USER_NICKNAME}'
        }
    };
</script>

<div class="file-panel">

    <!-- ===== 顶部操作栏 ===== -->
    <div class="file-header">
        <div class="file-header-left">
            <button class="btn-upload" onclick="document.getElementById('fileInput').click()">
                📤 上传文件
            </button>
            <input type="file" id="fileInput" style="display:none"
                onchange="FileRepo.upload(this)">
        </div>
        <div class="file-header-right">
            <input type="text" id="fileSearchInput"
                placeholder="🔍 搜索文件名..."
                onkeyup="FileRepo.search()">
        </div>
    </div>

    <!-- ===== 文件列表 ===== -->
    <div class="file-list" id="fileList">
        <%-- <div class="loading"></div> --%>
    </div>

    <!-- ===== 上传进度条（隐藏） ===== -->
    <div class="upload-progress" id="uploadProgress" style="display:none;">
        <div class="upload-progress-bar" id="uploadProgressBar"></div>
        <span class="upload-progress-text" id="uploadProgressText">0%</span>
    </div>
</div>

<!-- ===== 发送给好友弹窗 ===== -->
<div class="modal" id="sendFileModal">
    <div class="modal-content">
        <div class="modal-header">
            <h3>📤 发送给好友</h3>
            <button class="btn-modal-close" type="button"
                    onclick="FileRepo.closeSendModal()">关闭</button>
        </div>
        <div class="friend-pick-list" id="friendPickList">
            <div class="search-tip">加载好友列表中...</div>
        </div>
    </div>
</div>

<!-- ===== 已发送文件（撤回管理）弹窗 ===== -->
<div class="modal" id="sentFilesModal">
    <div class="modal-content">
        <div class="modal-header">
            <h3>↩️ 已发送文件（可撤回）</h3>
            <button class="btn-modal-close" type="button"
                    onclick="FileRepo.closeSentModal()">关闭</button>
        </div>
        <div class="sent-files-list" id="sentFilesList">
            <div class="search-tip">加载中...</div>
        </div>
    </div>
</div>

<!-- ===== 接收到的文件弹窗 ===== -->
<div class="modal" id="receivedFilesModal">
    <div class="modal-content">
        <div class="modal-header">
            <h3>📥 好友发送给我的文件</h3>
            <button class="btn-modal-close" type="button"
                    onclick="FileRepo.closeReceivedModal()">关闭</button>
        </div>
        <div class="sent-files-list" id="receivedFilesList">
            <div class="search-tip">加载中...</div>
        </div>
    </div>
</div>

<!-- 依赖 -->
<script src="<c:url value='/static/js/ChatClient.js' />"></script>
<!-- 业务 JS -->
<script src="<c:url value='/static/js/file_repository.js' />"></script>
