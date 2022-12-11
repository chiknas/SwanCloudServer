package com.chiknas.swancloudserver.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordResetRequest {
    private String oldPassword;
    private String newPassword;
}
