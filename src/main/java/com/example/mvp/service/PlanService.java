package com.example.mvp.service;

import com.example.mvp.audit.AuditService;
import com.example.mvp.dto.plan.PlanCreateRequest;
import com.example.mvp.dto.plan.PlanResponse;
import com.example.mvp.dto.plan.PlanUpdateRequest;
import com.example.mvp.entity.PlanStatus;
import com.example.mvp.entity.ProductionPlan;
import com.example.mvp.entity.Role;
import com.example.mvp.entity.UserEntity;
import com.example.mvp.exception.ConflictException;
import com.example.mvp.exception.ForbiddenOperationException;
import com.example.mvp.exception.NotFoundException;
import com.example.mvp.repository.ProductionOrderRepository;
import com.example.mvp.repository.ProductionPlanRepository;
import com.example.mvp.security.CurrentUserService;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlanService {

    private final ProductionPlanRepository planRepository;
    private final ProductionOrderRepository orderRepository;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    public PlanService(ProductionPlanRepository planRepository,
                       ProductionOrderRepository orderRepository,
                       CurrentUserService currentUserService,
                       AuditService auditService) {
        this.planRepository = planRepository;
        this.orderRepository = orderRepository;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
    }

    @Transactional
    public PlanResponse createPlan(PlanCreateRequest request) {
        UserEntity actor = currentUserService.getCurrentUser();
        if (actor.getRole() != Role.PLANNER) {
            throw new ForbiddenOperationException("Only planner can create plans");
        }
        OffsetDateTime now = OffsetDateTime.now();
        ProductionPlan plan = new ProductionPlan();
        plan.setProductCode(request.productCode());
        plan.setProductName(request.productName());
        plan.setPlannedQuantity(request.plannedQuantity());
        plan.setPlannedDate(request.plannedDate());
        plan.setStatus(PlanStatus.APPROVED);
        plan.setCreatedBy(actor);
        plan.setUpdatedBy(actor);
        plan.setCreatedAt(now);
        plan.setUpdatedAt(now);
        ProductionPlan saved = planRepository.save(plan);
        auditService.write(actor, "PLAN_CREATED", "ProductionPlan", saved.getId(),
                "Plan created for productCode=" + saved.getProductCode());
        return toResponse(saved);
    }

    @Transactional
    public PlanResponse updatePlan(Long planId, PlanUpdateRequest request) {
        UserEntity actor = currentUserService.getCurrentUser();
        if (actor.getRole() != Role.PLANNER) {
            throw new ForbiddenOperationException("Only planner can update plans");
        }
        ProductionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new NotFoundException("Plan not found"));

        if (!plan.getCreatedBy().getId().equals(actor.getId())) {
            throw new ForbiddenOperationException("Only plan owner can update plan");
        }
        if (orderRepository.findByPlanId(planId).isPresent() || plan.getStatus() == PlanStatus.IN_PROGRESS
                || plan.getStatus() == PlanStatus.COMPLETED) {
            throw new ConflictException("Plan cannot be changed after production start");
        }

        plan.setProductCode(request.productCode());
        plan.setProductName(request.productName());
        plan.setPlannedQuantity(request.plannedQuantity());
        plan.setPlannedDate(request.plannedDate());
        plan.setUpdatedBy(actor);
        plan.setUpdatedAt(OffsetDateTime.now());
        ProductionPlan saved = planRepository.save(plan);
        auditService.write(actor, "PLAN_UPDATED", "ProductionPlan", saved.getId(),
                "Plan updated for productCode=" + saved.getProductCode());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PlanResponse getById(Long id) {
        UserEntity actor = currentUserService.getCurrentUser();
        if (actor.getRole() == Role.OPERATOR) {
            throw new ForbiddenOperationException("Access denied");
        }
        ProductionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Plan not found"));
        return toResponse(plan);
    }

    @Transactional(readOnly = true)
    public List<PlanResponse> getAll(int page, int size) {
        UserEntity actor = currentUserService.getCurrentUser();
        if (actor.getRole() == Role.OPERATOR) {
            throw new ForbiddenOperationException("Access denied");
        }
        return planRepository.findAll(PageRequest.of(page, size)).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProductionPlan getEntityById(Long id) {
        return planRepository.findById(id).orElseThrow(() -> new NotFoundException("Plan not found"));
    }

    @Transactional
    public void markInProgress(ProductionPlan plan, UserEntity actor) {
        plan.setStatus(PlanStatus.IN_PROGRESS);
        plan.setUpdatedBy(actor);
        plan.setUpdatedAt(OffsetDateTime.now());
        planRepository.save(plan);
    }

    @Transactional
    public void markCompleted(ProductionPlan plan, UserEntity actor) {
        plan.setStatus(PlanStatus.COMPLETED);
        plan.setUpdatedBy(actor);
        plan.setUpdatedAt(OffsetDateTime.now());
        planRepository.save(plan);
    }

    private PlanResponse toResponse(ProductionPlan plan) {
        return new PlanResponse(
                plan.getId(),
                plan.getProductCode(),
                plan.getProductName(),
                plan.getPlannedQuantity(),
                plan.getPlannedDate(),
                plan.getStatus().name(),
                plan.getCreatedBy().getUsername(),
                plan.getUpdatedBy().getUsername(),
                plan.getCreatedAt(),
                plan.getUpdatedAt()
        );
    }
}
