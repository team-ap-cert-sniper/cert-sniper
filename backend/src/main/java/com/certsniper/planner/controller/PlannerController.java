package com.certsniper.planner.controller;

import com.certsniper.global.security.LoginUser;
import com.certsniper.planner.dto.BarRequest;
import com.certsniper.planner.dto.MilestoneRequest;
import com.certsniper.planner.dto.PersonalScheduleRequest;
import com.certsniper.planner.dto.PlannerResponse;
import com.certsniper.planner.service.PlannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "09. 플래너 다이어리")
@RestController
@RequestMapping("/api/v1/planner")
@RequiredArgsConstructor
public class PlannerController {

    private final PlannerService plannerService;

    @Operation(summary = "플래너 조회 (기간)")
    @GetMapping
    public PlannerResponse getPlanner(
            @LoginUser Long userId,
            @Parameter(description = "조회 시작일") @RequestParam(name = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "조회 종료일") @RequestParam(name = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return plannerService.getPlanner(userId, from, to);
    }

    @Operation(summary = "플래너 모드 전환 (AI_RENDER · MANUAL)")
    @PatchMapping("/mode")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeMode(
            @LoginUser Long userId,
            @Parameter(description = "모드 (AI_RENDER · MANUAL)") @RequestParam(name = "mode") String mode) {
        plannerService.changeMode(userId, mode);
    }

    @Operation(summary = "일정바 등록")
    @PostMapping("/bars")
    @ResponseStatus(HttpStatus.CREATED)
    public void createBar(
            @LoginUser Long userId,
            @Valid @RequestBody BarRequest request) {
        plannerService.createBar(userId, request);
    }

    @Operation(summary = "일정바 수정")
    @PatchMapping("/bars/{barId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateBar(
            @LoginUser Long userId,
            @Parameter(description = "일정바 ID") @PathVariable Long barId,
            @Valid @RequestBody BarRequest request) {
        plannerService.updateBar(userId, barId, request);
    }

    @Operation(summary = "일정바 삭제")
    @DeleteMapping("/bars/{barId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBar(
            @LoginUser Long userId,
            @Parameter(description = "일정바 ID") @PathVariable Long barId) {
        plannerService.deleteBar(userId, barId);
    }

    @Operation(summary = "세부 일정 등록")
    @PostMapping("/bars/{barId}/milestones")
    @ResponseStatus(HttpStatus.CREATED)
    public void createMilestone(
            @LoginUser Long userId,
            @Parameter(description = "일정바 ID") @PathVariable Long barId,
            @Valid @RequestBody MilestoneRequest request) {
        plannerService.createMilestone(userId, barId, request);
    }

    @Operation(summary = "세부 일정 수정")
    @PatchMapping("/milestones/{milestoneId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateMilestone(
            @LoginUser Long userId,
            @Parameter(description = "세부 일정 ID") @PathVariable Long milestoneId,
            @Valid @RequestBody MilestoneRequest request) {
        plannerService.updateMilestone(userId, milestoneId, request);
    }

    @Operation(summary = "세부 일정 삭제")
    @DeleteMapping("/milestones/{milestoneId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMilestone(
            @LoginUser Long userId,
            @Parameter(description = "세부 일정 ID") @PathVariable Long milestoneId) {
        plannerService.deleteMilestone(userId, milestoneId);
    }

    @Operation(summary = "개인 일정 등록")
    @PostMapping("/personal-schedules")
    @ResponseStatus(HttpStatus.CREATED)
    public void createPersonalSchedule(
            @LoginUser Long userId,
            @Valid @RequestBody PersonalScheduleRequest request) {
        plannerService.createPersonalSchedule(userId, request);
    }

    @Operation(summary = "개인 일정 수정")
    @PatchMapping("/personal-schedules/{scheduleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePersonalSchedule(
            @LoginUser Long userId,
            @Parameter(description = "시험 일정 ID") @PathVariable Long scheduleId,
            @Valid @RequestBody PersonalScheduleRequest request) {
        plannerService.updatePersonalSchedule(userId, scheduleId, request);
    }

    @Operation(summary = "개인 일정 삭제")
    @DeleteMapping("/personal-schedules/{scheduleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePersonalSchedule(
            @LoginUser Long userId,
            @Parameter(description = "시험 일정 ID") @PathVariable Long scheduleId) {
        plannerService.deletePersonalSchedule(userId, scheduleId);
    }
}
