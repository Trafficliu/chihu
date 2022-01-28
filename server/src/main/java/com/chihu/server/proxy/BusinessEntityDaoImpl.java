package com.chihu.server.proxy;

import com.chihu.server.model.BusinessEntity;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class BusinessEntityDaoImpl implements BusinessEntityDao {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public void createBusinessEntity(@NonNull BusinessEntity businessEntity) {
        String sql =
            "INSERT INTO business_entities " +
            "        SET owner_id = :owner_id, " +
            "            business_entity_name = :business_entity_name, " +
            "            business_group_id = :business_group_id, " +
            "            business_name = :business_name, " +
            "            operation_type = :operation_type, " +
            "            phone_number = :phone_number, " +
            "            working_date = :working_date, " +
            "            working_days = :working_days, " +
            "            working_hours = :working_hours, " +
            "            address_line1 = :address_line1, " +
            "            address_line2 = :address_line2, " +
            "            city = :city, " +
            "            state = :state, " +
            "            country = :country, " +
            "            zip_code = :zip_code, " +
            "            geo_code = :geo_code "
            ;
        SqlParameterSource parameterSource = new MapSqlParameterSource()
            .addValue("owner_id", businessEntity.getOwnerId())
            .addValue("business_entity_name", businessEntity.getBusinessEntityName())
            .addValue("business_group_id", businessEntity.getBusinessGroupId())
            .addValue("business_name", businessEntity.getBusinessName())
            .addValue("operation_type", businessEntity.getOperationType())
            .addValue("phone_number", businessEntity.getPhoneNumber())
            .addValue("working_date", businessEntity.getWorkingDate())
            .addValue("working_days", businessEntity.getWorkingDays())
            .addValue("working_hours", businessEntity.getWorkingHours())
            .addValue("address_line1", businessEntity.getAddressLine1())
            .addValue("address_line2", businessEntity.getAddressLine2())
            .addValue("city", businessEntity.getCity())
            .addValue("state", businessEntity.getState())
            .addValue("country", businessEntity.getCountry())
            .addValue("zip_code", businessEntity.getZipCode())
            .addValue("geo_code", businessEntity.getGeoCode())
            ;

        namedParameterJdbcTemplate.update(sql, parameterSource);
    }

    @Override
    public void updateBusinessEntity(@NonNull BusinessEntity businessEntity) {
        String sql =
            "UPDATE business_entities " +
            "   SET owner_id = :owner_id, " +
            "       business_entity_name = :business_entity_name, " +
            "       business_group_id = :business_group_id, " +
            "       business_name = :business_name, " +
            "       operation_type = :operation_type, " +
            "       phone_number = :phone_number, " +
            "       working_date = :working_date, " +
            "       working_days = :working_days, " +
            "       working_hours = :working_hours, " +
            "       address_line1 = :address_line1, " +
            "       address_line2 = :address_line2, " +
            "       city = :city, " +
            "       state = :state, " +
            "       country = :country, " +
            "       zip_code = :zip_code, " +
            "       geo_code = :geo_code " +
            " WHERE business_entity_id = :business_entity_id"
            ;
        SqlParameterSource parameterSource = new MapSqlParameterSource()
            .addValue("business_entity_id", businessEntity.getBusinessEntityId())
            .addValue("owner_id", businessEntity.getOwnerId())
            .addValue("business_entity_name", businessEntity.getBusinessEntityName())
            .addValue("business_group_id", businessEntity.getBusinessGroupId())
            .addValue("business_name", businessEntity.getBusinessName())
            .addValue("operation_type", businessEntity.getOperationType())
            .addValue("phone_number", businessEntity.getPhoneNumber())
            .addValue("working_date", businessEntity.getWorkingDate())
            .addValue("working_days", businessEntity.getWorkingDays())
            .addValue("working_hours", businessEntity.getWorkingHours())
            .addValue("address_line1", businessEntity.getAddressLine1())
            .addValue("address_line2", businessEntity.getAddressLine2())
            .addValue("city", businessEntity.getCity())
            .addValue("state", businessEntity.getState())
            .addValue("country", businessEntity.getCountry())
            .addValue("zip_code", businessEntity.getZipCode())
            .addValue("geo_code", businessEntity.getGeoCode())
            ;

        namedParameterJdbcTemplate.update(sql, parameterSource);
    }

    @Override
    public void deleteBusinessEntity(@NonNull Long businessEntityId) {
        String sql =
            "DELETE FROM business_entities " +
            "      WHERE business_entity_id = :business_entity_id";
        SqlParameterSource parameterSource = new MapSqlParameterSource()
            .addValue("business_entity_id", businessEntityId);
        namedParameterJdbcTemplate.update(sql, parameterSource);
    }

    @Override
    public Optional<BusinessEntity> getBusinessEntityById(@NonNull Long businessEntityId) {
        String sql =
            "SELECT * " +
            "  FROM business_entities " +
            " WHERE business_entity_id = :business_entity_id" +
            " LIMIT 1";
        SqlParameterSource parameterSource = new MapSqlParameterSource()
            .addValue("business_entity_id", businessEntityId);

        List<BusinessEntity> result =
            namedParameterJdbcTemplate.query(
                sql,
                parameterSource,
                new BeanPropertyRowMapper<>(BusinessEntity.class));
        if (result.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(result.iterator().next());
    }

    @Override
    public Optional<BusinessEntity> getBusinessEntityByName(@NonNull String businessEntityName) {
        String sql =
            "SELECT * " +
            "  FROM business_entities " +
            " WHERE business_entity_name = :business_entity_name" +
            " LIMIT 1";
        SqlParameterSource parameterSource = new MapSqlParameterSource()
            .addValue("business_entity_name", businessEntityName);
        List<BusinessEntity> result =
            namedParameterJdbcTemplate.query(
                sql,
                parameterSource,
                new BeanPropertyRowMapper<>(BusinessEntity.class));
        if (result.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(result.iterator().next());
    }

    // TODO: Business Entity should either have a Working Date,
    //       or Working Days. But not both.
    // Working date are used for pop-ups, set in to represent the date for pop-up
    @Override
    public void setWorkingDate(
        @NonNull Long businessEntityId, @NonNull LocalDate date) {
        String sql =
            "UPDATE business_entities " +
            "   SET working_date = :working_date, " +
            "       repeated = :repeated, " +
            "       working_days = 0 " +
            " WHERE business_entity_id = :business_entity_id";
        log.info("SQL query is set to be: {}", sql);
        SqlParameterSource parameterSource = new MapSqlParameterSource()
            .addValue("business_entity_id", businessEntityId)
            .addValue("working_date", Date.valueOf(date))
            .addValue("repeated", false);
        namedParameterJdbcTemplate.update(sql, parameterSource);
    }

    // Working days are set in bits representing if business opens on Monday ... Sunday
    @Override
    public void setWorkingDays(
        @NonNull Long businessEntityId,@NonNull Integer workingDaysBits) {
        String sql =
            "UPDATE business_entities " +
            "   SET working_days = :working_days, " +
            "       repeated = :repeated, " +
            "       working_date = NULL " +
            " WHERE business_entity_id = :business_entity_id";
        SqlParameterSource parameterSource = new MapSqlParameterSource()
            .addValue("business_entity_id", businessEntityId)
            .addValue("working_days", workingDaysBits)
            .addValue("repeated", true);
        namedParameterJdbcTemplate.update(sql, parameterSource);
    }

    // Working hours are set in bits, each bit stands for a 30 minutes block
    // representing if business opens on that 30-minute block
    @Override
    public void setWorkingHours(
        @NonNull Long businessEntityId,@NonNull Long workingHoursBits) {
        String sql =
            "UPDATE business_entities " +
            "   SET working_hours = :working_hours" +
            " WHERE business_entity_id = :business_entity_id";
        SqlParameterSource parameterSource = new MapSqlParameterSource()
            .addValue("business_entity_id", businessEntityId)
            .addValue("working_hours", workingHoursBits);
        namedParameterJdbcTemplate.update(sql, parameterSource);
    }

}
