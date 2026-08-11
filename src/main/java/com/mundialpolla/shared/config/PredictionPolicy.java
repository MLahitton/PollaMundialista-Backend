package com.mundialpolla.shared.config;

import java.time.Duration;

public final class PredictionPolicy {

    public static final Duration PREDICTION_CLOSE_BEFORE = Duration.ofMinutes(15);

    private PredictionPolicy() {
    }
}
