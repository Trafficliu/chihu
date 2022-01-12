package com.chihu.server.proxy;

import com.chihu.server.model.BusinessEntity;
import lombok.NonNull;

import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;

public interface BusinessEntityDao {
    void insertOrUpdateBusinessEntity(@NonNull BusinessEntity businessEntity);
    void deleteBusinessEntity(@NonNull Long businessEntityId);

    Optional<BusinessEntity> getBusinessEntityById(@NonNull Long businessEntityId);
    Optional<BusinessEntity> getBusinessEntityByName(@NonNull String name);

    void setWorkingDate(@NonNull Long businessEntityId, @NonNull LocalDate date);
    void setWorkingDays(@NonNull Long businessEntityId, @NonNull Integer workingDaysBits);
    void setWorkingHours(@NonNull Long businessEntityId, @NonNull Long workingHoursBits);

}
