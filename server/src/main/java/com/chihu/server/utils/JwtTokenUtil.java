package com.chihu.server.utils;

import com.chihu.server.model.User;
import com.chihu.server.service.ChihuUserDetailsService;
import com.chihu.server.service.UserService;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Component
@Slf4j
public class JwtTokenUtil {
    @Autowired
    private ChihuUserDetailsService userDetailsService;

    @Autowired
    private UserService userService;

    // TODO: PROVIDE AN ACTUAL JWT SECRET!!!
    @Value("${app.jwtSecret:JwtDevSecretKey}")
    private String jwtSecret = "default_dev_secret";

    /**
     * ISO 8601 duration format
     *
     * default to one week
     */
    @Value("${app.jwtDefaultValidDuration:PT604800S}")
    private String jwtDefaultValidDuration;

    private static final String ACTIVATION_TOKEN_AUDIENCE = "link-signature-validator";
    private static final String ACTIVATION_TOKEN_ISSUER = "aid-account-activation";
    private static final String EMAIL_VERIFICATION = "emailVerification";
    private static final String PASSWORD_RESET_TOKEN_AUDIENCE = "password-reset-link-signature-validator";
    private static final String PASSWORD_RESET_TOKEN_ISSUER = "aid-account-password";
    private static final String PASSWORD_RESET = "passwordReset";

    /**
     * ISO 8601 duration format
     *
     * default to one week
     */
    @Value("${app.activationTokenValidDuration:PT604800S}")
    private String activationTokenValidDuration;

    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    public String generateActivationToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("scope", EMAIL_VERIFICATION);
        return createToken(
                claims, username, getActivationValidDuration(),
                ACTIVATION_TOKEN_AUDIENCE, ACTIVATION_TOKEN_ISSUER);
    }

    public String generateActivationToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Map<String, Object> claims = new HashMap<>();
        claims.put("scope", EMAIL_VERIFICATION);

        return createToken(
                claims, userDetails.getUsername(), getActivationValidDuration(),
                ACTIVATION_TOKEN_AUDIENCE, ACTIVATION_TOKEN_ISSUER);
    }

    // TODO: revisit this when there is a more professional web security expert
    public String generatePasswordResetToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("scope", PASSWORD_RESET);

        return createToken(
                claims, user.getUsername(), getActivationValidDuration(),
                PASSWORD_RESET_TOKEN_AUDIENCE,
                PASSWORD_RESET_TOKEN_ISSUER,
                getPasswordResetTokenSecretKey(user.getPassword()));
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return createToken(claims, subject, getDefaultValidDuration(), null, null);
    }

    private String createToken(Map<String, Object> claims, String subject,
                               Duration validDuration, String audience, String issuer) {
        return createToken(claims, subject, getDefaultValidDuration(),
                audience, issuer, jwtSecret);
    }

    private String createToken(Map<String, Object> claims, String subject,
                               Duration validDuration, String audience, String issuer, String securityKey) {
        Instant expirationDate = Instant.now().plus(validDuration);
        JwtBuilder jwtBuilder = Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(expirationDate.toDate())
                .signWith(SignatureAlgorithm.HS512, securityKey);
        if (audience != null && !audience.isBlank()) {
            jwtBuilder.setAudience(audience);
        }
        if (issuer != null && !issuer.isBlank()) {
            jwtBuilder.setIssuer(issuer);
        }
        return jwtBuilder.compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpirationDate(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return extractAllClaims(token, jwtSecret);
    }

    private Claims extractAllClaims(String token, String secretKey) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
    }

    public Duration getDefaultValidDuration() {
        return Duration.parse(jwtDefaultValidDuration);
    }

    private Duration getActivationValidDuration() {
        return Duration.parse(activationTokenValidDuration);
    }

    public boolean isTokenValid(String authToken) {
        return isTokenValid(authToken, jwtSecret);
    }

    public boolean isTokenValid(String authToken, String securityKey) {
        if (!StringUtils.hasText(authToken)) {
            return false;
        }
        try {
            Jwts.parser().setSigningKey(securityKey).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty.", e.getMessage());
        }
        return false;
    }

    public boolean isActivationTokenValid(String activationToken) {
        if (!isTokenValid(activationToken)) {
            return false;
        }
        final Claims claims = extractAllClaims(activationToken);
        return claims.getAudience().equals(ACTIVATION_TOKEN_AUDIENCE)
                && claims.getIssuer().equals(ACTIVATION_TOKEN_ISSUER);
    }

    public boolean isPasswordResetTokenValid(String username, String token) {
        Optional<User> user = userService.getUserByUsername(username);
        if (user.isEmpty()) {
            return false;
        }
        String secretKey = getPasswordResetTokenSecretKey(user.get().getPassword());
        if (!isTokenValid(token, secretKey)) {
            return false;
        }
        final Claims claims = extractAllClaims(token, secretKey);
        return claims.getAudience().equals(PASSWORD_RESET_TOKEN_AUDIENCE)
                && claims.getIssuer().equals(PASSWORD_RESET_TOKEN_ISSUER);
    }

    // Helper methods
    private String getPasswordResetTokenSecretKey(String passwordHash) {
        return jwtSecret + "-" + passwordHash;
    }
}
