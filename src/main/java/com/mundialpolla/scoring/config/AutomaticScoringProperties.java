package com.mundialpolla.scoring.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.scoring.auto")
public record AutomaticScoringProperties(
        boolean enabled,
        Duration fixedDelay
) {
}
