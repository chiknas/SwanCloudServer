package com.chiknas.swancloudserver.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CurrentUserResponse {

    private LocalDate lastUploadedFileDate;
}
