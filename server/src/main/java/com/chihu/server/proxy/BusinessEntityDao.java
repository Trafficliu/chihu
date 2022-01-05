package com.chihu.server.proxy;

import com.chihu.server.model.BusinessEntity;
import lombok.NonNull;

import java.util.Optional;

public interface BusinessEntityDao {
    void insertOrUpdateBusinessEntity(@NonNull BusinessEntity businessEntity);
    void deleteBusinessEntity(@NonNull Long businessEntityId);

    Optional<BusinessEntity> getBusinessEntityById(@NonNull Long businessEntityId);
    Optional<BusinessEntity> getBusinessEntityByName(@NonNull String name);

}
