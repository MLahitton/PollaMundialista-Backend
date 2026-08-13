package com.mundialpolla.scoring.application;

import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        prefix = "app.scoring.auto",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class AutomaticScoringScheduler {

    private final AutomaticScoringService automaticScoringService;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public AutomaticScoringScheduler(AutomaticScoringService automaticScoringService) {
        this.automaticScoringService = automaticScoringService;
    }

    @Scheduled(fixedDelayString = "${app.scoring.auto.fixed-delay}")
    public void run() {
        if (!running.compareAndSet(false, true)) {
            return;
        }

        try {
            automaticScoringService.runOnce();
        } finally {
            running.set(false);
        }
    }
}
