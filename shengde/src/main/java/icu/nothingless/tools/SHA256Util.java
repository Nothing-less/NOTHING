package icu.nothingless.tools;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SHA256 加密工具类
 * 前后端共用算法，确保一致性
 */
public class SHA256Util {
    
    /**
     * 将字符串进行 SHA256 加密
     * @param input 原始字符串（UTF-8编码）
     * @return 64位十六进制密文（小写）
     */
    public static String encrypt(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }
        
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            
            // 转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 算法不可用", e);
        }
    }
    
    /**
     * 验证密码（比较两个密文是否一致）
     * @param inputPassword 用户输入的明文（前端已加密传入）
     * @param storedHash 数据库存储的密文
     * @return 是否匹配
     */
    public static boolean verify(String inputPassword, String storedHash) {
        if (inputPassword == null || storedHash == null) {
            return false;
        }
        // 直接比较密文（因为前端已经加密了）
        return inputPassword.equalsIgnoreCase(storedHash);
    }
    
    /**
     * 测试方法：验证前后端一致性
     */
    public static void main(String[] args) {
        String testPassword = "123456";
        String encrypted = encrypt(testPassword);
        System.out.println("测试密码: " + testPassword);
        System.out.println("加密结果: " + encrypted);
        // 应该输出: 8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92
    }
}