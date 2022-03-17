package com.chihu.server.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "order_summaries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class OrderSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Column(name = "order_status")
    private String orderStatus;

    @Column(name = "delivery_type")
    private String deliveryType;

    @Column(name = "overall_rating")
    private int overallRating;

    @Column(name = "subtotal_in_cent")
    private int subtotalInCent;

    @Column(name = "platform_service_fee_in_cent")
    private int platformServiceFeeInCent;

    @Column(name = "transaction_service_fee_in_cent")
    private int transactionServiceFeeInCent;

    @Column(name = "tip_in_cent")
    private int tipInCent;

    @Column(name = "delivery_fee_in_cent")
    private int deliveryFeeInCent;

    @Column(name = "discount_in_cent")
    private int discountInCent;

    @Column(name = "total_in_cent")
    private int totalInCent;

    @Column(name = "contact_name")
    private String contactName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "address_line1")
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "country")
    @Builder.Default
    private String country = "US";

    @Column(name = "zip_code")
    private String zipCode;

    @Column(name = "distance")
    private Float distance;

    @Column(name = "creation_timestamp")
    @Builder.Default
    private Long creationTimestamp = System.currentTimeMillis();

    @Column(name = "update_timestamp")
    @Builder.Default
    private Long updateTimestamp = System.currentTimeMillis();
}
