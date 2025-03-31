package ro.unibuc.hello.service.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;

import javax.crypto.SecretKey;

class JwtManagerTest {

    private JwtManager jwtManager;

    @BeforeEach
    void setUp() {
        // generate a new key for each test
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String base64Secret = Encoders.BASE64.encode(key.getEncoded());
        jwtManager = new JwtManager(base64Secret);
    }

    @Test
    void testGenerateTokenAndExtractUsername() {
        String username = "testuser";
        String token = jwtManager.generateToken(username);

        assertNotNull(token, "Token should not be null");
        assertFalse(token.isEmpty(), "Token should not be empty");

        String extractedUsername = jwtManager.extractUsername(token);
        assertEquals(username, extractedUsername, "Extracted username should match");
    }

    @Test
    void testExtractUsernameWithInvalidToken() {
        String invalidToken = "invalid.token.string";

        Exception exception = assertThrows(RuntimeException.class, () -> jwtManager.extractUsername(invalidToken));
        assertTrue(exception.getMessage().contains("Invalid token"), "Exception message should contain 'Invalid token'");
    }
}
