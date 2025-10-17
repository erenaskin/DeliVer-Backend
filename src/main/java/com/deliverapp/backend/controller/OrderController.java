package com.deliverapp.backend.controller;

import com.deliverapp.backend.dto.request.CreateOrderRequest;
import com.deliverapp.backend.dto.request.UpdateOrderStatusRequest;
import com.deliverapp.backend.dto.response.OrderResponse;
import com.deliverapp.backend.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;
    
    /**
     * Sepetten sipariş oluştur
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        OrderResponse order = orderService.createOrderFromCart(request, userEmail);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }
    
    /**
     * Kullanıcının tüm siparişlerini getir (sayfalama ile)
     */
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getUserOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        Page<OrderResponse> orders = orderService.getUserOrders(userEmail, page, size);
        
        return ResponseEntity.ok(orders);
    }
    
    /**
     * Belirli bir siparişin detayını getir
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long orderId,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        OrderResponse order = orderService.getOrderById(orderId, userEmail);
        
        return ResponseEntity.ok(order);
    }
    
    /**
     * Kullanıcının aktif siparişlerini getir
     */
    @GetMapping("/active")
    public ResponseEntity<List<OrderResponse>> getActiveOrders(
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        List<OrderResponse> activeOrders = orderService.getActiveOrders(userEmail);
        
        return ResponseEntity.ok(activeOrders);
    }
    
    /**
     * Sipariş durumunu güncelle (kullanıcı sadece iptal edebilir)
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        
        // Kullanıcılar sadece CANCELLED status'una geçiş yapabilir
        if (!"CANCELLED".equals(request.getOrderStatus())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .build();
        }
        
        OrderResponse updatedOrder = orderService.updateOrderStatus(orderId, request, userEmail);
        
        return ResponseEntity.ok(updatedOrder);
    }
    
    /**
     * Sipariş numarasına göre sipariş getir
     */
    @GetMapping("/by-number/{orderNumber}")
    public ResponseEntity<OrderResponse> getOrderByNumber(
            @PathVariable String orderNumber,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        OrderResponse order = orderService.getOrderByNumber(orderNumber, userEmail);
        
        return ResponseEntity.ok(order);
    }
}