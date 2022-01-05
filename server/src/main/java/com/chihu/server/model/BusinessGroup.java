package com.chihu.server.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "business_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class BusinessGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "business_group_id")
    private Long businessGroupId;

    @Column(name = "owner_id")
    private Long ownerId;

    // Valid types: bakery, home cook, restaurant, food truck, drink
    @Column(name = "business_type")
    private String businessType;

    @Column(name = "business_name")
    private String businessName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "store_image_path")
    private String storeImagePath;

    @Column(name = "external_image_id")
    private String externalImageId;

    @Column(name = "primary_food_type")
    private String primaryFoodType;

    @Column(name = "secondary_food_type")
    private String secondaryFoodType;

    @Column(name = "rating")
    private double rating;

    @Column(name = "num_of_reviews")
    private int numOfReviews;

    @Column(name = "support_deliver")
    private boolean deliverSupported;

    @Column(name = "support_pickup")
    private boolean pickUpSupported;

    @Column(name = "creation_timestamp")
    @Builder.Default
    private Long creationTimestamp = System.currentTimeMillis();

    @Column(name = "last_update_timestamp")
    @Builder.Default
    private Long lastUpdateTimestamp = System.currentTimeMillis();

}
