package com.chihu.server.controller;

import com.chihu.server.model.BusinessEntity;
import com.chihu.server.model.BusinessGroup;
import com.chihu.server.model.ChihuUserDetails;
import com.chihu.server.proxy.BusinessEntityDao;
import com.chihu.server.proxy.BusinessGroupDao;
import com.chihu.server.serializer.ApiServerSerializer;
import com.chihu.server.service.UserService;
import com.chihu.server.utils.OwnershipUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    @Autowired
    private OwnershipUtil ownershipUtil;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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

        businessGroupDao.createBusinessGroup(businessGroup);
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
        if (!ownershipUtil.userIsBusinessGroupOwner(userId, businessGroupId)) {
            throw new AccessDeniedException(
                "The user is not the owner of the business.");
        }
        businessGroup.setOwnerId(userId);

        businessGroupDao.updateBusinessGroup(businessGroup);
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
        if (!ownershipUtil.userIsBusinessGroupOwner(userId, businessGroupId)) {
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

        businessEntityDao.createBusinessEntity(businessEntity);
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
        if (!ownershipUtil.userIsBusinessEntityOwner(userId, businessEntityId)) {
            throw new AccessDeniedException(
                "The user is not the owner of the business.");
        }
        businessEntity.setOwnerId(userId);

        businessEntityDao.updateBusinessEntity(businessEntity);
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
        if (!ownershipUtil.userIsBusinessEntityOwner(userId, businessEntityId)) {
            throw new AccessDeniedException(
                "The user is not the owner of the business.");
        }

        businessEntityDao.deleteBusinessEntity(businessEntityId);
    }

    @GetMapping("/business/get_business_entity_by_id")
    public BusinessEntity getBusinessEntityById(
        Authentication authentication,
        @RequestParam(value = "business_entity_id", required = true) Long businessEntityId)
        throws IllegalArgumentException {

        Optional<BusinessEntity> businessEntity =
            businessEntityDao.getBusinessEntityById(businessEntityId);
        if (businessEntity.isEmpty()) {
            throw new IllegalArgumentException(
                "Couldn't find a valid business entity with the provided ID");
        }
        log.info("Got business entity:{}, business entity id is {}, isOneTime is set to: {}",
            businessEntity.get(), businessEntityId,businessEntity.get().isRepeated());

        return businessEntity.get();
    }

    @GetMapping("/business/get_business_entity_by_name")
    public BusinessEntity getBusinessEntityByName(
        Authentication authentication,
        @RequestParam(value = "business_entity_name", required = true) String businessEntityName)
        throws IllegalArgumentException {

        Optional<BusinessEntity> businessEntity =
            businessEntityDao.getBusinessEntityByName(businessEntityName);
        if (businessEntity.isEmpty()) {
            throw new IllegalArgumentException(
                "Couldn't find a valid business entity with the provided name");
        }

        return businessEntity.get();
    }

    @PostMapping("/business/set_working_date")
    public void setWorkingDate(
        Authentication authentication,
        @RequestParam(value = "business_entity_id", required = true) Long businessEntityId,
        @RequestParam(value = "working_date_str", required = true) String workingDateStr)
    throws IllegalArgumentException, AccessDeniedException {

        ChihuUserDetails userDetails =
            (ChihuUserDetails)authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        if (!ownershipUtil.userIsBusinessEntityOwner(userId, businessEntityId)) {
            throw new AccessDeniedException(
                "The user is not the owner of the business.");
        }
        LocalDate date = LocalDate.parse(workingDateStr, formatter);

        log.info(
            "Trying to set the working date to be {} for business entity: {}",
            date, businessEntityId);
        businessEntityDao.setWorkingDate(businessEntityId, date);
    }

    @PostMapping("/business/set_working_days")
    public void setWorkingDays(
        Authentication authentication,
        @RequestParam(value = "business_entity_id", required = true) Long businessEntityId,
        @RequestParam(value = "working_days", required = true) Integer workingDays)
        throws IllegalArgumentException, AccessDeniedException {

        ChihuUserDetails userDetails =
            (ChihuUserDetails)authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        if (!ownershipUtil.userIsBusinessEntityOwner(userId, businessEntityId)) {
            throw new AccessDeniedException(
                "The user is not the owner of the business.");
        }

        businessEntityDao.setWorkingDays(businessEntityId, workingDays);
    }

    @PostMapping("/business/set_working_hours")
    public void setWorkingHours(
        Authentication authentication,
        @RequestParam(value = "business_entity_id", required = true) Long businessEntityId,
        @RequestParam(value = "working_hours", required = true) Long workingHours)
        throws IllegalArgumentException, AccessDeniedException {

        ChihuUserDetails userDetails =
            (ChihuUserDetails)authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        if (!ownershipUtil.userIsBusinessEntityOwner(userId, businessEntityId)) {
            throw new AccessDeniedException(
                "The user is not the owner of the business.");
        }

        businessEntityDao.setWorkingHours(businessEntityId, workingHours);
    }

//    @PostMapping("/business/is_open")
//    public void isOpenForDate(
//        Authentication authentication,
//        @RequestParam(value = "business_entity_id", required = true) Long businessEntityId,
//        @RequestParam(value = "working_days", required = true) Integer workingDays)
//        throws IllegalArgumentException, AccessDeniedException {
//
//        ChihuUserDetails userDetails =
//            (ChihuUserDetails)authentication.getPrincipal();
//        Long userId = userDetails.getUserId();
//        if (!userIsBusinessEntityOwner(userId, businessEntityId)) {
//            throw new AccessDeniedException(
//                "The user is not the owner of the business.");
//        }
//
//        businessEntityDao.setWorkingDays(businessEntityId, workingDays);
//    }

}
