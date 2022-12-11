package com.chiknas.swancloudserver.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenRefreshResponse {
    private String accessToken;
    // Epoch time in seconds of the time the accessToken expires.
    private long accessTokenExpiry;

    @Setter(AccessLevel.NONE)
    private String tokenType = "Bearer";

    public TokenRefreshResponse(String accessToken, long refreshToken) {
        this.accessToken = accessToken;
        this.accessTokenExpiry = refreshToken;
    }
}
