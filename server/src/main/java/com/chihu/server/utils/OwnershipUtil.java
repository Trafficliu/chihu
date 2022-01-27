package com.chihu.server.utils;

import com.chihu.server.model.BusinessEntity;
import com.chihu.server.model.BusinessGroup;
import com.chihu.server.proxy.BusinessEntityDao;
import com.chihu.server.proxy.BusinessGroupDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class OwnershipUtil {
    @Autowired
    private BusinessGroupDao businessGroupDao;

    @Autowired
    private BusinessEntityDao businessEntityDao;

    public boolean userIsBusinessGroupOwner(Long userId, Long businessGroupId) {
        Optional<BusinessGroup> businessGroup =
            businessGroupDao.getBusinessGroupById(businessGroupId);
        if (businessGroup.isEmpty()) {
            throw new IllegalArgumentException(
                "Couldn't find a valid business group with the provided ID");
        }
        Long businessOwnerId = businessGroup.get().getOwnerId();
        return businessOwnerId.equals(userId);
    }

    public boolean userIsBusinessEntityOwner(Long userId, Long businessEntityId) {
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
