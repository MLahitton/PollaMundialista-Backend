package com.mundialpolla.shared.time.api;

import com.mundialpolla.shared.time.application.ApplicationClockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/internal/clock")
@Tag(name = "Clock")
public class ApplicationClockController {

    private final ApplicationClockService applicationClockService;

    public ApplicationClockController(ApplicationClockService applicationClockService) {
        this.applicationClockService = applicationClockService;
    }

    @GetMapping
    @Operation(summary = "Get application clock state")
    public ApplicationClockResponse getCurrentState() {
        return applicationClockService.getCurrentState();
    }

    @PostMapping("/real")
    @Operation(summary = "Use real UTC time")
    public ApplicationClockResponse useRealTime() {
        return applicationClockService.useRealTime();
    }

    @PostMapping("/historical")
    @Operation(summary = "Use a fixed historical instant")
    public ApplicationClockResponse useHistoricalTime(@Valid @RequestBody SetHistoricalTimeRequest request) {
        return applicationClockService.useHistoricalTime(request.instant());
    }
}
