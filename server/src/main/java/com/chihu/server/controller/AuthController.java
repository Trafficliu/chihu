package com.chihu.server.controller;

import com.chihu.server.config.CookieConfigurationProvider;
import com.chihu.server.model.Role;
import com.chihu.server.repository.RoleRepository;
import com.chihu.server.utils.JwtTokenUtil;
import com.chihu.server.model.ChihuUserDetails;
import com.chihu.server.model.User;
import com.chihu.server.model.UserType;
import com.chihu.server.service.ChihuUserDetailsService;
import com.chihu.server.service.UserService;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
//import org.springframework.security.core.Authentication;

@Slf4j
@RestController
public class AuthController {
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private ChihuUserDetailsService userDetailsService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CookieConfigurationProvider cookieConfigurationProvider;

    @PostMapping("/auth/register")
    public String register(@RequestParam(value = "username", required = true) String username,
                           @RequestParam(value = "password", required = true) String password,
                           @RequestParam(value = "email", required = true) String email,
                           @RequestParam(value = "phone", required = true) String phone) {
        // TODO: Add email validation
        // TODO: Add password validation
        Set<Role> roles = new HashSet<Role>();
        Role role = roleRepository.findByName("EATER");
        roles.add(role);
        User user = User.builder()
                .username(username)
                .password(password)
                .email(email)
                .phone(phone)
                .roles(roles)
                .build();

        //TODO: add exception handling here.
        try {
            userService.addUser(user);
            // TODO(traffic): Add a step to send activation token via email or text
//            // Generate activation verification code
//            final String activationToken = jwtTokenUtil.generateActivationToken(username);
            // TODO: Send email verification code to user by creating sendEmail();
//            if (jwtTokenUtil.isActivationTokenValid(activationToken)) {
//                userService.updateActivation(user, true);
//            }
            // TODO: sign in automatically after this?
        } catch (Exception e) {
            Throwable rootCause = Throwables.getRootCause(e);
            if (rootCause.getMessage().contains("Duplicate entry")) {
                throw new IllegalArgumentException(
                    "A user with the username, email, or phone number already exists."
                );
            }
            throw e;
        }
        return "";
    }

    // Request an activation token with username.
    // This is used in the integration test to get token to activate user.
    @GetMapping("/auth/request_activation_token")
    public String requestActivationToken(
            @RequestParam(value= "username", required = true) String username) {
        Optional<User> user = userService.getUserByUsername(username);
        if (user.isPresent()) {
            String activationToken = jwtTokenUtil.generateActivationToken(username);
            return activationToken;
        } else {
            return "";
        }
    }

    // This can be use the original activation token is expired before used.
    @PostMapping("/auth/email_activation_token")
    public void emailActivationToken(
            @RequestParam(value= "email", required = true) String email) {
        Optional<User> user = userService.getUserByEmail(email);
        if (user.isPresent()) {
            String activationToken = jwtTokenUtil.generateActivationToken(user.get().getUsername());
            // TODO: create an emailManager to send out activation token
        }
        return;
    }

    @PostMapping("/auth/activate")
    public void activate(@RequestParam(value = "token", required = true) String token) {
        if (!StringUtils.hasText(token)
            || !jwtTokenUtil.isActivationTokenValid(token)) {
            return;
        }
        String username = jwtTokenUtil.extractUsername(token);
        Optional<User> user = userService.getUserByUsername(username);
        user.ifPresent(value -> userService.updateActivation(value, true));
    }

    // Map<String, String>
    @PostMapping("/auth/sign_in")
    public String signIn(HttpServletResponse response,
                         @RequestParam(value = "username", required = true) String username,
                         @RequestParam(value = "password", required = true) String password) {
        final String jwtToken = authenticate(username, password);

        response.addCookie(
                buildJwtTokenCookie(
                        UserType.EATER.getId(),
                        jwtToken,
                        jwtTokenUtil.getDefaultValidDuration()));
//        return ImmutableMap.of(ApiServerConstants.JWT_COOKIE_NAME, jwtToken);
        return jwtToken;
    }

    @GetMapping("/auth/request_pwd_reset_token")
    public String requestPasswordResetToken(
            @RequestParam(value = "username", required = true) String username) {
        Optional<User> user = userService.getUserByUsername(username);
        if (user.isPresent()) {
            String passwordResetToken = jwtTokenUtil.generatePasswordResetToken(user.get());
            return passwordResetToken;
        } else {
            return "";
        }
    }

    @PostMapping("/auth/email_pwd_reset_token")
    public void emailPasswordResetToken(
            @RequestParam(value = "email", required = true) String email) {
        Optional<User> user = userService.getUserByEmail(email);
        if (user.isPresent()) {
            String passwordResetToken = jwtTokenUtil.generatePasswordResetToken(user.get());
            // TODO: create an emailManager to send out password reset token
        }
    }

    @PostMapping("/auth/reset_password_with_token")
    public void resetPassword(
            @RequestParam(value = "token", required = true) String passwordResetToken,
            @RequestParam(value = "username", required = true) String username,
            @RequestParam(value = "password", required = true) String password,
            @RequestParam(value = "confirm_password", required = true) String confirmPassword) {
        if (!jwtTokenUtil.isPasswordResetTokenValid(username, passwordResetToken)) {
            throw new IllegalArgumentException("The token provided is invalid or expired.");
        }
        Optional<User> user = userService.getUserByUsername(username);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("The user is not found in our record.");
        }
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("new password and confirm password doesn't match.");
        }
        userService.changePassword(user.get(), password);
        // TODO: send an email to the user saying their password is reset
    }

    @GetMapping("/auth/get_user")
    public User getUser(Authentication authentication) {
        String username = authentication.getName();
        Optional<User> user = userService.getUserByUsername(username);
        return user.orElse(null);
    }

    // Helper methods
    private String authenticate(String username, String password) {
        ChihuUserDetails userDetails = userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(
                        username, password, userDetails.getAuthorities());
        Authentication authentication =
                authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        if (usernamePasswordAuthenticationToken.isAuthenticated()) {
            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        }
        return jwtTokenUtil.generateToken(authentication);
    }

    private Cookie buildJwtTokenCookie(UserType userType, String tokenValue, Duration expirationDuration) {
        return buildJwtTokenCookie(userType.getId(), tokenValue, expirationDuration);
    }

    private Cookie buildJwtTokenCookie(int userTypeId, String tokenValue, Duration expirationDuration) {
        final Cookie cookie = new Cookie(
                cookieConfigurationProvider.getJwtKeyFromType(userTypeId), tokenValue);
        cookie.setDomain(cookieConfigurationProvider.getDomain());
        cookie.setPath(cookieConfigurationProvider.getPath());
        cookie.setSecure(cookieConfigurationProvider.isSecure());
        cookie.setHttpOnly(cookieConfigurationProvider.isHttpOnly());
        cookie.setMaxAge((int) expirationDuration.getStandardSeconds());
        return cookie;
    }

}
