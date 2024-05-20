package utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Date;

public class JwtUtil {
    private static final String SECRET ;
    private static final Algorithm ALGORITHM ;

    static {
//        Dotenv dotenv = Dotenv.load();
        SECRET = "D1JcyY87dUlxvAsXH1X9M5cHp7Ou9UCB";
        if (SECRET == null) {
            throw new IllegalArgumentException("SECRET must be set in .env");
        }
        ALGORITHM = Algorithm.HMAC256(SECRET);
    }

    public static String generateToken(String id) {
        return JWT.create()
                .withSubject(id)
                .withExpiresAt(new Date(System.currentTimeMillis() + 60 * 60 * 1000)) // 1 hour
                .sign(ALGORITHM);
    }

    public static String verifyToken(String token) {
        try {
            DecodedJWT jwt = JWT.require(ALGORITHM).build().verify(token);
            return jwt.getSubject();
        } catch (JWTVerificationException exception) {
            return null;
        }
    }
}