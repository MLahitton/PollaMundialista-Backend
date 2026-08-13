package com.mundialpolla.predictions.application;

import com.mundialpolla.predictions.domain.Prediction;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class PredictionVisibilityResolver {

    public PredictionVisibility resolve(Prediction prediction, Instant now) {
        if (now.isBefore(prediction.getMatch().getPredictionClosesAt())) {
            return PredictionVisibility.PRIVATE;
        }

        return PredictionVisibility.PUBLIC;
    }
}
