package com.chihu.server.proxy;

import com.chihu.server.common.ApiServerConstants;
import com.chihu.server.model.*;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class OrderDaoImpl implements OrderDao {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private DishDao dishDao;

    @Override
    public List<OrderDetail> generateOrderDetails(
        @NonNull Map<Long, Integer> dishIdsAndCounts,
        @NonNull Long userId,
        @NonNull BusinessEntity businessEntity) throws IllegalArgumentException {
        if (dishIdsAndCounts == null || dishIdsAndCounts.isEmpty()) {
            throw new IllegalArgumentException("The order is empty!");
        }
        Set<Long> dishIds = dishIdsAndCounts.keySet();

        List<Dish> dishList = dishDao.getDishesOfSet(dishIds);
        if (dishList == null || dishList.isEmpty()) {
            throw new IllegalArgumentException("Illegal Dish ids!!");
        }
        for (Dish dish : dishList) {
            if (!dish.getBusinessGroupId()
                    .equals(businessEntity.getBusinessGroupId())) {
                throw new IllegalArgumentException(
                    "Order contains dishes not belonging to the businesses!!");
            }
        }
        List<OrderDetail> orderDetailList =
            dishList.stream().map(dish -> {
                OrderDetail orderDetail =
                    OrderDetail.builder()
                        .userId(userId)
                        .businessEntityId(
                            businessEntity.getBusinessEntityId())
                        .businessEntityName(
                            businessEntity.getBusinessEntityName())
                        .businessOwnerId(
                            businessEntity.getOwnerId())
                        .dishId(dish.getDishId())
                        .dishName(dish.getDishName())
                        .priceInCent(dish.getPriceInCent())
                        .itemCount(dishIdsAndCounts.get(dish.getDishId()))
                        .build();
                return orderDetail;
            }).collect(Collectors.toList());

        return orderDetailList;
    }

    @Override
    public void validateOrderDetails(
        @NonNull List<OrderDetail> orderDetailList,
        @NonNull BusinessEntity businessEntity)
        throws IllegalArgumentException {
        // Check if order is empty
        if (orderDetailList == null || orderDetailList.isEmpty()) {
            throw new IllegalArgumentException("The order is empty!");
        }
        // Make sure all the items belong to the same business entity
        Long businessEntityId = orderDetailList.get(0).getBusinessEntityId();
        Long userId = orderDetailList.get(0).getUserId();
        for (OrderDetail orderDetail : orderDetailList) {
            if (!orderDetail.getBusinessEntityId().equals(businessEntityId)
                || !orderDetail.getUserId().equals(userId)) {
                throw new IllegalArgumentException(
                    "Order items of multiple users or business entities!");
            }
        }
        // Get the latest prices from DB record
        List<Long> dishIds =
            orderDetailList
                .stream()
                .map(orderDetail -> (orderDetail.getDishId()))
                .collect(Collectors.toList());
        List<Dish> dishList = dishDao.getDishesOfList(dishIds);

        Map<Long, Integer> dishPrices = new HashMap<>();
        for (Dish dish : dishList) {
            if (!dish.getBusinessGroupId()
                .equals(businessEntity.getBusinessGroupId())) {
                throw new IllegalArgumentException(
                    "Order contains dishes not belonging to the businesses!!");
            }
            dishPrices.put(dish.getDishId(), dish.getPriceInCent());
        }
        for (OrderDetail orderDetail : orderDetailList) {
            if (dishPrices.containsKey(orderDetail.getDishId())) {
                orderDetail.setPriceInCent(dishPrices.get(orderDetail.getDishId()));
            } else {
                throw new IllegalArgumentException(
                    "Order contains invalid dish_id : " + orderDetail.getDishId());
            }
        }
    }

    @Override
    public void updateOrderDetails(@NonNull List<OrderDetail> orderDetailList)
        throws IllegalArgumentException {
        // Create order details records
        Long businessEntityId = orderDetailList.get(0).getBusinessEntityId();
        Long userId = orderDetailList.get(0).getUserId();
        // Remove any existing not submitted items in the DB
        String sql =
            "DELETE " +
            "  FROM order_details " +
            " WHERE user_id = :user_id " +
            "       AND business_entity_id = :business_entity_id " +
            "       AND order_status = 'IN_CART' "
            ;
        SqlParameterSource parameterSource = new MapSqlParameterSource()
            .addValue("business_entity_id", businessEntityId)
            .addValue("user_id", userId);
        namedParameterJdbcTemplate.update(sql, parameterSource);
        // Insert order details record to DB.
        Instant now = Instant.now();
        sql =
            "INSERT INTO order_details " +
            "        SET user_id = :user_id, " +
            "            business_entity_id = :business_entity_id, " +
            "            business_entity_name = :business_entity_name, " +
            "            dish_id = :dish_id, " +
            "            dish_name = :dish_name, " +
            "            price_in_cent = :price_in_cent, " +
            "            item_count = :item_count, " +
            "            order_status = :order_status, " +
            "            creation_timestamp = :creation_timestamp, " +
            "            update_timestamp = :update_timestamp "
        ;
        SqlParameterSource[] batchParameterSources =
            new SqlParameterSource[orderDetailList.size()];
        for (int i = 0; i < orderDetailList.size(); i++) {
            OrderDetail orderDetail = orderDetailList.get(i);
            batchParameterSources[i] = new MapSqlParameterSource()
                .addValue("user_id", orderDetail.getUserId())
                .addValue("business_entity_id", orderDetail.getBusinessEntityId())
                .addValue("business_entity_name", orderDetail.getBusinessEntityName())
                .addValue("dish_id", orderDetail.getDishId())
                .addValue("dish_name", orderDetail.getDishName())
                .addValue("price_in_cent", orderDetail.getPriceInCent())
                .addValue("item_count", orderDetail.getItemCount())
                .addValue("order_status", "IN_CART")
                .addValue("creation_timestamp", now.toEpochMilli())
                .addValue("update_timestamp", now.toEpochMilli())
            ;
        }
        namedParameterJdbcTemplate.batchUpdate(sql, batchParameterSources);
    }

    // This can be used when customer made an order,
    // but for some reason they need to change certain items
    @Override
    public void updateOrderDetails(@NonNull Long orderId,
                                   @NonNull Long userId,
                                   @NonNull List<OrderDetail> orderDetailList)
        throws IllegalArgumentException {
        // Create order details records
        // Remove any existing not submitted items in the DB
        String sql =
            "DELETE " +
            "  FROM order_details " +
            " WHERE order_id = :order_id " +
            "       AND user_id = :user_id "
            ;
        SqlParameterSource parameterSource = new MapSqlParameterSource()
            .addValue("order_id", orderId)
            .addValue("user_id", userId);
        namedParameterJdbcTemplate.update(sql, parameterSource);
        // Insert order details record to DB.
        Instant now = Instant.now();
        sql =
            "INSERT INTO order_details " +
            "        SET order_id = :order_id, " +
            "            user_id = :user_id, " +
            "            business_entity_id = :business_entity_id, " +
            "            business_entity_name = :business_entity_name, " +
            "            dish_id = :dish_id, " +
            "            dish_name = :dish_name, " +
            "            price_in_cent = :price_in_cent, " +
            "            item_count = :item_count, " +
            "            order_status = :order_status, " +
            "            creation_timestamp = :creation_timestamp, " +
            "            update_timestamp = :update_timestamp "
            ;
        SqlParameterSource[] batchParameterSources =
            new SqlParameterSource[orderDetailList.size()];
        for (int i = 0; i < orderDetailList.size(); i++) {
            OrderDetail orderDetail = orderDetailList.get(i);
            batchParameterSources[i] = new MapSqlParameterSource()
                .addValue("order_id", orderId)
                .addValue("user_id", orderDetail.getUserId())
                .addValue("business_entity_id", orderDetail.getBusinessEntityId())
                .addValue("business_entity_name", orderDetail.getBusinessEntityName())
                .addValue("dish_id", orderDetail.getDishId())
                .addValue("dish_name", orderDetail.getDishName())
                .addValue("price_in_cent", orderDetail.getPriceInCent())
                .addValue("item_count", orderDetail.getItemCount())
                .addValue("order_status", "IN_CART")
                .addValue("creation_timestamp", now.toEpochMilli())
                .addValue("update_timestamp", now.toEpochMilli())
                ;
        }
        namedParameterJdbcTemplate.batchUpdate(sql, batchParameterSources);

    }

    @Override
    public void submitOrder(@NonNull List<OrderDetail> orderDetailList,
                            @NonNull Address fromAddress,
                            @NonNull Address toAddress,
                            @NonNull String deliveryType,
                            int tipInCent) {
        String sql =
            "INSERT INTO order_summaries " +
            "        SET user_id = :user_id, " +
            "            business_entity_id = :business_entity_id, " +
            "            business_entity_name = :business_entity_name, " +
            "            business_owner_id = :business_owner_id, " +
            "            order_status = :order_status, " +
            "            delivery_type = :delivery_type, " +
            "            subtotal_in_cent = :subtotal_in_cent, " +
            "            tax_in_cent = :tax_in_cent, " +
            "            platform_service_fee_in_cent = :platform_service_fee_in_cent, " +
            "            transaction_service_fee_in_cent = :transaction_service_fee_in_cent, " +
            "            tip_in_cent = :tip_in_cent, " +
            "            delivery_fee_in_cent = :delivery_fee_in_cent, " +
            "            discount_in_cent = :discount_in_cent, " +
            "            total_in_cent = :total_in_cent, " +
            "            contact_name = :contact_name, " +
            "            phone_number = :phone_number, " +
            "            address_line1 = :address_line1, " +
            "            address_line2 = :address_line2, " +
            "            city = :city, " +
            "            state = :state, " +
            "            country = :country, " +
            "            zip_code = :zip_code, " +
            "            distance = :distance, " +
            "            creation_timestamp = :creation_timestamp, " +
            "            update_timestamp = :update_timestamp "
        ;
        Long userId = orderDetailList.get(0).getUserId();
        Long businessEntityId = orderDetailList.get(0).getBusinessEntityId();
        String businessEntityName = orderDetailList.get(0).getBusinessEntityName();
        Long businessOwnerId = orderDetailList.get(0).getBusinessOwnerId();
        tipInCent = Math.max(0, tipInCent);
        int subTotalInCent = getSubtotal(orderDetailList);
        BigDecimal subTotalAsBigDecimal = new BigDecimal(subTotalInCent);
        int taxInCent = subTotalAsBigDecimal.multiply(BigDecimal.valueOf(0.1)).intValue();
        int platformServiceFeeInCent =
            subTotalAsBigDecimal.multiply(
                BigDecimal.valueOf(ApiServerConstants.PLATFORM_SERVICE_FEE_RATE)).intValue();
        int transactionServiceFeeInCent =
            subTotalAsBigDecimal.multiply(
                BigDecimal.valueOf(ApiServerConstants.TRANSACTION_SERVICE_FEE_RATE)).intValue();
        int discountInCent =
            subTotalAsBigDecimal.multiply(
                BigDecimal.valueOf(ApiServerConstants.DISCOUNT_RATE)).intValue();
        double distance = getDistance(fromAddress, toAddress);
        int deliveryFeeInCent = calculateDeliveryFee(distance);
        int totalInCent =
            subTotalInCent + taxInCent + platformServiceFeeInCent
                + transactionServiceFeeInCent + tipInCent + deliveryFeeInCent
                - discountInCent;
        Instant now = Instant.now();

        SqlParameterSource parameterSource = new MapSqlParameterSource()
            .addValue("user_id", userId)
            .addValue("business_entity_id", businessEntityId)
            .addValue("business_entity_name", businessEntityName)
            .addValue("business_owner_id", businessOwnerId)
            .addValue("order_status", "PENDING_CONFIRM")
            .addValue("delivery_type", deliveryType)
            .addValue("subtotal_in_cent", subTotalInCent)
            .addValue("tax_in_cent", taxInCent)
            .addValue("platform_service_fee_in_cent", platformServiceFeeInCent)
            .addValue("transaction_service_fee_in_cent", transactionServiceFeeInCent)
            .addValue("tip_in_cent", tipInCent)
            .addValue("delivery_fee_in_cent", deliveryFeeInCent)
            .addValue("discount_in_cent", discountInCent)
            .addValue("total_in_cent", totalInCent)
            .addValue("contact_name", toAddress.getContactName())
            .addValue("phone_number", toAddress.getPhoneNumber())
            .addValue("address_line1", toAddress.getAddressLine1())
            .addValue("address_line2", toAddress.getAddressLine2())
            .addValue("city", toAddress.getCity())
            .addValue("state", toAddress.getState())
            .addValue("country", toAddress.getCountry())
            .addValue("zip_code", toAddress.getZipCode())
            .addValue("distance", distance)
            .addValue("creation_timestamp", now.toEpochMilli())
            .addValue("update_timestamp", now.toEpochMilli())
            ;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(sql, parameterSource, keyHolder);
        Long orderId = keyHolder.getKey().longValue();
        // Update Order Detail with order id and update order status
        assignOrderIdToOrderDetail(userId, businessEntityId, orderId);
    }

    @Override
    public void updateOrderStatus(@NonNull Long orderId,
                                  @NonNull String status) {
        // Update status in order_details
        String sql =
            "UPDATE order_details " +
            "   SET order_status = :order_status " +
            " WHERE order_id = :order_id "
            ;
        SqlParameterSource parameterSource = new MapSqlParameterSource()
            .addValue("order_id", orderId)
            .addValue("order_status", status)
            ;
        namedParameterJdbcTemplate.update(sql, parameterSource);
        // Update status in order_summaries
        sql =
            "UPDATE order_summaries " +
            "   SET order_status = :order_status " +
            " WHERE order_id = :order_id "
            ;
        parameterSource = new MapSqlParameterSource()
            .addValue("order_id", orderId)
            .addValue("order_status", status)
        ;
        namedParameterJdbcTemplate.update(sql, parameterSource);
    }

    @Override
    public List<OrderDetail> getDishesInCart(@NonNull Long userId)
        throws IllegalArgumentException {
        String sql =
            "SELECT * " +
            "  FROM order_details " +
            " WHERE user_id = :user_id " +
            "       AND order_status = 'IN_CART' ";
        SqlParameterSource parameterSource = new MapSqlParameterSource()
            .addValue("user_id", userId);

        List<OrderDetail> result =
            namedParameterJdbcTemplate.query(
                sql,
                parameterSource,
                new BeanPropertyRowMapper<>(OrderDetail.class));

        return result;
    }

    @Override
    public List<OrderDetail> getOrderDishes(@NonNull Long userId,
                                            @NonNull Long orderId) {
        String sql =
            "SELECT * " +
                "  FROM order_details " +
                " WHERE user_id = :user_id " +
                "       AND order_id = :order_id ";
        SqlParameterSource parameterSource = new MapSqlParameterSource()
            .addValue("user_id", userId)
            .addValue("order_id", orderId);

        List<OrderDetail> result =
            namedParameterJdbcTemplate.query(
                sql,
                parameterSource,
                new BeanPropertyRowMapper<>(OrderDetail.class));

        return result;
    }

    @Override
    public Optional<OrderSummary> getOrderSummary(@NonNull Long orderId)
        throws IllegalArgumentException {
        String sql =
            "SELECT * " +
            "  FROM order_summaries " +
            " WHERE order_id = :order_id ";
        SqlParameterSource parameterSource = new MapSqlParameterSource()
            .addValue("order_id", orderId);

        List<OrderSummary> result =
            namedParameterJdbcTemplate.query(
                sql,
                parameterSource,
                new BeanPropertyRowMapper<>(OrderSummary.class));
        if (result.isEmpty()) {
            return Optional.empty();
        }
        if (result.size() > 1) {
            throw new IllegalArgumentException(
                "Multiple order summaries associated with order: " + orderId);
        }

        return Optional.of(result.iterator().next());
    }

    @Override
    public List<OrderSummary> getAllOrderSummaries(@NonNull Long userId)
        throws IllegalArgumentException {
        String sql =
            "SELECT * " +
            "  FROM order_summaries " +
            " WHERE user_id = :user_id ";
        SqlParameterSource parameterSource = new MapSqlParameterSource()
            .addValue("user_id", userId);

        List<OrderSummary> result =
            namedParameterJdbcTemplate.query(
                sql,
                parameterSource,
                new BeanPropertyRowMapper<>(OrderSummary.class));

        return result;
    }

    // Helper functions
    private int getSubtotal(List<OrderDetail> orderDetailList) {
        return orderDetailList.stream().mapToInt(orderDetail -> (
            orderDetail.getItemCount() * orderDetail.getPriceInCent()
        )).sum();
    }

    // TODO: use geo api to properly calculate the distance
    private double getDistance(Address fromAddress, Address toAddress) {
        return 0.0;
    }

    // TODO: support seller customizable delivery fee calculation
    private int calculateDeliveryFee(double distanceInMile) {
        return BigDecimal
            .valueOf(distanceInMile)
            .multiply(BigDecimal.valueOf(50)) // 50 cents per mile
            .intValue();
    }

    private void assignOrderIdToOrderDetail(@NonNull Long userId,
                                            @NonNull Long businessEntityId,
                                            @NonNull Long orderId) {
        String sql =
            "UPDATE order_details " +
            "   SET order_id = :order_id," +
            "       order_status = 'PENDING_CONFIRM' " +
            " WHERE user_id = :user_id " +
            "       AND business_entity_id = :business_entity_id " +
            "       AND order_status = 'IN_CART' "
            ;
        SqlParameterSource parameterSource = new MapSqlParameterSource()
            .addValue("user_id", userId)
            .addValue("business_entity_id", businessEntityId)
            .addValue("order_id", orderId)
            ;

        namedParameterJdbcTemplate.update(sql, parameterSource);
    }
}
