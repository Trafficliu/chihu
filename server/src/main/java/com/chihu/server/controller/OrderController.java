package com.chihu.server.controller;

import com.chihu.server.model.*;
import com.chihu.server.proxy.AddressDao;
import com.chihu.server.proxy.BusinessEntityDao;
import com.chihu.server.proxy.OrderDao;
import com.chihu.server.serializer.ApiServerSerializer;
import com.chihu.server.service.UserService;
import com.google.common.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Type;
import java.util.*;

@Slf4j
@RestController
public class OrderController {

    private static final Type DISH_COUNT_MAP_TYPE =
        new TypeToken<Map<Long, Integer>>() {}.getType();
    private static final String DELIVERY_TYPE_DELIVER = "deliver";
    private static final String DELIVERY_TYPE_PICK_UP = "pick-up";
    private static final Set<String> EATER_ACTIONS =
        new HashSet<>(Arrays.asList("CANCEL"));
    private static final Set<String> OWNER_ACTIONS =
        new HashSet<>(
            Arrays.asList(
                "CANCEL", "CONFIRMED", "COOKING",
                "PENDING_PICK_UP", "ON_THE_WAY", "COMPLETED"));

    @Autowired
    private UserService userService;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private AddressDao addressDao;

    @Autowired
    private BusinessEntityDao businessEntityDao;

