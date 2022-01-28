package com.chihu.server.proxy;

import com.chihu.server.model.BusinessGroup;
import lombok.NonNull;

import java.util.Optional;

public interface BusinessGroupDao {
    void createBusinessGroup(@NonNull BusinessGroup businessGroup);
    void updateBusinessGroup(@NonNull BusinessGroup businessGroup);
    void deleteBusinessGroup(@NonNull Long businessGroupId);

    Optional<BusinessGroup> getBusinessGroupById(@NonNull Long businessGroupId);
    Optional<BusinessGroup> getBusinessGroupByName(@NonNull String name);
}
