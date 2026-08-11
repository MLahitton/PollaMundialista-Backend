package com.mundialpolla.groups.api;

import java.util.UUID;

public record TournamentGroupResponse(
        UUID id,
        UUID tournamentId,
        UUID stageId,
        Long externalId,
        String name,
        String code,
        int orderNumber
) {
}
