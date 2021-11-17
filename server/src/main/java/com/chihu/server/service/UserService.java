package com.chihu.server.service;

import com.chihu.server.cache.CacheFactory;
import com.chihu.server.model.User;
import com.chihu.server.repository.UserDetailsRepository;
import com.google.common.base.Charsets;
import com.google.common.cache.Cache;
import com.google.common.hash.Hashing;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.Charset;
import java.util.Optional;

@Repository
@Slf4j
@Transactional
public class UserService {
    private static final Cache<Long, String> userIdToEmailAddressCache = CacheFactory.createCacheInstance();
    private static final Cache<Long, String> userIdToContactCache = CacheFactory.createCacheInstance();

    @Autowired
    private UserDetailsRepository userDetailsRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public Optional<User> getUserByUsername(String username) {
        return userDetailsRepository.findByUsername(username);
    }

    public Optional<User> getUserByEmail(String email) {
        return userDetailsRepository.findByEmail(email);
    }

    public Optional<User> getUserByPhone(String phone) {
        return userDetailsRepository.findByPhone(phone);
    }

    public Optional<User> getUserById(Long userId) {
        return userDetailsRepository.findByUserId(userId);
    }

    public User addUser(User user) {
        user.setPassword(
                bCryptPasswordEncoder.encode(user.getPassword()));
        long now = System.currentTimeMillis();
        user.setCreateTimestamp(now);
        user.setLastUpdateTimestamp(now);
        return userDetailsRepository.save(user);
    }

    public User updateActivation(User user, boolean activated) {
        user.setActivated(activated);
        user.setLastUpdateTimestamp(System.currentTimeMillis());
        return userDetailsRepository.save(user);
    }

    public User changePassword(User user, String newPassword) {
        user.setPassword(
                bCryptPasswordEncoder.encode(newPassword));
        user.setLastUpdateTimestamp(System.currentTimeMillis());
        return userDetailsRepository.save(user);
    }

//    public Long getUserIdFromActivationToken(String token) {
//        StringBuilder sql = new StringBuilder()
//                .append("SELECT user_id")
//                .append("  FROM user_activations")
//                .append(" WHERE token=:token")
//                .append("   AND expiration_timestamp>:now");
//        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
//                .addValue("token", Hashing.sha256().hashString(token, Charsets.UTF_8).toString())
//                .addValue("now", System.currentTimeMillis());
//        return namedParameterJdbcTemplate.queryForObject(sql.toString(), parameterSource, Long.class);
//    }
}
