package com.mrblue.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT工具类
 */
@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "mrblue.jwt")
public class JwtUtils {

    private String secret;
    private long expire;
    private String header;

    // 使用 Keys.secretKeyFor(SignatureAlgorithm.HS512) 生成安全的密钥
    private SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        log.info("JWT SecretKey initialized successfully.");
    }

    /**
     * 生成JWT token
     */
    public String generateToken(long userId) {
        Date nowDate = new Date();
        // 过期时间
        Date expireDate = new Date(nowDate.getTime() + expire * 1000);

        String token = Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setSubject(userId + "")
                .setIssuedAt(nowDate)
                .setExpiration(expireDate)
                .signWith(secretKey)
                .compact();
        log.debug("Token generated: {}, expire: {}, value: {}", nowDate, expireDate, token);
        return token;
    }

    /**
     * 根据Token获取Claims
     */
    public Claims getClaimByToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey) // 使用生成的密钥
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            log.debug("Token claims: {}", claims);
            return claims;
        } catch (Exception e) {
            log.debug("validate is token error ", e);
            return null;
        }
    }

    /**
     * 判断Token是否过期
     * @return  true：过期
     */
    public boolean isTokenExpired(Date expiration) {
        Date now = new Date();
        boolean expired = expiration.before(now);
        log.debug("Token expiration: {}, now: {}, expired: {}", expiration, now, expired);
        return expired;
    }
}