package com.chihu.server;

import com.chihu.server.model.BusinessEntity;
import com.chihu.server.model.BusinessGroup;
import com.chihu.server.model.User;

import java.util.UUID;

public class TestConstants {
    public static final String HOST = "127.0.0.1";
    public static final String DEFAULT_PASSWORD = "Default_Pass_1";
    public static final String DEFAULT_STORE_IMAGE_PATH = "";

    public static final User BASIC_TEST_USER = User.builder()
            .username(UUID.randomUUID().toString())
            .password(DEFAULT_PASSWORD)
            .passwordConfirm(DEFAULT_PASSWORD)
            .activated(true)
            .build();

    public static final BusinessGroup BASIC_TEST_BUSINESS_GROUP = BusinessGroup.builder()
            .businessName(
                "TestBusinessGroupName-" + UUID.randomUUID().toString().substring(0, 10))
            .businessType("restaurant")
            .phoneNumber(UUID.randomUUID().toString().substring(0, 13))
            .storeImagePath(DEFAULT_STORE_IMAGE_PATH)
            .primaryFoodType("Chinese NorthEastern")
            .secondaryFoodType("Chinese Northern")
            .rating(5.0)
            .numOfReviews(100)
            .build();

    public static final BusinessEntity BASIC_TEST_BUSINESS_ENTITY =
        BusinessEntity.builder()
            .businessEntityName(
                "TestBusinessEntityName-" + UUID.randomUUID().toString().substring(0, 7))
            .operationType("restaurant")
            .phoneNumber(UUID.randomUUID().toString().substring(0, 13))
//            .workingDays(127)
//            .repeated(true)
            .workingHours(Long.valueOf((1<<48)-1))
            .addressLine1("888 S Vermont Ave")
            .addressLine2("#18")
            .city("Los Angeles")
            .state("CA")
            .country("US")
            .zipCode("90005")
            .build();
}
