package com.mundialpolla.shared.time.api;

import com.mundialpolla.shared.time.ApplicationClockMode;
import java.time.Instant;

public record ApplicationClockResponse(
        ApplicationClockMode mode,
        Instant currentTime,
        Instant historicalTime
) {
}
