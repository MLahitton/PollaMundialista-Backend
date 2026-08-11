package com.mundialpolla.dataset.infrastructure.json.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WorldCupMatchDto(
        String round,
        Integer num,
        String date,
        String time,
        String team1,
        String team2,
        WorldCupScoreDto score,
        String group
) {
}
