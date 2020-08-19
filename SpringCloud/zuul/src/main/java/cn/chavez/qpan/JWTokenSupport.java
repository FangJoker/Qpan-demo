package cn.chavez.qpan;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.http.util.TextUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Map;

/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/5/18 22:06
 */
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

    public static String getRequeStParam(String param ){
        ServletRequestAttributes res = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = res.getRequest();
        String parameter = request.getParameter(param);
        return parameter;
    }

    public static String getJWTokenUuid() {
        return JWTokenSupport.validateJWToken(JWTokenSupport.getRequestHeader("Access-Token"), "uuid");
    }
    public static String getJWTokenUuid(String token) {
        return JWTokenSupport.validateJWToken(token,"uuid");
    }


    /**
     * 过滤掉没token的请求
     * @return
     * @throws IllegalStateException
     */
    public static boolean validateJWTokenAspect() throws IllegalStateException {
        String token = getRequestHeader("Access-Token");
        String paramToken = getRequeStParam("token");
        System.out.println("token:"+token);
        if (TextUtils.isBlank(token) && TextUtils.isBlank(paramToken)){
            return false;
        } else {
            return true;
        }
    }
}
