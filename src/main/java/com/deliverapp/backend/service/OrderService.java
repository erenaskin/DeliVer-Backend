package com.deliverapp.backend.service;

import com.deliverapp.backend.dto.request.CreateOrderRequest;
import com.deliverapp.backend.dto.request.UpdateOrderStatusRequest;
import com.deliverapp.backend.dto.response.OrderResponse;
import com.deliverapp.backend.dto.response.OrderItemResponse;
import com.deliverapp.backend.exception.ResourceNotFoundException;
import com.deliverapp.backend.model.*;
import com.deliverapp.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartService cartService;
    private final UserRepository userRepository;
    
    @Transactional
    public OrderResponse createOrderFromCart(CreateOrderRequest request, String userEmail) {
        log.info("Creating order from cart for user: {}", userEmail);
        
        // Kullanıcıyı getir
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));
        
        // Kullanıcının sepetini getir
        Cart cart = cartService.getCartByUserId(user.getId());
        
        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new IllegalStateException("Sepet boş, sipariş oluşturulamaz");
        }
        
        // Sipariş numarası üret
        String orderNumber = generateOrderNumber();
        
        // Toplam tutarı hesapla
        BigDecimal totalAmount = cart.getCartItems().stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Siparişi oluştur
        Order order = Order.builder()
                .user(user)
                .orderNumber(orderNumber)
                .orderStatus(Order.OrderStatus.PENDING)
                .paymentStatus(Order.PaymentStatus.PENDING)
                .totalAmount(totalAmount)
                .deliveryAddress(request.getDeliveryAddress())
                .phoneNumber(request.getPhoneNumber())
                .notes(request.getNotes())
                .estimatedDeliveryTime(LocalDateTime.now().plusMinutes(45)) // 45 dakika tahmini
                .build();
        
        order = orderRepository.save(order);
        final Order savedOrder = order; // Lambda için final reference
        
        // Sepet item'larını sipariş item'larına dönüştür
        List<OrderItem> orderItems = cart.getCartItems().stream()
                .map(cartItem -> createOrderItemFromCartItem(cartItem, savedOrder))
                .collect(Collectors.toList());
        
        orderItemRepository.saveAll(orderItems);
        savedOrder.setOrderItems(orderItems);
        
        log.info("Order created successfully with ID: {} for user: {}", savedOrder.getId(), userEmail);
        
        // Sipariş başarıyla oluşturulduktan sonra sepeti temizle
        // Cart clearing hatasının transaction'ı etkilememesi için ayrı method'da yapıyoruz
        return mapToOrderResponse(savedOrder);
    }
    
    private OrderItem createOrderItemFromCartItem(CartItem cartItem, Order order) {
        return OrderItem.builder()
                .order(order)
                .product(cartItem.getProduct())
                .variant(cartItem.getVariant())
                .quantity(cartItem.getQuantity())
                .unitPrice(cartItem.getUnitPrice())
                .totalPrice(cartItem.getTotalPrice())
                .productName(cartItem.getProduct().getName())
                .productDescription(cartItem.getProduct().getDescription())
                .variantName(cartItem.getVariant() != null ? cartItem.getVariant().getVariantName() : null)
                .selectedOptions(cartItem.getSelectedOptions())
                .specialNotes(cartItem.getSpecialNotes())
                .build();
    }
    
    @Transactional(readOnly = true)
    public Page<OrderResponse> getUserOrders(String userEmail, int page, int size) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
        
        return orders.map(this::mapToOrderResponse);
    }
    
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));
        
        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Sipariş bulunamadı"));
        
        return mapToOrderResponse(order);
    }
    
    @Transactional(readOnly = true)
    public List<OrderResponse> getActiveOrders(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));
        
        List<Order> activeOrders = orderRepository.findActiveOrdersByUserId(user.getId());
        
        return activeOrders.stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request, String userEmail) {
        // Bu method admin kullanıcılar için de genişletilebilir
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));
        
        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Sipariş bulunamadı"));
        
        // Status güncellemelerini sadece belirli durumlar arasında izin ver
        Order.OrderStatus newStatus = Order.OrderStatus.valueOf(request.getOrderStatus());
        validateStatusTransition(order.getOrderStatus(), newStatus);
        
        order.setOrderStatus(newStatus);
        
        // Teslim edildiğinde gerçek teslimat zamanını kaydet
        if (newStatus == Order.OrderStatus.DELIVERED) {
            order.setActualDeliveryTime(LocalDateTime.now());
            order.setPaymentStatus(Order.PaymentStatus.PAID); // Teslimatta ödeme tamamlandı kabul et
        }
        
        order = orderRepository.save(order);
        
        log.info("Order {} status updated to {} by user {}", orderId, newStatus, userEmail);
        
        return mapToOrderResponse(order);
    }
    
    private void validateStatusTransition(Order.OrderStatus currentStatus, Order.OrderStatus newStatus) {
        // Basit validation - daha karmaşık iş kuralları eklenebilir
        if (currentStatus == Order.OrderStatus.DELIVERED || currentStatus == Order.OrderStatus.CANCELLED) {
            throw new IllegalStateException("Teslim edilmiş veya iptal edilmiş sipariş güncellenemez");
        }
        
        // Kullanıcılar sadece iptal edebilir
        if (newStatus == Order.OrderStatus.CANCELLED) {
            if (currentStatus == Order.OrderStatus.OUT_FOR_DELIVERY || currentStatus == Order.OrderStatus.PREPARING) {
                throw new IllegalStateException("Hazırlanan veya yola çıkmış sipariş iptal edilemez");
            }
        }
    }
    
    private String generateOrderNumber() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfNextDay = startOfDay.plusDays(1);
        
        String dateStr = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Long todayCount = orderRepository.countTodaysOrders(startOfDay, startOfNextDay);
        String orderNumber = String.format("DLV%s%04d", dateStr, todayCount + 1);
        
        // Unique kontrolü
        while (orderRepository.findByOrderNumber(orderNumber).isPresent()) {
            todayCount++;
            orderNumber = String.format("DLV%s%04d", dateStr, todayCount + 1);
        }
        
        return orderNumber;
    }
    
    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> orderItems = order.getOrderItems().stream()
                .map(this::mapToOrderItemResponse)
                .collect(Collectors.toList());
        
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .orderStatus(order.getOrderStatus().toString())
                .paymentStatus(order.getPaymentStatus().toString())
                .totalAmount(order.getTotalAmount())
                .deliveryAddress(order.getDeliveryAddress())
                .phoneNumber(order.getPhoneNumber())
                .notes(order.getNotes())
                .estimatedDeliveryTime(order.getEstimatedDeliveryTime())
                .actualDeliveryTime(order.getActualDeliveryTime())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .orderItems(orderItems)
                .userId(order.getUser().getId())
                .userEmail(order.getUser().getEmail())
                .build();
    }
    
    private OrderItemResponse mapToOrderItemResponse(OrderItem orderItem) {
        return OrderItemResponse.builder()
                .id(orderItem.getId())
                .productId(orderItem.getProduct().getId())
                .productName(orderItem.getProductName())
                .productDescription(orderItem.getProductDescription())
                .variantId(orderItem.getVariant() != null ? orderItem.getVariant().getId() : null)
                .variantName(orderItem.getVariantName())
                .quantity(orderItem.getQuantity())
                .unitPrice(orderItem.getUnitPrice())
                .totalPrice(orderItem.getTotalPrice())
                .selectedOptions(orderItem.getSelectedOptions())
                .specialNotes(orderItem.getSpecialNotes())
                .createdAt(orderItem.getCreatedAt())
                .build();
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void clearCartAfterOrder(Long userId) {
        try {
            cartService.clearCart(userId);
            log.info("Cart cleared successfully after order creation for user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to clear cart after order creation for user: {}. Error: {}", userId, e.getMessage());
            // Exception'ı throw etmeyin, böylece ana transaction rollback olmaz
        }
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderByNumber(String orderNumber, String userEmail) {
        log.info("Getting order by number: {} for user: {}", orderNumber, userEmail);
        
        // Kullanıcıyı getir
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));
        
        // Order number ve user ID ile siparişi bul (orderItems ile birlikte)
        Order order = orderRepository.findByOrderNumberAndUserIdWithItems(orderNumber, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Sipariş bulunamadı: " + orderNumber));
        
        log.info("Order found with number: {} for user: {}", orderNumber, userEmail);
        
        return mapToOrderResponse(order);
    }
}