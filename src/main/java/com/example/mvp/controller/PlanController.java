package com.example.mvp.controller;

import com.example.mvp.dto.plan.PlanCreateRequest;
import com.example.mvp.dto.plan.PlanResponse;
import com.example.mvp.dto.plan.PlanUpdateRequest;
import com.example.mvp.service.PlanService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/plans")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @PostMapping
    @PreAuthorize("hasRole('PLANNER')")
    public ResponseEntity<PlanResponse> create(@Valid @RequestBody PlanCreateRequest request) {
        return ResponseEntity.ok(planService.createPlan(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PLANNER')")
    public ResponseEntity<PlanResponse> update(@PathVariable Long id, @Valid @RequestBody PlanUpdateRequest request) {
        return ResponseEntity.ok(planService.updatePlan(id, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PLANNER','MASTER')")
    public ResponseEntity<PlanResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(planService.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('PLANNER','MASTER')")
    public ResponseEntity<List<PlanResponse>> getAll(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(planService.getAll(page, size));
    }
}
