package com.chihu.server.model;

import lombok.*;

import javax.persistence.*;
import java.sql.Date;

@Entity
@Table(name = "business_entities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class BusinessEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "business_entity_id")
    private Long businessEntityId;

    @Column(name = "business_group_id")
    private Long businessGroupId;

    @Column(name = "owner_id")
    private Long ownerId;

    @Column(name = "business_name")
    private String businessName;

    @Column(name = "business_entity_name")
    private String businessEntityName;

    // Valid types: permanent address, fixed schedule mobile, one time pop-up
    @Column(name = "operation_type")
    private String operationType;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "repeated")
    private boolean repeated;

    @Column(name = "working_date")
    private Date workingDate;

    @Column(name = "working_days")
    private Integer workingDays;

    @Column(name = "working_hours")
    private Long workingHours;

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
