package com.chiknas.swancloudserver.dto.response;

import com.chiknas.swancloudserver.entities.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class JwtResponse {
    private String token;

    @Setter(AccessLevel.NONE)
    private String type = "Bearer";
    private String refreshToken;
    private Long id;
    private String username;
    private List<String> roles;

    public JwtResponse(String token, String refreshToken, Long id, User user) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.id = id;
        this.username = user.getUsername();
        this.roles = user.getRoles();
    }
}
