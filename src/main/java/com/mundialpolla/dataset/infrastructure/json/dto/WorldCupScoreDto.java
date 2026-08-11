package com.mundialpolla.dataset.infrastructure.json.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WorldCupScoreDto(
        List<Integer> ft,
        List<Integer> et,
        List<Integer> p
) {
}
