package com.example.mvp.controller;

import com.example.mvp.dto.order.OperationCreateRequest;
import com.example.mvp.dto.order.OperationResponse;
import com.example.mvp.dto.order.OrderResponse;
import com.example.mvp.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/{planId}/start")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<OrderResponse> start(@PathVariable Long planId) {
        return ResponseEntity.ok(orderService.startOrder(planId));
    }

    @PostMapping("/{orderId}/operations")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<OperationResponse> addOperation(@PathVariable Long orderId,
                                                          @Valid @RequestBody OperationCreateRequest request) {
        return ResponseEntity.ok(orderService.addOperation(orderId, request));
    }

    @PostMapping("/{orderId}/release")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<OrderResponse> release(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.releaseOrder(orderId));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('PLANNER','MASTER','OPERATOR')")
    public ResponseEntity<List<OrderResponse>> getOrders() {
        return ResponseEntity.ok(orderService.getOrders());
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('PLANNER','MASTER','OPERATOR')")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrder(orderId));
    }
}
