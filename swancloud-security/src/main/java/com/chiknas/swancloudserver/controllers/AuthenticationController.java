package com.chiknas.swancloudserver.controllers;

import com.chiknas.swancloudserver.dto.request.LoginRequest;
import com.chiknas.swancloudserver.dto.request.PasswordResetRequest;
import com.chiknas.swancloudserver.dto.request.TokenRefreshRequest;
import com.chiknas.swancloudserver.dto.response.JwtResponse;
import com.chiknas.swancloudserver.dto.response.TokenRefreshResponse;
import com.chiknas.swancloudserver.entities.RefreshToken;
import com.chiknas.swancloudserver.entities.User;
import com.chiknas.swancloudserver.filters.jwt.JwtTokenProvider;
import com.chiknas.swancloudserver.services.RefreshTokenService;
import com.chiknas.swancloudserver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Optional;

import static com.chiknas.swancloudserver.SecurityConfiguration.WEBAPP_LOGIN_URL;

@RestController
@RequestMapping("auth")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

    @Autowired
    public AuthenticationController(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider,
                                    RefreshTokenService refreshTokenService, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenService = refreshTokenService;
        this.userService = userService;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        return Optional.ofNullable(authenticate(loginRequest))
                .map(authentication -> {
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    User user = (User) authentication.getPrincipal();

                    String accessToken = jwtTokenProvider.createToken(user);
                    RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

                    return ResponseEntity.ok(new JwtResponse(accessToken, refreshToken.getToken(), user.getId(), user));
                }).orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    private Authentication authenticate(LoginRequest loginRequest) {
        return authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshtoken(@RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtTokenProvider.createToken(user);
                    return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
                })
                .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));
    }

    @PostMapping(value = "/resetpassword", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<?> resetPassword(@RequestBody MultiValueMap<String, String> paramMap, HttpSession session) {
        PasswordResetRequest passwordResetRequest = new PasswordResetRequest();
        passwordResetRequest.setOldPassword(paramMap.get("oldPassword").get(0));
        passwordResetRequest.setNewPassword(paramMap.get("newPassword").get(0));

        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(authentication -> resetPassword(authentication, passwordResetRequest, session))
                .orElseThrow(() -> new RuntimeException("No authenticated user found!"));
    }


    private ResponseEntity<?> resetPassword(Authentication authentication, PasswordResetRequest request, HttpSession session) {
        User user = (User) authentication.getPrincipal();
        return userService.changePassword(user, request).map(updatedUser -> {

                    // Kick user out, so he can log in with the new password
                    SecurityContextHolder.clearContext();
                    if (session != null) {
                        session.invalidate();
                    }

                    HttpHeaders headers = new HttpHeaders();
                    headers.add("Location", WEBAPP_LOGIN_URL);
                    return new ResponseEntity<String>(headers, HttpStatus.TEMPORARY_REDIRECT);
                })
                .orElseThrow(() -> new RuntimeException("Password reset for user " + user.getUsername() + " failed!"));
    }
}
