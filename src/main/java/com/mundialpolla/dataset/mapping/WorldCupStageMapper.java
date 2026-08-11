package com.mundialpolla.dataset.mapping;

import com.mundialpolla.stages.domain.StageType;
import org.springframework.stereotype.Component;

@Component
public class WorldCupStageMapper {

    public StageType map(String round, String group) {
        if (group != null && !group.trim().isEmpty()) {
            return StageType.GROUP_STAGE;
        }

        String normalizedRound = round == null ? "" : round.trim();
        if (normalizedRound.equalsIgnoreCase("Round of 32")) {
            return StageType.ROUND_OF_32;
        }
        if (normalizedRound.equalsIgnoreCase("Round of 16")) {
            return StageType.ROUND_OF_16;
        }
        if (normalizedRound.equalsIgnoreCase("Quarter-final")) {
            return StageType.QUARTER_FINAL;
        }
        if (normalizedRound.equalsIgnoreCase("Semi-final")) {
            return StageType.SEMI_FINAL;
        }
        if (normalizedRound.equalsIgnoreCase("Match for third place")) {
            return StageType.THIRD_PLACE;
        }
        if (normalizedRound.equalsIgnoreCase("Final")) {
            return StageType.FINAL;
        }

        return StageType.OTHER;
    }
}
