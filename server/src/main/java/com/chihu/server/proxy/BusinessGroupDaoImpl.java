package com.chihu.server.proxy;

import com.chihu.server.model.BusinessGroup;
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
public class BusinessGroupDaoImpl implements BusinessGroupDao{

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public void insertOrUpdateBusinessGroup(@NonNull BusinessGroup businessGroup) {
        Instant now = Instant.now();
        StringBuilder sqlBuilder = new StringBuilder()
                .append("INSERT INTO business_groups")
                .append("        SET owner_id = :owner_id, ")
                .append("            business_name = :business_name, ")
                .append("            business_type = :business_type, ")
                .append("            phone_number = :phone_number, ")
                .append("            store_image_path = :store_image_path, ")
                .append("            external_image_id = :external_image_id, ")
                .append("            primary_food_type = :primary_food_type, ")
                .append("            secondary_food_type = :secondary_food_type, ")
                .append("            rating = :rating, ")
                .append("            num_of_reviews = :num_of_reviews, ")
                .append("            support_deliver = :support_deliver, ")
                .append("            support_pickup = :support_pickup, ")
                .append("            creation_timestamp = :creation_timestamp, ")
                .append("            last_update_timestamp = :last_update_timestamp ")
                .append("ON DUPLICATE KEY UPDATE ")
                .append("            business_name = :business_name, ")
                .append("            business_type = :business_type, ")
                .append("            phone_number = :phone_number, ")
                .append("            store_image_path = :store_image_path, ")
                .append("            external_image_id = :external_image_id, ")
                .append("            primary_food_type = :primary_food_type, ")
                .append("            secondary_food_type = :secondary_food_type, ")
                .append("            rating = :rating, ")
                .append("            num_of_reviews = :num_of_reviews, ")
                .append("            support_deliver = :support_deliver, ")
                .append("            support_pickup = :support_pickup, ")
                .append("            creation_timestamp = :creation_timestamp, ")
                .append("            last_update_timestamp = :last_update_timestamp ")
                ;
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("owner_id", businessGroup.getOwnerId())
                .addValue("business_name", businessGroup.getBusinessName())
                .addValue("business_type", businessGroup.getBusinessType())
                .addValue("phone_number", businessGroup.getPhoneNumber())
                .addValue("store_image_path", businessGroup.getStoreImagePath())
                .addValue("external_image_id", businessGroup.getExternalImageId())
                .addValue("primary_food_type", businessGroup.getPrimaryFoodType())
                .addValue("secondary_food_type", businessGroup.getSecondaryFoodType())
                .addValue("rating", businessGroup.getRating())
                .addValue("num_of_reviews", businessGroup.getNumOfReviews())
                .addValue("support_deliver", businessGroup.isDeliverSupported())
                .addValue("support_pickup", businessGroup.isPickUpSupported())
                .addValue("creation_timestamp", now.toEpochMilli())
                .addValue("last_update_timestamp", now.toEpochMilli());

        namedParameterJdbcTemplate.update(sqlBuilder.toString(), parameterSource);
    }

    @Override
    public void deleteBusinessGroup(@NonNull Long businessGroupId) {
        String sql =
            "DELETE FROM business_groups " +
            "      WHERE business_group_id = :business_group_id";
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("business_group_id", businessGroupId);
        namedParameterJdbcTemplate.update(sql, parameterSource);
    }

    @Override
    public Optional<BusinessGroup> getBusinessGroupById(@NonNull Long businessGroupId) {
        String sql =
            "SELECT * " +
            "  FROM business_groups " +
            " WHERE business_group_id = :business_group_id" +
            " LIMIT 1";
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("business_group_id", businessGroupId);

        List<BusinessGroup> result =
                namedParameterJdbcTemplate.query(
                    sql,
                    parameterSource,
                    new BeanPropertyRowMapper<>(BusinessGroup.class));
        if (result.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(result.iterator().next());
    }

    @Override
    public Optional<BusinessGroup> getBusinessGroupByName(@NonNull String businessName) {
        String sql =
            "SELECT * " +
            "  FROM business_groups " +
                " WHERE business_name = :business_name" +
            " LIMIT 1";
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("business_name", businessName);

        List<BusinessGroup> result =
                namedParameterJdbcTemplate.query(
                        sql,
                        parameterSource,
                        new BeanPropertyRowMapper<>(BusinessGroup.class));
        if (result.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(result.iterator().next());
    }
}
