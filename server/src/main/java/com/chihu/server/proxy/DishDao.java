package com.chihu.server.proxy;

import com.chihu.server.model.Dish;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DishDao {
    void addDish(@NonNull Dish dish);
    void updateDish(@NonNull Dish dish);
    void deleteDish(@NonNull Long dishId);

    Optional<Dish> getDishById(@NonNull Long dishId);
    Optional<Dish> getDishByBusinessGroupIdAndName(
        @NonNull Long businessGroupId, @NonNull String dishName);
    List<Dish> getDishesOfList(@NonNull List<Long> dishIds);
    List<Dish> getDishesOfSet(@NonNull Set<Long> dishIds);
}
