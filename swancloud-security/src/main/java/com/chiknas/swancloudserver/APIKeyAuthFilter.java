package com.chiknas.swancloudserver;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Authentication filter to authenticate applications based on the api key provided with the request.
 * Only known applications should be allowed through this filter.
 *
 * @author nkukn
 * @since 2/17/2021
 */
public class APIKeyAuthFilter extends AbstractPreAuthenticatedProcessingFilter {

    private final static String AUTHORIZATION_HEADER = "Authorization";

    public APIKeyAuthFilter(List<String> knownApiKeys) {

        List<String> hashedKnownKeys = knownApiKeys.stream()
                .map(APIKeyAuthFilter::hashSHA256).collect(Collectors.toUnmodifiableList());

        setAuthenticationManager(authentication -> {
            String principal = (String) authentication.getPrincipal();
            if (!hashedKnownKeys.contains(principal)) {
                throw new BadCredentialsException("Unauthorized!");
            }
            authentication.setAuthenticated(true);
            return authentication;
        });
    }

    protected static String hashSHA256(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        return request.getHeader(AUTHORIZATION_HEADER);
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return "N/A";
    }
}