# 文件仓库系统 - 部署说明

## 一、文件结构

```
项目根/
├── sql/
│   └── create_tables.sql          ← 建表脚本
├── src/
│   └── icu/nothingless/
│       ├── entity/
│       │   ├── UserFile.java      ← 用户文件实体
│       │   └── FileShare.java    ← 文件分享实体
│       ├── dao/
│       │   ├── UserFileDao.java
│       │   ├── FileShareDao.java
│       │   └── impl/
│       │       ├── UserFileDaoImpl.java
│       │       └── FileShareDaoImpl.java
│       ├── controller/
│       │   └── FileServlet.java  ← 核心控制器
│       └── util/
│           ├── DBUtil.java        ← 数据库连接
│           └── JsonResponse.java  ← 统一响应
└── web/
    ├── WEB-INF/jsp/pages/
    │   └── file_repository.jsp   ← 文件仓库页面
    └── static/
        ├── css/
        │   └── file_repository.css
        └── js/pages/
            └── file_repository.js
```

## 二、部署步骤

### 1. 建表
```bash
mysql -u root -p musong < sql/create_tables.sql
```

### 2. 修改 DBUtil.java 中的数据库连接配置
```java
private static final String URL = "jdbc:mysql://localhost:3306/musong?...";
private static final String USER = "你的用户名";
private static final String PASSWORD = "你的密码";
```

### 3. 添加依赖（pom.xml 或 lib/）
- mysql-connector-j-8.x.jar
- jackson-databind-2.x.jar
- jakarta.servlet-api-6.x.jar

### 4. 编译 & 部署
```bash
javac -d build/ -cp "lib/*" src/icu/nothingless/**/*.java
# 打包为 WAR 部署到 Tomcat
```

### 5. 创建上传目录
```bash
mkdir -p ~/MuSong/files/
```

## 三、API 接口一览

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| GET  | /file/list      | 我的文件列表 | - |
| GET  | /file/search    | 搜索文件 | keyword |
| GET  | /file/download  | 下载文件 | fileId |
| POST | /file/upload    | 上传文件 | multipart: file |
| POST | /file/delete    | 删除文件 | fileId |
| POST | /file/send      | 发送给好友 | fileId, friendId |
| POST | /file/revoke    | 撤回文件 | shareId |
| GET  | /file/sent      | 我发出的文件 | - |
| GET  | /file/received  | 我收到的文件 | - |

## 四、撤回规则

- 仅发送者可撤回
- 仅 24 小时内可撤回
- 撤回后接收方看到的文件标记为 [已撤回]

## 五、与现有系统集成

### 在菜单/导航中添加入口
```html
<button onclick="location.href='<c:url value='/page/file_repository'/>'">
    📁 我的文件
</button>
```

### 在聊天窗口中接收文件消息
```js
// 当收到 file 类型消息时
function renderFileMessage(share) {
    return '<div class="file-msg">' +
        '<span class="file-icon">📄</span>' +
        '<span class="file-name">' + share.fileName + '</span>' +
        '<button onclick="FileRepo.download(' + share.fileId + ')">⬇️</button>' +
    '</div>';
}
```

## 六、安全注意事项

1. **文件大小限制**：当前设置为 100MB/文件，可在 @MultipartConfig 调整
2. **文件类型限制**：建议在 uploadFile() 中增加白名单校验
3. **路径遍历防护**：stored_name 使用 UUID，防止恶意文件名
4. **权限校验**：每个接口都验证了 userId 归属
5. **好友关系校验**：sendFile() 中 TODO 部分需接入你的 FriendDao
