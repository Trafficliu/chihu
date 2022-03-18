package com.chihu.server;

import com.chihu.server.client.ApiServerClient;
import com.chihu.server.model.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class OrderTest {

    @Rule
    public TestName name = new TestName();

    private ApiServerClient ownerClient;
    private ApiServerClient eaterClient;

    @Before
    public void setup() throws Exception {
        // create business owner client
        String businessOwnerName =
            TestHelper.generateRandomNameWithPrefix(
                "Business_Owner-" + name.getMethodName());
        ownerClient = TestHelper.createUserAndLogin(businessOwnerName);
        // create eater client
        String eaterName =
            TestHelper.generateRandomNameWithPrefix("Eater-" + name.getMethodName());
        eaterClient = TestHelper.createUserAndLogin(eaterName);
    }

    @After
    public void cleanup() throws Exception {
        ownerClient.close();
        eaterClient.close();
    }

    @Test
    public void createAndUpdateOrder_succeed() throws Exception {
        // Create business and add a couple of dishes
        BusinessGroup businessGroup =
            TestConstants.BASIC_TEST_BUSINESS_GROUP.toBuilder()
                .businessName(
                    TestHelper.generateRandomNameWithPrefix(
                        "TestBusinessGroup-"+ name.getMethodName()))
                .externalImageId(UUID.randomUUID().toString().substring(0, 15))
                .build();
        ownerClient.createBusinessGroup(businessGroup);
        BusinessGroup actualBusinessGroup =
            ownerClient.getBusinessGroupByName(
                businessGroup.getBusinessName());
        Long businessGroupId = actualBusinessGroup.getBusinessGroupId();
        int testDish1PriceInCent = 1000;
        int testDish2PriceInCent = 1500;
        Dish testDish1 =
            TestConstants.BASIC_TEST_DISH.toBuilder()
                .businessGroupId(businessGroupId)
                .dishName(TestHelper.generateRandomNameWithPrefix("OrderTestDishName"))
                .priceInCent(testDish1PriceInCent)
                .build();
        ownerClient.createDish(testDish1);
        Dish testDish1DbRecord =
            ownerClient.getDishByName(businessGroupId, testDish1.getDishName());
        Long testDish1Id = testDish1DbRecord.getDishId();
        testDish1.setDishId(testDish1Id);
        Dish testDish2 =
            TestConstants.BASIC_TEST_DISH.toBuilder()
                .businessGroupId(businessGroupId)
                .dishName(TestHelper.generateRandomNameWithPrefix("OrderTestDishName"))
                .priceInCent(testDish2PriceInCent)
                .build();
        ownerClient.createDish(testDish2);
        Dish testDish2DbRecord =
            ownerClient.getDishByName(businessGroupId, testDish2.getDishName());
        Long testDish2Id = testDish2DbRecord.getDishId();
        testDish2.setDishId(testDish2Id);

        // Create a business entity where the eater make an order
        BusinessEntity testBusinessEntity =
            TestConstants.BASIC_TEST_BUSINESS_ENTITY.toBuilder()
                .businessEntityName(
                    TestHelper.generateRandomNameWithPrefix(
                        "TestBusinessEntity-" + name.getMethodName()))
                .businessGroupId(businessGroupId)
                .businessName(actualBusinessGroup.getBusinessName())
                .phoneNumber(UUID.randomUUID().toString().substring(0, 13))
                .addressLine1("12121 W Pico Blvd")
                .addressLine2("")
                .city("Los Angeles")
                .state("CA")
                .country("US")
                .zipCode("90064")
                .build();
        ownerClient.createBusinessEntity(testBusinessEntity);
        BusinessEntity testBusinessEntityRecord =
            ownerClient.getBusinessEntityByName(
                testBusinessEntity.getBusinessEntityName());
        Long businessEntityId = testBusinessEntityRecord.getBusinessEntityId();
        // User that makes the order
        User eater = eaterClient.getUser();
        // Add some dishes to user cart
        Map<Long, Integer> dishCount = new HashMap<>();
//        Integer testDish1Count = 2;
//        Integer testDish2Count = 3;
        dishCount.put(testDish1.getDishId(), 2);
        dishCount.put(testDish2.getDishId(), 3);
        eaterClient.updateCart(dishCount, businessEntityId);

        List<OrderDetail> actualDishesInCart = eaterClient.getDishesInCart();
        assertEquals(actualDishesInCart.size(), dishCount.size());
        for (OrderDetail orderDetail : actualDishesInCart) {
            assertEquals(
                orderDetail.getItemCount(),
                dishCount.get(orderDetail.getDishId()).intValue());
        }

        dishCount.remove(testDish2.getDishId());
        eaterClient.updateCart(dishCount, businessEntityId);
        actualDishesInCart = eaterClient.getDishesInCart();
        assertEquals(actualDishesInCart.size(), dishCount.size());
        for (OrderDetail orderDetail : actualDishesInCart) {
            assertEquals(
                dishCount.get(orderDetail.getDishId()).intValue(),
                orderDetail.getItemCount());
        }

        dishCount.put(testDish2.getDishId(), 4);
        eaterClient.updateCart(dishCount, businessEntityId);
        actualDishesInCart = eaterClient.getDishesInCart();
        assertEquals(actualDishesInCart.size(), dishCount.size());
        for (OrderDetail orderDetail : actualDishesInCart) {
            assertEquals(
                dishCount.get(orderDetail.getDishId()).intValue(),
                orderDetail.getItemCount());
        }

        // Create an address for the user to make a delivery order
        Address eaterAddress =
            TestConstants.BASIC_TEST_ADDRESS.toBuilder()
                .userId(eater.getId())
                .contactName(eater.getUsername())
                .phoneNumber(UUID.randomUUID().toString().substring(0, 13))
                .addressLine1("10800 W Pico Blvd")
                .addressLine2("Suite 312")
                .city("Los Angeles")
                .state("CA")
                .country("US")
                .zipCode("90064")
                .build();
        eaterClient.createAddress(eaterAddress);
        List<Address> eaterAddressRecords = eaterClient.getUserAddresses();
        Long addressId = eaterAddressRecords.get(0).getAddressId();
        eaterClient.createOrder(
            dishCount, businessEntityId, addressId, "PICK_UP", 500);
        List<OrderSummary> eaterOrderSummaryRecords =
            eaterClient.getAllOrderSummaries();
        // Verify that order is submitted
        assertEquals(eaterOrderSummaryRecords.size(), 1);
        int expectedSubtotalInCent =
            testDish1PriceInCent * dishCount.get(testDish1Id)
                + testDish2PriceInCent * dishCount.get(testDish2Id);
        // Verify that order summary is as expected
        OrderSummary orderSummary = eaterOrderSummaryRecords.get(0);
        assertEquals(eater.getId(), orderSummary.getUserId());
        assertEquals(businessEntityId, orderSummary.getBusinessEntityId());
        assertEquals(expectedSubtotalInCent, orderSummary.getSubtotalInCent());
        assertEquals(eaterAddress.getAddressLine1(), orderSummary.getAddressLine1());
        assertEquals(eaterAddress.getAddressLine2(), orderSummary.getAddressLine2());
        assertEquals("PENDING_CONFIRM", orderSummary.getOrderStatus());
        // Verify that order contains the correct number of dishes
        Long orderId = orderSummary.getOrderId();
        List<OrderDetail> actualOrderDetails = eaterClient.getOrderDishes(orderId);
        assertEquals(actualOrderDetails.size(), dishCount.size());
        for (OrderDetail orderDetail : actualOrderDetails) {
            assertEquals(
                orderDetail.getItemCount(),
                dishCount.get(orderDetail.getDishId()).intValue());
        }
        // Owner confirms the order
        ownerClient.ownerUpdateOrderStatus(orderId, "CONFIRMED");
        orderSummary = eaterClient.getOrderSummary(orderId);
        assertEquals("CONFIRMED", orderSummary.getOrderStatus());
        ownerClient.ownerUpdateOrderStatus(orderId, "COMPLETED");
        orderSummary = eaterClient.getOrderSummary(orderId);
        assertEquals("COMPLETED", orderSummary.getOrderStatus());
//        // User update items in the order
//        dishCount.put(testDish1Id, 5);


    }

}
