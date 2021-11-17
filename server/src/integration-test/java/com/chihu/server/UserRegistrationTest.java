package com.chihu.server;

import com.chihu.server.client.ApiServerClient;
import com.chihu.server.model.User;
import com.chihu.server.service.UserService;
import com.chihu.server.utils.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.Assert.assertEquals;

@Slf4j
public class UserRegistrationTest {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserService userService;

    @Rule
    public TestName name = new TestName();

    private ApiServerClient client;

    @Before
    public void setup() throws Exception {
        client = ApiServerClient.create(TestConstants.HOST);
    }

    @After
    public void cleanup() throws Exception{
        client.close();
    }

    @Test
    public void registerUser_succeed() throws Exception{
        String username = String.format(
                "TestUser-%s-%s", name.getMethodName(), UUID.randomUUID().toString().substring(0, 10));
        String email = username + "@chihu.com";
        String phone = UUID.randomUUID().toString().substring(0, 10);
        String password = TestConstants.DEFAULT_PASSWORD;
        User testUser = User.builder()
                .username(username)
                .password(password)
                .passwordConfirm(password)
                .email(email)
                .phone(phone)
                .build();

        // Register the test user.
        client.userRegister(testUser);

        // TODO: add a flag to disable activation check if this is undesired
        // Test user activation process
        // Test user is not activated right now
        assertEquals(false, testUser.isActivated());
        assertThrows(
                RuntimeException.class,
                () -> client.userSignIn(username, password));

        // Activate user
        final String activationToken = client.requestActivationToken(username);
        client.activateUser(activationToken);
        // Get auth token and update client with token
        String authToken = client.userSignIn(username, password);
        client = ApiServerClient.create(client, authToken);
        // Get updated user information from DB and verify it is now activated
        User actualUser = client.getUser();
        assertEquals(true, actualUser.isActivated());

        // Login with incorrect password will fail
        assertThrows(
            RuntimeException.class,
            () -> client.userSignIn(username, UUID.randomUUID().toString()));

        // Request user password reset token
        final String passwordResetToken = client.requestPasswordResetToken(username);
        // Use the token to reset password
        final String newPassword = "Random-New-Password-0";
        client.resetPasswordWithToken(passwordResetToken, username, newPassword, newPassword);
        // Not able to use the old password anymore
        assertThrows(
            RuntimeException.class,
            () -> client.userSignIn(username, password));
        // The token becomes invalid after resetting the password
        final String anotherPassword = UUID.randomUUID().toString();
        assertThrows(
            RuntimeException.class,
            () -> client.resetPasswordWithToken(passwordResetToken, username, anotherPassword, anotherPassword)
        );
        authToken = client.userSignIn(username, newPassword);
        client = ApiServerClient.create(client, authToken);

    }
}
