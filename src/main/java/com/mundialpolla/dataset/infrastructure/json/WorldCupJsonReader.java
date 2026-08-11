package com.mundialpolla.dataset.infrastructure.json;

import com.mundialpolla.dataset.infrastructure.json.dto.WorldCupDatasetDto;
import java.io.IOException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Component
public class WorldCupJsonReader {

    private static final String DATASET_LOCATION = "datasets/world-cup-2026.json";

    private final JsonMapper jsonMapper;

    public WorldCupJsonReader(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public WorldCupDatasetDto read() {
        ClassPathResource resource = new ClassPathResource(DATASET_LOCATION);
        if (!resource.exists()) {
            throw new IllegalStateException("dataset missing: classpath:" + DATASET_LOCATION);
        }

        try (var inputStream = resource.getInputStream()) {
            WorldCupDatasetDto dataset = jsonMapper.readValue(inputStream, WorldCupDatasetDto.class);
            if (dataset.matches() == null || dataset.matches().isEmpty()) {
                throw new IllegalStateException("dataset without matches: classpath:" + DATASET_LOCATION);
            }
            return dataset;
        } catch (JacksonException exception) {
            throw new IllegalStateException("invalid JSON dataset: classpath:" + DATASET_LOCATION, exception);
        } catch (IOException exception) {
            throw new IllegalStateException("could not read dataset: classpath:" + DATASET_LOCATION, exception);
        }
    }
}
