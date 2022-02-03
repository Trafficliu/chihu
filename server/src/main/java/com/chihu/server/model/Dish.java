package com.chihu.server.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "dishes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Dish {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dish_id")
    private Long dishId;

    @Column(name = "business_group_id")
    private Long businessGroupId;

    @Column(name = "dish_name")
    private String dishName;

    @Column(name = "price_in_cent")
    private int priceInCent;

    @Column(name = "cuisine_index")
    private String cuisineIndex;

    @Column(name = "cuisine_name")
    private String cuisineName;

    @Column(name = "external_image_id")
    private String externalImageId;

    @Column(name = "dish_image_path")
    private String dishImagePath;

    @Column(name = "overall_rating")
    private float overallRating;

    @Column(name = "overall_rating_votes")
    private int overallRatingVotes;

    @Column(name = "spicy_level")
    private float spicyLevel;

    @Column(name = "spicy_level_votes")
    private int spicyLevelVotes;

    @Column(name = "salt_level")
    private float saltLevel;

    @Column(name = "salt_level_votes")
    private int saltLevelVotes;

    @Column(name = "sweet_level")
    private float sweetLevel;

    @Column(name = "sweet_level_votes")
    private int sweetLevelVotes;

    @Column(name = "oil_level")
    private float oilLevel;

    @Column(name = "oil_level_votes")
    private int oilLevelVotes;

    @Column(name = "sour_level")
    private float sourLevel;

    @Column(name = "sour_level_votes")
    private int sourLevelVotes;

    @Column(name = "serving_size")
    private float servingSize;

    @Column(name = "serving_size_votes")
    private int servingSizeVotes;

    @Column(name = "same_as_picture")
    private float sameAsPicture;

    @Column(name = "same_as_picture_votes")
    private int sameAsPictureVotes;

    @Column(name = "healthy")
    private float healthy;

    @Column(name = "healthy_votes")
    private int healthyVotes;

    @Column(name = "portable")
    private float portable;

    @Column(name = "portable_votes")
    private int portableVotes;

    @Column(name = "odorous")
    private float odorous;

    @Column(name = "odorous_votes")
    private int odorousVotes;

    @Column(name = "special_flags")
    private int specialFlags;
}
