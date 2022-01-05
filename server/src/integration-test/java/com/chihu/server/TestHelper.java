package com.chihu.server;

import com.chihu.server.client.ApiServerClient;
import com.chihu.server.model.User;

import java.util.UUID;

public class TestHelper {

    public static User buildRandomUser(String username) {
        return TestConstants.BASIC_TEST_USER.toBuilder()
                .username(username)
                .email(
                    UUID.randomUUID().toString().substring(0, 7) + "@chihu.com")
                .phone(UUID.randomUUID().toString().substring(0, 13))
                .build();
    }

    public static ApiServerClient createUserAndLogin(String username) throws Exception {
        ApiServerClient client = ApiServerClient.create(TestConstants.HOST);
        User testUser = buildRandomUser(username);
        client.userRegister(testUser);

        final String activationToken = client.requestActivationToken(testUser.getUsername());
        client.activateUser(activationToken);

        String token = client.userSignIn(
                testUser.getUsername(), testUser.getPassword());
        client = ApiServerClient.create(client, token);
        return client;
    }

}
