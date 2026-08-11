package com.mundialpolla.shared.time;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Component;

@Component
public class SystemApplicationClock implements ApplicationClock {

    private final Clock clock;
    private final AtomicReference<ClockState> state = new AtomicReference<>(ClockState.real());

    public SystemApplicationClock() {
        this(Clock.systemUTC());
    }

    SystemApplicationClock(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Instant now() {
        ClockState currentState = state.get();
        if (currentState.mode() == ApplicationClockMode.HISTORICAL_REPLAY) {
            return currentState.historicalInstant();
        }

        return Instant.now(clock);
    }

    public ApplicationClockMode getMode() {
        return state.get().mode();
    }

    public Instant getHistoricalInstant() {
        return state.get().historicalInstant();
    }

    public void useRealTime() {
        state.set(ClockState.real());
    }

    public void useHistoricalTime(Instant instant) {
        if (instant == null) {
            throw new IllegalArgumentException("instant must not be null");
        }

        state.set(ClockState.historical(instant));
    }

    private record ClockState(ApplicationClockMode mode, Instant historicalInstant) {

        private static ClockState real() {
            return new ClockState(ApplicationClockMode.REAL, null);
        }

        private static ClockState historical(Instant historicalInstant) {
            return new ClockState(ApplicationClockMode.HISTORICAL_REPLAY, historicalInstant);
        }
    }
}
