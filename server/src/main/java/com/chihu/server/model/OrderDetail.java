package com.chihu.server.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "order_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long recordId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "business_entity_id")
    private Long businessEntityId;

    @Column(name = "business_entity_name")
    private String businessEntityName;

    @Column(name = "business_owner_id")
    private Long businessOwnerId;

    @Column(name = "dish_id")
    private Long dishId;

    @Column(name = "dish_name")
    private String dishName;

    @Column(name = "price_in_cent")
    private int priceInCent;

    @Column(name = "item_count")
    private int itemCount;

    @Column(name = "order_status")
    private String orderStatus;

    @Column(name = "creation_timestamp")
    @Builder.Default
    private Long creationTimestamp = System.currentTimeMillis();

    @Column(name = "update_timestamp")
    @Builder.Default
    private Long updateTimestamp = System.currentTimeMillis();
}
