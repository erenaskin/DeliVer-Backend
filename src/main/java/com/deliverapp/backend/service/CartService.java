package com.deliverapp.backend.service;

import com.deliverapp.backend.dto.request.AddToCartRequest;
import com.deliverapp.backend.dto.request.UpdateCartItemRequest;
import com.deliverapp.backend.dto.response.CartItemResponse;
import com.deliverapp.backend.dto.response.CartResponse;
import com.deliverapp.backend.model.*;
import com.deliverapp.backend.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    // Get or create user's cart
    public CartResponse getCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<Cart> cartOpt = cartRepository.findByUserIdWithCartItems(userId);
        
        if (cartOpt.isEmpty()) {
            // Create new cart for user
            Cart newCart = new Cart();
            newCart.setUser(user);
            newCart = cartRepository.save(newCart);
            return convertToCartResponse(newCart);
        }
        
        Cart cart = cartOpt.get();
        // Recalculate totals to ensure consistency
        cart.calculateTotals();
        cart = cartRepository.save(cart);
        
        return convertToCartResponse(cart);
    }

    // Add item to cart
    public CartResponse addToCart(Long userId, AddToCartRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ProductVariant variant = null;
        if (request.getProductVariantId() != null) {
            variant = productVariantRepository.findById(request.getProductVariantId())
                    .orElseThrow(() -> new RuntimeException("Product variant not found"));
        }

        // Get or create cart
        Cart cart = cartRepository.findByUserAndIsActiveTrue(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        // Check if item already exists in cart
        Optional<CartItem> existingItemOpt = variant != null
                ? cartItemRepository.findByCartAndProductAndProductVariant(cart, product, variant)
                : cartItemRepository.findByCartAndProduct(cart, product);

        CartItem cartItem;
        if (existingItemOpt.isPresent()) {
            // Update existing item quantity
            cartItem = existingItemOpt.get();
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
        } else {
            // Create new cart item
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setProductVariant(variant);
            cartItem.setQuantity(request.getQuantity());
        }

        // Set unit price and calculate subtotal
        cartItem.setUnitPriceFromProduct();
        
        // Set selected options if provided
        if (request.getSelectedOptions() != null) {
            cartItem.setSelectedOptionsJson(toJson(request.getSelectedOptions()));
        }
        
        // Set notes if provided
        if (request.getNotes() != null) {
            cartItem.setNotes(request.getNotes());
        }

        cartItemRepository.save(cartItem);

        // Recalculate cart totals
        cart.calculateTotals();
        cart = cartRepository.save(cart);

        return convertToCartResponse(cart);
    }

    // Update cart item
    public CartResponse updateCartItem(Long userId, Long cartItemId, UpdateCartItemRequest request) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        // Verify the cart item belongs to the user
        if (!cartItem.getCart().getUser().getId().equals(userId)) {
            throw new RuntimeException("Cart item does not belong to user");
        }

        // Update quantity
        if (request.getQuantity() != null) {
            if (request.getQuantity() <= 0) {
                // If quantity is 0 or negative, remove the item
                return removeFromCart(userId, cartItemId);
            }
            cartItem.setQuantity(request.getQuantity());
            cartItem.calculateSubtotal();
        }

        // Update selected options
        if (request.getSelectedOptions() != null) {
            cartItem.setSelectedOptionsJson(toJson(request.getSelectedOptions()));
        }

        // Update notes
        if (request.getNotes() != null) {
            cartItem.setNotes(request.getNotes());
        }

        cartItemRepository.save(cartItem);

        // Recalculate cart totals
        Cart cart = cartItem.getCart();
        cart.calculateTotals();
        cart = cartRepository.save(cart);

        return convertToCartResponse(cart);
    }

    // Remove item from cart
    public CartResponse removeFromCart(Long userId, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        // Verify the cart item belongs to the user
        if (!cartItem.getCart().getUser().getId().equals(userId)) {
            throw new RuntimeException("Cart item does not belong to user");
        }

        Cart cart = cartItem.getCart();
        cartItemRepository.delete(cartItem);

        // Manual calculation using repository queries instead of collection
        Integer totalItems = cartItemRepository.getTotalQuantityByCartId(cart.getId());
        BigDecimal totalAmount = cartItemRepository.getTotalAmountByCartId(cart.getId());
        
        cart.setTotalItems(totalItems != null ? totalItems : 0);
        cart.setTotalAmount(totalAmount != null ? totalAmount : BigDecimal.ZERO);
        cart = cartRepository.save(cart);

        return convertToCartResponse(cart);
    }

    // Clear entire cart
    public CartResponse clearCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<Cart> cartOpt = cartRepository.findByUserAndIsActiveTrue(user);
        if (cartOpt.isEmpty()) {
            throw new RuntimeException("Cart not found");
        }

        Cart cart = cartOpt.get();
        cartItemRepository.deleteByCart(cart);

        // Reset cart totals
        cart.setTotalAmount(BigDecimal.ZERO);
        cart.setTotalItems(0);
        cart = cartRepository.save(cart);

        return convertToCartResponse(cart);
    }

    // Get cart item count
    public Integer getCartItemCount(Long userId) {
        Optional<Cart> cartOpt = cartRepository.findByUserIdAndIsActiveTrue(userId);
        if (cartOpt.isEmpty()) {
            return 0;
        }
        return cartOpt.get().getTotalItems();
    }
    
    // Get cart by user ID (for order creation)
    public Cart getCartByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return cartRepository.findByUserAndIsActiveTrue(user)
                .orElseThrow(() -> new RuntimeException("Cart not found for user"));
    }

    // Helper methods
    private CartResponse convertToCartResponse(Cart cart) {
        // Get cart items from repository instead of lazy collection
        List<CartItem> cartItemsList = cartItemRepository.findByCartId(cart.getId());
        
        List<CartItemResponse> cartItems = cartItemsList.stream()
                .map(this::convertToCartItemResponse)
                .collect(Collectors.toList());

        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUser().getId())
                .totalAmount(cart.getTotalAmount())
                .totalItems(cart.getTotalItems())
                .isEmpty(cartItems.isEmpty())
                .cartItems(cartItems)
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    private CartItemResponse convertToCartItemResponse(CartItem cartItem) {
        // Extract product image from attributes JSON
        String productImage = null;
        Map<String, Object> productAttributes = fromJson(cartItem.getProduct().getAttributesJson());
        if (productAttributes.containsKey("image")) {
            productImage = (String) productAttributes.get("image");
        } else if (productAttributes.containsKey("imageUrl")) {
            productImage = (String) productAttributes.get("imageUrl");
        } else if (productAttributes.containsKey("mainImage")) {
            productImage = (String) productAttributes.get("mainImage");
        }
        
        return CartItemResponse.builder()
                .id(cartItem.getId())
                .cartId(cartItem.getCart().getId())
                .productId(cartItem.getProduct().getId())
                .productName(cartItem.getProduct().getName())
                .productImage(productImage)
                .productVariantId(cartItem.getProductVariant() != null ? cartItem.getProductVariant().getId() : null)
                .variantName(cartItem.getProductVariant() != null ? cartItem.getProductVariant().getVariantName() : null)
                .quantity(cartItem.getQuantity())
                .unitPrice(cartItem.getUnitPrice())
                .subtotal(cartItem.getSubtotal())
                .selectedOptions(fromJson(cartItem.getSelectedOptionsJson()))
                .notes(cartItem.getNotes())
                .createdAt(cartItem.getCreatedAt())
                .updatedAt(cartItem.getUpdatedAt())
                .build();
    }

    private String toJson(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fromJson(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            return Map.of();
        }
    }
}