package com.mundialpolla.dataset.infrastructure.json.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WorldCupDatasetDto(
        String name,
        List<WorldCupMatchDto> matches
) {
}
