package com.chiknas.swancloudserver.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CurrentUserResponse {

    private LocalDateTime lastUploadedFileDate;
}
