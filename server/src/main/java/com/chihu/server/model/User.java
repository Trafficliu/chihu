package com.chihu.server.model;

import com.chihu.server.common.ApiServerConstants;
import lombok.*;

import javax.persistence.*;
import java.time.Instant;
import java.util.Set;


@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;
    @Transient
    private String passwordConfirm;

//    @Column(name = "user_type")
//    @Builder.Default
//    private String userType = UserType.CLIENT.toString();
    // TODO: change this to enabled
    @Column(name = "activated")
    @Builder.Default
    private boolean activated = false;

    /**
     * This identifies whether this account is linked to credit card or not and
     * deletion status.
     * 0: isNotActivated
     * 1: Active
     * 2: Deleted
     */
    @Column(name = "status")
    @Builder.Default
    private Integer status = 1;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "create_timestamp")
    @Builder.Default
    private Long createTimestamp = Instant.now().toEpochMilli();

    @Column(name = "last_update_timestamp")
    @Builder.Default
    private Long lastUpdateTimestamp = Instant.now().toEpochMilli();

    @Column(name = "last_updated_by")
    @Builder.Default
    private String lastUpdatedBy = ApiServerConstants.ADMIN;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = {@JoinColumn(name="user_id")},
            inverseJoinColumns = {@JoinColumn(name="role_id")})
    private Set<Role> roles;

}
