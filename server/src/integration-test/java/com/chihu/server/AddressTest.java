package com.chihu.server;

import com.chihu.server.client.ApiServerClient;
import com.chihu.server.model.Address;
import com.chihu.server.model.User;
import com.chihu.server.service.UserService;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AddressTest {
    @Autowired
    private UserService userService;

    @Rule
    public TestName name = new TestName();

    private ApiServerClient client;

    @Before
    public void setup() throws Exception {
        String userName =
            "Address-Test-User"
                + name.getMethodName()
                + UUID.randomUUID().toString().substring(0, 10);
        client = TestHelper.createUserAndLogin(userName);
    }

    @After
    public void cleanup() throws Exception {
        client.close();
    }

    @Test
    public void createAndUpdateDish_succeed() throws Exception {
        // Create a new address and verify it can be found in DB
        User actualUser = client.getUser();
        Address expectedAddress =
            TestConstants.BASIC_TEST_ADDRESS.toBuilder()
                .contactName(actualUser.getUsername())
                .phoneNumber(UUID.randomUUID().toString().substring(0, 13))
                .build();
        client.createAddress(expectedAddress);
        List<Address> actualAddresses = client.getUserAddresses();
        assertEquals(1, actualAddresses.size());
        Address actualAddressFromList = actualAddresses.get(0);
        assertEquals(
            expectedAddress.getPhoneNumber(), actualAddressFromList.getPhoneNumber());
        Long addressId = actualAddressFromList.getAddressId();
        Address actualAddress = client.getAddressById(addressId);
        assertEquals(
            expectedAddress.getPhoneNumber(), actualAddress.getPhoneNumber());
        // Verify that user can update address
        expectedAddress.setAddressId(addressId);
        expectedAddress.setContactName("Updated_" + actualUser.getUsername());
        client.updateAddress(expectedAddress);
        actualAddress = client.getAddressById(addressId);
        assertEquals(
            expectedAddress.getContactName(), actualAddress.getContactName());

        // Create another client with a different user
        String randomUserName =
            "Random_user-"
                + name.getMethodName()
                + UUID.randomUUID().toString().substring(0, 10);
        ApiServerClient anotherClient =
            TestHelper.createUserAndLogin(randomUserName);
        // Verify that another user cannot update or delete the address
        Address randomAddress =
            TestConstants.BASIC_TEST_ADDRESS.toBuilder()
                .addressId(addressId)
                .contactName(actualUser.getUsername())
                .phoneNumber(UUID.randomUUID().toString().substring(0, 13))
                .build();
        anotherClient.updateAddress(randomAddress);
        // Verify that address is not changed when another user attempted to make change
        actualAddress = client.getAddressById(addressId);
        assertEquals(
            expectedAddress.getContactName(), actualAddress.getContactName());
        assertEquals(
            expectedAddress.getPhoneNumber(), actualAddress.getPhoneNumber());
        // Verify that address is not deleted when another user attempted to delete it
        anotherClient.deleteAddress(addressId);
        actualAddress = client.getAddressById(addressId);
        assertEquals(
            expectedAddress.getContactName(), actualAddress.getContactName());
        assertEquals(
            expectedAddress.getPhoneNumber(), actualAddress.getPhoneNumber());

        // Delete the test address, so it can no longer be found
        client.deleteAddress(addressId);
        assertThrows(
            RuntimeException.class,
            () -> client.getAddressById(addressId)
        );
    }
}
