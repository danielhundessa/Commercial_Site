package com.layoff.order_service.services;

import com.layoff.order_service.clients.ProductServiceClient;
import com.layoff.order_service.dtos.CartItemRequest;
import com.layoff.order_service.models.CartItem;
import com.layoff.order_service.repositories.CartItemRepository;
import com.layoff.product_service.dtos.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartItemRepository cartItemRepository;
    private final ProductServiceClient productServiceClient;
    
    public List<CartItem> getCart(String userId) {
        return cartItemRepository.findByUserId(userId);
    }

    public void clearCart(String userId) {
        cartItemRepository.deleteByUserId(userId);
    }
    
    public Optional<CartItem> addToCart(String userId, CartItemRequest request) {
        try {
            // Fetch product details to validate and get price
            ProductResponse product = productServiceClient.getProductDetails(request.getProductId());
            
            if (product == null || !product.getActive()) {
                return Optional.empty();
            }
            
            // Check if product already exists in cart for this user
            List<CartItem> existingItems = cartItemRepository.findByUserId(userId);
            Optional<CartItem> existingItem = existingItems.stream()
                    .filter(item -> item.getProductId().equals(request.getProductId()))
                    .findFirst();
            
            CartItem cartItem;
            if (existingItem.isPresent()) {
                // Update quantity if item already exists
                cartItem = existingItem.get();
                cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
            } else {
                // Create new cart item
                cartItem = new CartItem();
                cartItem.setUserId(userId);
                cartItem.setProductId(request.getProductId());
                cartItem.setQuantity(request.getQuantity());
            }
            
            // Set price from product
            BigDecimal itemPrice = product.getPrice().multiply(new BigDecimal(cartItem.getQuantity()));
            cartItem.setPrice(itemPrice);
            
            CartItem savedItem = cartItemRepository.save(cartItem);
            return Optional.of(savedItem);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    public boolean removeFromCart(String userId, Long cartItemId) {
        Optional<CartItem> cartItem = cartItemRepository.findById(cartItemId);
        if (cartItem.isPresent() && cartItem.get().getUserId().equals(userId)) {
            cartItemRepository.delete(cartItem.get());
            return true;
        }
        return false;
    }
}
