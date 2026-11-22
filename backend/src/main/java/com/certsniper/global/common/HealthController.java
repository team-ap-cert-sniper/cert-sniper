package com.certsniper.global.common;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 서버 상태 확인 (docs/API_CONTRACT.md 6번) */
@Tag(name = "00. 공통", description = "서버 상태 확인")
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @Value("${app.schema-version}")
    private String schemaVersion;

    @Operation(summary = "서버 상태 확인")
    @GetMapping
    public Map<String, Object> health() {
        return Map.of("status", "ok", "schema_version", schemaVersion);
    }
}
