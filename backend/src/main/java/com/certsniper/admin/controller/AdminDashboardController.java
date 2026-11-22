package com.certsniper.admin.controller;

import com.certsniper.admin.dto.DashboardResponse;
import com.certsniper.admin.service.AdminDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "16. 관리자")
@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @Operation(summary = "대시보드 건수")
    @GetMapping
    public DashboardResponse getDashboard() {
        return adminDashboardService.getDashboard();
    }
}
