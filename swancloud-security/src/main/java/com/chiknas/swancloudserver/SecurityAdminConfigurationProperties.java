package com.chiknas.swancloudserver;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties("security.admin")
public class SecurityAdminConfigurationProperties {
    private List<String> accounts;
}
