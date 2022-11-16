package com.chiknas.swancloudserver.filters.basicauth;

import com.chiknas.swancloudserver.entities.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static com.chiknas.swancloudserver.SecurityConfiguration.WEBAPP_RESET_PASSWORD_URL;

/**
 * Forces the user to change its password if it is expired {@link User#isPasswordExpired()}
 * by redirecting him to the reset password page instead of allowing access to the app.
 */
public class PasswordExpirationFilter extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        String currentUrl = ((HttpServletRequest) request).getRequestURL().toString();
        if (!isStaticContent(currentUrl)) {
            Optional<User> loggedInUser = getLoggedInUser();
            loggedInUser.filter(User::isPasswordExpired)
                    .ifPresent(user -> resetPassword(request, response));
            loggedInUser.filter(user -> !user.isPasswordExpired() && isCurrentPathResetPassword(request))
                    // Only allow access to the reset password page if the password of the user is expired.
                    // This will lock down access to resetting the password.
                    .ifPresent(user -> redirect("/", response));

        }
        continueChain(request, response, chain);
    }

    private boolean isStaticContent(String currentUrl) {
        return currentUrl.contains("/img/") || currentUrl.contains("/css/");
    }

    private Optional<User> getLoggedInUser() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getPrincipal)
                .filter(principal -> principal instanceof User)
                .map(principal -> (User) principal);
    }

    private boolean isCurrentPathResetPassword(ServletRequest request) {
        String currentUrl = ((HttpServletRequest) request).getRequestURL().toString();
        return currentUrl.contains(WEBAPP_RESET_PASSWORD_URL);
    }


    private void resetPassword(ServletRequest request, ServletResponse response) {
        // Don't redirect to the reset password page we are already there.
        if (isCurrentPathResetPassword(request)) {
            return;
        }

        redirect(WEBAPP_RESET_PASSWORD_URL, response);
    }

    private void redirect(String path, ServletResponse response) {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        try {
            httpResponse.sendRedirect(path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void continueChain(ServletRequest request, ServletResponse response, FilterChain chain) {
        try {
            chain.doFilter(request, response);
        } catch (IOException | ServletException e) {
            throw new RuntimeException(e);
        }
    }
}