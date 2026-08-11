package com.mundialpolla.dataset.api;

import com.mundialpolla.dataset.application.WorldCupImportResult;
import com.mundialpolla.dataset.application.WorldCupImportService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/internal/dataset/world-cup-2026")
@Tag(name = "Dataset")
public class WorldCupDatasetController {

    private final WorldCupImportService worldCupImportService;

    public WorldCupDatasetController(WorldCupImportService worldCupImportService) {
        this.worldCupImportService = worldCupImportService;
    }

    @PostMapping("/import")
    public WorldCupImportResult importDataset() {
        return worldCupImportService.importDataset();
    }
}
