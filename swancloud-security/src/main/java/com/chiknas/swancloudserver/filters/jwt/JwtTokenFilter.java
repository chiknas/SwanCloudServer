package com.chiknas.swancloudserver.filters.jwt;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Optional;

/**
 * Security filter to authenticate the user if the correct jwt token is passed in the request.
 * If SecurityContextHolder.getContext().setAuthentication(auth) is NOT set then a 403 response is returned.
 */
public class JwtTokenFilter extends GenericFilterBean {
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtTokenConnectionMode jwtTokenConnectionMode;

    public JwtTokenFilter(JwtTokenProvider jwtTokenProvider, JwtTokenConnectionMode jwtTokenConnectionMode) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtTokenConnectionMode = jwtTokenConnectionMode;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain)
            throws IOException, ServletException {

        resolveJwtToken((HttpServletRequest) req).ifPresent(this::authenticateWithToken);
        filterChain.doFilter(req, res);
    }

    /**
     * Find the JTW Token on the http request based on the connection mode
     * set for this filter.
     * Returns empty if the token with the specified mode is not found.
     *
     * @throws IllegalStateException - If a connection mode for the token is not set,
     *                               meaning the filter is not setup correctly.
     */
    private Optional<String> resolveJwtToken(HttpServletRequest req) {
        Optional<String> token;
        switch (jwtTokenConnectionMode) {
            case URL:
                token = jwtTokenProvider.resolveUrlToken(req);
                break;
            case HEADER:
                token = jwtTokenProvider.resolveHeaderToken(req);
                break;
            default:
                throw new IllegalStateException("An access token connection mode must be set for the HTTP filter" +
                        "to know how to resolve the token.");
        }
        return token;
    }

    private void authenticateWithToken(String token) {
        if (jwtTokenProvider.validateToken(token)) {
            Authentication auth = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
    }
}
