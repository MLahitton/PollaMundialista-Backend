package com.mundialpolla.stages.api;

import com.mundialpolla.stages.domain.StageType;
import java.util.UUID;

public record StageResponse(
        UUID id,
        UUID tournamentId,
        Long externalId,
        String name,
        StageType type,
        int orderNumber,
        boolean active
) {
}
