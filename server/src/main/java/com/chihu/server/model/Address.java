package com.chihu.server.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long addressId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "contact_name")
    private String contactName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "address_line1")
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "country")
    @Builder.Default
    private String country = "US";

    @Column(name = "zip_code")
    private String zipCode;

    @Column(name = "geo_code")
    private Long geoCode;

}
