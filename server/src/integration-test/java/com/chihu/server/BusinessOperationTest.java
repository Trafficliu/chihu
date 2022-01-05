package com.chihu.server;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.chihu.server.client.ApiServerClient;
import com.chihu.server.model.BusinessEntity;
import com.chihu.server.model.BusinessGroup;
import com.chihu.server.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@Slf4j
public class BusinessOperationTest {

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
    public void createAndUpdateBusiness_succeed() throws Exception {
        // Create a new business group entity and verify it can be found in DB
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
        assertEquals(
            expectedBusinessGroup.getExternalImageId(),
            actualBusinessGroup.getExternalImageId());

        // Check that current user can update the business group details
        Long businessGroupId = actualBusinessGroup.getBusinessGroupId();
        expectedBusinessGroup.setBusinessGroupId(businessGroupId);
        expectedBusinessGroup.setSecondaryFoodType("Chinese NorthWestern");

        client.updateBusinessGroup(expectedBusinessGroup);
        actualBusinessGroup = client.getBusinessGroupById(businessGroupId);
        assertEquals(
            expectedBusinessGroup.getSecondaryFoodType(),
            actualBusinessGroup.getSecondaryFoodType());

        // Create a new business entity under the test business group
        BusinessEntity expectedBusinessEntity =
            TestConstants.BASIC_TEST_BUSINESS_ENTITY.toBuilder()
                .businessEntityName(
                    "TestBusinessEntityName-" +
                        name.getMethodName() +
                        UUID.randomUUID().toString().substring(0, 7))
                .businessGroupId(businessGroupId)
                .businessName(actualBusinessGroup.getBusinessName())
                .phoneNumber(UUID.randomUUID().toString().substring(0, 13))
                .build();

        client.createBusinessEntity(expectedBusinessEntity);
        BusinessEntity actualBusinessEntity =
            client.getBusinessEntityByName(
                expectedBusinessEntity.getBusinessEntityName());
        assertEquals(
            expectedBusinessEntity.getPhoneNumber(),
            actualBusinessEntity.getPhoneNumber());

        // Check that current user can update business entity details
        Long businessEntityId = actualBusinessEntity.getBusinessEntityId();
        expectedBusinessEntity.setBusinessEntityId(businessEntityId);
        expectedBusinessEntity.setOperationType("pop-up");

        client.updateBusinessEntity(expectedBusinessEntity);
        actualBusinessEntity = client.getBusinessEntityById(businessEntityId);

        assertEquals(
            expectedBusinessEntity.getOperationType(),
            actualBusinessEntity.getOperationType());

        // Create another client with a different user
        String randomUserName =
            "Random_user-"
                + name.getMethodName()
                + UUID.randomUUID().toString().substring(0, 10);
        ApiServerClient anotherClient =
            TestHelper.createUserAndLogin(randomUserName);
        // Verify that another user cannot update or delete the business group
        assertThrows(
            RuntimeException.class,
            () -> anotherClient.updateBusinessGroup(expectedBusinessGroup)
        );
        assertThrows(
            RuntimeException.class,
            () -> anotherClient.deleteBusinessGroup(businessGroupId)
        );
        // Verify that another user cannot update or delete the business entity
        assertThrows(
            RuntimeException.class,
            () -> anotherClient.updateBusinessEntity(expectedBusinessEntity)
        );
        assertThrows(
            RuntimeException.class,
            () -> anotherClient.deleteBusinessEntity(businessEntityId)
        );

        // Delete the test business entity, so it can no longer be found
        client.deleteBusinessEntity(businessEntityId);
        assertThrows(
            RuntimeException.class,
            () -> client.getBusinessEntityById(businessEntityId)
        );

        // Delete the test business group, so it can no longer be found
        client.deleteBusinessGroup(businessGroupId);
        assertThrows(
            RuntimeException.class,
            () -> client.getBusinessGroupById(businessGroupId)
        );
    }

}
