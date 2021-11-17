package com.chihu.server.repository;

import com.chihu.server.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional
public interface UserDetailsRepository extends CrudRepository<User, String> {
    @Query("SELECT user FROM User user WHERE user.id = :userId")
    public Optional<User> findByUserId(@Param("userId") Long userId);

    @Query("SELECT user FROM User user WHERE user.username = :username")
    public Optional<User> findByUsername(@Param("username") String username);

    @Query("SELECT user FROM User user WHERE user.email = :email")
    public Optional<User> findByEmail(@Param("email") String email);

    @Query("SELECT user FROM User user WHERE user.phone = :phone")
    public Optional<User> findByPhone(@Param("phone") String phone);
}
