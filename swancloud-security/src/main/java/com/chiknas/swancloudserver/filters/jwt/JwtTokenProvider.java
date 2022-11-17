package com.chiknas.swancloudserver.filters.jwt;

import com.chiknas.swancloudserver.entities.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import static com.chiknas.swancloudserver.SecurityConfiguration.JWT_TOKEN_NAME;

@Slf4j
@Component
public class JwtTokenProvider {
    @Value("${security.jwt.token.expire-length:3600000}")
    private long validityInMilliseconds;

    private final SignatureAlgorithm HASH_ALGORITHM = SignatureAlgorithm.HS256;

    private final UserDetailsService userDetailsService;

    private final SecretKey secretKey;

    @Autowired
    public JwtTokenProvider(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
        this.secretKey = Keys.secretKeyFor(HASH_ALGORITHM);
    }

    /**
     * Generates a new JWT token based on given userName.
     * The token is signed with the {@link JwtTokenProvider#HASH_ALGORITHM} and is valid for
     * {@link JwtTokenProvider#validityInMilliseconds} milliseconds.
     */
    public String createToken(User user) {
        Claims claims = Jwts.claims().setSubject(user.getUsername());
        claims.put("roles", user.getRoles());
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey, HASH_ALGORITHM)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(getUsername(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String getUsername(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody().getSubject();
    }

    public Optional<String> resolveToken(HttpServletRequest req) {
        // Search request header for token (this is used on the API)
        String bearerToken = req.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return Optional.of(bearerToken.substring(7));
        }

        // Search jwt cookie for token (this is used in the WEB APP)
        return Optional.ofNullable(req.getCookies())
                .map(Arrays::stream)
                .flatMap(cookies ->
                        cookies.filter(cookie -> JWT_TOKEN_NAME.equals(cookie.getName())).findFirst().map(Cookie::getValue)
                );
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}
