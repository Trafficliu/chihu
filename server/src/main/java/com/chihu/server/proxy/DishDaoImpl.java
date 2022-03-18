package com.chihu.server.proxy;

import com.chihu.server.model.Dish;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishDaoImpl implements DishDao {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public void addDish(@NonNull Dish dish) {
        String sql =
            "INSERT INTO dishes " +
            "   SET business_group_id = :business_group_id, " +
            "       dish_name = :dish_name, " +
            "       price_in_cent = :price_in_cent, " +
            "       cuisine_index = :cuisine_index, " +
            "       cuisine_name = :cuisine_name, " +
            "       external_image_id = :external_image_id, " +
            "       dish_image_path = :dish_image_path, " +
            "       special_flags = :special_flags "
//                +
//            " ON DUPLICATE KEY UPDATE " +
//            "       price_in_cent = :price_in_cent, " +
//            "       cuisine_index = :cuisine_index, " +
//            "       cuisine_name = :cuisine_name, " +
//            "       external_image_id = :external_image_id, " +
//            "       dish_image_path = :dish_image_path, " +
//            "       special_flags = :special_flags "
            ;
        SqlParameterSource parameterSource = new MapSqlParameterSource()
            .addValue("business_group_id", dish.getBusinessGroupId())
            .addValue("dish_name", dish.getDishName())
            .addValue("price_in_cent", dish.getPriceInCent())
            .addValue("cuisine_index", dish.getCuisineIndex())
            .addValue("cuisine_name", dish.getCuisineName())
            .addValue("external_image_id", dish.getExternalImageId())
            .addValue("dish_image_path", dish.getDishImagePath())
            .addValue("special_flags", dish.getSpecialFlags());

        namedParameterJdbcTemplate.update(sql, parameterSource);
    }

    @Override
    public void updateDish(@NonNull Dish dish) {
        String sql =
            "UPDATE dishes " +
            "   SET business_group_id = :business_group_id, " +
            "       dish_name = :dish_name, " +
            "       price_in_cent = :price_in_cent, " +
            "       cuisine_index = :cuisine_index, " +
            "       cuisine_name = :cuisine_name, " +
            "       external_image_id = :external_image_id, " +
            "       dish_image_path = :dish_image_path, " +
            "       special_flags = :special_flags " +
            " WHERE dish_id = :dish_id "
            ;
        SqlParameterSource parameterSource = new MapSqlParameterSource()
            .addValue("dish_id", dish.getDishId())
            .addValue("business_group_id", dish.getBusinessGroupId())
            .addValue("dish_name", dish.getDishName())
            .addValue("price_in_cent", dish.getPriceInCent())
            .addValue("cuisine_index", dish.getCuisineIndex())
            .addValue("cuisine_name", dish.getCuisineName())
            .addValue("external_image_id", dish.getExternalImageId())
            .addValue("dish_image_path", dish.getDishImagePath())
            .addValue("special_flags", dish.getSpecialFlags());

        namedParameterJdbcTemplate.update(sql, parameterSource);
    }

    @Override
    public void deleteDish(@NonNull Long dishId) {
        String sql =
            "DELETE FROM dishes " +
            "      WHERE dish_id = :dish_id ";
        SqlParameterSource parameterSource = new MapSqlParameterSource()
            .addValue("dish_id", dishId);
        namedParameterJdbcTemplate.update(sql, parameterSource);
    }

    @Override
    public Optional<Dish> getDishById(@NonNull Long dishId) {
        String sql =
            "SELECT * " +
            "  FROM dishes " +
            " WHERE dish_id = :dish_id " +
            " LIMIT 1";
        SqlParameterSource parameterSource = new MapSqlParameterSource()
            .addValue("dish_id", dishId);

        List<Dish> result =
            namedParameterJdbcTemplate.query(
                sql,
                parameterSource,
                new BeanPropertyRowMapper<>(Dish.class));
        if (result.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(result.iterator().next());
    }

    @Override
    public Optional<Dish> getDishByBusinessGroupIdAndName(
        @NonNull Long businessGroupId, @NonNull String dishName) {
        String sql =
            "SELECT * " +
            "  FROM dishes " +
            " WHERE business_group_id = :business_group_id " +
            "   AND dish_name = :dish_name " +
            " LIMIT 1";
        SqlParameterSource parameterSource = new MapSqlParameterSource()
            .addValue("business_group_id", businessGroupId)
            .addValue("dish_name", dishName);

        List<Dish> result =
            namedParameterJdbcTemplate.query(
                sql,
                parameterSource,
                new BeanPropertyRowMapper<>(Dish.class));
        if (result.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(result.iterator().next());
    }

    @Override
    public List<Dish> getDishesOfList(@NonNull List<Long> dishIds) {
        String sql =
            "SELECT * " +
            "  FROM dishes " +
            " WHERE dish_id IN (:dish_id) "
            ;
        SqlParameterSource parameterSource = new MapSqlParameterSource()
            .addValue(
                "dish_id",
                String.join(
                    ",",
                    dishIds
                        .stream()
                        .map(dishId -> (dishId.toString()))
                        .collect(Collectors.toList())));

        List<Dish> result =
            namedParameterJdbcTemplate.query(
                sql,
                parameterSource,
                new BeanPropertyRowMapper<>(Dish.class));
        return result;
    }

    @Override
    public List<Dish> getDishesOfSet(@NonNull Set<Long> dishIds) {
        String sql =
            "SELECT * " +
            "  FROM dishes " +
            " WHERE dish_id IN (:dish_id) "
            ;

        String dish_ids =
            String.join(
                ",",
                dishIds
                    .stream()
                    .map(dishId -> (dishId.toString()))
                    .collect(Collectors.toList()));
//        SqlParameterSource parameterSource = new MapSqlParameterSource()
//            .addValue(
//                "dish_id",
//                String.join(
//                    ",",
//                    dishIds
//                        .stream()
//                        .map(dishId -> (dishId.toString()))
//                        .collect(Collectors.toList())));

        SqlParameterSource parameterSource = new MapSqlParameterSource()
            .addValue(
                "dish_id",
                    dishIds);
        log.info("The dish ids: {}", dish_ids);
        log.info(parameterSource.toString());

        List<Dish> result =
            namedParameterJdbcTemplate.query(
                sql,
                parameterSource,
                new BeanPropertyRowMapper<>(Dish.class));
        log.info("result size is: {}", result.size());

        return result;
    }
}
