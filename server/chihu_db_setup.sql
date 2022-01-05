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
`working_date` DATE DEFAULT NULL,
`working_days` INT(10) NOT NULL DEFAULT 0,
`working_hours` BIGINT(64) NOT NULL,
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
INDEX(`geo_code`),
INDEX(`zip_code`),
INDEX(`city`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
