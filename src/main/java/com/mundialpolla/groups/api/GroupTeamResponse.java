package com.mundialpolla.groups.api;

import java.util.UUID;

public record GroupTeamResponse(
        UUID assignmentId,
        UUID groupId,
        UUID teamId,
        String teamName,
        String teamCode,
        String teamLogoUrl,
        int orderNumber
) {
}
