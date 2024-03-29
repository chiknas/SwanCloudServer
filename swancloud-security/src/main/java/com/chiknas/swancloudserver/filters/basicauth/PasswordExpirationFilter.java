package com.chiknas.swancloudserver.filters.basicauth;

import com.chiknas.swancloudserver.entities.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

import static com.chiknas.swancloudserver.SecurityConfiguration.WEBAPP_LOGOUT_URL;
import static com.chiknas.swancloudserver.SecurityConfiguration.WEBAPP_RESET_PASSWORD_URL;

/**
 * Forces the user to change its password if it is expired {@link User#isPasswordExpired()}
 * by redirecting him to the reset password page instead of allowing access to the app.
 */
@Slf4j
public class PasswordExpirationFilter extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        String currentUrl = ((HttpServletRequest) request).getRequestURL().toString();
        Optional<User> loggedInUser = getLoggedInUser();
        if (!isStaticContent(currentUrl) && loggedInUser.isPresent()) {
            User user = loggedInUser.get();

            // Dont allow access to the api until the user changes his password through the webapp
            if (isApiRequest(currentUrl) && user.isPasswordExpired()) {
                forbidden(response);
            }
            // Send user to the reset password page if the password is expired
            else if (!isCurrentPathResetPassword(request) && user.isPasswordExpired()) {
                redirect(WEBAPP_RESET_PASSWORD_URL, response);
            }
            // Lock down the reset password page, only to be used by users with expired password
            else if (isCurrentPathResetPassword(request) && !user.isPasswordExpired()) {
                redirect("/", response);
            }
        }

        continueChain(request, response, chain);
    }


    /**
     * Allow any resources needed for the user to reset his password.
     */
    private boolean isStaticContent(String currentUrl) {
        return currentUrl.contains("/img/") || currentUrl.contains("/css/") ||
                currentUrl.contains(WEBAPP_LOGOUT_URL) ||
                currentUrl.contains("/js/reset_password.js") || currentUrl.contains("/js/refresh_token_interceptor.js");
    }

    private boolean isApiRequest(String currentUrl) {
        return currentUrl.contains("/api/") || currentUrl.contains("/streaming/");
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


    private void redirect(String path, ServletResponse response) {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        try {
            httpResponse.sendRedirect(path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void forbidden(ServletResponse response) {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        try {
            SecurityContextHolder.getContext().setAuthentication(null);
            httpResponse.setStatus(HttpStatus.FORBIDDEN.value());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void continueChain(ServletRequest request, ServletResponse response, FilterChain chain) {
        try {
            chain.doFilter(request, response);
        } catch (ClientAbortException e) {
            // Video tag was complaining that we are too slow and dropping the connection
            // before we responded to the request. This is fine for our app because we are
            // reading from files which takes some time. Give us a break!
            if (log.isDebugEnabled()) {
                log.debug(e.getMessage(), e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
