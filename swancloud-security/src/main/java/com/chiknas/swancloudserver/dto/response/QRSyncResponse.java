package com.chiknas.swancloudserver.dto.response;

import lombok.*;

/**
 * Information to be encoded to a QR code to be used to sync the user account to other swancloud apps.
 * ex. mobile app.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QRSyncResponse {

    private String baseServerUrl;
    private String refreshToken;
    private String email;
    // Epoch seconds timestamp when the refresh token expires.
    private long expiryTime;


}
