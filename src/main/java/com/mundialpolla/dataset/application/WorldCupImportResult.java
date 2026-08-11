package com.mundialpolla.dataset.application;

public record WorldCupImportResult(
        int tournamentsCreated,
        int tournamentsUpdated,
        int teamsCreated,
        int teamsUpdated,
        int stagesCreated,
        int stagesUpdated,
        int groupsCreated,
        int groupsUpdated,
        int groupTeamsCreated,
        int matchesCreated,
        int matchesUpdated,
        int matchesUnchanged
) {
}
