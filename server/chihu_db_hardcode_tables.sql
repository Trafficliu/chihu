CREATE TABLE IF NOT EXISTS `roles` (
`id` INT(11) NOT NULL AUTO_INCREMENT,
`role_name` VARCHAR(25),
PRIMARY KEY(`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4;
INSERT INTO roles
  (id, role_name)
VALUES
  (1, 'EATER'),
  (2, 'OWNER'),
  (3, 'ADMIN');

CREATE TABLE IF NOT EXISTS `cuisine_categories` (
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
INSERT INTO cuisine_categories
  (cuisine_index, continent, region, area, sub_area)
VALUES
  ('C01010101', 'Asian', 'Chinese', 'ChuanYu', 'Sichuan'),
  ('C01010102', 'Asian', 'Chinese', 'ChuanYu', 'Chongqing'),
  ('C01010201', 'Asian', 'Chinese', 'Guangdong', 'Guangdong'),
  ('C01010202', 'Asian', 'Chinese', 'Guangdong', 'Chaozhou'),
  ('C01010301', 'Asian', 'Chinese', 'Northwestern', 'Shanxi'),
  ('C01010302', 'Asian', 'Chinese', 'Northwestern', 'Xinjiang'),
  ('C01010401', 'Asian', 'Chinese', 'Northern', 'Beijing'),
  ('C01010402', 'Asian', 'Chinese', 'Northern', 'Shandong'),
  ('C01010403', 'Asian', 'Chinese', 'Northern', 'Dongbei'),
  ('C01010404', 'Asian', 'Chinese', 'Northern', 'Shanxi'),
  ('C01010405', 'Asian', 'Chinese', 'Northern', 'Henan'),
  ('C01010406', 'Asian', 'Chinese', 'Northern', 'Tianjin'),
  ('C01010501', 'Asian', 'Chinese', 'JiangZheHu', 'Zhejiang'),
  ('C01010502', 'Asian', 'Chinese', 'JiangZheHu', 'Shanghai'),
  ('C01010503', 'Asian', 'Chinese', 'JiangZheHu', 'Suzhou'),
  ('C01010504', 'Asian', 'Chinese', 'JiangZheHu', 'Anhui'),
  ('C01010601', 'Asian', 'Chinese', 'XiangEGan', 'Hunan'),
  ('C01010602', 'Asian', 'Chinese', 'XiangEGan', 'Hubei'),
  ('C01010603', 'Asian', 'Chinese', 'XiangEGan', 'Jiangxi'),
  ('C01010701', 'Asian', 'Chinese', 'Southwestern', 'Yunnan'),
  ('C01010702', 'Asian', 'Chinese', 'Southwestern', 'Guangxi'),
  ('C01010703', 'Asian', 'Chinese', 'Southwestern', 'Guizhou'),
  ('C01010800', 'Asian', 'Chinese', 'Hainan', NULL),
  ('C01010900', 'Asian', 'Chinese', 'Fujian', NULL),
  ('C01011000', 'Asian', 'Chinese', 'Kejia', NULL),
  ('C01020000', 'Asian', 'Japanese', NULL, NULL),
  ('C01030000', 'Asian', 'Korean', NULL, NULL)
;
