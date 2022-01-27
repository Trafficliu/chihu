package com.chihu.server;

import com.chihu.server.client.ApiServerClient;
import com.chihu.server.model.BusinessEntity;
import com.chihu.server.model.BusinessGroup;
import com.chihu.server.model.Dish;
import com.chihu.server.serializer.ApiServerSerializer;
import com.chihu.server.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
public class DishOperationTest {

    @Autowired
    private UserService userService;

    @Rule
    public TestName name = new TestName();

    private ApiServerClient client;

    @Before
    public void setup() throws Exception {
        String businessOwnerName =
            "Business_Owner-"
                + name.getMethodName()
                + UUID.randomUUID().toString().substring(0, 10);
        client = TestHelper.createUserAndLogin(businessOwnerName);
    }

    @After
    public void cleanup() throws Exception {
        client.close();
    }

    @Test
    public void createAndUpdateDish_succeed() throws Exception {
        // Create a new dish entity and verify it can be found in DB
        BusinessGroup expectedBusinessGroup =
            TestConstants.BASIC_TEST_BUSINESS_GROUP.toBuilder()
                .businessName(
                    "TestBusinessGroupName-"
                        + name.getMethodName()
                        + UUID.randomUUID().toString().substring(0, 8))
                .externalImageId(UUID.randomUUID().toString().substring(0, 15))
                .build();
        client.createBusinessGroup(expectedBusinessGroup);
        BusinessGroup actualBusinessGroup =
            client.getBusinessGroupByName(
                expectedBusinessGroup.getBusinessName());
        Long businessGroupId = actualBusinessGroup.getBusinessGroupId();
        Dish expectedDish =
            TestConstants.BASIC_TEST_DISH.toBuilder()
                .businessGroupId(businessGroupId)
                .externalImageId(UUID.randomUUID().toString().substring(0, 15))
                .build();
        client.createDish(expectedDish);
        Dish actualDish =
            client.getDishByName(businessGroupId, expectedDish.getDishName());
        assertEquals(
            expectedDish.getExternalImageId(), actualDish.getExternalImageId());

        // Check that current user can update the dish details
        Long dishId = actualDish.getDishId();
        expectedDish.setDishId(dishId);
        expectedDish.setSpecialFlags(255);
        expectedDish.setPriceInCent(2399);
        client.updateDish(expectedDish);
        actualDish = client.getDishById(dishId);
        assertEquals(expectedDish.getSpecialFlags(), actualDish.getSpecialFlags());
        assertEquals(expectedDish.getPriceInCent(), actualDish.getPriceInCent());

        // Create another client with a different user
        String randomUserName =
            "Random_user-"
                + name.getMethodName()
                + UUID.randomUUID().toString().substring(0, 10);
        ApiServerClient anotherClient =
            TestHelper.createUserAndLogin(randomUserName);

        // Verify that another user cannot update or delete the dish
        assertThrows(
            RuntimeException.class,
            () -> anotherClient.updateDish(expectedDish)
        );
        assertThrows(
            RuntimeException.class,
            () -> anotherClient.deleteDish(dishId)
        );

        // Delete the test dish, so it can no longer be found
        client.deleteDish(dishId);
        assertThrows(
            RuntimeException.class,
            () -> client.getDishById(dishId)
        );
    }

}
