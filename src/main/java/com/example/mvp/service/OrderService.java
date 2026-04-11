package com.example.mvp.service;

import com.example.mvp.audit.AuditService;
import com.example.mvp.dto.order.OperationCreateRequest;
import com.example.mvp.dto.order.OperationResponse;
import com.example.mvp.dto.order.OrderResponse;
import com.example.mvp.entity.OperationExecution;
import com.example.mvp.entity.OrderStatus;
import com.example.mvp.entity.ProductionOrder;
import com.example.mvp.entity.ProductionPlan;
import com.example.mvp.entity.Role;
import com.example.mvp.entity.UserEntity;
import com.example.mvp.exception.ConflictException;
import com.example.mvp.exception.ForbiddenOperationException;
import com.example.mvp.exception.NotFoundException;
import com.example.mvp.repository.OperationExecutionRepository;
import com.example.mvp.repository.ProductionOrderRepository;
import com.example.mvp.security.CurrentUserService;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final ProductionOrderRepository orderRepository;
    private final OperationExecutionRepository operationRepository;
    private final PlanService planService;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    public OrderService(ProductionOrderRepository orderRepository,
                        OperationExecutionRepository operationRepository,
                        PlanService planService,
                        CurrentUserService currentUserService,
                        AuditService auditService) {
        this.orderRepository = orderRepository;
        this.operationRepository = operationRepository;
        this.planService = planService;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
    }

    @Transactional
    public OrderResponse startOrder(Long planId) {
        UserEntity actor = currentUserService.getCurrentUser();
        if (actor.getRole() != Role.MASTER) {
            throw new ForbiddenOperationException("Only master can start order");
        }
        ProductionPlan plan = planService.getEntityById(planId);
        if (orderRepository.findByPlanId(planId).isPresent()) {
            throw new ConflictException("Order already exists for plan");
        }

        ProductionOrder order = new ProductionOrder();
        order.setPlan(plan);
        order.setStatus(OrderStatus.STARTED);
        order.setStartedBy(actor);
        order.setStartedAt(OffsetDateTime.now());
        order.setCreatedAt(OffsetDateTime.now());
        ProductionOrder saved = orderRepository.save(order);

        planService.markInProgress(plan, actor);
        auditService.write(actor, "ORDER_STARTED", "ProductionOrder", saved.getId(),
                "Order started for planId=" + planId);
        return toOrderResponse(saved);
    }

    @Transactional
    public OperationResponse addOperation(Long orderId, OperationCreateRequest request) {
        UserEntity actor = currentUserService.getCurrentUser();
        if (actor.getRole() != Role.OPERATOR) {
            throw new ForbiddenOperationException("Only operator can execute operations");
        }

        ProductionOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.STARTED && order.getStatus() != OrderStatus.OPERATIONS_IN_PROGRESS) {
            throw new ConflictException("Order is not in progress");
        }

        int maxAllowed = order.getPlan().getPlannedQuantity() * 10;
        if (request.completedQuantity() > maxAllowed) {
            throw new ConflictException("completedQuantity is not realistic for this order");
        }

        OperationExecution operation = new OperationExecution();
        operation.setOrder(order);
        operation.setOperationName(request.operationName());
        operation.setCompletedQuantity(request.completedQuantity());
        operation.setPerformedBy(actor);
        operation.setPerformedAt(OffsetDateTime.now());
        OperationExecution saved = operationRepository.save(operation);

        int total = operationRepository.totalCompletedByOrderId(orderId);
        if (total >= order.getPlan().getPlannedQuantity()) {
            order.setStatus(OrderStatus.READY_FOR_RELEASE);
        } else {
            order.setStatus(OrderStatus.OPERATIONS_IN_PROGRESS);
        }
        orderRepository.save(order);

        auditService.write(actor, "OPERATION_EXECUTED", "OperationExecution", saved.getId(),
                "Operation=" + request.operationName() + ", qty=" + request.completedQuantity());
        return toOperationResponse(saved);
    }

    @Transactional
    public OrderResponse releaseOrder(Long orderId) {
        UserEntity actor = currentUserService.getCurrentUser();
        if (actor.getRole() != Role.MASTER) {
            throw new ForbiddenOperationException("Only master can release orders");
        }

        ProductionOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        if (order.getStatus() != OrderStatus.READY_FOR_RELEASE) {
            throw new ConflictException("Order must be READY_FOR_RELEASE before release");
        }

        order.setStatus(OrderStatus.RELEASED);
        order.setReleasedBy(actor);
        order.setReleasedAt(OffsetDateTime.now());
        ProductionOrder saved = orderRepository.save(order);

        planService.markCompleted(order.getPlan(), actor);
        auditService.write(actor, "ORDER_RELEASED", "ProductionOrder", saved.getId(),
                "Order released for planId=" + order.getPlan().getId());
        return toOrderResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrders() {
        UserEntity actor = currentUserService.getCurrentUser();
        if (actor.getRole() == Role.OPERATOR) {
            return orderRepository.findByStatusIn(List.of(OrderStatus.STARTED, OrderStatus.OPERATIONS_IN_PROGRESS, OrderStatus.READY_FOR_RELEASE))
                    .stream()
                    .map(this::toOrderResponse)
                    .toList();
        }
        return orderRepository.findAll().stream().map(this::toOrderResponse).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {
        UserEntity actor = currentUserService.getCurrentUser();
        ProductionOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        if (actor.getRole() == Role.OPERATOR
                && order.getStatus() != OrderStatus.STARTED
                && order.getStatus() != OrderStatus.OPERATIONS_IN_PROGRESS
                && order.getStatus() != OrderStatus.READY_FOR_RELEASE) {
            throw new ForbiddenOperationException("Access denied");
        }
        return toOrderResponse(order);
    }

    private OrderResponse toOrderResponse(ProductionOrder order) {
        return new OrderResponse(
                order.getId(),
                order.getPlan().getId(),
                order.getStatus().name(),
                order.getStartedBy() != null ? order.getStartedBy().getUsername() : null,
                order.getStartedAt(),
                order.getReleasedBy() != null ? order.getReleasedBy().getUsername() : null,
                order.getReleasedAt()
        );
    }

    private OperationResponse toOperationResponse(OperationExecution operation) {
        return new OperationResponse(
                operation.getId(),
                operation.getOrder().getId(),
                operation.getOperationName(),
                operation.getCompletedQuantity(),
                operation.getPerformedBy().getUsername(),
                operation.getPerformedAt()
        );
    }
}
