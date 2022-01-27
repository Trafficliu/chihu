package com.chihu.server.proxy;

import com.chihu.server.model.Dish;
import lombok.NonNull;

import java.util.Optional;

public interface DishDao {
    void addDish(@NonNull Dish dish);
    void updateDish(@NonNull Dish dish);
    void deleteDish(@NonNull Long dishId);

    Optional<Dish> getDishById(@NonNull Long dishId);
    Optional<Dish> getDishByBusinessGroupIdAndName(
        @NonNull Long businessGroupId, @NonNull String dishName);
}
