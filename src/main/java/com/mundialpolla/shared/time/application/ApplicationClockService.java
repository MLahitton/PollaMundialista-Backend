package com.mundialpolla.shared.time.application;

import com.mundialpolla.shared.time.ApplicationClockMode;
import com.mundialpolla.shared.time.SystemApplicationClock;
import com.mundialpolla.shared.time.api.ApplicationClockResponse;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class ApplicationClockService {

    private final SystemApplicationClock systemApplicationClock;

    public ApplicationClockService(SystemApplicationClock systemApplicationClock) {
        this.systemApplicationClock = systemApplicationClock;
    }

    public ApplicationClockResponse getCurrentState() {
        return toResponse();
    }

    public ApplicationClockResponse useRealTime() {
        systemApplicationClock.useRealTime();
        return toResponse();
    }

    public ApplicationClockResponse useHistoricalTime(Instant instant) {
        systemApplicationClock.useHistoricalTime(instant);
        return toResponse();
    }

    private ApplicationClockResponse toResponse() {
        ApplicationClockMode mode = systemApplicationClock.getMode();
        Instant currentTime = systemApplicationClock.now();
        Instant historicalTime = mode == ApplicationClockMode.HISTORICAL_REPLAY
                ? systemApplicationClock.getHistoricalInstant()
                : null;

        return new ApplicationClockResponse(mode, currentTime, historicalTime);
    }
}
