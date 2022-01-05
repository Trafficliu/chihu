package com.chihu.server.controller;

import com.chihu.server.model.BusinessEntity;
import com.chihu.server.model.BusinessGroup;
import com.chihu.server.model.ChihuUserDetails;
import com.chihu.server.model.User;
import com.chihu.server.proxy.BusinessEntityDao;
import com.chihu.server.proxy.BusinessGroupDao;
import com.chihu.server.serializer.ApiServerSerializer;
import com.chihu.server.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@RestController
public class BusinessController {

    @Autowired
    private UserService userService;

    @Autowired
    private BusinessGroupDao businessGroupDao;

    @Autowired
    private BusinessEntityDao businessEntityDao;

    /**
     *  Business Group Operations
     */
    // TODO: only the business owner should have access to update or delete a business group
    @PostMapping("/business/create_business_group")
    public String createBusinessGroup(
        Authentication authentication,
        @RequestParam(value = "business_group_str", required = true) String businessGroupStr)
        throws IllegalArgumentException, AccessDeniedException {

        BusinessGroup businessGroup =
            ApiServerSerializer.toBusinessGroup(businessGroupStr);
        ChihuUserDetails userDetails =
            (ChihuUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        businessGroup.setOwnerId(userId);

        businessGroupDao.insertOrUpdateBusinessGroup(businessGroup);
        return "";
    }

    @PostMapping("/business/update_business_group")
    public String updateBusinessGroup(
        Authentication authentication,
        @RequestParam(value = "business_group_str", required = true) String businessGroupStr)
        throws IllegalArgumentException, AccessDeniedException {
        BusinessGroup businessGroup =
            ApiServerSerializer.toBusinessGroup(businessGroupStr);

        // Verify that the current is the owner of the business group
        Long businessGroupId = businessGroup.getBusinessGroupId();
        ChihuUserDetails userDetails =
            (ChihuUserDetails)authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        if (!userIsBusinessGroupOwner(userId, businessGroupId)) {
            throw new AccessDeniedException(
                "The user is not the owner of the business.");
        }
        businessGroup.setOwnerId(userId);

        businessGroupDao.insertOrUpdateBusinessGroup(businessGroup);
        return "";
    }

    @PostMapping("/business/delete_business_group")
    public void deleteBusinessGroup(
        Authentication authentication,
        @RequestParam(value = "business_group_id", required = true) Long businessGroupId)
        throws IllegalArgumentException, AccessDeniedException {

        // Verify the current user is the business group owner
        ChihuUserDetails userDetails =
            (ChihuUserDetails)authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        if (!userIsBusinessGroupOwner(userId, businessGroupId)) {
            throw new AccessDeniedException(
                "The user is not the owner of the business.");
        }

        businessGroupDao.deleteBusinessGroup(businessGroupId);
    }

    @GetMapping("/business/get_business_group_by_id")
    public BusinessGroup getBusinessGroupId(
            Authentication authentication,
            @RequestParam(value = "business_group_id", required = true) Long businessGroupId)
        throws IllegalArgumentException {

        Optional<BusinessGroup> businessGroup =
            businessGroupDao.getBusinessGroupById(businessGroupId);
        if (businessGroup.isEmpty()) {
            throw new IllegalArgumentException(
                "Couldn't find a valid business group with the provided ID");
        }

        return businessGroup.get();
    }

    @GetMapping("/business/get_business_group_by_name")
    public BusinessGroup getBusinessGroupByName(
        Authentication authentication,
        @RequestParam(value = "business_group_name", required = true) String businessGroupName)
        throws IllegalArgumentException {

        Optional<BusinessGroup> businessGroup =
            businessGroupDao.getBusinessGroupByName(businessGroupName);
        if (businessGroup.isEmpty()) {
            throw new IllegalArgumentException(
                "Couldn't find a valid business group with the provided name");
        }
        return businessGroup.get();
    }

    /**
     *  Business Entity Operations
     */
    @PostMapping("/business/create_business_entity")
    public String createBusinessEntity(
        Authentication authentication,
        @RequestParam(value = "business_entity_str", required = true) String businessEntityStr)
        throws IllegalArgumentException {

        BusinessEntity businessEntity =
            ApiServerSerializer.toBusinessEntity(businessEntityStr);
        ChihuUserDetails userDetails =
            (ChihuUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        businessEntity.setOwnerId(userId);

        businessEntityDao.insertOrUpdateBusinessEntity(businessEntity);
        return "";
    }

    @PostMapping("/business/update_business_entity")
    public String updateBusinessEntity(
        Authentication authentication,
        @RequestParam(value = "business_entity_str", required = true) String businessEntityStr)
        throws IllegalArgumentException, AccessDeniedException {

        BusinessEntity businessEntity =
            ApiServerSerializer.toBusinessEntity(businessEntityStr);
        Long businessEntityId = businessEntity.getBusinessEntityId();
        ChihuUserDetails userDetails =
            (ChihuUserDetails)authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        if (!userIsBusinessEntityOwner(userId, businessEntityId)) {
            throw new AccessDeniedException(
                "The user is not the owner of the business.");
        }
        businessEntity.setOwnerId(userId);

        businessEntityDao.insertOrUpdateBusinessEntity(businessEntity);
        return "";
    }

    @PostMapping("/business/delete_business_entity")
    public void deleteBusinessEntity(
        Authentication authentication,
        @RequestParam(value = "business_entity_id", required = true) Long businessEntityId)
        throws IllegalArgumentException, AccessDeniedException {

        ChihuUserDetails userDetails =
            (ChihuUserDetails)authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        if (!userIsBusinessEntityOwner(userId, businessEntityId)) {
            throw new AccessDeniedException(
                "The user is not the owner of the business.");
        }

        businessEntityDao.deleteBusinessEntity(businessEntityId);
    }

    @GetMapping("/business/get_business_entity_by_id")
    public BusinessEntity getBusinessEntityById(
        Authentication authentication,
        @RequestParam(value = "business_entity_id", required = true) Long business_entity_id)
        throws IllegalArgumentException {

        Optional<BusinessEntity> businessEntity =
            businessEntityDao.getBusinessEntityById(business_entity_id);
        if (businessEntity.isEmpty()) {
            throw new IllegalArgumentException(
                "Couldn't find a valid business entity with the provided ID");
        }

        return businessEntity.get();
    }

    @GetMapping("/business/get_business_entity_by_name")
    public BusinessEntity getBusinessEntityByName(
        Authentication authentication,
        @RequestParam(value = "business_entity_name", required = true) String business_entity_name)
        throws IllegalArgumentException {

        Optional<BusinessEntity> businessEntity =
            businessEntityDao.getBusinessEntityByName(business_entity_name);
        if (businessEntity.isEmpty()) {
            throw new IllegalArgumentException(
                "Couldn't find a valid business entity with the provided name");
        }

        return businessEntity.get();
    }

    // Helper Functions
    private boolean userIsBusinessGroupOwner(Long userId, Long businessGroupId) {
        Optional<BusinessGroup> businessGroup =
            businessGroupDao.getBusinessGroupById(businessGroupId);
        if (businessGroup.isEmpty()) {
            throw new IllegalArgumentException(
                "Couldn't find a valid business group with the provided ID");
        }
        Long businessOwnerId = businessGroup.get().getOwnerId();
        return businessOwnerId.equals(userId);
    }

    private boolean userIsBusinessEntityOwner(Long userId, Long businessEntityId) {
        Optional<BusinessEntity> businessEntity =
            businessEntityDao.getBusinessEntityById(businessEntityId);
        if (businessEntity.isEmpty()) {
            throw new IllegalArgumentException(
                "Couldn't find a valid business entity with the provided ID");
        }
        Long businessOwnerId = businessEntity.get().getOwnerId();
        return businessOwnerId.equals(userId);
    }
}