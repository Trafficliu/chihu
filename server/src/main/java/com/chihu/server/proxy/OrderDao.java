package com.chihu.server.proxy;

import com.chihu.server.model.Address;
import com.chihu.server.model.BusinessEntity;
import com.chihu.server.model.OrderSummary;
import lombok.NonNull;
import com.chihu.server.model.OrderDetail;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OrderDao {
    List<OrderDetail> generateOrderDetails(
        @NonNull Map<Long, Integer> dishIdsAndCounts,
        @NonNull Long userId,
        @NonNull BusinessEntity businessEntity);
    void validateOrderDetails(@NonNull List<OrderDetail> orderDetailList,
                              @NonNull BusinessEntity businessEntity);
    void updateOrderDetails(@NonNull List<OrderDetail> orderDetailList);
    void updateOrderDetails(@NonNull Long orderId,
                            @NonNull Long userId,
                            @NonNull List<OrderDetail> orderDetailList);
    void submitOrder(@NonNull List<OrderDetail> orderDetailList,
                     @NonNull Address fromAddress,
                     @NonNull Address toAddress,
                     @NonNull String deliveryType,
                     int tipInCent);
    void updateOrderStatus(@NonNull Long orderId, @NonNull String status);
    List<OrderDetail> getDishesInCart(@NonNull Long userId);
    List<OrderDetail> getOrderDishes(@NonNull Long userId, @NonNull Long orderId);
    Optional<OrderSummary> getOrderSummary(@NonNull Long orderId);
    List<OrderSummary> getAllOrderSummaries(@NonNull Long userId);
}
