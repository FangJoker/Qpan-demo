package cn.chavez.qpan.support.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.http.util.TextUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Map;

/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/3/30 22:50
 */
@Component
public class JWTokenSupport {
    private static String KEY;

    @Value("${q-pan.jwtToken.key}")
    private void setKey(String key) {
        KEY = key;
    }

    private final static long DEFAULT_EXPIRATION_TIME = 60 * 1000 * 60;

    /**
     * 生成JWToken
     *
     * @param map
     * @param expirationTime
     * @return
     */
    public static String generateJWToken(Map<String, Object> map, long expirationTime) {
        map.put("expiration_time", expirationTime);
        String jwt = Jwts.builder()
                .setClaims(map)
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SignatureAlgorithm.HS512, KEY)
                .compact();
        return jwt;
    }

    public static String generateJWToken(Map<String, Object> map) {
        map.put("expiration_time", System.currentTimeMillis() + DEFAULT_EXPIRATION_TIME);
        String jwt = Jwts.builder()
                .setClaims(map)
                .setExpiration(new Date(System.currentTimeMillis() + DEFAULT_EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, KEY)
                .compact();
        return jwt;
    }

    public static String validateJWToken(String token, String key) {
        try {
            Map<String, Object> map = Jwts.parser()
                    .setSigningKey(KEY)
                    .parseClaimsJws(token)
                    .getBody();
            return String.valueOf(map.get(key));
        } catch (Exception e) {
            throw new IllegalStateException("Invalid Token: " + e.getMessage());
        }
    }

    public static String getRequestHeader(String header) {
        ServletRequestAttributes res = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = res.getRequest();
        String accessToken = request.getHeader(header);
        return accessToken;
    }

    public static String getJWTokenUuid() {
        return JWTokenSupport.validateJWToken(JWTokenSupport.getRequestHeader("Access-Token"), "uuid");
    }
    public static String getJWTokenUuid(String token) {
        return JWTokenSupport.validateJWToken(token,"uuid");
    }


    public static boolean validateJWTokenAspect() throws IllegalStateException {
        String token = getRequestHeader("Access-Token");
        if (TextUtils.isBlank(token)) {
            return false;
        } else {
            //判断token是否过期
            String uuid = JWTokenSupport.validateJWToken(token, "uuid");
            return !TextUtils.isBlank(uuid) ? true : false;
        }
    }
}
