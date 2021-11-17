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
`role_id` INT(11) NOT NULL
--,
--KEY `user_fk_idx` (`user_id`),
--KEY `role_fk_idx` (`role_id`),
--CONSTRAINT `role_fk` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`),
--CONSTRAINT `user_fk` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

