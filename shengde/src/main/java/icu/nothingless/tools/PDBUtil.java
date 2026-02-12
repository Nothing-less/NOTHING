package icu.nothingless.tools;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icu.nothingless.tools.DBPools.PDBPoolManager;

public class PDBUtil {

    private static Logger logger = LoggerFactory.getLogger(PDBUtil.class);
    /**
     * 执行查询（返回 Map 列表，适用于简单查询）
     */
    public static List<Map<String, Object>> executeQuery(String sql, Object... params)
            throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();

        try (Connection conn = PDBPoolManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            logger.info("SQL: {}", sql);
            logger.info("Parameters: {}", java.util.Arrays.toString(params));
            setParameters(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        row.put(metaData.getColumnLabel(i).toUpperCase(), rs.getObject(i));
                    }
                    results.add(row);
                }
            }
        }
        return results;
    }

    /**
     * 执行更新（INSERT/UPDATE/DELETE）
     * 
     * @return 影响行数
     */
    public static int executeUpdate(String sql, Object... params) throws SQLException {
        try (Connection conn = PDBPoolManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            logger.info("SQL: {}", sql);
            logger.info("Parameters: {}", java.util.Arrays.toString(params));

            setParameters(ps, params);
            return ps.executeUpdate();
        }
    }

    /**
     * 执行插入并返回自增 ID
     */
    public static Long executeInsert(String sql, Object... params) throws SQLException {
        try (Connection conn = PDBPoolManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql,
                        Statement.RETURN_GENERATED_KEYS)) {
            
            logger.info("SQL: {}", sql);
            logger.info("Parameters: {}", java.util.Arrays.toString(params));

            setParameters(ps, params);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                // 加非空判断更健壮
                return (rs.next() && !rs.wasNull())? rs.getLong(1) : null;
            }
        }
    }

    /**
     * 事务执行模板
     */
    public static void executeTransaction(TransactionCallback callback) throws SQLException {
        Connection conn = null;
        try {
            conn = PDBPoolManager.getConnection();
            conn.setAutoCommit(false);

            callback.execute(conn);

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    /**
     * 查询单个值（如 COUNT(*)
     */
    public static <T> T queryForObject(String sql, Class<T> requiredType, Object... params)
            throws SQLException {
        try (Connection conn = PDBPoolManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            logger.info("SQL: {}", sql);
            logger.info("Parameters: {}", java.util.Arrays.toString(params));
            setParameters(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Object value = rs.getObject(1);
                    if (requiredType.isInstance(value)) {
                        return (T) value;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 设置参数（防注入）
     */
    private static void setParameters(PreparedStatement ps, Object... params)
            throws SQLException {
        if (params != null) {
            logger.info("Parameters: {}", java.util.Arrays.toString(params));
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
        }
    }

    /**
     * 事务回调接口
     */
    @FunctionalInterface
    public interface TransactionCallback {
        void execute(Connection conn) throws SQLException;
    }
}
