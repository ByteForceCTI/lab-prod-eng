package ro.unibuc.hello.service.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import ro.unibuc.hello.exception.InvalidAuthTokenException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtManager {
    private final SecretKey secretKey;
    private static final long EXPIRATION_TIME = 86400000;

    public JwtManager(@Value("${jwt.secret}") String base64Secret) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
    }

    public String generateToken(String username) {
        return Jwts.builder()
                   .setSubject(username)
                   .setIssuedAt(new Date())
                   .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                   .signWith(secretKey, SignatureAlgorithm.HS256)
                   .compact();
    }

    public String extractUsername(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                                .setSigningKey(secretKey)
                                .build()
                                .parseClaimsJws(token)
                                .getBody();
            return claims.getSubject();
        } catch (JwtException e) {
            throw new InvalidAuthTokenException("Invalid token");
        }
    }
}
