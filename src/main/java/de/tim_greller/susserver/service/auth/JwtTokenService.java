package de.tim_greller.susserver.service.auth;

import java.time.Duration;
import java.time.Instant;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    private static final Duration JWT_TOKEN_VALIDITY = Duration.ofHours(1);

    private final Algorithm hmac512;
    private final JWTVerifier verifier;

    public JwtTokenService(@Value("${jwt.secret}") final String secret) {
        this.hmac512 = Algorithm.HMAC512(secret);
        this.verifier = JWT.require(this.hmac512).build();
    }

    public String generateToken(final UserDetails userDetails) {
        return generateToken(userDetails.getUsername());
    }

    public String generateToken(final String username) {
        final Instant now = Instant.now();
        return JWT.create()
                .withSubject(username)
                .withIssuer("app")
                .withIssuedAt(now)
                .withExpiresAt(now.plusMillis(JWT_TOKEN_VALIDITY.toMillis()))
                .sign(this.hmac512);
    }

    public String validateTokenAndGetUsername(final String token) {
        try {
            return verifier.verify(token).getSubject();
        } catch (final JWTVerificationException verificationEx) {
            System.err.println("token invalid: " + verificationEx.getMessage());
            return null;
        }
    }
}