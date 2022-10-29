package com.chiknas.swancloudserver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Filter that should be used after the authentication phase. When a user has been successfully authenticated
 * this filter will check his details and kick him out if his account is not known to this system.
 */
@Slf4j
public class AuthorizedUsersFilter extends GenericFilterBean {

    // Email accounts that have access to this system.
    // Should be setup as env variables on system startup and CAN NOT BE CHANGED.
    private final List<String> adminEmailAccounts;

    public AuthorizedUsersFilter(List<String> adminEmailAccounts) {
        this.adminEmailAccounts = adminEmailAccounts;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication()).ifPresent(auth ->
        {
            String email = ((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getPrincipal().getAttribute("email");
            if (!adminEmailAccounts.contains(email)) {
                SecurityContextHolder.getContext().getAuthentication().setAuthenticated(false);
                log.error("User with email: " + email + " tried to login unauthorized!!");
            }
        });
        chain.doFilter(request, response);
    }
}
