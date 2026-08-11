package com.mundialpolla.shared.time.api;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record SetHistoricalTimeRequest(
        @NotNull
        Instant instant
) {
}
