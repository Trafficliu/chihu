CREATE DATABASE IF NOT EXISTS `chihu_db`;
USE `chihu_db`;
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
`id` BIGINT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
`username` VARCHAR(255) NOT NULL,
`password` VARCHAR(255) NOT NULL,
`email` VARCHAR(255) NOT NULL,
`phone` VARCHAR(255) NOT NULL,
`activated` BOOLEAN DEFAULT FALSE,
`status` TINYINT DEFAULT 1,
`create_timestamp` BIGINT UNSIGNED NOT NULL,
`last_update_timestamp` BIGINT UNSIGNED NOT NULL,
`last_updated_by` VARCHAR(255) DEFAULT 'Admin',
PRIMARY KEY (`id`),
UNIQUE(`username`),
UNIQUE(`phone`),
UNIQUE(`email`),
INDEX(`username`),
INDEX(`phone`),
INDEX(`email`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles` (
`id` INT(11) NOT NULL AUTO_INCREMENT,
`role_name` VARCHAR(25),
PRIMARY KEY(`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `user_roles`;
CREATE TABLE `user_roles` (
`user_id` BIGINT(11) UNSIGNED NOT NULL,
`role_id` INT(11) NOT NULL,
PRIMARY KEY (`user_id`),
UNIQUE(`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `business_groups`;
CREATE TABLE `business_groups` (
`business_group_id` BIGINT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
`owner_id` BIGINT(11) UNSIGNED NOT NULL,
`business_name` VARCHAR(255) NOT NULL,
-- bakery, home cook, restaurant, food truck, drink, pop-up
`business_type` VARCHAR(255) NOT NULL,
`phone_number` VARCHAR(255) NOT NULL,
`store_image_path` BLOB DEFAULT NULL,
`external_image_id` VARCHAR(255) DEFAULT NULL,
-- `working_hours` BIGINT(64) NOT NULL,
`primary_food_type` VARCHAR(100) NOT NULL,
`secondary_food_type` VARCHAR(100) NOT NULL,
`rating` FLOAT(2,1) DEFAULT 0.0,
`num_of_reviews` INT(10) DEFAULT 0,
`support_deliver` Boolean DEFAULT false,
`support_pickup` Boolean DEFAULT false,
`creation_timestamp` BIGINT UNSIGNED NOT NULL,
`last_update_timestamp` BIGINT UNSIGNED NOT NULL,
PRIMARY KEY(`business_group_id`),
UNIQUE(`business_group_id`),
UNIQUE(`owner_id`, `business_name`),
INDEX(`business_group_id`),
INDEX(`owner_id`),
INDEX(`business_name`),
INDEX(`business_type`),
INDEX(`primary_food_type`),
INDEX(`secondary_food_type`),
INDEX(`rating`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `business_entities`;
CREATE TABLE `business_entities` (
`business_entity_id` BIGINT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
`owner_id` BIGINT(11) UNSIGNED NOT NULL,
`business_group_id` BIGINT(11) UNSIGNED NOT NULL,
`business_name` VARCHAR(255) NOT NULL,
`business_entity_name` VARCHAR(255) NOT NULL,
# -- is this permanent address, fixed schedule mobile, or one time pop-ups
`operation_type` VARCHAR(20) NOT NULL,
`phone_number` VARCHAR(255) NOT NULL,
`repeated` BOOLEAN DEFAULT FALSE,
`working_date` DATE DEFAULT NULL,
`working_days` INT(10) DEFAULT 0,
`working_hours` BIGINT(20) NOT NULL,
`address_line1` VARCHAR(255) NOT NULL,
`address_line2` VARCHAR(255) DEFAULT NULL,
`city` VARCHAR(255) NOT NULL,
`state` VARCHAR(255) NOT NULL,
`country` VARCHAR(10) NOT NULL DEFAULT 'US',
`zip_code` VARCHAR(20),
`geo_code` BIGINT(20) UNSIGNED,
PRIMARY KEY(`business_entity_id`),
UNIQUE(`business_entity_id`),
UNIQUE(`owner_id`, `business_entity_name`),
INDEX(`business_entity_id`),
INDEX(`business_group_id`),
INDEX(`business_entity_name`),
INDEX(`business_name`),
INDEX(`working_date`),
INDEX(`working_days`),
INDEX(`geo_code`),
INDEX(`zip_code`),
INDEX(`city`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `cuisine_categories`;
CREATE TABLE `cuisine_categories` (
`cuisine_index` VARCHAR(10) NOT NULL,
`continent` VARCHAR(10) NOT NULL,
`region` VARCHAR(20) NOT NULL,
`area` VARCHAR(20) NULL,
`sub_area` VARCHAR(20),
PRIMARY KEY(`cuisine_index`),
UNIQUE(`cuisine_index`),
UNIQUE(`continent`, `region`, `area`, `sub_area`),
INDEX(`cuisine_index`)
);

DROP TABLE IF EXISTS `dishes`;
CREATE TABLE `dishes` (
`dish_id` BIGINT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
`business_group_id` BIGINT(11) UNSIGNED NOT NULL,
`dish_name` VARCHAR(50) NOT NULL,
`price_in_cent` INT(10) NOT NULL,
`cuisine_index` VARCHAR(10) NOT NULL,
`cuisine_name` VARCHAR(50) NOT NULL,
`external_image_id` VARCHAR(255) DEFAULT NULL,
`dish_image_path` BLOB DEFAULT NULL,
`special_flags` INT(10) DEFAULT 0,
`overall_rating` FLOAT(2, 1) DEFAULT -1.0,
`overall_rating_votes` INT(10) UNSIGNED DEFAULT 0,
`spicy_level` FLOAT(2, 1) DEFAULT -1.0,
`spicy_level_votes` INT(10) UNSIGNED DEFAULT 0,
`salt_level` FLOAT(2, 1) DEFAULT -1.0,
`salt_level_votes` INT(10) UNSIGNED DEFAULT 0,
`sweet_level` FLOAT(2, 1) DEFAULT -1.0,
`sweet_level_votes` INT(10) UNSIGNED DEFAULT 0,
`oil_level` FLOAT(2, 1) DEFAULT -1.0,
`oil_level_votes` INT(10) UNSIGNED DEFAULT 0,
`sour_level` FLOAT(2, 1) DEFAULT -1.0,
`sour_level_votes` INT(10) UNSIGNED DEFAULT 0,
`serving_size` FLOAT(2, 1) DEFAULT -1.0,
`serving_size_votes` INT(10) UNSIGNED DEFAULT 0,
`healthy` FLOAT(2, 1) DEFAULT -1.0,
`healthy_votes` INT(10) UNSIGNED DEFAULT 0,
`portable` FLOAT(2, 1) DEFAULT -1.0,
`portable_votes` INT(10) UNSIGNED DEFAULT 0,
`odorous` FLOAT(2, 1) DEFAULT -1.0,
`odorous_votes` INT(10) UNSIGNED DEFAULT 0,
`same_as_picture` FLOAT(2, 1) DEFAULT -1.0,
`same_as_picture_votes` INT(10) UNSIGNED DEFAULT 0,
PRIMARY KEY(`dish_id`),
UNIQUE(`dish_id`),
UNIQUE(`business_group_id`, `dish_name`),
INDEX(`dish_name`)
);