    @PostMapping("/order/update_cart")
    public String updateCart(
        Authentication authentication,
        @RequestParam(value = "dish_count_str", required = true) String dishCountStr,
        @RequestParam(value = "business_entity_id", required = true) Long businessEntityId
    ) throws IllegalArgumentException {
        // Get order dishes and counts
        Map<Long, Integer> dishCount =
            ApiServerSerializer.getGsonInstance()
                .fromJson(dishCountStr, DISH_COUNT_MAP_TYPE);
        // Get user info
        ChihuUserDetails userDetails =
            (ChihuUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        // Get business entity from the business entity id provided
        Optional<BusinessEntity> businessEntityOptional =
            businessEntityDao.getBusinessEntityById(businessEntityId);
        if (businessEntityOptional.isEmpty()) {
            throw new IllegalArgumentException(
                "Cannot find a matching business record!!!");
        }
        BusinessEntity businessEntity = businessEntityOptional.get();
        List<OrderDetail> orderDetailList =
            orderDao.generateOrderDetails(dishCount, userId, businessEntity);

        orderDao.updateOrderDetails(orderDetailList);

        return "";
    }

    @PostMapping("/order/create_order")
    public String createOrder(
        Authentication authentication,
        @RequestParam(value = "dish_count_str", required = true) String dishCountStr,
        @RequestParam(value = "business_entity_id", required = true) Long businessEntityId,
        @RequestParam(value = "to_address_id", required = true) Long toAddressId,
        @RequestParam(value = "delivery_type", required = true) String deliveryType,
        @RequestParam(value = "tip_in_cent", required = true) Integer tipInCent
    ) throws IllegalArgumentException {
        // Get order dishes and counts
        Map<Long, Integer> dishCount =
            ApiServerSerializer.getGsonInstance()
                .fromJson(dishCountStr, DISH_COUNT_MAP_TYPE);
        // Get user info
        ChihuUserDetails userDetails =
            (ChihuUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        // Get business entity from the business entity id provided
        Optional<BusinessEntity> businessEntityOptional =
            businessEntityDao.getBusinessEntityById(businessEntityId);
        if (businessEntityOptional.isEmpty()) {
            throw new IllegalArgumentException(
                "Cannot find a matching business record!!!");
        }
        BusinessEntity businessEntity = businessEntityOptional.get();
        // Get business entity address as the from address
        Address fromAddress = businessEntityDao.getBusinessAddress(businessEntity);
        Optional<Address> toAddress = addressDao.getAddressById(toAddressId);
        if (toAddress.isEmpty()) {
            throw new IllegalArgumentException(
                "Invalid to address - cannot find a matching address!");
        }
        if (deliveryType.equals(DELIVERY_TYPE_DELIVER)
            && !toAddress.get().getUserId().equals(userId)) {
            throw new IllegalArgumentException(
                "Invalid to address - user doesn't have access to the address!");
        }

        // Generate order details with DB sync
        List<OrderDetail> orderDetailList =
            orderDao.generateOrderDetails(dishCount, userId, businessEntity);
        // Update order details in DB and submit order
        orderDao.updateOrderDetails(orderDetailList);
        orderDao.submitOrder(
            orderDetailList,
            fromAddress, toAddress.get(), deliveryType,
            tipInCent);

        return "";
    }

    @PostMapping("/order/update_order")
    public String updateOrder(
        Authentication authentication,
        @RequestParam(value = "order_id", required = true) Long orderId,
        @RequestParam(value = "dish_count_str", required = true) String dishCountStr,
        @RequestParam(value = "business_entity_id", required = true) Long businessEntityId
    ) throws IllegalArgumentException {
        Map<Long, Integer> dishCount =
            ApiServerSerializer.getGsonInstance()
                .fromJson(dishCountStr, DISH_COUNT_MAP_TYPE);
        ChihuUserDetails userDetails =
            (ChihuUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        // Get business entity from the business entity id provided
        Optional<BusinessEntity> businessEntityOptional =
            businessEntityDao.getBusinessEntityById(businessEntityId);
        if (businessEntityOptional.isEmpty()) {
            throw new IllegalArgumentException(
                "Cannot find a matching business record!!!");
        }
        BusinessEntity businessEntity = businessEntityOptional.get();

        List<OrderDetail> orderDetailList =
            orderDao.generateOrderDetails(dishCount, userId, businessEntity);
        orderDao.updateOrderDetails(orderId, userId, orderDetailList);

        return "";
    }

    @PostMapping("/order/eater_update_order_status")
    public String eaterUpdateOrderStatus(
        Authentication authentication,
        @RequestParam(value = "order_id", required = true) Long orderId,
        @RequestParam(value = "status", required = true) String status
    ) throws IllegalArgumentException, AccessDeniedException {
        if (!EATER_ACTIONS.contains(status)) {
            throw new IllegalArgumentException(
                String.format(
                    "Eater cannot make the following status change: %s",
                    status));
        }
        Optional<OrderSummary> orderSummary = orderDao.getOrderSummary(orderId);
        if (orderSummary.isEmpty()) {
            throw new IllegalArgumentException(
                String.format("Invalid order id: %d", orderId));
        }
        ChihuUserDetails userDetails =
            (ChihuUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        if (!orderSummary.get().getUserId().equals(userId)) {
            throw new AccessDeniedException(
                String.format(
                    "Order %d is not made by user %d",
                    orderId, userId));
        }

        orderDao.updateOrderStatus(orderId, status);

        return "";
    }

    @PostMapping("/order/owner_update_order_status")
    public String ownerUpdateOrderStatus(
        Authentication authentication,
        @RequestParam(value = "order_id", required = true) Long orderId,
        @RequestParam(value = "status", required = true) String status
    ) throws IllegalArgumentException, AccessDeniedException {
        if (!OWNER_ACTIONS.contains(status)) {
            throw new IllegalArgumentException(
                String.format(
                    "Business owner cannot make the following status change: %s",
                    status));
        }
        Optional<OrderSummary> orderSummary = orderDao.getOrderSummary(orderId);
        if (orderSummary.isEmpty()) {
            throw new IllegalArgumentException("Invalid order id: " + orderId);
        }
        Long businessEntityId = orderSummary.get().getBusinessEntityId();
        Optional<BusinessEntity> businessEntity =
            businessEntityDao.getBusinessEntityById(businessEntityId);
        if (businessEntity.isEmpty()) {
            throw new IllegalArgumentException(
                "Business entity not found: " + businessEntityId);
        }
        ChihuUserDetails userDetails =
            (ChihuUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        if (!businessEntity.get().getOwnerId().equals(userId)) {
            throw new AccessDeniedException(
                String.format(
                    "Business %d is not owned by user %d",
                    businessEntityId, userId)
            );
        }

        orderDao.updateOrderStatus(orderId, status);

        return "";
    }

    @GetMapping("/order/get_dishes_in_cart")
    public List<OrderDetail> getDishesInCart(
        Authentication authentication)
        throws IllegalArgumentException, AccessDeniedException {
        ChihuUserDetails userDetails =
            (ChihuUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        return orderDao.getDishesInCart(userId);
    }

    @GetMapping("/order/get_order_dishes")
    public List<OrderDetail> getOrderDishes(
        Authentication authentication,
        @RequestParam(value = "order_id", required = true) Long orderId)
        throws IllegalArgumentException, AccessDeniedException {
        ChihuUserDetails userDetails =
            (ChihuUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        return orderDao.getOrderDishes(userId, orderId);
    }

    @GetMapping("/order/get_order_summary")
    public OrderSummary getOrderSummary(
        Authentication authentication,
        @RequestParam(value = "order_id", required = true) Long orderId)
        throws IllegalArgumentException, AccessDeniedException {
        Optional<OrderSummary> orderSummary = orderDao.getOrderSummary(orderId);
        if (orderSummary.isEmpty()) {
            throw new IllegalArgumentException(
                String.format("Invalid order id: %d", orderId));
        }
        ChihuUserDetails userDetails =
            (ChihuUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        if (!orderSummary.get().getUserId().equals(userId)
            && !orderSummary.get().getBusinessOwnerId().equals(userId) ) {
            throw new AccessDeniedException (
                String.format(
                    "User %d doesn't have access to the order %d!",
                    userId, orderId));
        }

        return orderSummary.get();
    }

    @GetMapping("/order/get_all_order_summaries")
    public List<OrderSummary> getAllOrderSummaries(
        Authentication authentication)
        throws IllegalArgumentException, AccessDeniedException {
        ChihuUserDetails userDetails =
            (ChihuUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        return orderDao.getAllOrderSummaries(userId);
    }

}
