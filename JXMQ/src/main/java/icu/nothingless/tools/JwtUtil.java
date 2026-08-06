package icu.nothingless.tools;

// need jjwt-api dependency
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtUtil {
    // ⚠️ 生产环境请把密钥放到配置文件，且长度不少于 32 字符
    private static final String SECRET = "your-256-bit-secret-key-change-in-production!!";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    private static final long EXPIRATION = 86400000; // 24 小时

    public static String generateToken(String userId, String username, String nickname) {
        return Jwts.builder()
            .subject(userId)
            .claim("username", username)
            .claim("nickname", nickname)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
            .signWith(KEY)
            .compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parser()
            .verifyWith(KEY)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public static boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String getUserId(String token) {
        return parseToken(token).getSubject();
    }
}