package com.chihu.server.service;

import com.chihu.server.model.ChihuUserDetails;
import com.chihu.server.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ChihuUserDetailsService implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    public ChihuUserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        Optional<User> user = userService.getUserByUsername(username);
        user.orElseThrow(
                () -> new UsernameNotFoundException(
                        String.format("Username %s is not found", username)));

        return user.map(ChihuUserDetails::new).get();
    }

}
