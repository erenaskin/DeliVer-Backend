package com.deliverapp.backend.controller;

import com.deliverapp.backend.dto.request.AddToCartRequest;
import com.deliverapp.backend.dto.request.UpdateCartItemRequest;
import com.deliverapp.backend.dto.request.CreateOrderRequest;
import com.deliverapp.backend.dto.response.CartResponse;
import com.deliverapp.backend.dto.response.OrderConfirmationResponse;
import com.deliverapp.backend.dto.response.OrderResponse;
import com.deliverapp.backend.model.User;
import com.deliverapp.backend.repository.UserRepository;
import com.deliverapp.backend.service.CartService;
import com.deliverapp.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final OrderService orderService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        Long userId = getCurrentUserId();
        CartResponse cart = cartService.getCart(userId);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/add")
    public ResponseEntity<CartResponse> addToCart(@Valid @RequestBody AddToCartRequest request) {
        Long userId = getCurrentUserId();
        CartResponse cart = cartService.addToCart(userId, request);
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> updateCartItem(
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        Long userId = getCurrentUserId();
        CartResponse cart = cartService.updateCartItem(userId, cartItemId, request);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> removeFromCart(@PathVariable Long cartItemId) {
        Long userId = getCurrentUserId();
        CartResponse cart = cartService.removeFromCart(userId, cartItemId);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<CartResponse> clearCart() {
        Long userId = getCurrentUserId();
        CartResponse cart = cartService.clearCart(userId);
        return ResponseEntity.ok(cart);
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Integer>> getCartItemCount() {
        Long userId = getCurrentUserId();
        Integer count = cartService.getCartItemCount(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PostMapping("/checkout")
    public ResponseEntity<OrderConfirmationResponse> checkout() {
        try {
            String userEmail = getCurrentUserEmail();
            
            // Sepeti siparişe dönüştür
            OrderResponse orderResponse = orderService.createOrderFromCart(
                CreateOrderRequest.builder()
                    .deliveryAddress("Varsayılan adres") // Frontend'den gelecek
                    .phoneNumber("Varsayılan telefon")   // Frontend'den gelecek
                    .notes("Cart checkout")              // Frontend'den gelecek
                    .build(),
                userEmail
            );
            
            // Order confirmation response oluştur
            OrderConfirmationResponse response = OrderConfirmationResponse.builder()
                    .orderId(orderResponse.getId())
                    .orderNumber(orderResponse.getOrderNumber())
                    .orderStatus(orderResponse.getOrderStatus())
                    .paymentStatus(orderResponse.getPaymentStatus())
                    .totalAmount(orderResponse.getTotalAmount())
                    .deliveryAddress(orderResponse.getDeliveryAddress())
                    .phoneNumber(orderResponse.getPhoneNumber())
                    .notes(orderResponse.getNotes())
                    .estimatedDeliveryTime(orderResponse.getEstimatedDeliveryTime())
                    .createdAt(orderResponse.getCreatedAt())
                    .message("Siparişiniz başarıyla oluşturuldu!")
                    .success(true)
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            // Hata durumunda error response
            OrderConfirmationResponse errorResponse = OrderConfirmationResponse.builder()
                    .message("Sipariş oluşturulurken hata oluştu: " + e.getMessage())
                    .success(false)
                    .build();
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // Helper method to get current user ID from security context
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        
        // Get email from JWT token
        String email = authentication.getName();
        
        // Find user by email and return ID
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        return user.getId();
    }

    // Helper method to get current user email from security context
    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        
        // Get email from JWT token
        return authentication.getName();
    }
}