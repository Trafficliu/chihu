package com.chihu.server.proxy;

import com.chihu.server.model.BusinessEntity;
import com.chihu.server.model.BusinessGroup;
import com.chihu.server.proxy.BusinessEntityDao;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class BusinessEntityDaoImpl implements BusinessEntityDao {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public void insertOrUpdateBusinessEntity(@NonNull BusinessEntity businessEntity) {
        Instant now = Instant.now();
        StringBuilder sqlBuilder = new StringBuilder()
            .append("INSERT INTO business_entities")
            .append("        SET owner_id = :owner_id, ")
            .append("            business_entity_name = :business_entity_name, ")
            .append("            business_group_id = :business_group_id, ")
            .append("            business_name = :business_name, ")
            .append("            operation_type = :operation_type, ")
            .append("            phone_number = :phone_number, ")
            .append("            working_date = :working_date, ")
            .append("            working_days = :working_days, ")
            .append("            working_hours = :working_hours, ")
            .append("            address_line1 = :address_line1, ")
            .append("            address_line2 = :address_line2, ")
            .append("            city = :city, ")
            .append("            state = :state, ")
            .append("            country = :country, ")
            .append("            zip_code = :zip_code, ")
            .append("            geo_code = :geo_code ")
            .append("ON DUPLICATE KEY UPDATE ")
            .append("            business_entity_name = :business_entity_name, ")
            .append("            business_group_id = :business_group_id, ")
            .append("            business_name = :business_name, ")
            .append("            operation_type = :operation_type, ")
            .append("            phone_number = :phone_number, ")
            .append("            working_date = :working_date, ")
            .append("            working_days = :working_days, ")
            .append("            working_hours = :working_hours, ")
            .append("            address_line1 = :address_line1, ")
            .append("            address_line2 = :address_line2, ")
            .append("            city = :city, ")
            .append("            state = :state, ")
            .append("            country = :country, ")
            .append("            zip_code = :zip_code, ")
            .append("            geo_code = :geo_code ")
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
//            .addValue("creation_timestamp", now.toEpochMilli())
//            .addValue("last_update_timestamp", now.toEpochMilli())
            ;

        namedParameterJdbcTemplate.update(sqlBuilder.toString(), parameterSource);
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


}
