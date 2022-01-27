package com.chihu.server.controller;


import com.chihu.server.model.ChihuUserDetails;
import com.chihu.server.model.Dish;
import com.chihu.server.proxy.DishDao;
import com.chihu.server.serializer.ApiServerSerializer;
import com.chihu.server.utils.OwnershipUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Slf4j
@RestController
public class DishController {

    @Autowired
    private DishDao dishDao;

    @Autowired
    private OwnershipUtil ownershipUtil;

    @PostMapping("/dish/create_dish")
    public String createDish(
        Authentication authentication,
        @RequestParam(value = "dish_str", required = true) String dishStr
    ) throws IllegalArgumentException, AccessDeniedException {

        Dish dish = ApiServerSerializer.toDish(dishStr);
        if (dish.getBusinessGroupId() == null) {
            throw new IllegalArgumentException("Business Group Id cannot be null");
        }
        ChihuUserDetails userDetails =
            (ChihuUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        if (!ownershipUtil.userIsBusinessGroupOwner(
            userId, dish.getBusinessGroupId())) {
            throw new AccessDeniedException(
                "The user is not the owner of the business.");
        }

        dishDao.addDish(dish);
        return "";
    }

    @PostMapping("/dish/update_dish")
    public String updateDish(
        Authentication authentication,
        @RequestParam(value = "dish_str", required = true) String dishStr) {

        log.info("Input dish str is: {}", dishStr);
        Dish dish = ApiServerSerializer.toDish(dishStr);
        if (dish.getBusinessGroupId() == null) {
            throw new IllegalArgumentException("Business Group Id cannot be null");
        }
        ChihuUserDetails userDetails =
            (ChihuUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        if (!ownershipUtil.userIsBusinessGroupOwner(
            userId, dish.getBusinessGroupId())) {
            throw new AccessDeniedException(
                "The user is not the owner of the business.");
        }

        dishDao.updateDish(dish);
        return "";
    }

    @PostMapping("/dish/delete_dish")
    public String deleteDish(
        Authentication authentication,
        @RequestParam(value = "dish_id", required = true) Long dishId
    ) throws IllegalArgumentException, AccessDeniedException {

        Optional<Dish> dish = dishDao.getDishById(dishId);
        if (dish.isEmpty()) {
            throw new IllegalArgumentException("Invalid dish id.");
        }
        if (dish.get().getBusinessGroupId() == null) {
            throw new IllegalArgumentException("Dish Business Group Id is null!");
        }
        ChihuUserDetails userDetails =
            (ChihuUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        if (!ownershipUtil.userIsBusinessGroupOwner(
            userId, dish.get().getBusinessGroupId())) {
            throw new AccessDeniedException(
                "The user is not the owner of the business.");
        }

        dishDao.deleteDish(dishId);
        return "";
    }

    @GetMapping("/dish/get_dish_by_id")
    public Dish getDishById(
        @RequestParam(value = "dish_id", required = true) Long dishId
    ) throws IllegalArgumentException, AccessDeniedException {

        Optional<Dish> dish = dishDao.getDishById(dishId);
        if (dish.isEmpty()) {
            throw new IllegalArgumentException("Invalid dish id");
        }
        return dish.get();
    }

    @GetMapping("/dish/get_dish_by_name")
    public Dish getDishByName(
        @RequestParam(value = "business_group_id", required = true) Long businessGroupId,
        @RequestParam(value = "dish_name", required = true) String dishName
    ) throws IllegalArgumentException, AccessDeniedException {

        Optional<Dish> dish =
            dishDao.getDishByBusinessGroupIdAndName(businessGroupId, dishName);
        if (dish.isEmpty()) {
            throw new IllegalArgumentException(
                "Invalid business group id or dish name");
        }
        return dish.get();
    }
}
