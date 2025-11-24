/**
 * @file: OrderServiceImpl.java
 * @description: Implementation of OrderService
 * @dependencies: Spring, JPA, MapStruct
 * @created: 2025-11-03
 */
package com.baganov.klassifikator.order.service.impl;

import com.baganov.klassifikator.common.model.entity.Order;
import com.baganov.klassifikator.common.model.entity.OrderItem;
import com.baganov.klassifikator.order.model.enums.OrderStatus;
import com.baganov.klassifikator.common.model.entity.Product;
import com.baganov.klassifikator.common.model.entity.Organization;
import com.baganov.klassifikator.common.model.entity.Landing;
import com.baganov.klassifikator.common.model.entity.OrganizationContent;
import com.baganov.klassifikator.order.exception.OrderNotFoundException;
import com.baganov.klassifikator.order.exception.ProductNotFoundException;
import com.baganov.klassifikator.order.mapper.OrderMapper;
import com.baganov.klassifikator.order.model.dto.OrderDto;
import com.baganov.klassifikator.order.model.dto.OrderItemRequestDto;
import com.baganov.klassifikator.order.model.dto.OrderRequestDto;
import com.baganov.klassifikator.order.repository.OrderItemRepository;
import com.baganov.klassifikator.order.repository.OrderRepository;
import com.baganov.klassifikator.order.repository.ProductRepository;
import com.baganov.klassifikator.order.repository.OrganizationRepository;
import com.baganov.klassifikator.order.repository.LandingRepository;
import com.baganov.klassifikator.order.repository.OrganizationContentRepository;
import com.baganov.klassifikator.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final OrganizationRepository organizationRepository;
    private final LandingRepository landingRepository;
    private final OrganizationContentRepository organizationContentRepository;
    private final OrderMapper orderMapper;
    private final WebClient.Builder webClientBuilder;
    
    @Override
    @Transactional
    public OrderDto createOrder(OrderRequestDto request) {
        log.info("Creating order for organization {} from landing {}", 
                 request.getOrganizationId(), request.getLandingId());
        
        // Create order entity
        Order order = Order.builder()
                .organizationId(request.getOrganizationId())
                .landingId(request.getLandingId())
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .customerEmail(request.getCustomerEmail())
                .deliveryAddress(request.getDeliveryAddress())
                .comment(request.getComment())
                .status(OrderStatus.PENDING.name())
                .totalAmount(BigDecimal.ZERO)
                .build();
        
        // Create order items and calculate total
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (OrderItemRequestDto itemRequest : request.getItems()) {
            // Get product
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(itemRequest.getProductId()));
            
            // Calculate item total
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
            
            // Create order item
            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(itemRequest.getQuantity())
                    .productPrice(product.getPrice())
                    .subtotal(itemTotal)
                    .build();
            
            orderItems.add(orderItem);
        }
        
        order.setTotalAmount(totalAmount);
        
        // Save order
        Order savedOrder = orderRepository.save(order);
        
        // Set order ID for items and save
        orderItems.forEach(item -> item.setOrderId(savedOrder.getId()));
        orderItemRepository.saveAll(orderItems);
        
        // Load items to order
        savedOrder.setItems(orderItems);
        
        log.info("Order created successfully with id {}, total amount: {}", 
                 savedOrder.getId(), savedOrder.getTotalAmount());
        
        // Send Telegram notification asynchronously
        sendTelegramNotification(savedOrder);
        
        return orderMapper.toDto(savedOrder);
    }
    
    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long id) {
        log.debug("Getting order by id: {}", id);
        
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        
        // Load items
        List<OrderItem> items = orderItemRepository.findByOrderId(id);
        order.setItems(items);
        
        return orderMapper.toDto(order);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<OrderDto> getOrdersByOrganization(Long organizationId, Pageable pageable) {
        log.debug("Getting orders for organization: {}", organizationId);
        
        Page<Order> orders = orderRepository.findByOrganizationId(organizationId, pageable);
        
        // Load items for each order
        orders.forEach(order -> {
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
            order.setItems(items);
        });
        
        return orders.map(orderMapper::toDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<OrderDto> getOrdersByLanding(Long landingId, Pageable pageable) {
        log.debug("Getting orders for landing: {}", landingId);
        
        Page<Order> orders = orderRepository.findByLandingId(landingId, pageable);
        
        // Load items for each order
        orders.forEach(order -> {
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
            order.setItems(items);
        });
        
        return orders.map(orderMapper::toDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<OrderDto> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        log.debug("Getting orders by status: {}", status);
        
        Page<Order> orders = orderRepository.findByStatus(status, pageable);
        
        // Load items for each order
        orders.forEach(order -> {
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
            order.setItems(items);
        });
        
        return orders.map(orderMapper::toDto);
    }
    
    @Override
    @Transactional
    public OrderDto updateOrderStatus(Long id, OrderStatus status) {
        log.info("Updating order {} status to {}", id, status);
        
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        
        order.setStatus(status.name());
        Order updatedOrder = orderRepository.save(order);
        
        // Load items
        List<OrderItem> items = orderItemRepository.findByOrderId(id);
        updatedOrder.setItems(items);
        
        log.info("Order {} status updated to {}", id, status);
        
        return orderMapper.toDto(updatedOrder);
    }
    
    @Override
    @Transactional
    public OrderDto cancelOrder(Long id) {
        log.info("Cancelling order {}", id);
        return updateOrderStatus(id, OrderStatus.CANCELLED);
    }
    
    @Override
    @Transactional
    public void deleteOrder(Long id) {
        log.info("Deleting order {}", id);
        
        if (!orderRepository.existsById(id)) {
            throw new OrderNotFoundException(id);
        }
        
        // Delete order items first
        List<OrderItem> items = orderItemRepository.findByOrderId(id);
        orderItemRepository.deleteAll(items);
        
        // Delete order
        orderRepository.deleteById(id);
        
        log.info("Order {} deleted successfully", id);
    }
    
    /**
     * Send Telegram notification about new order (asynchronous)
     */
    private void sendTelegramNotification(Order order) {
        try {
            log.debug("Sending Telegram notification for order {}", order.getId());
            
            // Build order data map
            Map<String, Object> orderData = new HashMap<>();
            orderData.put("orderId", order.getId());
            orderData.put("customerName", order.getCustomerName());
            orderData.put("customerPhone", order.getCustomerPhone());
            orderData.put("customerEmail", order.getCustomerEmail());
            orderData.put("deliveryAddress", order.getDeliveryAddress());
            orderData.put("totalAmount", order.getTotalAmount());
            orderData.put("comment", order.getComment());
            
            // Add organization info
            try {
                Organization organization = organizationRepository.findById(order.getOrganizationId()).orElse(null);
                if (organization != null) {
                    orderData.put("organizationName", organization.getName());
                }
            } catch (Exception e) {
                log.warn("Could not fetch organization info for order {}", order.getId());
            }
            
            // Add landing/domain info
            String domain = null;
            try {
                if (order.getLandingId() != null) {
                    Landing landing = landingRepository.findById(order.getLandingId()).orElse(null);
                    if (landing != null) {
                        // Construct full domain from subdomain and domain
                        if (landing.getSubdomain() != null && !landing.getSubdomain().isEmpty()) {
                            domain = landing.getSubdomain() + "." + landing.getDomain();
                        } else {
                            domain = landing.getDomain();
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Could not fetch landing info for order {}", order.getId());
            }
            
            // If no landing domain, try to get from OrganizationContent
            if (domain == null || domain.isEmpty()) {
                try {
                    OrganizationContent content = organizationContentRepository.findByOrganizationId(order.getOrganizationId()).orElse(null);
                    if (content != null && content.getContentData() != null) {
                        Object domainObj = content.getContentData().get("domain");
                        if (domainObj != null) {
                            domain = domainObj.toString();
                        }
                    }
                } catch (Exception e) {
                    log.warn("Could not fetch organization content for order {}", order.getId());
                }
            }
            
            if (domain != null && !domain.isEmpty()) {
                orderData.put("domain", domain);
            }
            
            // Add organization details (name, telegram bot token, chat ID)
            try {
                Organization organization = organizationRepository.findById(order.getOrganizationId()).orElse(null);
                if (organization != null) {
                    orderData.put("organizationName", organization.getName());
                    
                    // Add organization's custom Telegram bot if configured
                    if (organization.getTelegramBotToken() != null && !organization.getTelegramBotToken().isEmpty()) {
                        orderData.put("organizationTelegramBotToken", organization.getTelegramBotToken());
                        log.debug("Organization {} has custom Telegram bot token configured", organization.getName());
                    }
                    
                    if (organization.getTelegramChatId() != null && !organization.getTelegramChatId().isEmpty()) {
                        orderData.put("organizationTelegramChatId", organization.getTelegramChatId());
                        // For Google Sheets "Бот клиента" column
                        orderData.put("organizationTelegramBot", organization.getTelegramChatId());
                        log.debug("Organization {} has custom Telegram chat ID configured: {}", 
                                  organization.getName(), organization.getTelegramChatId());
                    }
                }
            } catch (Exception e) {
                log.warn("Could not fetch organization details for order {}", order.getId());
            }
            
            // Add items
            List<Map<String, Object>> items = new ArrayList<>();
            for (OrderItem item : order.getItems()) {
                Map<String, Object> itemData = new HashMap<>();
                itemData.put("productName", item.getProductName());
                itemData.put("quantity", item.getQuantity());
                itemData.put("price", item.getProductPrice());
                itemData.put("totalPrice", item.getSubtotal());
                items.add(itemData);
            }
            orderData.put("items", items);
            
            // Call Integration Service to send Telegram notification
            // Use service name for Kubernetes, localhost for local development
            String integrationServiceUrl = System.getenv().getOrDefault(
                "INTEGRATION_SERVICE_URL", 
                "http://localhost:8085"
            );
            
            WebClient webClient = webClientBuilder
                    .baseUrl(integrationServiceUrl)
                    .build();
            
            webClient.post()
                    .uri("/api/v1/integration/telegram/notify/order")
                    .bodyValue(orderData)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .subscribe(
                            result -> log.info("Telegram notification sent for order {}", order.getId()),
                            error -> log.error("Failed to send Telegram notification for order {}", order.getId(), error)
                    );
            
        } catch (Exception e) {
            log.error("Error sending Telegram notification for order {}", order.getId(), e);
            // Don't fail order creation if notification fails
        }
    }
}

