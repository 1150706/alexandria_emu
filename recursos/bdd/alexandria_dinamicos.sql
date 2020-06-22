/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 100411
 Source Host           : localhost:3306
 Source Schema         : alexandria_dinamicos

 Target Server Type    : MySQL
 Target Server Version : 100411
 File Encoding         : 65001

 Date: 22/06/2020 01:10:30
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for datos_acciones_tiempo_real
-- ----------------------------
DROP TABLE IF EXISTS `datos_acciones_tiempo_real`;
CREATE TABLE `datos_acciones_tiempo_real`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `personaje` int(11) NOT NULL,
  `accion` int(11) NOT NULL,
  `nombre` int(11) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 66 CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = Fixed;

-- ----------------------------
-- Table structure for datos_area
-- ----------------------------
DROP TABLE IF EXISTS `datos_area`;
CREATE TABLE `datos_area`  (
  `id` int(11) NOT NULL,
  `nombre` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL,
  `superarea` int(11) NOT NULL,
  INDEX `id`(`id`) USING BTREE
) ENGINE = MyISAM CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of datos_area
-- ----------------------------
INSERT INTO `datos_area` VALUES (0, 'Amakna', 0);
INSERT INTO `datos_area` VALUES (1, 'L\'île des Wabbits', 0);
INSERT INTO `datos_area` VALUES (2, 'L\'île de Moon', 0);
INSERT INTO `datos_area` VALUES (3, 'Prison', 0);
INSERT INTO `datos_area` VALUES (4, 'Tainéla', 0);
INSERT INTO `datos_area` VALUES (5, 'Sufokia', 0);
INSERT INTO `datos_area` VALUES (6, 'Forêt des Abraknydes', 0);
INSERT INTO `datos_area` VALUES (7, 'Bonta', 0);
INSERT INTO `datos_area` VALUES (8, 'Plaine de Cania', 0);
INSERT INTO `datos_area` VALUES (11, 'Brâkmar', 0);
INSERT INTO `datos_area` VALUES (12, 'Lande de Sidimote', 0);
INSERT INTO `datos_area` VALUES (13, 'Territoire des Dopeuls', 0);
INSERT INTO `datos_area` VALUES (14, 'Village des Brigandins', 0);
INSERT INTO `datos_area` VALUES (15, 'Foire du Trool', 0);
INSERT INTO `datos_area` VALUES (16, 'Foire du Trool', 0);
INSERT INTO `datos_area` VALUES (17, 'Tainéla', 0);
INSERT INTO `datos_area` VALUES (18, 'Astrub', 0);
INSERT INTO `datos_area` VALUES (19, 'Pandala Neutre', 0);
INSERT INTO `datos_area` VALUES (20, 'Pandala Eau', 0);
INSERT INTO `datos_area` VALUES (21, 'Pandala Terre', 0);
INSERT INTO `datos_area` VALUES (22, 'Pandala Feu', 0);
INSERT INTO `datos_area` VALUES (23, 'Pandala Air', 0);
INSERT INTO `datos_area` VALUES (24, 'Pandala Donjon', 0);
INSERT INTO `datos_area` VALUES (25, 'Le Champ du Repos', 0);
INSERT INTO `datos_area` VALUES (26, 'Le labyrinthe du Dragon Cochon', 0);
INSERT INTO `datos_area` VALUES (27, 'Donjon Abraknyde', 0);
INSERT INTO `datos_area` VALUES (28, 'Montagne des Koalaks', 0);
INSERT INTO `datos_area` VALUES (29, 'Donjon des Tofus', 0);
INSERT INTO `datos_area` VALUES (30, 'L\'île du Minotoror', 0);
INSERT INTO `datos_area` VALUES (31, 'Le labyrinthe du Minotoror', 0);
INSERT INTO `datos_area` VALUES (32, 'La bibliothèque du Maître Corbac', 0);
INSERT INTO `datos_area` VALUES (33, 'Donjon des Canidés', 0);
INSERT INTO `datos_area` VALUES (34, 'Caverne du Koulosse', 0);
INSERT INTO `datos_area` VALUES (35, 'Repaire de Skeunk', 0);
INSERT INTO `datos_area` VALUES (36, 'Sanctuaire des Familiers', 0);
INSERT INTO `datos_area` VALUES (37, 'Donjon des Craqueleurs', 0);
INSERT INTO `datos_area` VALUES (39, 'Donjon des Bworks', 0);
INSERT INTO `datos_area` VALUES (40, 'Donjon des Scarafeuilles', 0);
INSERT INTO `datos_area` VALUES (41, 'Donjon des Champs', 0);
INSERT INTO `datos_area` VALUES (42, 'Zone arctique', 0);
INSERT INTO `datos_area` VALUES (43, 'Donjon du Dragon Cochon', 0);
INSERT INTO `datos_area` VALUES (44, 'Donjon des Dragoeufs', 0);
INSERT INTO `datos_area` VALUES (45, 'Incarnam', 3);
INSERT INTO `datos_area` VALUES (46, 'Ile d\'Otomaï ', 0);
INSERT INTO `datos_area` VALUES (47, 'Village des Zoths', 0);

-- ----------------------------
-- Table structure for datos_casas
-- ----------------------------
DROP TABLE IF EXISTS `datos_casas`;
CREATE TABLE `datos_casas`  (
  `id` int(10) UNSIGNED NOT NULL,
  `mapa` int(10) UNSIGNED NOT NULL DEFAULT 0,
  `celda` int(10) UNSIGNED NOT NULL DEFAULT 0,
  `dueño` int(10) NOT NULL DEFAULT 0,
  `venta` int(10) NOT NULL DEFAULT -1,
  `gremio` int(10) NOT NULL DEFAULT -1,
  `acceso` int(10) UNSIGNED NOT NULL DEFAULT 0,
  `llave` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '00000000',
  `derechosgremio` int(8) UNSIGNED NOT NULL DEFAULT 0,
  `mapa_interior` int(11) NOT NULL DEFAULT 0,
  `celda_interior` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of datos_casas
-- ----------------------------
INSERT INTO `datos_casas` VALUES (66, 675, 194, 0, 1000000, 0, 0, '-', 0, 1649, 194);
INSERT INTO `datos_casas` VALUES (67, 675, 278, 0, 1000000, 0, 0, '-', 0, 1647, 278);
INSERT INTO `datos_casas` VALUES (68, 30, 309, 0, 1000000, 0, 0, '-', 0, 1524, 309);
INSERT INTO `datos_casas` VALUES (69, 40, 330, 0, 1000000, 0, 0, '-', 0, 505, 330);
INSERT INTO `datos_casas` VALUES (70, 40, 351, 0, 1000000, 0, 0, '-', 0, 501, 351);
INSERT INTO `datos_casas` VALUES (74, 36, 331, 0, 1000000, 0, 0, '-', 0, 101, 331);
INSERT INTO `datos_casas` VALUES (75, 37, 213, 0, 1000000, 0, 0, '-', 0, 499, 213);
INSERT INTO `datos_casas` VALUES (76, 37, 305, 0, 1000000, 0, 0, '-', 0, 104, 305);
INSERT INTO `datos_casas` VALUES (77, 42, 248, 0, 1000000, 0, 0, '-', 0, 507, 248);
INSERT INTO `datos_casas` VALUES (78, 42, 293, 0, 1000000, 0, 0, '-', 0, 510, 293);
INSERT INTO `datos_casas` VALUES (79, 49, 52, 0, 1000000, 0, 0, '-', 0, 514, 52);
INSERT INTO `datos_casas` VALUES (80, 49, 407, 0, 1000000, 0, 0, '-', 0, 519, 407);
INSERT INTO `datos_casas` VALUES (81, 50, 161, 0, 1000000, 0, 0, '-', 0, 516, 161);
INSERT INTO `datos_casas` VALUES (82, 50, 122, 0, 1000000, 0, 0, '-', 0, 1535, 122);
INSERT INTO `datos_casas` VALUES (83, 44, 105, 0, 1000000, 0, 0, '-', 0, 1538, 105);
INSERT INTO `datos_casas` VALUES (84, 48, 379, 0, 1000000, 0, 0, '-', 0, 512, 379);
INSERT INTO `datos_casas` VALUES (85, 482, 195, 0, 1000000, 0, 0, '-', 0, 1542, 195);
INSERT INTO `datos_casas` VALUES (87, 540, 323, 0, 1000000, 0, 0, '-', 0, 1587, 323);
INSERT INTO `datos_casas` VALUES (88, 540, 181, 0, 1000000, 0, 0, '-', 0, 1582, 181);
INSERT INTO `datos_casas` VALUES (89, 542, 148, 0, 1000000, 0, 0, '-', 0, 1593, 148);
INSERT INTO `datos_casas` VALUES (90, 761, 285, 0, 1000000, 0, 0, '-', 0, 1583, 285);
INSERT INTO `datos_casas` VALUES (91, 761, 119, 0, 1000000, 0, 0, '-', 0, 1584, 119);
INSERT INTO `datos_casas` VALUES (92, 763, 99, 0, 1000000, 0, 0, '-', 0, 1586, 99);
INSERT INTO `datos_casas` VALUES (93, 763, 53, 0, 1000000, 0, 0, '-', 0, 1588, 53);
INSERT INTO `datos_casas` VALUES (94, 763, 162, 0, 1000000, 0, 0, '-', 0, 1590, 162);
INSERT INTO `datos_casas` VALUES (95, 763, 189, 0, 1000000, 0, 0, '-', 0, 1594, 189);
INSERT INTO `datos_casas` VALUES (96, 166, 359, 0, 1000000, 0, 0, '-', 0, 1595, 359);
INSERT INTO `datos_casas` VALUES (97, 166, 234, 0, 1000000, 0, 0, '-', 0, 1600, 234);
INSERT INTO `datos_casas` VALUES (109, 551, 243, 0, 1000000, 0, 0, '-', 0, 1609, 243);
INSERT INTO `datos_casas` VALUES (110, 548, 409, 0, 1000000, 0, 0, '-', 0, 1605, 409);
INSERT INTO `datos_casas` VALUES (111, 537, 240, 0, 1000000, 0, 0, '-', 0, 1598, 240);
INSERT INTO `datos_casas` VALUES (112, 537, 164, 0, 1000000, 0, 0, '-', 0, 1601, 164);
INSERT INTO `datos_casas` VALUES (113, 485, 192, 0, 1000000, 0, 0, '-', 0, 1622, 192);
INSERT INTO `datos_casas` VALUES (114, 165, 173, 0, 1000000, 0, 0, '-', 0, 1602, 173);
INSERT INTO `datos_casas` VALUES (115, 165, 64, 0, 1000000, 0, 0, '-', 0, 1606, 64);
INSERT INTO `datos_casas` VALUES (116, 165, 190, 0, 1000000, 0, 0, '-', 0, 1603, 190);
INSERT INTO `datos_casas` VALUES (117, 165, 194, 0, 1000000, 0, 0, '-', 0, 1607, 194);
INSERT INTO `datos_casas` VALUES (118, 165, 236, 0, 1000000, 0, 0, '-', 0, 1610, 236);
INSERT INTO `datos_casas` VALUES (119, 167, 191, 0, 1000000, 0, 0, '-', 0, 1613, 191);
INSERT INTO `datos_casas` VALUES (120, 167, 150, 0, 1000000, 0, 0, '-', 0, 1614, 150);
INSERT INTO `datos_casas` VALUES (122, 2023, 206, 0, 1000000, 0, 0, '-', 0, 2022, 206);
INSERT INTO `datos_casas` VALUES (123, 1440, 137, 0, 1000000, 0, 0, '-', 0, 1635, 137);
INSERT INTO `datos_casas` VALUES (124, 1391, 139, 0, 1000000, 0, 0, '-', 0, 1630, 139);
INSERT INTO `datos_casas` VALUES (125, 1144, 229, 0, 1000000, 0, 0, '-', 0, 1626, 229);
INSERT INTO `datos_casas` VALUES (126, 1144, 286, 0, 1000000, 0, 0, '-', 0, 1628, 286);
INSERT INTO `datos_casas` VALUES (127, 568, 198, 0, 1000000, 0, 0, '-', 0, 1621, 198);
INSERT INTO `datos_casas` VALUES (128, 569, 236, 0, 1000000, 0, 0, '-', 0, 1617, 236);
INSERT INTO `datos_casas` VALUES (129, 185, 241, 0, 1000000, 0, 0, '-', 0, 1620, 241);
INSERT INTO `datos_casas` VALUES (130, 908, 308, 0, 1000000, 0, 0, '-', 0, 1627, 308);
INSERT INTO `datos_casas` VALUES (131, 908, 171, 0, 1000000, 0, 0, '-', 0, 1632, 171);
INSERT INTO `datos_casas` VALUES (132, 912, 301, 0, 1000000, 0, 0, '-', 0, 1633, 301);
INSERT INTO `datos_casas` VALUES (133, 918, 239, 0, 1000000, 0, 0, '-', 0, 1636, 239);
INSERT INTO `datos_casas` VALUES (134, 918, 281, 0, 1000000, 0, 0, '-', 0, 1640, 281);
INSERT INTO `datos_casas` VALUES (135, 184, 137, 0, 1000000, 0, 0, '-', 0, 0, 0);
INSERT INTO `datos_casas` VALUES (136, 496, 234, 18, 999999999, 0, 0, '-', 0, 1638, 234);
INSERT INTO `datos_casas` VALUES (137, 486, 273, 0, 1000000, 0, 0, '-', 0, 1642, 273);
INSERT INTO `datos_casas` VALUES (138, 486, 226, 0, 1000000, 0, 0, '-', 0, 1639, 226);
INSERT INTO `datos_casas` VALUES (139, 531, 109, 0, 1000000, 0, 0, '-', 0, 1644, 109);
INSERT INTO `datos_casas` VALUES (140, 531, 154, 0, 1000000, 0, 0, '-', 0, 1648, 154);
INSERT INTO `datos_casas` VALUES (142, 749, 297, 0, 1000000, 0, 0, '-', 0, 1520, 297);
INSERT INTO `datos_casas` VALUES (143, 763, 419, 0, 1000000, 0, 0, '-', 0, 1799, 419);
INSERT INTO `datos_casas` VALUES (144, 1166, 169, 0, 1000000, 0, 0, '-', 0, 1872, 169);
INSERT INTO `datos_casas` VALUES (145, 1166, 177, 0, 1000000, 0, 0, '-', 0, 1917, 177);
INSERT INTO `datos_casas` VALUES (146, 1909, 77, 0, 1000000, 0, 0, '-', 0, 1918, 77);
INSERT INTO `datos_casas` VALUES (147, 1906, 219, 0, 1000000, 0, 0, '-', 0, 1922, 219);
INSERT INTO `datos_casas` VALUES (148, 1906, 163, 0, 1000000, 0, 0, '-', 0, 1927, 163);
INSERT INTO `datos_casas` VALUES (149, 1903, 229, 0, 1000000, 0, 0, '-', 0, 1938, 229);
INSERT INTO `datos_casas` VALUES (150, 1903, 154, 0, 1000000, 0, 0, '-', 0, 1933, 154);
INSERT INTO `datos_casas` VALUES (151, 1219, 266, 0, 1000000, 0, 0, '-', 0, 2005, 307);
INSERT INTO `datos_casas` VALUES (152, 1219, 168, 0, 1000000, 0, 0, '-', 0, 1953, 168);
INSERT INTO `datos_casas` VALUES (153, 1879, 183, 0, 1000000, 0, 0, '-', 0, 2007, 183);
INSERT INTO `datos_casas` VALUES (154, 1879, 171, 0, 1000000, 0, 0, '-', 0, 2009, 228);
INSERT INTO `datos_casas` VALUES (155, 1884, 124, 0, 1000000, 0, 0, '-', 0, 2010, 124);
INSERT INTO `datos_casas` VALUES (156, 1884, 274, 0, 1000000, 0, 0, '-', 0, 2011, 274);
INSERT INTO `datos_casas` VALUES (157, 1884, 199, 0, 1000000, 0, 0, '-', 0, 2012, 199);
INSERT INTO `datos_casas` VALUES (158, 1877, 193, 0, 1000000, 0, 0, '-', 0, 2013, 193);
INSERT INTO `datos_casas` VALUES (159, 1877, 213, 0, 1000000, 0, 0, '-', 0, 2014, 213);
INSERT INTO `datos_casas` VALUES (160, 2015, 223, 0, 1000000, 0, 0, '-', 0, 2016, 223);
INSERT INTO `datos_casas` VALUES (161, 1894, 151, 0, 1000000, 0, 0, '-', 0, 1871, 151);
INSERT INTO `datos_casas` VALUES (162, 1896, 250, 0, 1000000, 0, 0, '-', 0, 1920, 250);
INSERT INTO `datos_casas` VALUES (163, 1896, 280, 0, 1000000, 0, 0, '-', 0, 1921, 280);
INSERT INTO `datos_casas` VALUES (164, 1198, 184, 0, 1000000, 0, 0, '-', 0, 1923, 184);
INSERT INTO `datos_casas` VALUES (165, 1198, 206, 0, 1000000, 0, 0, '-', 0, 1959, 206);
INSERT INTO `datos_casas` VALUES (166, 1891, 138, 0, 1000000, 0, 0, '-', 0, 1925, 138);
INSERT INTO `datos_casas` VALUES (167, 1891, 194, 0, 1000000, 0, 0, '-', 0, 1926, 194);
INSERT INTO `datos_casas` VALUES (168, 1891, 222, 0, 1000000, 0, 0, '-', 0, 1961, 222);
INSERT INTO `datos_casas` VALUES (169, 1893, 368, 0, 1000000, 0, 0, '-', 0, 1929, 368);
INSERT INTO `datos_casas` VALUES (170, 1893, 398, 0, 1000000, 0, 0, '-', 0, 1930, 398);
INSERT INTO `datos_casas` VALUES (171, 1893, 170, 0, 1000000, 0, 0, '-', 0, 1931, 170);
INSERT INTO `datos_casas` VALUES (172, 1902, 204, 0, 1000000, 0, 0, '-', 0, 1932, 204);
INSERT INTO `datos_casas` VALUES (173, 1889, 151, 0, 1000000, 0, 0, '-', 0, 1935, 151);
INSERT INTO `datos_casas` VALUES (174, 1889, 193, 0, 1000000, 0, 0, '-', 0, 1939, 193);
INSERT INTO `datos_casas` VALUES (175, 1889, 140, 0, 1000000, 0, 0, '-', 0, 1969, 140);
INSERT INTO `datos_casas` VALUES (176, 1883, 208, 0, 1000000, 0, 0, '-', 0, 1980, 208);
INSERT INTO `datos_casas` VALUES (177, 1883, 225, 0, 1000000, 0, 0, '-', 0, 1941, 225);
INSERT INTO `datos_casas` VALUES (178, 1899, 163, 0, 1000000, 0, 0, '-', 0, 1943, 163);
INSERT INTO `datos_casas` VALUES (179, 1899, 137, 0, 1000000, 0, 0, '-', 0, 1944, 137);
INSERT INTO `datos_casas` VALUES (180, 1899, 191, 0, 1000000, 0, 0, '-', 0, 1945, 191);
INSERT INTO `datos_casas` VALUES (181, 1878, 162, 0, 1000000, 0, 0, '-', 0, 1946, 162);
INSERT INTO `datos_casas` VALUES (182, 1878, 134, 0, 1000000, 0, 0, '-', 0, 1947, 134);
INSERT INTO `datos_casas` VALUES (183, 1881, 239, 0, 1000000, 0, 0, '-', 0, 1949, 239);
INSERT INTO `datos_casas` VALUES (184, 1881, 155, 0, 1000000, 0, 0, '-', 0, 1951, 155);
INSERT INTO `datos_casas` VALUES (185, 1881, 110, 0, 1000000, 0, 0, '-', 0, 1955, 110);
INSERT INTO `datos_casas` VALUES (186, 1881, 285, 0, 1000000, 0, 0, '-', 0, 1956, 285);
INSERT INTO `datos_casas` VALUES (187, 1898, 345, 0, 1000000, 0, 0, '-', 0, 1985, 345);
INSERT INTO `datos_casas` VALUES (188, 1889, 249, 0, 1000000, 0, 0, '-', 0, 1934, 249);
INSERT INTO `datos_casas` VALUES (190, 1890, 365, 0, 1000000, 0, 0, '-', 0, 1981, 365);
INSERT INTO `datos_casas` VALUES (191, 1890, 107, 0, 1000000, 0, 0, '-', 0, 1984, 107);
INSERT INTO `datos_casas` VALUES (192, 1890, 257, 0, 1000000, 0, 0, '-', 0, 1982, 257);
INSERT INTO `datos_casas` VALUES (193, 1908, 237, 0, 1000000, 0, 0, '-', 0, 1972, 237);
INSERT INTO `datos_casas` VALUES (194, 1895, 125, 0, 1000000, 0, 0, '-', 0, 1999, 125);
INSERT INTO `datos_casas` VALUES (195, 1895, 78, 0, 1000000, 0, 0, '-', 0, 2000, 78);
INSERT INTO `datos_casas` VALUES (196, 1895, 219, 0, 1000000, 0, 0, '-', 0, 2001, 219);
INSERT INTO `datos_casas` VALUES (197, 1895, 216, 0, 1000000, 0, 0, '-', 0, 2002, 216);
INSERT INTO `datos_casas` VALUES (198, 1895, 185, 0, 1000000, 0, 0, '-', 0, 2004, 185);
INSERT INTO `datos_casas` VALUES (199, 1914, 136, 0, 1000000, 0, 0, '-', 0, 1973, 136);
INSERT INTO `datos_casas` VALUES (200, 1914, 191, 0, 1000000, 0, 0, '-', 0, 1974, 191);
INSERT INTO `datos_casas` VALUES (201, 1914, 156, 0, 1000000, 0, 0, '-', 0, 1975, 156);
INSERT INTO `datos_casas` VALUES (202, 1914, 298, 0, 1000000, 0, 0, '-', 0, 1976, 298);
INSERT INTO `datos_casas` VALUES (203, 1914, 152, 0, 1000000, 0, 0, '-', 0, 1977, 152);
INSERT INTO `datos_casas` VALUES (204, 1914, 339, 0, 1000000, 0, 0, '-', 0, 1978, 339);
INSERT INTO `datos_casas` VALUES (205, 1892, 105, 0, 1000000, 0, 0, '-', 0, 1986, 105);
INSERT INTO `datos_casas` VALUES (206, 1892, 280, 0, 1000000, 0, 0, '-', 0, 1995, 280);
INSERT INTO `datos_casas` VALUES (207, 1892, 224, 0, 1000000, 0, 0, '-', 0, 1989, 224);
INSERT INTO `datos_casas` VALUES (208, 1892, 322, 0, 1000000, 0, 0, '-', 0, 1990, 322);
INSERT INTO `datos_casas` VALUES (209, 1892, 107, 0, 1000000, 0, 0, '-', 0, 1991, 107);
INSERT INTO `datos_casas` VALUES (210, 1892, 146, 0, 1000000, 0, 0, '-', 0, 1992, 146);
INSERT INTO `datos_casas` VALUES (211, 1897, 265, 0, 1000000, 0, 0, '-', 0, 1993, 265);
INSERT INTO `datos_casas` VALUES (212, 1897, 283, 0, 1000000, 0, 0, '-', 0, 1994, 283);
INSERT INTO `datos_casas` VALUES (214, 1897, 144, 0, 1000000, 0, 0, '-', 0, 1996, 144);
INSERT INTO `datos_casas` VALUES (215, 1897, 82, 0, 1000000, 0, 0, '-', 0, 1997, 82);
INSERT INTO `datos_casas` VALUES (216, 1897, 84, 0, 1000000, 0, 0, '-', 0, 1998, 84);
INSERT INTO `datos_casas` VALUES (217, 1915, 123, 0, 1000000, 0, 0, '-', 0, 1958, 123);
INSERT INTO `datos_casas` VALUES (218, 1915, 254, 0, 1000000, 0, 0, '-', 0, 1960, 254);
INSERT INTO `datos_casas` VALUES (219, 1915, 147, 0, 1000000, 0, 0, '-', 0, 1962, 147);
INSERT INTO `datos_casas` VALUES (220, 1915, 399, 0, 1000000, 0, 0, '-', 0, 1963, 399);
INSERT INTO `datos_casas` VALUES (221, 1910, 122, 0, 1000000, 0, 0, '-', 0, 1936, 122);
INSERT INTO `datos_casas` VALUES (222, 1910, 191, 0, 1000000, 0, 0, '-', 0, 1954, 191);
INSERT INTO `datos_casas` VALUES (223, 709, 300, 0, 1000000, 0, 0, '-', 0, 2026, 300);
INSERT INTO `datos_casas` VALUES (224, 2026, 264, 0, 1000000, 0, 0, '-', 0, 2052, 264);
INSERT INTO `datos_casas` VALUES (226, 4264, 436, 0, 1000000, 0, 0, '-', 0, 5122, 439);
INSERT INTO `datos_casas` VALUES (227, 4213, 394, 0, 1000000, 0, 0, '-', 0, 5124, 394);
INSERT INTO `datos_casas` VALUES (228, 4213, 454, 0, 1000000, 0, 0, '-', 0, 5129, 454);
INSERT INTO `datos_casas` VALUES (229, 4213, 142, 0, 1000000, 0, 0, '-', 0, 5131, 142);
INSERT INTO `datos_casas` VALUES (230, 4212, 261, 0, 1000000, 0, 0, '-', 0, 5135, 261);
INSERT INTO `datos_casas` VALUES (231, 4212, 329, 0, 1000000, 0, 0, '-', 0, 5141, 329);
INSERT INTO `datos_casas` VALUES (232, 4212, 386, 0, 1000000, 0, 0, '-', 0, 5146, 386);
INSERT INTO `datos_casas` VALUES (233, 4345, 125, 0, 1000000, 0, 0, '-', 0, 5144, 125);
INSERT INTO `datos_casas` VALUES (234, 4343, 181, 0, 1000000, 0, 0, '-', 0, 5297, 181);
INSERT INTO `datos_casas` VALUES (235, 4170, 373, 0, 1000000, 0, 0, '-', 0, 5284, 373);
INSERT INTO `datos_casas` VALUES (236, 4343, 187, 0, 1000000, 0, 0, '-', 0, 5303, 187);
INSERT INTO `datos_casas` VALUES (237, 4170, 116, 0, 1000000, 0, 0, '-', 0, 5307, 113);
INSERT INTO `datos_casas` VALUES (238, 4337, 201, 0, 1000000, 0, 0, '-', 0, 5310, 201);
INSERT INTO `datos_casas` VALUES (239, 4170, 528, 0, 1000000, 0, 0, '-', 0, 5312, 528);
INSERT INTO `datos_casas` VALUES (240, 4170, 127, 0, 1000000, 0, 0, '-', 0, 5316, 127);
INSERT INTO `datos_casas` VALUES (241, 4171, 415, 0, 1000000, 0, 0, '-', 0, 5320, 415);
INSERT INTO `datos_casas` VALUES (242, 4171, 475, 0, 1000000, 0, 0, '-', 0, 5323, 472);
INSERT INTO `datos_casas` VALUES (243, 4171, 403, 0, 1000000, 0, 0, '-', 0, 5329, 403);
INSERT INTO `datos_casas` VALUES (244, 4171, 738, 0, 1000000, 0, 0, '-', 0, 5325, 738);
INSERT INTO `datos_casas` VALUES (245, 4206, 192, 0, 1000000, 0, 0, '-', 0, 5337, 192);
INSERT INTO `datos_casas` VALUES (246, 4206, 307, 0, 1000000, 0, 0, '-', 0, 5338, 307);
INSERT INTO `datos_casas` VALUES (247, 4206, 367, 0, 1000000, 0, 0, '-', 0, 5341, 364);
INSERT INTO `datos_casas` VALUES (248, 4206, 243, 0, 1000000, 0, 0, '-', 0, 5345, 243);
INSERT INTO `datos_casas` VALUES (249, 4210, 174, 0, 1000000, 0, 0, '-', 0, 5346, 174);
INSERT INTO `datos_casas` VALUES (250, 4210, 270, 0, 1000000, 0, 0, '-', 0, 5347, 270);
INSERT INTO `datos_casas` VALUES (251, 4210, 628, 0, 1000000, 0, 0, '-', 0, 5350, 628);
INSERT INTO `datos_casas` VALUES (252, 4210, 404, 0, 1000000, 0, 0, '-', 0, 5353, 404);
INSERT INTO `datos_casas` VALUES (253, 4210, 187, 0, 1000000, 0, 0, '-', 0, 5356, 187);
INSERT INTO `datos_casas` VALUES (254, 4210, 327, 0, 1000000, 0, 0, '-', 0, 5360, 327);
INSERT INTO `datos_casas` VALUES (255, 4216, 287, 0, 1000000, 0, 0, '-', 0, 5365, 287);
INSERT INTO `datos_casas` VALUES (256, 4216, 230, 0, 1000000, 0, 0, '-', 0, 5368, 230);
INSERT INTO `datos_casas` VALUES (257, 4216, 325, 0, 1000000, 0, 0, '-', 0, 5371, 325);
INSERT INTO `datos_casas` VALUES (258, 4216, 363, 0, 1000000, 0, 0, '-', 0, 5374, 363);
INSERT INTO `datos_casas` VALUES (259, 4216, 423, 0, 1000000, 0, 0, '-', 0, 5377, 420);
INSERT INTO `datos_casas` VALUES (260, 4216, 243, 0, 1000000, 0, 0, '-', 0, 5380, 243);
INSERT INTO `datos_casas` VALUES (261, 4218, 659, 0, 1000000, 0, 0, '-', 0, 5384, 659);
INSERT INTO `datos_casas` VALUES (262, 4218, 180, 0, 1000000, 0, 0, '-', 0, 5385, 180);
INSERT INTO `datos_casas` VALUES (263, 2218, 621, 0, 1000000, 0, 0, '-', 0, 5386, 621);
INSERT INTO `datos_casas` VALUES (264, 4214, 395, 0, 1000000, 0, 0, '-', 0, 5388, 395);
INSERT INTO `datos_casas` VALUES (265, 2218, 544, 0, 1000000, 0, 0, '-', 0, 5394, 544);
INSERT INTO `datos_casas` VALUES (266, 4214, 494, 0, 1000000, 0, 0, '-', 0, 5393, 497);
INSERT INTO `datos_casas` VALUES (267, 2218, 756, 0, 1000000, 0, 0, '-', 0, 5398, 756);
INSERT INTO `datos_casas` VALUES (268, 4214, 335, 0, 1000000, 0, 0, '-', 0, 5401, 335);
INSERT INTO `datos_casas` VALUES (269, 2218, 384, 0, 1000000, 0, 0, '-', 0, 5405, 384);
INSERT INTO `datos_casas` VALUES (270, 4178, 150, 0, 1000000, 0, 0, '-', 0, 5408, 150);
INSERT INTO `datos_casas` VALUES (271, 2214, 728, 0, 1000000, 0, 0, '-', 0, 5410, 728);
INSERT INTO `datos_casas` VALUES (272, 4178, 248, 0, 1000000, 0, 0, '-', 0, 5413, 245);
INSERT INTO `datos_casas` VALUES (273, 2214, 568, 0, 1000000, 0, 0, '-', 0, 5416, 568);
INSERT INTO `datos_casas` VALUES (274, 2214, 243, 0, 1000000, 0, 0, '-', 0, 5417, 243);
INSERT INTO `datos_casas` VALUES (275, 4174, 679, 0, 1000000, 0, 0, '-', 0, 5418, 679);
INSERT INTO `datos_casas` VALUES (276, 4173, 600, 0, 1000000, 0, 0, '-', 0, 5419, 600);
INSERT INTO `datos_casas` VALUES (277, 4173, 561, 0, 1000000, 0, 0, '-', 0, 5423, 561);
INSERT INTO `datos_casas` VALUES (278, 2216, 531, 0, 1000000, 0, 0, '-', 0, 5425, 531);
INSERT INTO `datos_casas` VALUES (279, 2216, 227, 0, 1000000, 0, 0, '-', 0, 5430, 227);
INSERT INTO `datos_casas` VALUES (280, 4173, 345, 0, 1000000, 0, 0, '-', 0, 5431, 345);
INSERT INTO `datos_casas` VALUES (281, 4336, 671, 0, 1000000, 0, 0, '-', 0, 5435, 671);
INSERT INTO `datos_casas` VALUES (282, 4173, 170, 0, 1000000, 0, 0, '-', 0, 5437, 170);
INSERT INTO `datos_casas` VALUES (283, 4173, 279, 0, 1000000, 0, 0, '-', 0, 5441, 279);
INSERT INTO `datos_casas` VALUES (284, 4336, 305, 0, 1000000, 0, 0, '-', 0, 5443, 305);
INSERT INTO `datos_casas` VALUES (285, 4207, 374, 0, 1000000, 0, 0, '-', 0, 5446, 374);
INSERT INTO `datos_casas` VALUES (286, 4336, 181, 0, 1000000, 0, 0, '-', 0, 5448, 181);
INSERT INTO `datos_casas` VALUES (287, 4207, 450, 0, 1000000, 0, 0, '-', 0, 5450, 450);
INSERT INTO `datos_casas` VALUES (288, 4207, 488, 0, 1000000, 0, 0, '-', 0, 5455, 488);
INSERT INTO `datos_casas` VALUES (289, 4336, 188, 0, 1000000, 0, 0, '-', 0, 5457, 188);
INSERT INTO `datos_casas` VALUES (290, 4207, 498, 0, 1000000, 0, 0, '-', 0, 5460, 495);
INSERT INTO `datos_casas` VALUES (291, 4287, 81, 0, 1000000, 0, 0, '-', 0, 5466, 78);
INSERT INTO `datos_casas` VALUES (292, 4207, 150, 0, 1000000, 0, 0, '-', 0, 5465, 150);
INSERT INTO `datos_casas` VALUES (293, 4249, 508, 0, 1000000, 0, 0, '-', 0, 5470, 508);
INSERT INTO `datos_casas` VALUES (294, 4209, 654, 0, 1000000, 0, 0, '-', 0, 5469, 654);
INSERT INTO `datos_casas` VALUES (295, 4209, 730, 0, 1000000, 0, 0, '-', 0, 5471, 730);
INSERT INTO `datos_casas` VALUES (296, 4209, 353, 0, 1000000, 0, 0, '-', 0, 5474, 353);
INSERT INTO `datos_casas` VALUES (297, 4209, 439, 0, 1000000, 0, 0, '-', 0, 5478, 439);
INSERT INTO `datos_casas` VALUES (298, 4217, 625, 0, 1000000, 0, 0, '-', 0, 5479, 625);
INSERT INTO `datos_casas` VALUES (299, 4217, 506, 0, 1000000, 0, 0, '-', 0, 5482, 506);
INSERT INTO `datos_casas` VALUES (300, 4219, 360, 0, 1000000, 0, 0, '-', 0, 5483, 360);
INSERT INTO `datos_casas` VALUES (302, 4219, 87, 0, 1000000, 0, 0, '-', 0, 5485, 87);
INSERT INTO `datos_casas` VALUES (303, 4219, 126, 0, 1000000, 0, 0, '-', 0, 5488, 126);
INSERT INTO `datos_casas` VALUES (304, 4215, 327, 0, 1000000, 0, 0, '-', 0, 5490, 327);
INSERT INTO `datos_casas` VALUES (305, 4215, 187, 0, 1000000, 0, 0, '-', 0, 5493, 187);
INSERT INTO `datos_casas` VALUES (306, 4181, 49, 0, 1000000, 0, 0, '-', 0, 5494, 49);
INSERT INTO `datos_casas` VALUES (307, 4181, 184, 0, 1000000, 0, 0, '-', 0, 5496, 184);
INSERT INTO `datos_casas` VALUES (308, 4181, 89, 0, 1000000, 0, 0, '-', 0, 5499, 89);
INSERT INTO `datos_casas` VALUES (309, 4181, 372, 0, 1000000, 0, 0, '-', 0, 5503, 372);
INSERT INTO `datos_casas` VALUES (310, 4249, 47, 0, 1000000, 0, 0, '-', 0, 5505, 44);
INSERT INTO `datos_casas` VALUES (311, 4240, 753, 0, 1000000, 0, 0, '-', 0, 5508, 753);
INSERT INTO `datos_casas` VALUES (312, 4204, 88, 0, 1000000, 0, 0, '-', 0, 5509, 88);
INSERT INTO `datos_casas` VALUES (313, 4241, 619, 0, 1000000, 0, 0, '-', 0, 5512, 619);
INSERT INTO `datos_casas` VALUES (314, 4204, 669, 0, 1000000, 0, 0, '-', 0, 5515, 672);
INSERT INTO `datos_casas` VALUES (315, 4241, 311, 0, 1000000, 0, 0, '-', 0, 5518, 311);
INSERT INTO `datos_casas` VALUES (316, 4241, 279, 0, 1000000, 0, 0, '-', 0, 5526, 279);
INSERT INTO `datos_casas` VALUES (317, 4342, 714, 0, 1000000, 0, 0, '-', 0, 5530, 714);
INSERT INTO `datos_casas` VALUES (318, 4204, 119, 0, 1000000, 0, 0, '-', 0, 5529, 116);
INSERT INTO `datos_casas` VALUES (319, 4342, 200, 0, 1000000, 0, 0, '-', 0, 5533, 200);
INSERT INTO `datos_casas` VALUES (321, 2220, 216, 0, 1000000, 0, 0, '-', 0, 5538, 216);
INSERT INTO `datos_casas` VALUES (322, 4177, 295, 0, 1000000, 0, 0, '-', 0, 5537, 295);
INSERT INTO `datos_casas` VALUES (323, 2209, 706, 0, 1000000, 0, 0, '-', 0, 5542, 706);
INSERT INTO `datos_casas` VALUES (324, 2209, 548, 0, 1000000, 0, 0, '-', 0, 5546, 548);
INSERT INTO `datos_casas` VALUES (325, 4177, 139, 0, 1000000, 0, 0, '-', 0, 5547, 139);
INSERT INTO `datos_casas` VALUES (326, 4177, 214, 0, 1000000, 0, 0, '-', 0, 5544, 217);
INSERT INTO `datos_casas` VALUES (327, 2209, 586, 0, 1000000, 0, 0, '-', 0, 5550, 586);
INSERT INTO `datos_casas` VALUES (328, 2209, 430, 0, 1000000, 0, 0, '-', 0, 5551, 430);
INSERT INTO `datos_casas` VALUES (329, 4232, 591, 0, 1000000, 0, 0, '-', 0, 5552, 591);
INSERT INTO `datos_casas` VALUES (330, 4231, 149, 0, 1000000, 0, 0, '-', 0, 5555, 149);
INSERT INTO `datos_casas` VALUES (331, 2209, 200, 0, 1000000, 0, 0, '-', 0, 5556, 200);
INSERT INTO `datos_casas` VALUES (332, 4242, 529, 0, 1000000, 0, 0, '-', 0, 5557, 529);
INSERT INTO `datos_casas` VALUES (333, 2209, 143, 0, 1000000, 0, 0, '-', 0, 5559, 143);
INSERT INTO `datos_casas` VALUES (334, 4242, 201, 0, 1000000, 0, 0, '-', 0, 5561, 201);
INSERT INTO `datos_casas` VALUES (335, 2215, 650, 0, 1000000, 0, 0, '-', 0, 5562, 650);
INSERT INTO `datos_casas` VALUES (336, 4242, 239, 0, 1000000, 0, 0, '-', 0, 5566, 239);
INSERT INTO `datos_casas` VALUES (337, 2215, 725, 0, 1000000, 0, 0, '-', 0, 5568, 725);
INSERT INTO `datos_casas` VALUES (338, 4242, 586, 0, 1000000, 0, 0, '-', 0, 5574, 589);
INSERT INTO `datos_casas` VALUES (339, 2215, 204, 0, 1000000, 0, 0, '-', 0, 5573, 204);
INSERT INTO `datos_casas` VALUES (340, 4289, 268, 0, 1000000, 0, 0, '-', 0, 5582, 268);
INSERT INTO `datos_casas` VALUES (341, 4290, 279, 0, 1000000, 0, 0, '-', 0, 5581, 282);
INSERT INTO `datos_casas` VALUES (342, 4290, 748, 0, 1000000, 0, 0, '-', 0, 5585, 748);
INSERT INTO `datos_casas` VALUES (343, 4289, 306, 0, 1000000, 0, 0, '-', 0, 5586, 306);
INSERT INTO `datos_casas` VALUES (344, 4289, 440, 0, 1000000, 0, 0, '-', 0, 5591, 440);
INSERT INTO `datos_casas` VALUES (345, 4290, 146, 0, 1000000, 0, 0, '-', 0, 5593, 146);
INSERT INTO `datos_casas` VALUES (346, 4289, 363, 0, 1000000, 0, 0, '-', 0, 5597, 363);
INSERT INTO `datos_casas` VALUES (347, 4282, 612, 0, 1000000, 0, 0, '-', 0, 5600, 612);
INSERT INTO `datos_casas` VALUES (348, 4282, 40, 0, 1000000, 0, 0, '-', 0, 5604, 40);
INSERT INTO `datos_casas` VALUES (349, 4308, 299, 0, 1000000, 0, 0, '-', 0, 5606, 299);
INSERT INTO `datos_casas` VALUES (350, 4236, 377, 0, 1000000, 0, 0, '-', 0, 5609, 377);
INSERT INTO `datos_casas` VALUES (351, 4236, 339, 0, 1000000, 0, 0, '-', 0, 5614, 339);
INSERT INTO `datos_casas` VALUES (352, 4282, 91, 0, 1000000, 0, 0, '-', 0, 5615, 91);
INSERT INTO `datos_casas` VALUES (353, 4236, 220, 0, 1000000, 0, 0, '-', 0, 5618, 220);
INSERT INTO `datos_casas` VALUES (354, 4280, 494, 0, 1000000, 0, 0, '-', 0, 5617, 494);
INSERT INTO `datos_casas` VALUES (355, 4280, 569, 0, 1000000, 0, 0, '-', 0, 5621, 569);
INSERT INTO `datos_casas` VALUES (356, 4245, 394, 0, 1000000, 0, 0, '-', 0, 5625, 394);
INSERT INTO `datos_casas` VALUES (357, 4280, 645, 0, 1000000, 0, 0, '-', 0, 5626, 645);
INSERT INTO `datos_casas` VALUES (358, 4245, 454, 0, 1000000, 0, 0, '-', 0, 5629, 451);
INSERT INTO `datos_casas` VALUES (359, 4238, 564, 0, 1000000, 0, 0, '-', 0, 5639, 564);
INSERT INTO `datos_casas` VALUES (360, 4096, 306, 0, 1000000, 0, 0, '-', 0, 5637, 309);
INSERT INTO `datos_casas` VALUES (361, 4238, 488, 0, 1000000, 0, 0, '-', 0, 5640, 488);
INSERT INTO `datos_casas` VALUES (362, 4238, 412, 0, 1000000, 0, 0, '-', 0, 5644, 412);
INSERT INTO `datos_casas` VALUES (363, 4096, 249, 0, 1000000, 0, 0, '-', 0, 5646, 249);
INSERT INTO `datos_casas` VALUES (364, 4233, 217, 0, 1000000, 0, 0, '-', 0, 5647, 217);
INSERT INTO `datos_casas` VALUES (365, 4233, 44, 0, 1000000, 0, 0, '-', 0, 5649, 44);
INSERT INTO `datos_casas` VALUES (366, 4233, 427, 0, 1000000, 0, 0, '-', 0, 5653, 427);
INSERT INTO `datos_casas` VALUES (367, 4096, 62, 0, 1000000, 0, 0, '-', 0, 5652, 62);
INSERT INTO `datos_casas` VALUES (368, 4233, 508, 0, 1000000, 0, 0, '-', 0, 5656, 511);
INSERT INTO `datos_casas` VALUES (369, 4233, 83, 0, 1000000, 0, 0, '-', 0, 5659, 83);
INSERT INTO `datos_casas` VALUES (370, 4096, 152, 0, 1000000, 0, 0, '-', 0, 5662, 152);
INSERT INTO `datos_casas` VALUES (371, 4096, 187, 0, 1000000, 0, 0, '-', 0, 5665, 187);
INSERT INTO `datos_casas` VALUES (372, 4300, 225, 0, 1000000, 0, 0, '-', 0, 5669, 225);
INSERT INTO `datos_casas` VALUES (373, 4180, 651, 0, 1000000, 0, 0, '-', 0, 5670, 651);
INSERT INTO `datos_casas` VALUES (374, 4094, 268, 0, 1000000, 0, 0, '-', 0, 5673, 268);
INSERT INTO `datos_casas` VALUES (375, 4094, 87, 0, 1000000, 0, 0, '-', 0, 5676, 87);
INSERT INTO `datos_casas` VALUES (376, 4094, 504, 0, 1000000, 0, 0, '-', 0, 5679, 501);
INSERT INTO `datos_casas` VALUES (377, 4094, 163, 0, 1000000, 0, 0, '-', 0, 5683, 163);
INSERT INTO `datos_casas` VALUES (378, 4095, 724, 0, 1000000, 0, 0, '-', 0, 5684, 724);
INSERT INTO `datos_casas` VALUES (379, 4095, 509, 0, 1000000, 0, 0, '-', 0, 5685, 509);
INSERT INTO `datos_casas` VALUES (380, 4095, 566, 0, 1000000, 0, 0, '-', 0, 5688, 566);
INSERT INTO `datos_casas` VALUES (382, 4095, 347, 0, 1000000, 0, 0, '-', 0, 5709, 347);
INSERT INTO `datos_casas` VALUES (383, 4244, 332, 0, 1000000, 0, 0, '-', 0, 5726, 332);
INSERT INTO `datos_casas` VALUES (384, 4095, 404, 0, 1000000, 0, 0, '-', 0, 5725, 404);
INSERT INTO `datos_casas` VALUES (385, 4244, 602, 0, 1000000, 0, 0, '-', 0, 5728, 602);
INSERT INTO `datos_casas` VALUES (386, 4095, 461, 0, 1000000, 0, 0, '-', 0, 5730, 461);
INSERT INTO `datos_casas` VALUES (387, 4244, 614, 0, 1000000, 0, 0, '-', 0, 5733, 614);
INSERT INTO `datos_casas` VALUES (388, 4095, 449, 0, 1000000, 0, 0, '-', 0, 5737, 449);
INSERT INTO `datos_casas` VALUES (389, 4244, 116, 0, 1000000, 0, 0, '-', 0, 5739, 116);
INSERT INTO `datos_casas` VALUES (390, 2221, 272, 0, 1000000, 0, 0, '-', 0, 5744, 272);
INSERT INTO `datos_casas` VALUES (391, 2210, 619, 0, 1000000, 0, 0, '-', 0, 0, 0);
INSERT INTO `datos_casas` VALUES (392, 4095, 187, 0, 1000000, 0, 0, '-', 0, 5746, 187);
INSERT INTO `datos_casas` VALUES (393, 2210, 533, 0, 1000000, 0, 0, '-', 0, 5750, 530);
INSERT INTO `datos_casas` VALUES (394, 2210, 142, 0, 1000000, 0, 0, '-', 0, 5754, 142);
INSERT INTO `datos_casas` VALUES (395, 2210, 104, 0, 1000000, 0, 0, '-', 0, 5759, 104);
INSERT INTO `datos_casas` VALUES (396, 4246, 262, 0, 1000000, 0, 0, '-', 0, 5758, 262);
INSERT INTO `datos_casas` VALUES (397, 2210, 391, 0, 1000000, 0, 0, '-', 0, 5763, 394);
INSERT INTO `datos_casas` VALUES (398, 4104, 392, 0, 1000000, 0, 0, '-', 0, 5765, 392);
INSERT INTO `datos_casas` VALUES (399, 4303, 282, 0, 1000000, 0, 0, '-', 0, 5768, 285);
INSERT INTO `datos_casas` VALUES (400, 4104, 430, 0, 1000000, 0, 0, '-', 0, 5771, 430);
INSERT INTO `datos_casas` VALUES (401, 4104, 490, 0, 1000000, 0, 0, '-', 0, 5774, 487);
INSERT INTO `datos_casas` VALUES (402, 4303, 511, 0, 1000000, 0, 0, '-', 0, 5775, 511);
INSERT INTO `datos_casas` VALUES (403, 4303, 237, 0, 1000000, 0, 0, '-', 0, 5779, 237);
INSERT INTO `datos_casas` VALUES (405, 4172, 310, 0, 1000000, 0, 0, '-', 0, 5782, 310);
INSERT INTO `datos_casas` VALUES (406, 4301, 139, 0, 1000000, 0, 0, '-', 0, 5784, 139);
INSERT INTO `datos_casas` VALUES (407, 4301, 196, 0, 1000000, 0, 0, '-', 0, 5789, 196);
INSERT INTO `datos_casas` VALUES (408, 4172, 367, 0, 1000000, 0, 0, '-', 0, 5786, 367);
INSERT INTO `datos_casas` VALUES (409, 4172, 405, 0, 1000000, 0, 0, '-', 0, 5795, 405);
INSERT INTO `datos_casas` VALUES (410, 4301, 189, 0, 1000000, 0, 0, '-', 0, 5796, 186);
INSERT INTO `datos_casas` VALUES (411, 4172, 79, 0, 1000000, 0, 0, '-', 0, 5799, 79);
INSERT INTO `datos_casas` VALUES (412, 4259, 69, 0, 1000000, 0, 0, '-', 0, 5802, 69);
INSERT INTO `datos_casas` VALUES (413, 4097, 511, 0, 1000000, 0, 0, '-', 0, 5805, 511);
INSERT INTO `datos_casas` VALUES (414, 4259, 126, 0, 1000000, 0, 0, '-', 0, 5807, 126);
INSERT INTO `datos_casas` VALUES (415, 4097, 435, 0, 1000000, 0, 0, '-', 0, 5808, 435);
INSERT INTO `datos_casas` VALUES (416, 4097, 104, 0, 1000000, 0, 0, '-', 0, 5809, 104);
INSERT INTO `datos_casas` VALUES (417, 4275, 528, 0, 1000000, 0, 0, '-', 0, 5811, 528);
INSERT INTO `datos_casas` VALUES (418, 4097, 180, 0, 1000000, 0, 0, '-', 0, 5813, 180);
INSERT INTO `datos_casas` VALUES (419, 4275, 446, 0, 1000000, 0, 0, '-', 0, 5823, 446);
INSERT INTO `datos_casas` VALUES (420, 4097, 377, 0, 1000000, 0, 0, '-', 0, 5827, 377);
INSERT INTO `datos_casas` VALUES (421, 4275, 91, 0, 1000000, 0, 0, '-', 0, 5829, 91);
INSERT INTO `datos_casas` VALUES (422, 4273, 402, 0, 1000000, 0, 0, '-', 0, 5834, 402);
INSERT INTO `datos_casas` VALUES (423, 4097, 237, 0, 1000000, 0, 0, '-', 0, 5833, 237);
INSERT INTO `datos_casas` VALUES (424, 4273, 713, 0, 1000000, 0, 0, '-', 0, 5838, 713);
INSERT INTO `datos_casas` VALUES (425, 4090, 569, 0, 1000000, 0, 0, '-', 0, 5839, 569);
INSERT INTO `datos_casas` VALUES (426, 4090, 216, 0, 1000000, 0, 0, '-', 0, 5840, 216);
INSERT INTO `datos_casas` VALUES (427, 4243, 269, 0, 1000000, 0, 0, '-', 0, 5841, 269);
INSERT INTO `datos_casas` VALUES (428, 4090, 447, 0, 1000000, 0, 0, '-', 0, 5844, 447);
INSERT INTO `datos_casas` VALUES (429, 4243, 326, 0, 1000000, 0, 0, '-', 0, 5846, 329);
INSERT INTO `datos_casas` VALUES (430, 4090, 201, 0, 1000000, 0, 0, '-', 0, 5852, 201);
INSERT INTO `datos_casas` VALUES (431, 4243, 94, 0, 1000000, 0, 0, '-', 0, 5855, 94);
INSERT INTO `datos_casas` VALUES (432, 4248, 395, 0, 1000000, 0, 0, '-', 0, 5859, 395);
INSERT INTO `datos_casas` VALUES (433, 4090, 76, 0, 1000000, 0, 0, '-', 0, 5860, 76);
INSERT INTO `datos_casas` VALUES (434, 4093, 640, 0, 1000000, 0, 0, '-', 0, 5865, 640);
INSERT INTO `datos_casas` VALUES (435, 4248, 437, 0, 1000000, 0, 0, '-', 0, 5864, 437);
INSERT INTO `datos_casas` VALUES (436, 4093, 206, 0, 1000000, 0, 0, '-', 0, 5866, 206);
INSERT INTO `datos_casas` VALUES (437, 4070, 300, 0, 1000000, 0, 0, '-', 0, 5872, 300);
INSERT INTO `datos_casas` VALUES (438, 4093, 387, 0, 1000000, 0, 0, '-', 0, 5874, 384);
INSERT INTO `datos_casas` VALUES (439, 4077, 676, 0, 1000000, 0, 0, '-', 0, 5876, 676);
INSERT INTO `datos_casas` VALUES (440, 4077, 486, 0, 1000000, 0, 0, '-', 0, 5881, 486);
INSERT INTO `datos_casas` VALUES (441, 4247, 187, 0, 1000000, 0, 0, '-', 0, 5882, 187);
INSERT INTO `datos_casas` VALUES (442, 4106, 383, 0, 1000000, 0, 0, '-', 0, 5887, 383);
INSERT INTO `datos_casas` VALUES (443, 4077, 167, 0, 1000000, 0, 0, '-', 0, 5886, 170);
INSERT INTO `datos_casas` VALUES (444, 4106, 326, 0, 1000000, 0, 0, '-', 0, 5890, 326);
INSERT INTO `datos_casas` VALUES (445, 4106, 459, 0, 1000000, 0, 0, '-', 0, 5893, 459);
INSERT INTO `datos_casas` VALUES (446, 4106, 353, 0, 1000000, 0, 0, '-', 0, 5896, 353);
INSERT INTO `datos_casas` VALUES (447, 4169, 269, 0, 1000000, 0, 0, '-', 0, 5898, 269);
INSERT INTO `datos_casas` VALUES (448, 4169, 231, 0, 1000000, 0, 0, '-', 0, 5901, 231);
INSERT INTO `datos_casas` VALUES (449, 4305, 717, 0, 1000000, 0, 0, '-', 0, 5904, 717);
INSERT INTO `datos_casas` VALUES (450, 4169, 174, 0, 1000000, 0, 0, '-', 0, 5905, 174);
INSERT INTO `datos_casas` VALUES (451, 4304, 367, 0, 1000000, 0, 0, '-', 0, 5909, 367);
INSERT INTO `datos_casas` VALUES (452, 4169, 308, 0, 1000000, 0, 0, '-', 0, 5912, 311);
INSERT INTO `datos_casas` VALUES (453, 4223, 207, 0, 1000000, 0, 0, '-', 0, 5914, 207);
INSERT INTO `datos_casas` VALUES (454, 4098, 489, 0, 1000000, 0, 0, '-', 0, 5917, 489);
INSERT INTO `datos_casas` VALUES (455, 4098, 107, 0, 1000000, 0, 0, '-', 0, 5921, 107);
INSERT INTO `datos_casas` VALUES (456, 4098, 587, 0, 1000000, 0, 0, '-', 0, 5922, 587);
INSERT INTO `datos_casas` VALUES (457, 4223, 182, 0, 1000000, 0, 0, '-', 0, 5919, 182);
INSERT INTO `datos_casas` VALUES (458, 4291, 584, 0, 1000000, 0, 0, '-', 0, 5925, 584);
INSERT INTO `datos_casas` VALUES (459, 4098, 394, 0, 1000000, 0, 0, '-', 0, 5926, 394);
INSERT INTO `datos_casas` VALUES (460, 4291, 469, 0, 1000000, 0, 0, '-', 0, 5927, 469);
INSERT INTO `datos_casas` VALUES (461, 4291, 355, 0, 1000000, 0, 0, '-', 0, 5928, 355);
INSERT INTO `datos_casas` VALUES (462, 4291, 545, 0, 1000000, 0, 0, '-', 0, 5933, 545);
INSERT INTO `datos_casas` VALUES (463, 4098, 164, 0, 1000000, 0, 0, '-', 0, 5932, 164);
INSERT INTO `datos_casas` VALUES (464, 4291, 412, 0, 1000000, 0, 0, '-', 0, 5937, 412);
INSERT INTO `datos_casas` VALUES (465, 4098, 206, 0, 1000000, 0, 0, '-', 0, 5940, 206);
INSERT INTO `datos_casas` VALUES (466, 4269, 214, 0, 1000000, 0, 0, '-', 0, 5942, 214);
INSERT INTO `datos_casas` VALUES (467, 4269, 52, 0, 1000000, 0, 0, '-', 0, 5943, 52);
INSERT INTO `datos_casas` VALUES (468, 4072, 672, 0, 1000000, 0, 0, '-', 0, 5944, 672);
INSERT INTO `datos_casas` VALUES (469, 4072, 154, 0, 1000000, 0, 0, '-', 0, 5946, 154);
INSERT INTO `datos_casas` VALUES (470, 4072, 152, 0, 1000000, 0, 0, '-', 0, 5951, 152);
INSERT INTO `datos_casas` VALUES (471, 4269, 115, 0, 1000000, 0, 0, '-', 0, 5949, 115);
INSERT INTO `datos_casas` VALUES (472, 4072, 729, 0, 1000000, 0, 0, '-', 0, 5957, 729);
INSERT INTO `datos_casas` VALUES (473, 4264, 360, 0, 1000000, 0, 0, '-', 0, 5960, 360);
INSERT INTO `datos_casas` VALUES (474, 4260, 646, 0, 1000000, 0, 0, '-', 0, 5963, 646);
INSERT INTO `datos_casas` VALUES (476, 4072, 187, 0, 1000000, 0, 0, '-', 0, 5967, 187);
INSERT INTO `datos_casas` VALUES (477, 4260, 487, 0, 1000000, 0, 0, '-', 0, 5970, 490);
INSERT INTO `datos_casas` VALUES (478, 4260, 607, 0, 1000000, 0, 0, '-', 0, 5977, 607);
INSERT INTO `datos_casas` VALUES (479, 4074, 453, 0, 1000000, 0, 0, '-', 0, 5972, 453);
INSERT INTO `datos_casas` VALUES (480, 4074, 143, 0, 1000000, 0, 0, '-', 0, 5973, 143);
INSERT INTO `datos_casas` VALUES (481, 4074, 314, 0, 1000000, 0, 0, '-', 0, 5974, 314);
INSERT INTO `datos_casas` VALUES (482, 4073, 624, 0, 1000000, 0, 0, '-', 0, 5978, 624);
INSERT INTO `datos_casas` VALUES (483, 4073, 548, 0, 1000000, 0, 0, '-', 0, 5980, 548);
INSERT INTO `datos_casas` VALUES (484, 4073, 653, 0, 1000000, 0, 0, '-', 0, 5983, 653);
INSERT INTO `datos_casas` VALUES (485, 4074, 528, 0, 1000000, 0, 0, '-', 0, 5982, 528);
INSERT INTO `datos_casas` VALUES (486, 4073, 490, 0, 1000000, 0, 0, '-', 0, 5986, 490);
INSERT INTO `datos_casas` VALUES (487, 4074, 566, 0, 1000000, 0, 0, '-', 0, 5989, 566);
INSERT INTO `datos_casas` VALUES (488, 4073, 313, 0, 1000000, 0, 0, '-', 0, 5992, 313);
INSERT INTO `datos_casas` VALUES (489, 4073, 161, 0, 1000000, 0, 0, '-', 0, 5995, 161);
INSERT INTO `datos_casas` VALUES (490, 4073, 218, 0, 1000000, 0, 0, '-', 0, 6001, 218);
INSERT INTO `datos_casas` VALUES (491, 4074, 206, 0, 1000000, 0, 0, '-', 0, 6002, 206);
INSERT INTO `datos_casas` VALUES (492, 4278, 678, 0, 1000000, 0, 0, '-', 0, 6009, 678);
INSERT INTO `datos_casas` VALUES (493, 4284, 597, 0, 1000000, 0, 0, '-', 0, 6010, 597);
INSERT INTO `datos_casas` VALUES (494, 4073, 263, 0, 1000000, 0, 0, '-', 0, 6008, 260);
INSERT INTO `datos_casas` VALUES (495, 4284, 528, 0, 1000000, 0, 0, '-', 0, 6012, 528);
INSERT INTO `datos_casas` VALUES (496, 4284, 471, 0, 1000000, 0, 0, '-', 0, 6019, 471);
INSERT INTO `datos_casas` VALUES (497, 4082, 473, 0, 1000000, 0, 0, '-', 0, 6026, 473);
INSERT INTO `datos_casas` VALUES (498, 4082, 151, 0, 1000000, 0, 0, '-', 0, 6029, 151);
INSERT INTO `datos_casas` VALUES (499, 4284, 146, 0, 1000000, 0, 0, '-', 0, 6028, 146);
INSERT INTO `datos_casas` VALUES (500, 4082, 87, 0, 1000000, 0, 0, '-', 0, 6030, 87);
INSERT INTO `datos_casas` VALUES (501, 4285, 752, 0, 1000000, 0, 0, '-', 0, 6033, 752);
INSERT INTO `datos_casas` VALUES (502, 4082, 533, 0, 1000000, 0, 0, '-', 0, 6036, 533);
INSERT INTO `datos_casas` VALUES (503, 4082, 302, 0, 1000000, 0, 0, '-', 0, 6041, 302);
INSERT INTO `datos_casas` VALUES (504, 4285, 113, 0, 1000000, 0, 0, '-', 0, 6060, 113);
INSERT INTO `datos_casas` VALUES (505, 4082, 264, 0, 1000000, 0, 0, '-', 0, 6048, 264);
INSERT INTO `datos_casas` VALUES (506, 4082, 226, 0, 1000000, 0, 0, '-', 0, 6053, 226);
INSERT INTO `datos_casas` VALUES (507, 4299, 429, 0, 1000000, 0, 0, '-', 0, 6054, 429);
INSERT INTO `datos_casas` VALUES (508, 4082, 145, 0, 1000000, 0, 0, '-', 0, 6059, 145);
INSERT INTO `datos_casas` VALUES (510, 4302, 126, 0, 1000000, 0, 0, '-', 0, 6067, 126);
INSERT INTO `datos_casas` VALUES (511, 4299, 35, 0, 1000000, 0, 0, '-', 0, 6069, 35);
INSERT INTO `datos_casas` VALUES (513, 4280, 206, 0, 1000000, 0, 0, '-', 0, 5634, 209);
INSERT INTO `datos_casas` VALUES (514, 4219, 417, 0, 1000000, 0, 0, '-', 0, 6142, 417);
INSERT INTO `datos_casas` VALUES (515, 4594, 651, 0, 1000000, 0, 0, '-', 0, 6200, 651);
INSERT INTO `datos_casas` VALUES (516, 4594, 491, 0, 1000000, 0, 0, '-', 0, 6202, 491);
INSERT INTO `datos_casas` VALUES (517, 4594, 151, 0, 1000000, 0, 0, '-', 0, 6205, 151);
INSERT INTO `datos_casas` VALUES (518, 4594, 238, 0, 1000000, 0, 0, '-', 0, 6206, 238);
INSERT INTO `datos_casas` VALUES (519, 4616, 396, 0, 1000000, 0, 0, '-', 0, 6208, 396);
INSERT INTO `datos_casas` VALUES (520, 4616, 257, 0, 1000000, 0, 0, '-', 0, 6213, 257);
INSERT INTO `datos_casas` VALUES (521, 4616, 456, 0, 1000000, 0, 0, '-', 0, 6215, 456);
INSERT INTO `datos_casas` VALUES (522, 4631, 409, 0, 1000000, 0, 0, '-', 0, 6217, 409);
INSERT INTO `datos_casas` VALUES (523, 4631, 681, 0, 1000000, 0, 0, '-', 0, 6220, 681);
INSERT INTO `datos_casas` VALUES (524, 4631, 322, 0, 1000000, 0, 0, '-', 0, 6221, 322);
INSERT INTO `datos_casas` VALUES (525, 4549, 647, 0, 1000000, 0, 0, '-', 0, 6225, 647);
INSERT INTO `datos_casas` VALUES (526, 4549, 161, 0, 1000000, 0, 0, '-', 0, 6226, 161);
INSERT INTO `datos_casas` VALUES (527, 4549, 301, 0, 1000000, 0, 0, '-', 0, 6227, 301);
INSERT INTO `datos_casas` VALUES (528, 5277, 742, 0, 1000000, 0, 0, '-', 0, 6232, 742);
INSERT INTO `datos_casas` VALUES (529, 5277, 400, 0, 1000000, 0, 0, '-', 0, 6231, 400);
INSERT INTO `datos_casas` VALUES (530, 5277, 621, 0, 1000000, 0, 0, '-', 0, 6234, 621);
INSERT INTO `datos_casas` VALUES (531, 5277, 76, 0, 1000000, 0, 0, '-', 0, 6236, 76);
INSERT INTO `datos_casas` VALUES (532, 4610, 660, 0, 1000000, 0, 0, '-', 0, 6239, 660);
INSERT INTO `datos_casas` VALUES (533, 4610, 602, 0, 1000000, 0, 0, '-', 0, 6241, 602);
INSERT INTO `datos_casas` VALUES (534, 4610, 483, 0, 1000000, 0, 0, '-', 0, 6243, 483);
INSERT INTO `datos_casas` VALUES (535, 4610, 412, 0, 1000000, 0, 0, '-', 0, 6245, 412);
INSERT INTO `datos_casas` VALUES (536, 4610, 268, 0, 1000000, 0, 0, '-', 0, 6247, 268);
INSERT INTO `datos_casas` VALUES (537, 4936, 454, 0, 1000000, 0, 0, '-', 0, 6253, 454);
INSERT INTO `datos_casas` VALUES (538, 6248, 183, 0, 1000000, 0, 0, '-', 0, 6250, 183);
INSERT INTO `datos_casas` VALUES (539, 6249, 225, 0, 1000000, 0, 0, '-', 0, 6251, 225);
INSERT INTO `datos_casas` VALUES (540, 6181, 81, 0, 1000000, 0, 0, '-', 0, 6237, 81);
INSERT INTO `datos_casas` VALUES (541, 1915, 229, 0, 1000000, 0, 0, '-', 0, 1964, 229);
INSERT INTO `datos_casas` VALUES (542, 4595, 607, 0, 1000000, 0, 0, '-', 0, 6278, 607);
INSERT INTO `datos_casas` VALUES (543, 4649, 218, 0, 1000000, 0, 0, '-', 0, 6280, 218);
INSERT INTO `datos_casas` VALUES (544, 4649, 275, 0, 1000000, 0, 0, '-', 0, 6285, 275);
INSERT INTO `datos_casas` VALUES (545, 4591, 215, 0, 1000000, 0, 0, '-', 0, 6282, 215);
INSERT INTO `datos_casas` VALUES (546, 4591, 552, 0, 1000000, 0, 0, '-', 0, 6286, 552);
INSERT INTO `datos_casas` VALUES (547, 4591, 702, 0, 1000000, 0, 0, '-', 0, 6288, 702);
INSERT INTO `datos_casas` VALUES (548, 4649, 614, 0, 1000000, 0, 0, '-', 0, 6292, 614);
INSERT INTO `datos_casas` VALUES (549, 4605, 534, 0, 1000000, 0, 0, '-', 0, 6290, 534);
INSERT INTO `datos_casas` VALUES (550, 5108, 57, 0, 1000000, 0, 0, '-', 0, 6294, 57);
INSERT INTO `datos_casas` VALUES (551, 5108, 597, 0, 1000000, 0, 0, '-', 0, 6296, 597);
INSERT INTO `datos_casas` VALUES (552, 5108, 367, 0, 1000000, 0, 0, '-', 0, 6298, 367);
INSERT INTO `datos_casas` VALUES (553, 4622, 378, 0, 1000000, 0, 0, '-', 0, 6300, 378);
INSERT INTO `datos_casas` VALUES (554, 4622, 164, 0, 1000000, 0, 0, '-', 0, 6302, 164);
INSERT INTO `datos_casas` VALUES (555, 4666, 75, 0, 1000000, 0, 0, '-', 0, 6305, 75);
INSERT INTO `datos_casas` VALUES (556, 4666, 493, 0, 1000000, 0, 0, '-', 0, 6307, 493);
INSERT INTO `datos_casas` VALUES (557, 4666, 132, 0, 1000000, 0, 0, '-', 0, 6309, 132);
INSERT INTO `datos_casas` VALUES (558, 4666, 578, 0, 1000000, 0, 0, '-', 0, 6311, 578);
INSERT INTO `datos_casas` VALUES (559, 4605, 438, 0, 1000000, 0, 0, '-', 0, 6313, 438);
INSERT INTO `datos_casas` VALUES (560, 5279, 261, 0, 1000000, 0, 0, '-', 0, 6317, 261);
INSERT INTO `datos_casas` VALUES (561, 5279, 640, 0, 1000000, 0, 0, '-', 0, 6318, 640);
INSERT INTO `datos_casas` VALUES (562, 5279, 276, 0, 1000000, 0, 0, '-', 0, 6319, 276);
INSERT INTO `datos_casas` VALUES (563, 5317, 429, 0, 1000000, 0, 0, '-', 0, 6323, 429);
INSERT INTO `datos_casas` VALUES (564, 5279, 283, 0, 1000000, 0, 0, '-', 0, 6321, 283);
INSERT INTO `datos_casas` VALUES (565, 5317, 160, 0, 1000000, 0, 0, '-', 0, 6325, 160);
INSERT INTO `datos_casas` VALUES (566, 5317, 217, 0, 1000000, 0, 0, '-', 0, 6327, 217);
INSERT INTO `datos_casas` VALUES (568, 5317, 737, 0, 1000000, 0, 0, '-', 0, 6329, 737);
INSERT INTO `datos_casas` VALUES (569, 4611, 449, 0, 1000000, 0, 0, '-', 0, 6333, 449);
INSERT INTO `datos_casas` VALUES (570, 4606, 344, 0, 1000000, 0, 0, '-', 0, 6331, 344);
INSERT INTO `datos_casas` VALUES (571, 4611, 437, 0, 1000000, 0, 0, '-', 0, 6335, 437);
INSERT INTO `datos_casas` VALUES (572, 5326, 522, 0, 1000000, 0, 0, '-', 0, 6337, 522);
INSERT INTO `datos_casas` VALUES (573, 5326, 361, 0, 1000000, 0, 0, '-', 0, 6339, 361);
INSERT INTO `datos_casas` VALUES (574, 4606, 506, 0, 1000000, 0, 0, '-', 0, 6345, 506);
INSERT INTO `datos_casas` VALUES (575, 5326, 713, 0, 1000000, 0, 0, '-', 0, 6341, 713);
INSERT INTO `datos_casas` VALUES (576, 5326, 69, 0, 1000000, 0, 0, '-', 0, 6343, 69);
INSERT INTO `datos_casas` VALUES (577, 4644, 94, 0, 1000000, 0, 0, '-', 0, 6347, 94);
INSERT INTO `datos_casas` VALUES (578, 4644, 571, 0, 1000000, 0, 0, '-', 0, 6349, 571);
INSERT INTO `datos_casas` VALUES (579, 4644, 402, 0, 1000000, 0, 0, '-', 0, 6351, 402);
INSERT INTO `datos_casas` VALUES (580, 4644, 380, 0, 1000000, 0, 0, '-', 0, 6353, 380);
INSERT INTO `datos_casas` VALUES (581, 4646, 402, 0, 1000000, 0, 0, '-', 0, 6355, 402);
INSERT INTO `datos_casas` VALUES (582, 4646, 80, 0, 1000000, 0, 0, '-', 0, 6357, 80);
INSERT INTO `datos_casas` VALUES (583, 4586, 432, 0, 1000000, 0, 0, '-', 0, 6579, 432);
INSERT INTO `datos_casas` VALUES (584, 4647, 223, 0, 1000000, 0, 0, '-', 0, 6578, 223);
INSERT INTO `datos_casas` VALUES (585, 4937, 115, 0, 1000000, 0, 0, '-', 0, 6585, 115);
INSERT INTO `datos_casas` VALUES (586, 4647, 600, 0, 1000000, 0, 0, '-', 0, 6580, 600);
INSERT INTO `datos_casas` VALUES (587, 4647, 578, 0, 1000000, 0, 0, '-', 0, 6583, 578);
INSERT INTO `datos_casas` VALUES (588, 4604, 174, 0, 1000000, 0, 0, '-', 0, 6591, 174);
INSERT INTO `datos_casas` VALUES (589, 4647, 455, 0, 1000000, 0, 0, '-', 0, 6584, 455);
INSERT INTO `datos_casas` VALUES (590, 4937, 91, 0, 1000000, 0, 0, '-', 0, 6595, 91);
INSERT INTO `datos_casas` VALUES (591, 4614, 618, 0, 1000000, 0, 0, '-', 0, 6589, 618);
INSERT INTO `datos_casas` VALUES (592, 4614, 492, 0, 1000000, 0, 0, '-', 0, 6590, 492);
INSERT INTO `datos_casas` VALUES (593, 4614, 644, 0, 1000000, 0, 0, '-', 0, 6593, 644);
INSERT INTO `datos_casas` VALUES (594, 4603, 280, 0, 1000000, 0, 0, '-', 0, 6598, 280);
INSERT INTO `datos_casas` VALUES (595, 4603, 718, 0, 1000000, 0, 0, '-', 0, 6599, 718);
INSERT INTO `datos_casas` VALUES (596, 4936, 757, 0, 1000000, 0, 0, '-', 0, 6601, 757);
INSERT INTO `datos_casas` VALUES (597, 4615, 614, 0, 1000000, 0, 0, '-', 0, 6604, 614);
INSERT INTO `datos_casas` VALUES (598, 4604, 453, 0, 1000000, 0, 0, '-', 0, 6605, 453);
INSERT INTO `datos_casas` VALUES (599, 4615, 404, 0, 1000000, 0, 0, '-', 0, 6607, 404);
INSERT INTO `datos_casas` VALUES (600, 5280, 108, 0, 1000000, 0, 0, '-', 0, 6609, 108);
INSERT INTO `datos_casas` VALUES (601, 4600, 264, 0, 1000000, 0, 0, '-', 0, 6623, 264);
INSERT INTO `datos_casas` VALUES (602, 4588, 170, 0, 1000000, 0, 0, '-', 0, 6612, 170);
INSERT INTO `datos_casas` VALUES (603, 4588, 174, 0, 1000000, 0, 0, '-', 0, 6617, 174);
INSERT INTO `datos_casas` VALUES (604, 4588, 250, 0, 1000000, 0, 0, '-', 0, 6614, 250);
INSERT INTO `datos_casas` VALUES (605, 4593, 505, 0, 1000000, 0, 0, '-', 0, 6625, 505);
INSERT INTO `datos_casas` VALUES (606, 4593, 381, 0, 1000000, 0, 0, '-', 0, 6627, 381);
INSERT INTO `datos_casas` VALUES (607, 4588, 326, 0, 1000000, 0, 0, '-', 0, 6621, 326);
INSERT INTO `datos_casas` VALUES (608, 4936, 112, 0, 1000000, 0, 0, '-', 0, 6615, 112);
INSERT INTO `datos_casas` VALUES (609, 5280, 115, 0, 1000000, 0, 0, '-', 0, 6619, 115);
INSERT INTO `datos_casas` VALUES (610, 4620, 450, 0, 1000000, 0, 0, '-', 0, 6629, 450);
INSERT INTO `datos_casas` VALUES (611, 4620, 256, 0, 1000000, 0, 0, '-', 0, 6631, 256);
INSERT INTO `datos_casas` VALUES (612, 4612, 132, 0, 1000000, 0, 0, '-', 0, 6636, 132);
INSERT INTO `datos_casas` VALUES (613, 4640, 413, 0, 1000000, 0, 0, '-', 0, 6633, 413);
INSERT INTO `datos_casas` VALUES (614, 4640, 471, 0, 1000000, 0, 0, '-', 0, 6637, 471);
INSERT INTO `datos_casas` VALUES (615, 4600, 258, 0, 1000000, 0, 0, '-', 0, 6647, 258);
INSERT INTO `datos_casas` VALUES (616, 4640, 384, 0, 1000000, 0, 0, '-', 0, 6643, 384);
INSERT INTO `datos_casas` VALUES (617, 5136, 524, 0, 1000000, 0, 0, '-', 0, 6649, 524);
INSERT INTO `datos_casas` VALUES (618, 5136, 289, 0, 1000000, 0, 0, '-', 0, 6652, 289);
INSERT INTO `datos_casas` VALUES (619, 4640, 683, 0, 1000000, 0, 0, '-', 0, 6645, 683);
INSERT INTO `datos_casas` VALUES (620, 5304, 455, 0, 1000000, 0, 0, '-', 0, 6640, 455);
INSERT INTO `datos_casas` VALUES (621, 5304, 375, 0, 1000000, 0, 0, '-', 0, 6644, 375);
INSERT INTO `datos_casas` VALUES (622, 4596, 346, 0, 1000000, 0, 0, '-', 0, 6659, 346);
INSERT INTO `datos_casas` VALUES (623, 4597, 105, 0, 1000000, 0, 0, '-', 0, 6663, 105);
INSERT INTO `datos_casas` VALUES (624, 4623, 331, 0, 1000000, 0, 0, '-', 0, 6661, 331);
INSERT INTO `datos_casas` VALUES (625, 4613, 245, 0, 1000000, 0, 0, '-', 0, 6653, 245);
INSERT INTO `datos_casas` VALUES (626, 4613, 101, 0, 1000000, 0, 0, '-', 0, 6655, 101);
INSERT INTO `datos_casas` VALUES (627, 4613, 675, 0, 1000000, 0, 0, '-', 0, 6657, 675);
INSERT INTO `datos_casas` VALUES (628, 4584, 467, 0, 1000000, 0, 0, '-', 0, 6665, 467);
INSERT INTO `datos_casas` VALUES (629, 4941, 682, 0, 1000000, 0, 0, '-', 0, 6667, 682);
INSERT INTO `datos_casas` VALUES (630, 4941, 144, 0, 1000000, 0, 0, '-', 0, 6673, 144);
INSERT INTO `datos_casas` VALUES (631, 4628, 353, 0, 1000000, 0, 0, '-', 0, 6669, 353);
INSERT INTO `datos_casas` VALUES (632, 4628, 543, 0, 1000000, 0, 0, '-', 0, 6671, 543);
INSERT INTO `datos_casas` VALUES (633, 5139, 261, 0, 1000000, 0, 0, '-', 0, 6676, 261);
INSERT INTO `datos_casas` VALUES (634, 5139, 377, 0, 1000000, 0, 0, '-', 0, 6679, 377);
INSERT INTO `datos_casas` VALUES (635, 5139, 569, 0, 1000000, 0, 0, '-', 0, 6677, 569);
INSERT INTO `datos_casas` VALUES (636, 2209, 506, 0, 1000000, 0, 0, '-', 0, 6716, 506);
INSERT INTO `datos_casas` VALUES (637, 4646, 137, 0, 1000000, 0, 0, '-', 0, 6359, 137);
INSERT INTO `datos_casas` VALUES (638, 1897, 431, 0, 1000000, 0, 0, '-', 0, 6982, 431);
INSERT INTO `datos_casas` VALUES (639, 4302, 739, 0, 1000000, 0, 0, '-', 0, 7270, 739);
INSERT INTO `datos_casas` VALUES (640, 7441, 361, 0, 1000000, 0, 0, '-', 0, 7731, 361);
INSERT INTO `datos_casas` VALUES (641, 7444, 163, 0, 1000000, 0, 0, '-', 0, 7622, 150);
INSERT INTO `datos_casas` VALUES (642, 7445, 234, 0, 1000000, 0, 0, '-', 0, 7718, 234);
INSERT INTO `datos_casas` VALUES (643, 7445, 389, 0, 1000000, 0, 0, '-', 0, 7721, 389);
INSERT INTO `datos_casas` VALUES (644, 7447, 118, 0, 1000000, 0, 0, '-', 0, 7653, 118);
INSERT INTO `datos_casas` VALUES (645, 7426, 133, 0, 1000000, 0, 0, '-', 0, 7699, 141);
INSERT INTO `datos_casas` VALUES (647, 7428, 162, 0, 1000000, 0, 0, '-', 0, 0, 0);
INSERT INTO `datos_casas` VALUES (648, 7430, 442, 0, 1000000, 0, 0, '-', 0, 7691, 442);
INSERT INTO `datos_casas` VALUES (649, 7430, 177, 0, 1000000, 0, 0, '-', 0, 7693, 177);
INSERT INTO `datos_casas` VALUES (650, 7415, 147, 0, 1000000, 0, 0, '-', 0, 7658, 147);
INSERT INTO `datos_casas` VALUES (651, 7413, 74, 0, 1000000, 0, 0, '-', 0, 7730, 74);
INSERT INTO `datos_casas` VALUES (652, 7412, 156, 0, 1000000, 0, 0, '-', 0, 7635, 203);
INSERT INTO `datos_casas` VALUES (653, 7412, 103, 0, 1000000, 0, 0, '-', 0, 7638, 103);
INSERT INTO `datos_casas` VALUES (654, 7411, 170, 0, 1000000, 0, 0, '-', 0, 7669, 170);
INSERT INTO `datos_casas` VALUES (655, 7410, 216, 0, 1000000, 0, 0, '-', 0, 7708, 150);
INSERT INTO `datos_casas` VALUES (656, 7410, 294, 0, 1000000, 0, 0, '-', 0, 7716, 294);
INSERT INTO `datos_casas` VALUES (657, 7409, 323, 0, 1000000, 0, 0, '-', 0, 7739, 323);
INSERT INTO `datos_casas` VALUES (658, 7408, 185, 0, 1000000, 0, 0, '-', 0, 7711, 185);
INSERT INTO `datos_casas` VALUES (659, 7408, 208, 0, 1000000, 0, 0, '-', 0, 7646, 208);
INSERT INTO `datos_casas` VALUES (660, 7392, 162, 0, 1000000, 0, 0, '-', 0, 7642, 162);
INSERT INTO `datos_casas` VALUES (661, 7289, 189, 0, 1000000, 0, 0, '-', 0, 7734, 133);
INSERT INTO `datos_casas` VALUES (662, 7289, 246, 0, 1000000, 0, 0, '-', 0, 7744, 246);
INSERT INTO `datos_casas` VALUES (663, 7394, 284, 0, 1000000, 0, 0, '-', 0, 7723, 284);
INSERT INTO `datos_casas` VALUES (664, 7394, 339, 0, 1000000, 0, 0, '-', 0, 7727, 339);
INSERT INTO `datos_casas` VALUES (666, 7397, 143, 0, 1000000, 0, 0, '-', 0, 7736, 143);
INSERT INTO `datos_casas` VALUES (667, 7399, 133, 0, 1000000, 0, 0, '-', 0, 7662, 141);
INSERT INTO `datos_casas` VALUES (668, 7399, 127, 0, 1000000, 0, 0, '-', 0, 7664, 127);
INSERT INTO `datos_casas` VALUES (669, 7400, 105, 0, 1000000, 0, 0, '-', 0, 7620, 105);
INSERT INTO `datos_casas` VALUES (670, 7384, 150, 0, 1000000, 0, 0, '-', 0, 7626, 141);
INSERT INTO `datos_casas` VALUES (671, 7383, 342, 0, 1000000, 0, 0, '-', 0, 7670, 342);
INSERT INTO `datos_casas` VALUES (672, 7383, 172, 0, 1000000, 0, 0, '-', 0, 7673, 172);
INSERT INTO `datos_casas` VALUES (674, 7364, 143, 0, 1000000, 0, 0, '-', 0, 7742, 141);
INSERT INTO `datos_casas` VALUES (675, 7380, 99, 0, 1000000, 0, 0, '-', 0, 7643, 99);
INSERT INTO `datos_casas` VALUES (676, 7380, 107, 0, 1000000, 0, 0, '-', 0, 7645, 107);
INSERT INTO `datos_casas` VALUES (677, 7379, 144, 0, 1000000, 0, 0, '-', 0, 7683, 144);
INSERT INTO `datos_casas` VALUES (678, 7379, 163, 0, 1000000, 0, 0, '-', 0, 7679, 163);
INSERT INTO `datos_casas` VALUES (679, 7377, 221, 0, 1000000, 0, 0, '-', 0, 7725, 221);
INSERT INTO `datos_casas` VALUES (680, 7377, 126, 0, 1000000, 0, 0, '-', 0, 7728, 126);
INSERT INTO `datos_casas` VALUES (681, 7360, 208, 0, 1000000, 0, 0, '-', 0, 7631, 208);
INSERT INTO `datos_casas` VALUES (682, 7360, 278, 0, 1000000, 0, 0, '-', 0, 7623, 278);
INSERT INTO `datos_casas` VALUES (683, 7360, 158, 0, 1000000, 0, 0, '-', 0, 7634, 158);
INSERT INTO `datos_casas` VALUES (684, 7363, 118, 0, 1000000, 0, 0, '-', 0, 7685, 141);
INSERT INTO `datos_casas` VALUES (685, 7363, 201, 0, 1000000, 0, 0, '-', 0, 7690, 201);
INSERT INTO `datos_casas` VALUES (686, 7363, 397, 0, 1000000, 0, 0, '-', 0, 7684, 397);
INSERT INTO `datos_casas` VALUES (687, 7381, 221, 0, 1000000, 0, 0, '-', 0, 7651, 165);
INSERT INTO `datos_casas` VALUES (690, 7367, 441, 0, 1000000, 0, 0, '-', 0, 7682, 142);
INSERT INTO `datos_casas` VALUES (691, 7367, 135, 0, 1000000, 0, 0, '-', 0, 7677, 191);
INSERT INTO `datos_casas` VALUES (692, 7367, 172, 0, 1000000, 0, 0, '-', 0, 7681, 172);
INSERT INTO `datos_casas` VALUES (693, 7368, 105, 0, 1000000, 0, 0, '-', 0, 7630, 142);
INSERT INTO `datos_casas` VALUES (694, 7366, 104, 0, 1000000, 0, 0, '-', 0, 7745, 104);
INSERT INTO `datos_casas` VALUES (695, 7347, 285, 0, 1000000, 0, 0, '-', 0, 7665, 285);
INSERT INTO `datos_casas` VALUES (696, 7382, 447, 0, 1000000, 0, 0, '-', 0, 7702, 447);
INSERT INTO `datos_casas` VALUES (697, 7348, 273, 0, 1000000, 0, 0, '-', 0, 7659, 273);
INSERT INTO `datos_casas` VALUES (698, 7352, 114, 0, 1000000, 0, 0, '-', 0, 7648, 150);
INSERT INTO `datos_casas` VALUES (699, 7347, 99, 0, 1000000, 0, 0, '-', 0, 7667, 99);
INSERT INTO `datos_casas` VALUES (700, 7331, 124, 0, 1000000, 0, 0, '-', 0, 7694, 142);
INSERT INTO `datos_casas` VALUES (701, 7331, 446, 0, 1000000, 0, 0, '-', 0, 7695, 150);
INSERT INTO `datos_casas` VALUES (702, 7331, 129, 0, 1000000, 0, 0, '-', 0, 7698, 129);
INSERT INTO `datos_casas` VALUES (703, 7345, 335, 0, 1000000, 0, 0, '-', 0, 7717, 335);
INSERT INTO `datos_casas` VALUES (704, 7425, 425, 0, 1000000, 0, 0, '-', 0, 7746, 425);
INSERT INTO `datos_casas` VALUES (705, 7425, 184, 0, 1000000, 0, 0, '-', 0, 7750, 184);
INSERT INTO `datos_casas` VALUES (706, 7334, 258, 0, 1000000, 0, 0, '-', 0, 7757, 258);
INSERT INTO `datos_casas` VALUES (707, 7335, 239, 0, 1000000, 0, 0, '-', 0, 7753, 295);
INSERT INTO `datos_casas` VALUES (708, 7335, 388, 0, 1000000, 0, 0, '-', 0, 7755, 402);
INSERT INTO `datos_casas` VALUES (709, 7334, 268, 0, 1000000, 0, 0, '-', 0, 7759, 268);
INSERT INTO `datos_casas` VALUES (710, 7409, 267, 0, 1000000, 0, 0, '-', 0, 7737, 267);
INSERT INTO `datos_casas` VALUES (711, 7414, 236, 0, 1000000, 0, 0, '-', 0, 7779, 236);
INSERT INTO `datos_casas` VALUES (712, 7786, 165, 0, 1000000, 0, 0, '-', 0, 7788, 165);
INSERT INTO `datos_casas` VALUES (713, 7786, 210, 0, 1000000, 0, 0, '-', 0, 7789, 210);
INSERT INTO `datos_casas` VALUES (714, 7786, 255, 0, 1000000, 0, 0, '-', 0, 7790, 255);
INSERT INTO `datos_casas` VALUES (715, 7786, 300, 0, 1000000, 0, 0, '-', 0, 7791, 300);
INSERT INTO `datos_casas` VALUES (716, 7786, 345, 0, 1000000, 0, 0, '-', 0, 7792, 345);
INSERT INTO `datos_casas` VALUES (717, 7912, 265, 0, 1000000, 0, 0, '-', 0, 8378, 265);
INSERT INTO `datos_casas` VALUES (718, 8016, 256, 0, 1000000, 0, 0, '-', 0, 8379, 256);
INSERT INTO `datos_casas` VALUES (719, 7912, 286, 0, 1000000, 0, 0, '-', 0, 8381, 286);
INSERT INTO `datos_casas` VALUES (720, 7995, 299, 0, 1000000, 0, 0, '-', 0, 8384, 299);
INSERT INTO `datos_casas` VALUES (721, 8003, 67, 0, 1000000, 0, 0, '-', 0, 8386, 67);
INSERT INTO `datos_casas` VALUES (722, 7983, 269, 0, 1000000, 0, 0, '-', 0, 8388, 269);
INSERT INTO `datos_casas` VALUES (723, 7984, 226, 0, 1000000, 0, 0, '-', 0, 8390, 226);
INSERT INTO `datos_casas` VALUES (724, 7971, 343, 0, 1000000, 0, 0, '-', 0, 8393, 343);
INSERT INTO `datos_casas` VALUES (725, 7971, 446, 0, 1000000, 0, 0, '-', 0, 8396, 446);
INSERT INTO `datos_casas` VALUES (726, 8011, 270, 0, 1000000, 0, 0, '-', 0, 8398, 270);
INSERT INTO `datos_casas` VALUES (727, 7998, 220, 0, 1000000, 0, 0, '-', 0, 8401, 220);
INSERT INTO `datos_casas` VALUES (728, 7998, 212, 0, 1000000, 0, 0, '-', 0, 8404, 212);
INSERT INTO `datos_casas` VALUES (729, 7972, 241, 0, 1000000, 0, 0, '-', 0, 8407, 241);
INSERT INTO `datos_casas` VALUES (730, 8002, 46, 0, 1000000, 0, 0, '-', 0, 8409, 46);
INSERT INTO `datos_casas` VALUES (731, 8002, 211, 0, 1000000, 0, 0, '-', 0, 8414, 211);
INSERT INTO `datos_casas` VALUES (732, 7972, 186, 0, 1000000, 0, 0, '-', 0, 8412, 186);
INSERT INTO `datos_casas` VALUES (733, 8002, 257, 0, 1000000, 0, 0, '-', 0, 8419, 257);
INSERT INTO `datos_casas` VALUES (734, 7972, 347, 0, 1000000, 0, 0, '-', 0, 8417, 347);
INSERT INTO `datos_casas` VALUES (735, 8015, 225, 0, 1000000, 0, 0, '-', 0, 8422, 225);
INSERT INTO `datos_casas` VALUES (736, 8028, 235, 0, 1000000, 0, 0, '-', 0, 8429, 235);
INSERT INTO `datos_casas` VALUES (737, 7973, 168, 0, 1000000, 0, 0, '-', 0, 8425, 168);
INSERT INTO `datos_casas` VALUES (738, 7988, 100, 0, 1000000, 0, 0, '-', 0, 8435, 100);
INSERT INTO `datos_casas` VALUES (739, 7973, 122, 0, 1000000, 0, 0, '-', 0, 8432, 122);
INSERT INTO `datos_casas` VALUES (741, 7974, 206, 0, 1000000, 0, 0, '-', 0, 8436, 206);
INSERT INTO `datos_casas` VALUES (742, 8001, 263, 0, 1000000, 0, 0, '-', 0, 8440, 263);
INSERT INTO `datos_casas` VALUES (743, 7974, 108, 0, 1000000, 0, 0, '-', 0, 8441, 108);
INSERT INTO `datos_casas` VALUES (744, 8001, 170, 0, 1000000, 0, 0, '-', 0, 8447, 170);
INSERT INTO `datos_casas` VALUES (745, 7974, 71, 0, 1000000, 0, 0, '-', 0, 8445, 71);
INSERT INTO `datos_casas` VALUES (746, 7987, 103, 0, 1000000, 0, 0, '-', 0, 8449, 103);
INSERT INTO `datos_casas` VALUES (747, 8039, 104, 0, 1000000, 0, 0, '-', 0, 8453, 104);
INSERT INTO `datos_casas` VALUES (748, 7987, 84, 0, 1000000, 0, 0, '-', 0, 8452, 84);
INSERT INTO `datos_casas` VALUES (749, 7987, 269, 0, 1000000, 0, 0, '-', 0, 8455, 269);
INSERT INTO `datos_casas` VALUES (750, 8000, 109, 0, 1000000, 0, 0, '-', 0, 8457, 109);
INSERT INTO `datos_casas` VALUES (751, 8001, 239, 0, 1000000, 0, 0, '-', 0, 8443, 239);
INSERT INTO `datos_casas` VALUES (752, 8038, 421, 0, 1000000, 0, 0, '-', 0, 8459, 421);
INSERT INTO `datos_casas` VALUES (753, 8025, 296, 0, 1000000, 0, 0, '-', 0, 8461, 296);
INSERT INTO `datos_casas` VALUES (754, 7986, 152, 0, 1000000, 0, 0, '-', 0, 8464, 152);
INSERT INTO `datos_casas` VALUES (755, 8012, 271, 0, 1000000, 0, 0, '-', 0, 8465, 271);
INSERT INTO `datos_casas` VALUES (756, 7986, 263, 0, 1000000, 0, 0, '-', 0, 8468, 263);
INSERT INTO `datos_casas` VALUES (757, 7999, 176, 0, 1000000, 0, 0, '-', 0, 8470, 176);
INSERT INTO `datos_casas` VALUES (759, 9455, 397, 0, 1000000, 0, 0, '-', 0, 9667, 397);
INSERT INTO `datos_casas` VALUES (761, 9451, 385, 0, 1000000, 0, 0, '-', 0, 9669, 385);
INSERT INTO `datos_casas` VALUES (762, 9464, 306, 0, 1000000, 0, 0, '-', 0, 9671, 306);
INSERT INTO `datos_casas` VALUES (763, 9464, 184, 0, 1000000, 0, 0, '-', 0, 9673, 184);
INSERT INTO `datos_casas` VALUES (764, 9450, 137, 0, 1000000, 0, 0, '-', 0, 9675, 137);
INSERT INTO `datos_casas` VALUES (765, 9450, 434, 0, 1000000, 0, 0, '-', 0, 9677, 434);
INSERT INTO `datos_casas` VALUES (766, 9450, 114, 0, 1000000, 0, 0, '-', 0, 9679, 114);
INSERT INTO `datos_casas` VALUES (767, 9454, 129, 0, 1000000, 0, 0, '-', 0, 9681, 129);
INSERT INTO `datos_casas` VALUES (768, 9454, 88, 0, 1000000, 0, 0, '-', 0, 9683, 88);
INSERT INTO `datos_casas` VALUES (769, 9457, 218, 0, 1000000, 0, 0, '-', 0, 9686, 218);
INSERT INTO `datos_casas` VALUES (770, 9461, 349, 0, 1000000, 0, 0, '-', 0, 9688, 349);
INSERT INTO `datos_casas` VALUES (771, 9461, 129, 0, 1000000, 0, 0, '-', 0, 9690, 129);
INSERT INTO `datos_casas` VALUES (772, 9449, 382, 0, 1000000, 0, 0, '-', 0, 9692, 382);
INSERT INTO `datos_casas` VALUES (773, 9449, 302, 0, 1000000, 0, 0, '-', 0, 9694, 302);
INSERT INTO `datos_casas` VALUES (774, 9449, 99, 0, 1000000, 0, 0, '-', 0, 9697, 99);
INSERT INTO `datos_casas` VALUES (775, 9453, 76, 0, 1000000, 0, 0, '-', 0, 9700, 76);
INSERT INTO `datos_casas` VALUES (776, 9453, 442, 0, 1000000, 0, 0, '-', 0, 9702, 442);
INSERT INTO `datos_casas` VALUES (777, 9453, 233, 0, 1000000, 0, 0, '-', 0, 9704, 233);
INSERT INTO `datos_casas` VALUES (778, 9456, 86, 0, 1000000, 0, 0, '-', 0, 9706, 86);
INSERT INTO `datos_casas` VALUES (779, 9456, 448, 0, 1000000, 0, 0, '-', 0, 9708, 448);
INSERT INTO `datos_casas` VALUES (780, 9460, 119, 0, 1000000, 0, 0, '-', 0, 9710, 119);
INSERT INTO `datos_casas` VALUES (781, 9460, 107, 0, 1000000, 0, 0, '-', 0, 9712, 107);
INSERT INTO `datos_casas` VALUES (782, 9460, 399, 0, 1000000, 0, 0, '-', 0, 9715, 399);
INSERT INTO `datos_casas` VALUES (783, 220, 84, 0, 1000000, 0, 0, '-', 0, 9617, 84);
INSERT INTO `datos_casas` VALUES (784, 8779, 178, 0, 1000000, 0, 0, '-', 0, 8870, 178);
INSERT INTO `datos_casas` VALUES (785, 8780, 196, 0, 1000000, 0, 0, '-', 0, 8876, 196);
INSERT INTO `datos_casas` VALUES (786, 8781, 294, 0, 1000000, 0, 0, '-', 0, 8875, 294);
INSERT INTO `datos_casas` VALUES (787, 8817, 295, 0, 1000000, 0, 0, '-', 0, 8877, 295);
INSERT INTO `datos_casas` VALUES (788, 8784, 235, 0, 1000000, 0, 0, '-', 0, 8873, 235);
INSERT INTO `datos_casas` VALUES (789, 8785, 76, 0, 1000000, 0, 0, '-', 0, 8872, 76);
INSERT INTO `datos_casas` VALUES (790, 8785, 155, 0, 1000000, 0, 0, '-', 0, 8878, 155);
INSERT INTO `datos_casas` VALUES (791, 8786, 271, 0, 1000000, 0, 0, '-', 0, 8879, 271);
INSERT INTO `datos_casas` VALUES (792, 8821, 83, 0, 1000000, 0, 0, '-', 0, 8883, 83);
INSERT INTO `datos_casas` VALUES (793, 8821, 218, 0, 1000000, 0, 0, '-', 0, 8881, 218);
INSERT INTO `datos_casas` VALUES (794, 8789, 172, 0, 1000000, 0, 0, '-', 0, 8885, 172);
INSERT INTO `datos_casas` VALUES (795, 8789, 111, 0, 1000000, 0, 0, '-', 0, 8884, 111);
INSERT INTO `datos_casas` VALUES (796, 8790, 444, 0, 1000000, 0, 0, '-', 0, 8888, 444);
INSERT INTO `datos_casas` VALUES (797, 8790, 309, 0, 1000000, 0, 0, '-', 0, 8887, 309);
INSERT INTO `datos_casas` VALUES (798, 8825, 198, 0, 1000000, 0, 0, '-', 0, 8889, 198);
INSERT INTO `datos_casas` VALUES (799, 8794, 127, 0, 1000000, 0, 0, '-', 0, 8890, 127);
INSERT INTO `datos_casas` VALUES (800, 8794, 282, 0, 1000000, 0, 0, '-', 0, 8892, 282);
INSERT INTO `datos_casas` VALUES (801, 8795, 250, 0, 1000000, 0, 0, '-', 0, 8893, 250);
INSERT INTO `datos_casas` VALUES (802, 8795, 235, 0, 1000000, 0, 0, '-', 0, 8894, 235);
INSERT INTO `datos_casas` VALUES (803, 8795, 286, 0, 1000000, 0, 0, '-', 0, 8896, 286);
INSERT INTO `datos_casas` VALUES (804, 8796, 370, 0, 1000000, 0, 0, '-', 0, 8898, 370);
INSERT INTO `datos_casas` VALUES (805, 8829, 271, 0, 1000000, 0, 0, '-', 0, 8899, 271);
INSERT INTO `datos_casas` VALUES (806, 8800, 161, 0, 1000000, 0, 0, '-', 0, 8900, 161);
INSERT INTO `datos_casas` VALUES (807, 8832, 163, 0, 1000000, 0, 0, '-', 0, 8902, 163);
INSERT INTO `datos_casas` VALUES (808, 8833, 235, 0, 1000000, 0, 0, '-', 0, 8904, 235);
INSERT INTO `datos_casas` VALUES (813, 10744, 307, 0, 1000000, 0, 0, '-', 0, 10859, 307);
INSERT INTO `datos_casas` VALUES (814, 10744, 274, 0, 1000000, 0, 0, '-', 0, 10860, 274);
INSERT INTO `datos_casas` VALUES (815, 10744, 214, 0, 1000000, 0, 0, '-', 0, 10861, 214);
INSERT INTO `datos_casas` VALUES (817, 10745, 227, 0, 1000000, 0, 0, '-', 0, 10864, 227);
INSERT INTO `datos_casas` VALUES (818, 10635, 301, 0, 1000000, 0, 0, '-', 0, 10866, 301);
INSERT INTO `datos_casas` VALUES (819, 10635, 195, 0, 1000000, 0, 0, '-', 0, 10867, 195);
INSERT INTO `datos_casas` VALUES (820, 10635, 336, 0, 1000000, 0, 0, '-', 0, 10868, 336);
INSERT INTO `datos_casas` VALUES (821, 10746, 220, 0, 1000000, 0, 0, '-', 0, 10870, 220);
INSERT INTO `datos_casas` VALUES (822, 10638, 419, 0, 1000000, 0, 0, '-', 0, 10871, 419);
INSERT INTO `datos_casas` VALUES (823, 10638, 223, 0, 1000000, 0, 0, '-', 0, 10872, 223);
INSERT INTO `datos_casas` VALUES (824, 10638, 154, 0, 1000000, 0, 0, '-', 0, 10873, 154);
INSERT INTO `datos_casas` VALUES (825, 10638, 176, 0, 1000000, 0, 0, '-', 0, 10874, 176);
INSERT INTO `datos_casas` VALUES (826, 10640, 328, 0, 1000000, 0, 0, '-', 0, 10876, 328);
INSERT INTO `datos_casas` VALUES (827, 10640, 223, 0, 1000000, 0, 0, '-', 0, 10877, 223);
INSERT INTO `datos_casas` VALUES (828, 10640, 350, 0, 1000000, 0, 0, '-', 0, 10878, 350);
INSERT INTO `datos_casas` VALUES (829, 10642, 448, 0, 1000000, 0, 0, '-', 0, 10879, 448);
INSERT INTO `datos_casas` VALUES (830, 10642, 186, 0, 1000000, 0, 0, '-', 0, 10880, 186);
INSERT INTO `datos_casas` VALUES (831, 10642, 326, 0, 1000000, 0, 0, '-', 0, 10882, 326);
INSERT INTO `datos_casas` VALUES (832, 10644, 337, 0, 1000000, 0, 0, '-', 0, 10884, 337);
INSERT INTO `datos_casas` VALUES (833, 10646, 228, 0, 1000000, 0, 0, '-', 0, 10886, 228);
INSERT INTO `datos_casas` VALUES (834, 10649, 365, 0, 1000000, 0, 0, '-', 0, 10887, 365);
INSERT INTO `datos_casas` VALUES (835, 10748, 370, 0, 1000000, 0, 0, '-', 0, 10888, 370);
INSERT INTO `datos_casas` VALUES (836, 10641, 356, 0, 1000000, 0, 0, '-', 0, 10889, 356);
INSERT INTO `datos_casas` VALUES (837, 10643, 216, 0, 1000000, 0, 0, '-', 0, 10891, 216);
INSERT INTO `datos_casas` VALUES (838, 10647, 455, 0, 1000000, 0, 0, '-', 0, 10892, 455);
INSERT INTO `datos_casas` VALUES (839, 10650, 183, 0, 1000000, 0, 0, '-', 0, 10893, 183);
INSERT INTO `datos_casas` VALUES (840, 10750, 299, 0, 1000000, 0, 0, '-', 0, 10895, 299);
INSERT INTO `datos_casas` VALUES (841, 10751, 204, 0, 1000000, 0, 0, '-', 0, 10896, 204);
INSERT INTO `datos_casas` VALUES (842, 10752, 326, 0, 1000000, 0, 0, '-', 0, 10897, 326);
INSERT INTO `datos_casas` VALUES (843, 10753, 211, 0, 1000000, 0, 0, '-', 0, 10898, 211);
INSERT INTO `datos_casas` VALUES (844, 10755, 296, 0, 1000000, 0, 0, '-', 0, 10899, 296);
INSERT INTO `datos_casas` VALUES (846, 10649, 312, 0, 1000000, 0, 0, '-', 0, 10905, 312);
INSERT INTO `datos_casas` VALUES (847, 10748, 147, 0, 1000000, 0, 0, '-', 0, 10906, 147);
INSERT INTO `datos_casas` VALUES (848, 10641, 216, 0, 1000000, 0, 0, '-', 0, 10907, 216);
INSERT INTO `datos_casas` VALUES (849, 10645, 257, 0, 1000000, 0, 0, '-', 0, 10908, 257);
INSERT INTO `datos_casas` VALUES (850, 10645, 409, 0, 1000000, 0, 0, '-', 0, 10909, 409);
INSERT INTO `datos_casas` VALUES (851, 10650, 265, 0, 1000000, 0, 0, '-', 0, 10910, 265);
INSERT INTO `datos_casas` VALUES (852, 10750, 204, 0, 1000000, 0, 0, '-', 0, 10911, 204);
INSERT INTO `datos_casas` VALUES (853, 10751, 328, 0, 1000000, 0, 0, '-', 0, 10912, 328);
INSERT INTO `datos_casas` VALUES (854, 10752, 199, 0, 1000000, 0, 0, '-', 0, 10913, 199);
INSERT INTO `datos_casas` VALUES (855, 10753, 373, 0, 1000000, 0, 0, '-', 0, 10914, 373);
INSERT INTO `datos_casas` VALUES (856, 10644, 360, 0, 1000000, 0, 0, '-', 0, 10915, 360);
INSERT INTO `datos_casas` VALUES (857, 10646, 283, 0, 1000000, 0, 0, '-', 0, 10916, 283);
INSERT INTO `datos_casas` VALUES (858, 10646, 352, 0, 1000000, 0, 0, '-', 0, 10917, 352);
INSERT INTO `datos_casas` VALUES (859, 10747, 207, 0, 1000000, 0, 0, '-', 0, 10918, 207);
INSERT INTO `datos_casas` VALUES (860, 10748, 407, 0, 1000000, 0, 0, '-', 0, 10919, 407);
INSERT INTO `datos_casas` VALUES (861, 10641, 396, 0, 1000000, 0, 0, '-', 0, 10920, 396);
INSERT INTO `datos_casas` VALUES (862, 10643, 365, 0, 1000000, 0, 0, '-', 0, 10921, 365);
INSERT INTO `datos_casas` VALUES (864, 10647, 330, 0, 1000000, 0, 0, '-', 0, 10923, 330);
INSERT INTO `datos_casas` VALUES (865, 10647, 394, 0, 1000000, 0, 0, '-', 0, 10924, 394);
INSERT INTO `datos_casas` VALUES (866, 10650, 412, 0, 1000000, 0, 0, '-', 0, 10925, 412);
INSERT INTO `datos_casas` VALUES (867, 10749, 136, 0, 1000000, 0, 0, '-', 0, 10926, 136);
INSERT INTO `datos_casas` VALUES (868, 10750, 354, 0, 1000000, 0, 0, '-', 0, 10927, 354);
INSERT INTO `datos_casas` VALUES (869, 10751, 180, 0, 1000000, 0, 0, '-', 0, 10928, 180);
INSERT INTO `datos_casas` VALUES (870, 10751, 361, 0, 1000000, 0, 0, '-', 0, 10929, 361);
INSERT INTO `datos_casas` VALUES (871, 10645, 152, 0, 1000000, 0, 0, '-', 0, 10931, 152);
INSERT INTO `datos_casas` VALUES (872, 10753, 352, 0, 1000000, 0, 0, '-', 0, 10932, 352);
INSERT INTO `datos_casas` VALUES (873, 10755, 235, 0, 1000000, 0, 0, '-', 0, 10933, 235);
INSERT INTO `datos_casas` VALUES (874, 10616, 411, 0, 1000000, 0, 0, '-', 0, 10991, 411);
INSERT INTO `datos_casas` VALUES (875, 10618, 170, 0, 1000000, 0, 0, '-', 0, 10996, 170);
INSERT INTO `datos_casas` VALUES (876, 10630, 324, 0, 1000000, 0, 0, '-', 0, 11001, 324);
INSERT INTO `datos_casas` VALUES (877, 10622, 431, 0, 1000000, 0, 0, '-', 0, 11006, 431);
INSERT INTO `datos_casas` VALUES (878, 10606, 162, 0, 1000000, 0, 0, '-', 0, 11011, 162);
INSERT INTO `datos_casas` VALUES (879, 10607, 249, 0, 1000000, 0, 0, '-', 0, 11016, 249);
INSERT INTO `datos_casas` VALUES (880, 10609, 422, 0, 1000000, 0, 0, '-', 0, 11021, 422);
INSERT INTO `datos_casas` VALUES (881, 10611, 201, 0, 1000000, 0, 0, '-', 0, 11026, 201);
INSERT INTO `datos_casas` VALUES (882, 10599, 440, 0, 1000000, 0, 0, '-', 0, 11030, 440);
INSERT INTO `datos_casas` VALUES (883, 10600, 439, 0, 1000000, 0, 0, '-', 0, 11034, 439);
INSERT INTO `datos_casas` VALUES (884, 10601, 448, 0, 1000000, 0, 0, '-', 0, 11038, 448);
INSERT INTO `datos_casas` VALUES (885, 10602, 161, 0, 1000000, 0, 0, '-', 0, 11042, 161);
INSERT INTO `datos_casas` VALUES (886, 10561, 161, 0, 1000000, 0, 0, '-', 0, 11046, 161);
INSERT INTO `datos_casas` VALUES (887, 10559, 433, 0, 1000000, 0, 0, '-', 0, 11050, 433);
INSERT INTO `datos_casas` VALUES (888, 10557, 295, 0, 1000000, 0, 0, '-', 0, 11054, 295);
INSERT INTO `datos_casas` VALUES (889, 10554, 422, 0, 1000000, 0, 0, '-', 0, 11058, 422);
INSERT INTO `datos_casas` VALUES (890, 10745, 296, 0, 1000000, 0, 0, '-', 0, 11130, 296);
INSERT INTO `datos_casas` VALUES (891, 10752, 251, 0, 1000000, 0, 0, '-', 0, 11226, 251);
INSERT INTO `datos_casas` VALUES (892, 10644, 256, 0, 1000000, 0, 0, '-', 0, 10904, 256);
INSERT INTO `datos_casas` VALUES (894, 11227, 298, 0, 1000000, 0, 0, '-', 0, 11272, 298);
INSERT INTO `datos_casas` VALUES (895, 11225, 78, 0, 1000000, 0, 0, '-', 0, 11273, 78);
INSERT INTO `datos_casas` VALUES (896, 11225, 134, 0, 1000000, 0, 0, '-', 0, 11274, 134);
INSERT INTO `datos_casas` VALUES (897, 11225, 212, 0, 1000000, 0, 0, '-', 0, 11280, 212);
INSERT INTO `datos_casas` VALUES (898, 11225, 397, 0, 1000000, 0, 0, '-', 0, 11283, 397);
INSERT INTO `datos_casas` VALUES (899, 11225, 458, 0, 1000000, 0, 0, '-', 0, 11284, 458);
INSERT INTO `datos_casas` VALUES (900, 11224, 208, 0, 1000000, 0, 0, '-', 0, 11285, 208);
INSERT INTO `datos_casas` VALUES (901, 11224, 273, 0, 1000000, 0, 0, '-', 0, 11286, 273);
INSERT INTO `datos_casas` VALUES (902, 11223, 307, 0, 1000000, 0, 0, '-', 0, 11287, 307);
INSERT INTO `datos_casas` VALUES (903, 11223, 210, 0, 1000000, 0, 0, '-', 0, 0, 0);
INSERT INTO `datos_casas` VALUES (904, 11223, 154, 0, 1000000, 0, 0, '-', 0, 11289, 154);
INSERT INTO `datos_casas` VALUES (905, 11223, 157, 0, 1000000, 0, 0, '-', 0, 11290, 157);
INSERT INTO `datos_casas` VALUES (906, 11222, 179, 0, 1000000, 0, 0, '-', 0, 11292, 179);
INSERT INTO `datos_casas` VALUES (907, 11222, 257, 0, 1000000, 0, 0, '-', 0, 11293, 257);
INSERT INTO `datos_casas` VALUES (908, 11222, 187, 0, 1000000, 0, 0, '-', 0, 11294, 187);
INSERT INTO `datos_casas` VALUES (909, 11221, 205, 0, 1000000, 0, 0, '-', 0, 11297, 205);
INSERT INTO `datos_casas` VALUES (910, 11221, 135, 0, 1000000, 0, 0, '-', 0, 11298, 135);
INSERT INTO `datos_casas` VALUES (911, 11221, 368, 0, 1000000, 0, 0, '-', 0, 11299, 368);
INSERT INTO `datos_casas` VALUES (912, 11219, 165, 0, 1000000, 0, 0, '-', 0, 11301, 165);
INSERT INTO `datos_casas` VALUES (913, 11219, 323, 0, 1000000, 0, 0, '-', 0, 11304, 323);
INSERT INTO `datos_casas` VALUES (914, 11219, 268, 0, 1000000, 0, 0, '-', 0, 11305, 268);
INSERT INTO `datos_casas` VALUES (915, 11219, 344, 0, 1000000, 0, 0, '-', 0, 11307, 344);
INSERT INTO `datos_casas` VALUES (916, 11217, 149, 0, 1000000, 0, 0, '-', 0, 11308, 149);
INSERT INTO `datos_casas` VALUES (917, 11217, 210, 1, 0, 0, 0, '06660___', 0, 11310, 210);
INSERT INTO `datos_casas` VALUES (918, 11217, 272, 0, 1000000, 0, 0, '-', 0, 11313, 272);
INSERT INTO `datos_casas` VALUES (919, 11217, 424, 0, 1000000, 0, 0, '-', 0, 11314, 424);
INSERT INTO `datos_casas` VALUES (920, 11228, 267, 0, 1000000, 0, 0, '-', 0, 11316, 267);
INSERT INTO `datos_casas` VALUES (921, 11228, 357, 0, 1000000, 0, 0, '-', 0, 11318, 357);
INSERT INTO `datos_casas` VALUES (922, 11228, 462, 0, 1000000, 0, 0, '-', 0, 11319, 462);
INSERT INTO `datos_casas` VALUES (923, 11230, 154, 0, 1000000, 0, 0, '-', 0, 11321, 154);
INSERT INTO `datos_casas` VALUES (924, 11230, 208, 0, 1000000, 0, 0, '-', 0, 11324, 208);
INSERT INTO `datos_casas` VALUES (925, 11231, 135, 0, 1000000, 0, 0, '-', 0, 11325, 135);
INSERT INTO `datos_casas` VALUES (926, 11231, 205, 0, 1000000, 0, 0, '-', 0, 11328, 205);
INSERT INTO `datos_casas` VALUES (927, 11231, 229, 0, 1000000, 0, 0, '-', 0, 11330, 229);
INSERT INTO `datos_casas` VALUES (928, 11231, 390, 0, 1000000, 0, 0, '-', 0, 11331, 390);
INSERT INTO `datos_casas` VALUES (929, 11232, 209, 0, 1000000, 0, 0, '-', 0, 11333, 209);
INSERT INTO `datos_casas` VALUES (930, 11232, 198, 0, 1000000, 0, 0, '-', 0, 11336, 198);
INSERT INTO `datos_casas` VALUES (931, 11233, 150, 0, 1000000, 0, 0, '-', 0, 11338, 150);
INSERT INTO `datos_casas` VALUES (932, 11233, 198, 0, 1000000, 0, 0, '-', 0, 11341, 198);
INSERT INTO `datos_casas` VALUES (933, 11186, 298, 0, 1000000, 0, 0, '-', 0, 11344, 298);
INSERT INTO `datos_casas` VALUES (934, 11184, 209, 0, 1000000, 0, 0, '-', 0, 11346, 209);
INSERT INTO `datos_casas` VALUES (935, 11184, 198, 0, 1000000, 0, 0, '-', 0, 11349, 198);
INSERT INTO `datos_casas` VALUES (936, 11187, 273, 0, 1000000, 0, 0, '-', 0, 11350, 273);
INSERT INTO `datos_casas` VALUES (937, 11187, 323, 0, 1000000, 0, 0, '-', 0, 11353, 323);
INSERT INTO `datos_casas` VALUES (938, 11189, 78, 0, 1000000, 0, 0, '-', 0, 11354, 78);
INSERT INTO `datos_casas` VALUES (939, 11189, 134, 0, 1000000, 0, 0, '-', 0, 11355, 134);
INSERT INTO `datos_casas` VALUES (940, 11189, 212, 0, 1000000, 0, 0, '-', 0, 11358, 212);
INSERT INTO `datos_casas` VALUES (941, 11189, 272, 0, 1000000, 0, 0, '-', 0, 11359, 272);
INSERT INTO `datos_casas` VALUES (942, 11189, 458, 0, 1000000, 0, 0, '-', 0, 11360, 458);
INSERT INTO `datos_casas` VALUES (943, 11189, 397, 0, 1000000, 0, 0, '-', 0, 11362, 397);
INSERT INTO `datos_casas` VALUES (944, 11191, 149, 0, 1000000, 0, 0, '-', 0, 11363, 149);
INSERT INTO `datos_casas` VALUES (945, 11191, 272, 0, 1000000, 0, 0, '-', 0, 11366, 272);
INSERT INTO `datos_casas` VALUES (946, 11191, 432, 0, 1000000, 0, 0, '-', 0, 11367, 432);
INSERT INTO `datos_casas` VALUES (947, 11191, 424, 0, 1000000, 0, 0, '-', 0, 11368, 424);
INSERT INTO `datos_casas` VALUES (948, 11181, 292, 0, 1000000, 0, 0, '-', 0, 11369, 292);
INSERT INTO `datos_casas` VALUES (949, 11181, 184, 0, 1000000, 0, 0, '-', 0, 11372, 184);
INSERT INTO `datos_casas` VALUES (950, 11181, 158, 0, 1000000, 0, 0, '-', 0, 11373, 158);
INSERT INTO `datos_casas` VALUES (951, 11179, 237, 0, 1000000, 0, 0, '-', 0, 11376, 237);
INSERT INTO `datos_casas` VALUES (952, 11179, 371, 0, 1000000, 0, 0, '-', 0, 11377, 371);
INSERT INTO `datos_casas` VALUES (953, 11179, 431, 0, 1000000, 0, 0, '-', 0, 11378, 431);
INSERT INTO `datos_casas` VALUES (954, 11180, 185, 0, 1000000, 0, 0, '-', 0, 11381, 185);
INSERT INTO `datos_casas` VALUES (955, 11180, 149, 0, 1000000, 0, 0, '-', 0, 11383, 149);
INSERT INTO `datos_casas` VALUES (956, 11180, 398, 0, 1000000, 0, 0, '-', 0, 11384, 398);
INSERT INTO `datos_casas` VALUES (957, 11178, 340, 0, 1000000, 0, 0, '-', 0, 11386, 340);
INSERT INTO `datos_casas` VALUES (958, 11178, 402, 0, 1000000, 0, 0, '-', 0, 11388, 402);
INSERT INTO `datos_casas` VALUES (959, 11178, 285, 0, 1000000, 0, 0, '-', 0, 11389, 285);
INSERT INTO `datos_casas` VALUES (960, 11178, 78, 0, 1000000, 0, 0, '-', 0, 11390, 78);
INSERT INTO `datos_casas` VALUES (961, 11176, 268, 0, 1000000, 0, 0, '-', 0, 11393, 268);
INSERT INTO `datos_casas` VALUES (962, 11176, 357, 0, 1000000, 0, 0, '-', 0, 11394, 357);
INSERT INTO `datos_casas` VALUES (963, 11176, 462, 0, 1000000, 0, 0, '-', 0, 11395, 462);
INSERT INTO `datos_casas` VALUES (964, 11175, 284, 0, 1000000, 0, 0, '-', 0, 11398, 284);
INSERT INTO `datos_casas` VALUES (965, 11175, 177, 0, 1000000, 0, 0, '-', 0, 11399, 177);
INSERT INTO `datos_casas` VALUES (966, 11174, 187, 0, 1000000, 0, 0, '-', 0, 11401, 187);
INSERT INTO `datos_casas` VALUES (967, 11174, 257, 0, 1000000, 0, 0, '-', 0, 11402, 257);
INSERT INTO `datos_casas` VALUES (968, 11174, 179, 0, 1000000, 0, 0, '-', 0, 11404, 179);
INSERT INTO `datos_casas` VALUES (969, 11172, 157, 0, 1000000, 0, 0, '-', 0, 11405, 157);
INSERT INTO `datos_casas` VALUES (970, 11172, 210, 0, 1000000, 0, 0, '-', 0, 11408, 210);
INSERT INTO `datos_casas` VALUES (971, 11172, 307, 0, 1000000, 0, 0, '-', 0, 11409, 307);
INSERT INTO `datos_casas` VALUES (972, 11204, 253, 0, 1000000, 0, 0, '-', 0, 11412, 253);
INSERT INTO `datos_casas` VALUES (973, 11203, 205, 0, 1000000, 0, 0, '-', 0, 11415, 205);
INSERT INTO `datos_casas` VALUES (974, 11203, 135, 0, 1000000, 0, 0, '-', 0, 11416, 135);
INSERT INTO `datos_casas` VALUES (975, 11203, 229, 0, 1000000, 0, 0, '-', 0, 11418, 229);
INSERT INTO `datos_casas` VALUES (976, 11203, 390, 0, 1000000, 0, 0, '-', 0, 11419, 390);
INSERT INTO `datos_casas` VALUES (977, 11200, 293, 0, 1000000, 0, 0, '-', 0, 11420, 293);
INSERT INTO `datos_casas` VALUES (978, 11200, 198, 0, 1000000, 0, 0, '-', 0, 11421, 198);
INSERT INTO `datos_casas` VALUES (979, 11200, 157, 0, 1000000, 0, 0, '-', 0, 11422, 157);
INSERT INTO `datos_casas` VALUES (980, 11199, 292, 0, 1000000, 0, 0, '-', 0, 11423, 292);
INSERT INTO `datos_casas` VALUES (981, 11199, 211, 0, 1000000, 0, 0, '-', 0, 11425, 211);
INSERT INTO `datos_casas` VALUES (982, 11196, 78, 0, 1000000, 0, 0, '-', 0, 11426, 78);
INSERT INTO `datos_casas` VALUES (983, 11196, 134, 0, 1000000, 0, 0, '-', 0, 11427, 134);
INSERT INTO `datos_casas` VALUES (984, 11196, 212, 0, 1000000, 0, 0, '-', 0, 11430, 212);
INSERT INTO `datos_casas` VALUES (985, 11196, 397, 0, 1000000, 0, 0, '-', 0, 11432, 397);
INSERT INTO `datos_casas` VALUES (986, 11196, 458, 0, 1000000, 0, 0, '-', 0, 11433, 458);
INSERT INTO `datos_casas` VALUES (987, 11196, 272, 0, 1000000, 0, 0, '-', 0, 11434, 272);
INSERT INTO `datos_casas` VALUES (988, 11195, 462, 0, 1000000, 0, 0, '-', 0, 11435, 462);
INSERT INTO `datos_casas` VALUES (989, 11195, 357, 0, 1000000, 0, 0, '-', 0, 11436, 357);
INSERT INTO `datos_casas` VALUES (990, 11195, 268, 0, 1000000, 0, 0, '-', 0, 11439, 268);
INSERT INTO `datos_casas` VALUES (991, 11193, 325, 0, 1000000, 0, 0, '-', 0, 11441, 325);
INSERT INTO `datos_casas` VALUES (992, 11193, 78, 0, 1000000, 0, 0, '-', 0, 11442, 78);
INSERT INTO `datos_casas` VALUES (993, 11205, 340, 0, 1000000, 0, 0, '-', 0, 11444, 340);
INSERT INTO `datos_casas` VALUES (994, 11205, 78, 0, 1000000, 0, 0, '-', 0, 11445, 78);
INSERT INTO `datos_casas` VALUES (995, 11207, 444, 0, 1000000, 0, 0, '-', 0, 11446, 444);
INSERT INTO `datos_casas` VALUES (996, 11207, 224, 0, 1000000, 0, 0, '-', 0, 11448, 224);
INSERT INTO `datos_casas` VALUES (997, 11207, 185, 0, 1000000, 0, 0, '-', 0, 11451, 185);
INSERT INTO `datos_casas` VALUES (998, 11207, 245, 0, 1000000, 0, 0, '-', 0, 11452, 245);
INSERT INTO `datos_casas` VALUES (999, 11209, 273, 0, 1000000, 0, 0, '-', 0, 11453, 273);
INSERT INTO `datos_casas` VALUES (1000, 11209, 208, 0, 1000000, 0, 0, '-', 0, 11454, 208);
INSERT INTO `datos_casas` VALUES (1001, 11209, 321, 0, 1000000, 0, 0, '-', 0, 11456, 321);
INSERT INTO `datos_casas` VALUES (1002, 11211, 241, 0, 1000000, 0, 0, '-', 0, 11457, 241);
INSERT INTO `datos_casas` VALUES (1003, 11211, 251, 0, 1000000, 0, 0, '-', 0, 11459, 251);
INSERT INTO `datos_casas` VALUES (1004, 11213, 209, 0, 1000000, 0, 0, '-', 0, 11461, 209);
INSERT INTO `datos_casas` VALUES (1005, 11213, 198, 0, 1000000, 0, 0, '-', 0, 11464, 198);
INSERT INTO `datos_casas` VALUES (1006, 11216, 192, 0, 1000000, 0, 0, '-', 0, 11465, 192);
INSERT INTO `datos_casas` VALUES (1007, 11216, 211, 0, 1000000, 0, 0, '-', 0, 11467, 211);
INSERT INTO `datos_casas` VALUES (1008, 11216, 158, 0, 1000000, 0, 0, '-', 0, 11468, 158);
INSERT INTO `datos_casas` VALUES (1009, 11150, 370, 0, 1000000, 0, 0, '-', 0, 11469, 370);
INSERT INTO `datos_casas` VALUES (1010, 11150, 208, 0, 1000000, 0, 0, '-', 0, 11472, 208);
INSERT INTO `datos_casas` VALUES (1011, 11150, 154, 0, 1000000, 0, 0, '-', 0, 11474, 154);
INSERT INTO `datos_casas` VALUES (1012, 11146, 397, 0, 1000000, 0, 0, '-', 0, 11476, 397);
INSERT INTO `datos_casas` VALUES (1013, 11146, 458, 0, 1000000, 0, 0, '-', 0, 11477, 458);
INSERT INTO `datos_casas` VALUES (1014, 11146, 212, 0, 1000000, 0, 0, '-', 0, 11480, 212);
INSERT INTO `datos_casas` VALUES (1015, 11146, 134, 0, 1000000, 0, 0, '-', 0, 11481, 134);
INSERT INTO `datos_casas` VALUES (1016, 11146, 78, 0, 1000000, 0, 0, '-', 0, 11542, 78);
INSERT INTO `datos_casas` VALUES (1017, 11143, 453, 0, 1000000, 0, 0, '-', 0, 11485, 453);
INSERT INTO `datos_casas` VALUES (1018, 11143, 205, 0, 1000000, 0, 0, '-', 0, 11488, 205);
INSERT INTO `datos_casas` VALUES (1019, 11143, 135, 0, 1000000, 0, 0, '-', 0, 11489, 135);
INSERT INTO `datos_casas` VALUES (1020, 11143, 229, 0, 1000000, 0, 0, '-', 0, 11492, 229);
INSERT INTO `datos_casas` VALUES (1021, 11143, 390, 0, 1000000, 0, 0, '-', 0, 11493, 390);
INSERT INTO `datos_casas` VALUES (1022, 11138, 424, 0, 1000000, 0, 0, '-', 0, 11494, 424);
INSERT INTO `datos_casas` VALUES (1023, 11138, 432, 0, 1000000, 0, 0, '-', 0, 11495, 432);
INSERT INTO `datos_casas` VALUES (1024, 11138, 272, 0, 1000000, 0, 0, '-', 0, 11498, 272);
INSERT INTO `datos_casas` VALUES (1025, 11138, 149, 0, 1000000, 0, 0, '-', 0, 11499, 149);
INSERT INTO `datos_casas` VALUES (1026, 11137, 323, 0, 1000000, 0, 0, '-', 0, 11502, 323);
INSERT INTO `datos_casas` VALUES (1027, 11137, 344, 0, 1000000, 0, 0, '-', 0, 11504, 344);
INSERT INTO `datos_casas` VALUES (1028, 11151, 307, 0, 1000000, 0, 0, '-', 0, 11505, 307);
INSERT INTO `datos_casas` VALUES (1029, 11151, 325, 0, 1000000, 0, 0, '-', 0, 11507, 325);
INSERT INTO `datos_casas` VALUES (1030, 11151, 157, 0, 1000000, 0, 0, '-', 0, 11508, 157);
INSERT INTO `datos_casas` VALUES (1031, 11153, 292, 0, 1000000, 0, 0, '-', 0, 11509, 292);
INSERT INTO `datos_casas` VALUES (1032, 11153, 211, 0, 1000000, 0, 0, '-', 0, 11511, 211);
INSERT INTO `datos_casas` VALUES (1033, 11153, 158, 0, 1000000, 0, 0, '-', 0, 11512, 158);
INSERT INTO `datos_casas` VALUES (1034, 11158, 177, 0, 1000000, 0, 0, '-', 0, 11513, 177);
INSERT INTO `datos_casas` VALUES (1035, 11158, 52, 0, 1000000, 0, 0, '-', 0, 11514, 52);
INSERT INTO `datos_casas` VALUES (1036, 11158, 269, 0, 1000000, 0, 0, '-', 0, 11516, 269);
INSERT INTO `datos_casas` VALUES (1037, 11159, 387, 0, 1000000, 0, 0, '-', 0, 11521, 387);
INSERT INTO `datos_casas` VALUES (1038, 11159, 340, 0, 1000000, 0, 0, '-', 0, 11523, 340);
INSERT INTO `datos_casas` VALUES (1039, 11159, 78, 0, 1000000, 0, 0, '-', 0, 11524, 78);
INSERT INTO `datos_casas` VALUES (1040, 11160, 444, 0, 1000000, 0, 0, '-', 0, 11525, 444);
INSERT INTO `datos_casas` VALUES (1041, 11160, 245, 0, 1000000, 0, 0, '-', 0, 11526, 245);
INSERT INTO `datos_casas` VALUES (1042, 11160, 185, 0, 1000000, 0, 0, '-', 0, 11529, 185);
INSERT INTO `datos_casas` VALUES (1043, 11162, 238, 0, 1000000, 0, 0, '-', 0, 11530, 238);
INSERT INTO `datos_casas` VALUES (1044, 11162, 193, 0, 1000000, 0, 0, '-', 0, 11531, 193);
INSERT INTO `datos_casas` VALUES (1045, 11163, 187, 0, 1000000, 0, 0, '-', 0, 11532, 187);
INSERT INTO `datos_casas` VALUES (1046, 11163, 257, 0, 1000000, 0, 0, '-', 0, 11533, 257);
INSERT INTO `datos_casas` VALUES (1047, 11163, 179, 0, 1000000, 0, 0, '-', 0, 11535, 179);
INSERT INTO `datos_casas` VALUES (1048, 11166, 198, 0, 1000000, 0, 0, '-', 0, 11538, 198);
INSERT INTO `datos_casas` VALUES (1049, 11166, 209, 0, 1000000, 0, 0, '-', 0, 11540, 209);
INSERT INTO `datos_casas` VALUES (1050, 11146, 272, 0, 1000000, 0, 0, '-', 0, 11236, 272);
INSERT INTO `datos_casas` VALUES (1051, 9015, 207, 16, 0, 0, 0, '-', 0, 10901, 207);
INSERT INTO `datos_casas` VALUES (1052, 4590, 428, 0, 1000000, 0, 0, '-', 0, 11601, 428);
INSERT INTO `datos_casas` VALUES (1053, 4590, 394, 0, 1000000, 0, 0, '-', 0, 11605, 394);
INSERT INTO `datos_casas` VALUES (1054, 8756, 218, 0, 1000000, 0, 0, '-', 0, 11609, 218);
INSERT INTO `datos_casas` VALUES (1055, 8753, 45, 0, 1000000, 0, 0, '-', 0, 11626, 45);
INSERT INTO `datos_casas` VALUES (1056, 8754, 124, 0, 1000000, 0, 0, '-', 0, 11630, 124);
INSERT INTO `datos_casas` VALUES (1057, 8756, 279, 0, 1000000, 0, 0, '-', 0, 11611, 279);
INSERT INTO `datos_casas` VALUES (1058, 8756, 723, 0, 1000000, 0, 0, '-', 0, 11613, 723);
INSERT INTO `datos_casas` VALUES (1059, 8755, 150, 0, 1000000, 0, 0, '-', 0, 11615, 150);
INSERT INTO `datos_casas` VALUES (1060, 8755, 78, 0, 1000000, 0, 0, '-', 0, 11617, 78);
INSERT INTO `datos_casas` VALUES (1061, 8752, 649, 0, 1000000, 0, 0, '-', 0, 11619, 649);
INSERT INTO `datos_casas` VALUES (1062, 8749, 755, 0, 1000000, 0, 0, '-', 0, 11622, 755);
INSERT INTO `datos_casas` VALUES (1063, 8753, 705, 0, 1000000, 0, 0, '-', 0, 11624, 705);
INSERT INTO `datos_casas` VALUES (1064, 8753, 729, 0, 1000000, 0, 0, '-', 0, 11628, 729);
INSERT INTO `datos_casas` VALUES (1065, 8754, 274, 0, 1000000, 0, 0, '-', 0, 11633, 274);
INSERT INTO `datos_casas` VALUES (1066, 8754, 714, 0, 1000000, 0, 0, '-', 0, 11636, 714);
INSERT INTO `datos_casas` VALUES (1067, 4591, 193, 0, 1000000, 0, 0, '-', 0, 11638, 193);
INSERT INTO `datos_casas` VALUES (1068, 4621, 246, 0, 1000000, 0, 0, '-', 0, 11640, 246);
INSERT INTO `datos_casas` VALUES (1069, 4621, 681, 0, 1000000, 0, 0, '-', 0, 11642, 681);
INSERT INTO `datos_casas` VALUES (1070, 4621, 164, 0, 1000000, 0, 0, '-', 0, 11645, 164);
INSERT INTO `datos_casas` VALUES (1071, 4627, 273, 0, 1000000, 0, 0, '-', 0, 11647, 273);
INSERT INTO `datos_casas` VALUES (1072, 4598, 414, 0, 1000000, 0, 0, '-', 0, 11653, 414);
INSERT INTO `datos_casas` VALUES (1073, 4583, 601, 0, 1000000, 0, 0, '-', 0, 11656, 601);
INSERT INTO `datos_casas` VALUES (1074, 4583, 146, 0, 1000000, 0, 0, '-', 0, 11659, 146);
INSERT INTO `datos_casas` VALUES (1075, 4583, 51, 0, 1000000, 0, 0, '-', 0, 11662, 51);
INSERT INTO `datos_casas` VALUES (1076, 5147, 419, 0, 1000000, 0, 0, '-', 0, 11664, 419);
INSERT INTO `datos_casas` VALUES (1077, 4607, 331, 0, 1000000, 0, 0, '-', 0, 11670, 331);
INSERT INTO `datos_casas` VALUES (1078, 4607, 279, 0, 1000000, 0, 0, '-', 0, 11676, 279);
INSERT INTO `datos_casas` VALUES (1079, 4551, 293, 0, 1000000, 0, 0, '-', 0, 11679, 293);
INSERT INTO `datos_casas` VALUES (1080, 4629, 331, 0, 1000000, 0, 0, '-', 0, 11686, 331);
INSERT INTO `datos_casas` VALUES (1081, 4609, 658, 0, 1000000, 0, 0, '-', 0, 11688, 658);
INSERT INTO `datos_casas` VALUES (1082, 4609, 581, 0, 1000000, 0, 0, '-', 0, 11691, 581);
INSERT INTO `datos_casas` VALUES (1083, 4931, 347, 0, 1000000, 0, 0, '-', 0, 11698, 347);
INSERT INTO `datos_casas` VALUES (1084, 5127, 271, 0, 1000000, 0, 0, '-', 0, 11701, 271);
INSERT INTO `datos_casas` VALUES (1085, 5127, 581, 0, 1000000, 0, 0, '-', 0, 11703, 581);
INSERT INTO `datos_casas` VALUES (1086, 5133, 88, 0, 1000000, 0, 0, '-', 0, 11705, 88);
INSERT INTO `datos_casas` VALUES (1087, 5295, 447, 0, 1000000, 0, 0, '-', 0, 11709, 447);
INSERT INTO `datos_casas` VALUES (1088, 5295, 347, 0, 1000000, 0, 0, '-', 0, 11711, 347);
INSERT INTO `datos_casas` VALUES (1089, 5278, 194, 0, 1000000, 0, 0, '-', 0, 11715, 194);
INSERT INTO `datos_casas` VALUES (1090, 5334, 94, 0, 1000000, 0, 0, '-', 0, 11717, 94);
INSERT INTO `datos_casas` VALUES (1091, 5334, 348, 0, 1000000, 0, 0, '-', 0, 11722, 348);
INSERT INTO `datos_casas` VALUES (1092, 4932, 670, 0, 1000000, 0, 0, '-', 0, 11724, 670);
INSERT INTO `datos_casas` VALUES (1093, 4562, 624, 0, 1000000, 0, 0, '-', 0, 11731, 624);
INSERT INTO `datos_casas` VALUES (1094, 5332, 279, 0, 1000000, 0, 0, '-', 0, 11735, 279);
INSERT INTO `datos_casas` VALUES (1095, 5333, 394, 0, 1000000, 0, 0, '-', 0, 11737, 394);
INSERT INTO `datos_casas` VALUES (1096, 4617, 528, 0, 1000000, 0, 0, '-', 0, 11741, 528);
INSERT INTO `datos_casas` VALUES (1097, 4617, 625, 0, 1000000, 0, 0, '-', 0, 11745, 625);
INSERT INTO `datos_casas` VALUES (1098, 5112, 469, 0, 1000000, 0, 0, '-', 0, 11749, 469);
INSERT INTO `datos_casas` VALUES (1099, 5112, 289, 0, 1000000, 0, 0, '-', 0, 11751, 289);
INSERT INTO `datos_casas` VALUES (1100, 5111, 644, 0, 1000000, 0, 0, '-', 0, 11753, 644);
INSERT INTO `datos_casas` VALUES (1101, 5111, 183, 0, 1000000, 0, 0, '-', 0, 11755, 183);
INSERT INTO `datos_casas` VALUES (1102, 4690, 304, 0, 1000000, 0, 0, '-', 0, 11757, 304);
INSERT INTO `datos_casas` VALUES (1103, 4690, 418, 0, 1000000, 0, 0, '-', 0, 11759, 418);
INSERT INTO `datos_casas` VALUES (1104, 5113, 530, 0, 1000000, 0, 0, '-', 0, 11761, 530);
INSERT INTO `datos_casas` VALUES (1105, 4619, 430, 0, 1000000, 0, 0, '-', 0, 11769, 430);
INSERT INTO `datos_casas` VALUES (1107, 5147, 168, 0, 1000000, 0, 0, '-', 0, 11667, 168);
INSERT INTO `datos_casas` VALUES (1108, 4607, 163, 0, 1000000, 0, 0, '-', 0, 11674, 163);
INSERT INTO `datos_casas` VALUES (1109, 4582, 225, 0, 1000000, 0, 0, '-', 0, 11683, 225);
INSERT INTO `datos_casas` VALUES (1110, 4609, 680, 0, 1000000, 0, 0, '-', 0, 11693, 680);
INSERT INTO `datos_casas` VALUES (1111, 4609, 626, 0, 1000000, 0, 0, '-', 0, 11695, 626);
INSERT INTO `datos_casas` VALUES (1112, 5133, 430, 0, 1000000, 0, 0, '-', 0, 11707, 430);
INSERT INTO `datos_casas` VALUES (1113, 5334, 447, 0, 1000000, 0, 0, '-', 0, 11719, 447);
INSERT INTO `datos_casas` VALUES (1114, 4932, 241, 0, 1000000, 0, 0, '-', 0, 11726, 241);
INSERT INTO `datos_casas` VALUES (1115, 4932, 543, 0, 1000000, 0, 0, '-', 0, 11728, 543);
INSERT INTO `datos_casas` VALUES (1116, 5332, 170, 0, 1000000, 0, 0, '-', 0, 11733, 170);
INSERT INTO `datos_casas` VALUES (1117, 5333, 107, 0, 1000000, 0, 0, '-', 0, 11739, 107);
INSERT INTO `datos_casas` VALUES (1118, 4617, 585, 0, 1000000, 0, 0, '-', 0, 11747, 585);
INSERT INTO `datos_casas` VALUES (1119, 5113, 381, 0, 1000000, 0, 0, '-', 0, 11763, 381);
INSERT INTO `datos_casas` VALUES (1120, 5113, 131, 0, 1000000, 0, 0, '-', 0, 11767, 131);
INSERT INTO `datos_casas` VALUES (1121, 8848, 723, 0, 1000000, 0, 0, '-', 0, 11770, 723);
INSERT INTO `datos_casas` VALUES (1122, 8848, 651, 0, 1000000, 0, 0, '-', 0, 11773, 651);
INSERT INTO `datos_casas` VALUES (1123, 8743, 132, 0, 1000000, 0, 0, '-', 0, 11774, 132);
INSERT INTO `datos_casas` VALUES (1124, 8744, 126, 0, 1000000, 0, 0, '-', 0, 11775, 126);
INSERT INTO `datos_casas` VALUES (1125, 8744, 220, 0, 1000000, 0, 0, '-', 0, 11778, 220);
INSERT INTO `datos_casas` VALUES (1126, 8747, 108, 0, 1000000, 0, 0, '-', 0, 11781, 108);
INSERT INTO `datos_casas` VALUES (1127, 8745, 743, 0, 1000000, 0, 0, '-', 0, 11782, 743);
INSERT INTO `datos_casas` VALUES (1128, 8746, 241, 0, 1000000, 0, 0, '-', 0, 11786, 241);
INSERT INTO `datos_casas` VALUES (1129, 8746, 169, 0, 1000000, 0, 0, '-', 0, 11783, 169);
INSERT INTO `datos_casas` VALUES (1130, 8746, 115, 0, 1000000, 0, 0, '-', 0, 11789, 115);
INSERT INTO `datos_casas` VALUES (1131, 8746, 79, 0, 1000000, 0, 0, '-', 0, 11792, 79);
INSERT INTO `datos_casas` VALUES (1132, 8760, 143, 0, 1000000, 0, 0, '-', 0, 11799, 143);
INSERT INTO `datos_casas` VALUES (1133, 8760, 200, 0, 1000000, 0, 0, '-', 0, 11795, 200);
INSERT INTO `datos_casas` VALUES (1134, 8760, 257, 0, 1000000, 0, 0, '-', 0, 11798, 257);
INSERT INTO `datos_casas` VALUES (1135, 8757, 123, 0, 1000000, 0, 0, '-', 0, 11800, 123);
INSERT INTO `datos_casas` VALUES (1136, 8757, 180, 0, 1000000, 0, 0, '-', 0, 11803, 180);
INSERT INTO `datos_casas` VALUES (1137, 8758, 658, 0, 1000000, 0, 0, '-', 0, 11807, 658);
INSERT INTO `datos_casas` VALUES (1138, 8758, 715, 0, 1000000, 0, 0, '-', 0, 11808, 715);
INSERT INTO `datos_casas` VALUES (1139, 8759, 243, 0, 1000000, 0, 0, '-', 0, 11812, 243);
INSERT INTO `datos_casas` VALUES (1140, 9270, 189, 0, 1000000, 0, 0, '-', 0, 11818, 189);
INSERT INTO `datos_casas` VALUES (1141, 9269, 432, 0, 1000000, 0, 0, '-', 0, 11811, 432);
INSERT INTO `datos_casas` VALUES (1142, 8759, 366, 0, 1000000, 0, 0, '-', 0, 11815, 366);
INSERT INTO `datos_casas` VALUES (1143, 9268, 366, 0, 1000000, 0, 0, '-', 0, 11821, 366);
INSERT INTO `datos_casas` VALUES (1144, 9272, 487, 0, 1000000, 0, 0, '-', 0, 11824, 487);
INSERT INTO `datos_casas` VALUES (1145, 9294, 414, 0, 1000000, 0, 0, '-', 0, 11829, 414);
INSERT INTO `datos_casas` VALUES (1146, 9294, 498, 0, 1000000, 0, 0, '-', 0, 11832, 498);
INSERT INTO `datos_casas` VALUES (1147, 9294, 604, 0, 1000000, 0, 0, '-', 0, 11828, 604);
INSERT INTO `datos_casas` VALUES (1148, 9274, 124, 0, 1000000, 0, 0, '-', 0, 11833, 124);
INSERT INTO `datos_casas` VALUES (1149, 9274, 181, 0, 1000000, 0, 0, '-', 0, 11836, 181);
INSERT INTO `datos_casas` VALUES (1150, 9295, 542, 0, 1000000, 0, 0, '-', 0, 11839, 542);
INSERT INTO `datos_casas` VALUES (1151, 9295, 187, 0, 1000000, 0, 0, '-', 0, 11842, 187);
INSERT INTO `datos_casas` VALUES (1152, 9278, 143, 0, 1000000, 0, 0, '-', 0, 11843, 143);
INSERT INTO `datos_casas` VALUES (1153, 9278, 200, 0, 1000000, 0, 0, '-', 0, 11846, 200);
INSERT INTO `datos_casas` VALUES (1154, 9278, 257, 0, 1000000, 0, 0, '-', 0, 11849, 257);
INSERT INTO `datos_casas` VALUES (1155, 9277, 366, 0, 1000000, 0, 0, '-', 0, 11853, 366);
INSERT INTO `datos_casas` VALUES (1156, 9296, 316, 0, 1000000, 0, 0, '-', 0, 11856, 316);
INSERT INTO `datos_casas` VALUES (1157, 8757, 47, 0, 1000000, 0, 0, '-', 0, 11806, 47);
INSERT INTO `datos_casas` VALUES (1158, 9272, 199, 0, 1000000, 0, 0, '-', 0, 11827, 199);
INSERT INTO `datos_casas` VALUES (1159, 4627, 94, 0, 1000000, 0, 0, '-', 0, 0, 0);
INSERT INTO `datos_casas` VALUES (1160, 9277, 243, 0, 1000000, 0, 0, '-', 0, 11850, 243);
INSERT INTO `datos_casas` VALUES (1161, 4335, 112, 0, 1000000, 0, 0, '-', 0, 11867, 112);
INSERT INTO `datos_casas` VALUES (1162, 4335, 78, 0, 1000000, 0, 0, '-', 0, 11868, 78);
INSERT INTO `datos_casas` VALUES (1163, 5311, 82, 0, 1000000, 0, 0, '-', 0, 11870, 82);
INSERT INTO `datos_casas` VALUES (1164, 5311, 217, 0, 1000000, 0, 0, '-', 0, 11873, 217);

-- ----------------------------
-- Table structure for datos_cercados
-- ----------------------------
DROP TABLE IF EXISTS `datos_cercados`;
CREATE TABLE `datos_cercados`  (
  `mapa` int(11) NOT NULL,
  `celda` int(11) NOT NULL,
  `tamaño` int(11) NOT NULL,
  `dueño` int(11) NOT NULL,
  `gremio` int(11) NOT NULL DEFAULT -1,
  `precio` int(11) NOT NULL,
  `monturas` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  PRIMARY KEY (`mapa`) USING BTREE
) ENGINE = MyISAM CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of datos_cercados
-- ----------------------------
INSERT INTO `datos_cercados` VALUES (8760, 753, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (2221, 242, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4308, 507, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8848, 549, 5, -1, -1, 0, '');
INSERT INTO `datos_cercados` VALUES (8744, 579, 5, -1, -1, 0, '');
INSERT INTO `datos_cercados` VALUES (8743, 605, 5, -1, -1, 0, '');
INSERT INTO `datos_cercados` VALUES (9748, 356, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9747, 78, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9746, 169, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8747, 615, 5, -1, -1, 0, '');
INSERT INTO `datos_cercados` VALUES (9745, 168, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9744, 164, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9743, 208, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9736, 194, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9737, 243, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9738, 109, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9739, 388, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9740, 178, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9741, 298, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9728, 359, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9742, 214, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9732, 112, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8746, 208, 5, -1, -1, 0, '');
INSERT INTO `datos_cercados` VALUES (9733, 345, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9734, 279, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8745, 235, 5, -1, -1, 0, '');
INSERT INTO `datos_cercados` VALUES (9735, 342, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9356, 399, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8752, 602, 5, -1, -1, 0, '');
INSERT INTO `datos_cercados` VALUES (9357, 294, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9354, 264, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9353, 324, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9355, 308, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9358, 252, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9352, 112, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9729, 355, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9730, 282, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9731, 279, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9726, 343, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (3727, 366, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (10249, 164, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9349, 354, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9350, 125, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9346, 226, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9345, 226, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9725, 199, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9342, 327, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (10561, 380, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (3705, 354, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (3663, 344, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (3704, 382, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (3672, 196, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (3713, 212, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (3714, 342, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (3673, 149, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (3187, 268, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (3112, 323, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (3113, 235, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (3328, 381, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (3367, 368, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (3119, 314, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4432, 211, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4436, 425, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (3817, 151, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (3782, 384, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (3737, 324, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (3736, 323, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (3781, 353, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (3816, 382, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4547, 316, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4545, 185, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4544, 94, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4546, 127, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4485, 300, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4489, 127, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4468, 93, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4471, 353, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4585, 294, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4581, 315, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4491, 211, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4488, 308, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (10559, 380, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (10557, 287, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (10554, 374, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (10602, 365, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (10601, 366, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (10600, 373, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (10599, 293, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (10606, 308, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (10607, 325, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (10609, 331, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (10611, 381, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (10622, 323, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (10630, 412, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (10618, 380, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8598, 374, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8604, 337, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8564, 168, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8567, 323, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8570, 299, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8610, 236, 5, 1, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8607, 354, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4966, 169, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4967, 119, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4634, 268, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4757, 324, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4806, 342, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4805, 381, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4809, 357, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4810, 358, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4705, 367, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4711, 323, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4729, 338, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4723, 134, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8750, 468, 5, -1, -1, 0, '');
INSERT INTO `datos_cercados` VALUES (8851, 578, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8749, 614, 5, -1, -1, 0, '');
INSERT INTO `datos_cercados` VALUES (8748, 550, 5, -1, -1, 0, '');
INSERT INTO `datos_cercados` VALUES (8751, 356, 5, -1, -1, 0, '');
INSERT INTO `datos_cercados` VALUES (9450, 128, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9449, 268, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9451, 442, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9455, 416, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9453, 381, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9456, 181, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9457, 284, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9458, 401, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9459, 454, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9462, 426, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9461, 151, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9460, 308, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9463, 268, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9464, 381, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9465, 315, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9466, 307, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8757, 604, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8758, 181, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8759, 655, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9268, 598, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9270, 252, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9273, 271, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9274, 655, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9278, 753, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9277, 361, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4250, 635, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4246, 180, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4245, 587, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4242, 727, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4207, 636, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4206, 579, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4079, 418, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4211, 253, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4210, 603, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4209, 651, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4252, 542, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4225, 485, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4309, 541, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4258, 400, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4260, 576, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4248, 393, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4342, 458, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4241, 595, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4240, 326, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4238, 255, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4233, 523, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4243, 559, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4273, 247, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4269, 560, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4264, 617, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4278, 427, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4272, 397, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4271, 116, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4265, 620, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4262, 587, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4261, 325, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4217, 669, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4219, 431, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4218, 448, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4213, 597, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4216, 302, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4215, 155, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4270, 714, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4096, 652, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4104, 162, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4284, 283, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4291, 532, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4275, 451, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4280, 196, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4249, 528, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4287, 257, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4282, 172, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4169, 490, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4172, 615, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4300, 193, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4289, 506, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4181, 678, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4178, 541, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4212, 232, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4170, 467, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4204, 455, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4182, 308, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4208, 208, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4299, 472, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4304, 414, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4301, 620, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4290, 325, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4336, 437, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (2216, 675, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (2215, 567, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (2209, 674, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (2210, 472, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4303, 679, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4305, 438, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4077, 403, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4082, 414, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4302, 617, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4072, 287, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4090, 697, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4097, 707, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4180, 621, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4094, 529, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4236, 578, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4177, 556, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4232, 717, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4173, 660, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8479, 267, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8480, 342, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4231, 708, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4229, 543, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4093, 730, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4070, 468, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (2220, 373, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (2218, 410, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9216, 210, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9204, 369, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9158, 585, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9156, 605, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9157, 523, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9159, 676, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9162, 558, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9209, 220, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9218, 226, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9163, 599, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9160, 375, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9164, 691, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9207, 313, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9219, 161, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9220, 205, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9208, 429, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9165, 433, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9152, 503, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9166, 560, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9222, 222, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9210, 226, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9167, 449, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9153, 603, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (6154, 598, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9168, 468, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9211, 429, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9223, 197, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9169, 414, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9213, 206, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (9225, 219, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8773, 270, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8778, 263, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8783, 326, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8788, 384, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8793, 370, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8798, 323, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8803, 160, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8804, 125, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8799, 383, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8794, 398, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8789, 180, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8779, 157, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8774, 298, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8770, 236, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8780, 341, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8805, 314, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8806, 324, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8832, 295, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8791, 295, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8786, 294, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8781, 314, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8776, 196, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8813, 298, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8817, 315, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8821, 278, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8825, 326, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8829, 298, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8833, 283, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8837, 215, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8838, 313, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8834, 146, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8830, 366, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8826, 295, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8822, 190, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8818, 299, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8814, 396, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4590, 622, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4586, 511, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4605, 395, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4600, 494, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4596, 674, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4606, 454, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4584, 561, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4595, 190, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4624, 181, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4625, 694, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4627, 177, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4626, 697, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8753, 226, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8754, 598, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8755, 560, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (8756, 606, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4591, 525, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4593, 225, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4628, 675, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4620, 263, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4616, 618, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4622, 712, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4621, 378, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4614, 355, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4615, 204, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4592, 542, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4589, 269, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4583, 752, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4598, 598, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (5139, 641, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (5136, 563, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4930, 421, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4607, 578, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4582, 359, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4599, 376, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4601, 363, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4629, 599, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4644, 583, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4646, 578, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4603, 580, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4597, 452, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4609, 245, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4931, 618, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (5127, 617, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (5133, 578, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (5151, 152, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (5278, 322, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (5334, 251, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4932, 637, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4562, 524, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4549, 418, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4649, 154, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4647, 731, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4631, 579, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4630, 523, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4633, 544, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4640, 616, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4666, 231, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4588, 620, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4934, 709, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (5333, 488, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4617, 303, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4618, 434, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (5280, 619, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (5279, 734, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (5112, 152, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (5111, 266, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (5108, 433, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4941, 396, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4937, 579, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4639, 245, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4637, 542, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4690, 716, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4935, 566, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4936, 642, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (5277, 570, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (5324, 456, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (5113, 560, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (5304, 596, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (5311, 598, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (5326, 531, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (5331, 658, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4611, 632, 5, 0, -1, 3000000, '');
INSERT INTO `datos_cercados` VALUES (4613, 329, 5, 0, -1, 3000000, '');

-- ----------------------------
-- Table structure for datos_cofres
-- ----------------------------
DROP TABLE IF EXISTS `datos_cofres`;
CREATE TABLE `datos_cofres`  (
  `id` int(11) NOT NULL,
  `casa` int(11) NOT NULL,
  `mapa` int(11) NOT NULL,
  `celda` int(11) NOT NULL,
  `objeto` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `kamas` int(11) NOT NULL,
  `llave` varchar(8) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT '-',
  `dueño` int(11) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of datos_cofres
-- ----------------------------
INSERT INTO `datos_cofres` VALUES (1, 655, 7710, 107, '', 0, '-', 0);
INSERT INTO `datos_cofres` VALUES (2, 645, 7701, 156, '', 0, '-', 0);
INSERT INTO `datos_cofres` VALUES (3, 645, 7703, 166, '', 0, '-', 0);
INSERT INTO `datos_cofres` VALUES (4, 700, 7694, 88, '', 0, '-', 0);
INSERT INTO `datos_cofres` VALUES (5, 701, 7696, 107, '', 0, '-', 0);
INSERT INTO `datos_cofres` VALUES (6, 684, 7686, 156, '', 0, '-', 0);
INSERT INTO `datos_cofres` VALUES (7, 684, 7687, 166, '', 0, '-', 0);
INSERT INTO `datos_cofres` VALUES (8, 641, 7617, 107, '', 0, '-', 0);
INSERT INTO `datos_cofres` VALUES (9, 652, 7636, 154, '', 0, '-', 0);
INSERT INTO `datos_cofres` VALUES (10, 674, 7741, 156, '', 0, '-', 0);
INSERT INTO `datos_cofres` VALUES (11, 674, 7740, 166, '', 0, '-', 0);
INSERT INTO `datos_cofres` VALUES (13, 690, 7682, 88, '', 0, '-', 0);
INSERT INTO `datos_cofres` VALUES (14, 667, 7661, 156, '', 0, '-', 0);
INSERT INTO `datos_cofres` VALUES (15, 667, 7660, 166, '', 0, '-', 0);
INSERT INTO `datos_cofres` VALUES (16, 670, 7625, 156, '', 0, '-', 0);
INSERT INTO `datos_cofres` VALUES (17, 670, 7624, 166, '', 0, '-', 0);
INSERT INTO `datos_cofres` VALUES (18, 693, 7630, 88, '', 0, '-', 0);
INSERT INTO `datos_cofres` VALUES (19, 698, 7647, 107, '', 0, '-', 0);
INSERT INTO `datos_cofres` VALUES (20, 651, 7729, 107, '', 0, '-', 0);

-- ----------------------------
-- Table structure for datos_cuenta
-- ----------------------------
DROP TABLE IF EXISTS `datos_cuenta`;
CREATE TABLE `datos_cuenta`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `cuenta` varchar(30) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `contraseña` varchar(50) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `nivel` int(11) NOT NULL DEFAULT 0,
  `vip` int(1) NOT NULL DEFAULT 0,
  `email` varchar(100) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `ultimaip` varchar(15) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `ultimafechaconexion` varchar(100) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `pregunta` varchar(100) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT 'supprimer ?',
  `respuesta` varchar(100) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT 'oui',
  `apodo` varchar(30) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `baneado` tinyint(3) NOT NULL DEFAULT 0,
  `actualizarnecesita` tinyint(1) NOT NULL DEFAULT 1,
  `kamasbanco` int(11) NOT NULL DEFAULT 0,
  `banco` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `amigos` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `enemigos` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `puntos` int(11) NOT NULL DEFAULT 0,
  `conectado` int(1) NOT NULL DEFAULT 0,
  `verificar` varchar(50) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `fechavip` varchar(255) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT '0',
  `voto` int(4) NULL DEFAULT 0,
  `horasvoto` bigint(100) NULL DEFAULT 0,
  `estable` varchar(255) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT '',
  PRIMARY KEY (`id`, `cuenta`) USING BTREE,
  UNIQUE INDEX `account`(`cuenta`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 27 CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of datos_cuenta
-- ----------------------------
INSERT INTO `datos_cuenta` VALUES (24, 'admin', '123', 5, 0, '', '127.0.0.1', '2020~06~22~00~52', 'supprimer ?', 'oui', '', 0, 0, 0, '', '', '', 0, 1, '', '0', 0, 0, '');
INSERT INTO `datos_cuenta` VALUES (25, 'admin1', '123', 5, 0, '', '127.0.0.1', '2020~06~22~00~04', 'supprimer ?', 'oui', '', 0, 0, 0, '', '', '', 0, 0, '', '0', 0, 0, '');
INSERT INTO `datos_cuenta` VALUES (26, 'admin2', '123', 5, 0, '', '127.0.0.1', '2020~06~19~21~12', 'supprimer ?', 'oui', '', 0, 0, 0, '', '', '', 0, 0, '', '0', 0, 0, '');

-- ----------------------------
-- Table structure for datos_gremio
-- ----------------------------
DROP TABLE IF EXISTS `datos_gremio`;
CREATE TABLE `datos_gremio`  (
  `id` int(11) NOT NULL,
  `nombre` varchar(50) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `emblema` varchar(20) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `nivel` int(11) NOT NULL DEFAULT 1,
  `experiencia` bigint(20) NOT NULL DEFAULT 0,
  `capital` int(11) NOT NULL DEFAULT 0,
  `recaudadoresmaximos` int(11) NOT NULL DEFAULT 0,
  `hechizos` varchar(255) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT '462;0|461;0|460;0|459;0|458;0|457;0|456;0|455;0|454;0|453;0|452;0|451;0|',
  `caracteristicas` varchar(255) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT '176;100|158;1000|124;100|',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `id`(`id`) USING BTREE
) ENGINE = MyISAM CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of datos_gremio
-- ----------------------------
INSERT INTO `datos_gremio` VALUES (1, 'Testeo', '2,9o33h,7,0', 1, 0, 0, 0, '451;0|452;0|453;0|454;0|455;0|456;0|457;0|458;0|459;0|460;0|461;0|462;0', '124;100|158;1000|176;100');

-- ----------------------------
-- Table structure for datos_ipbaneadas
-- ----------------------------
DROP TABLE IF EXISTS `datos_ipbaneadas`;
CREATE TABLE `datos_ipbaneadas`  (
  `ip` varchar(15) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL
) ENGINE = MyISAM CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for datos_miembros_gremio
-- ----------------------------
DROP TABLE IF EXISTS `datos_miembros_gremio`;
CREATE TABLE `datos_miembros_gremio`  (
  `id` int(11) NOT NULL,
  `gremio` int(11) NOT NULL,
  `rango` int(11) NOT NULL,
  `xpdonada` bigint(20) NOT NULL,
  `porcentajexp` int(11) NOT NULL,
  `derechos` int(11) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `guid`(`id`) USING BTREE
) ENGINE = MyISAM CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = Fixed;

-- ----------------------------
-- Records of datos_miembros_gremio
-- ----------------------------
INSERT INTO `datos_miembros_gremio` VALUES (1, 1, 1, 0, 0, 1);

-- ----------------------------
-- Table structure for datos_montura
-- ----------------------------
DROP TABLE IF EXISTS `datos_montura`;
CREATE TABLE `datos_montura`  (
  `id` int(11) NOT NULL,
  `color` int(11) NOT NULL,
  `sexo` int(11) NOT NULL,
  `nombre` varchar(30) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `experiencia` int(32) NOT NULL,
  `nivel` int(11) NOT NULL,
  `resistencia` int(11) NOT NULL,
  `amor` int(11) NOT NULL,
  `madurez` int(11) NOT NULL,
  `serenidad` int(11) NOT NULL,
  `reproducciones` int(11) NOT NULL,
  `fatiga` int(11) NOT NULL,
  `energia` int(11) NOT NULL,
  `objetos` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `ancestros` varchar(50) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT ',,,,,,,,,,,,,',
  `habilidad` varchar(11) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `id`(`id`) USING BTREE
) ENGINE = MyISAM CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for datos_objetos
-- ----------------------------
DROP TABLE IF EXISTS `datos_objetos`;
CREATE TABLE `datos_objetos`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `modelo` int(11) NOT NULL COMMENT 'ID del modelo del objeto (datos_objeto_modelo) tabla estaticos',
  `cantidad` int(11) NOT NULL,
  `ubicacion` int(11) NOT NULL,
  `caracteristicas` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL COMMENT 'Stats del objeto en particular',
  `dueño` int(11) NOT NULL DEFAULT 0 COMMENT 'ID del PJ dueño del item',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `guid`(`id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 14 CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of datos_objetos
-- ----------------------------
INSERT INTO `datos_objetos` VALUES (1, 7401, 2, -1, '', 1);
INSERT INTO `datos_objetos` VALUES (13, 405, 5, -1, '', 1);

-- ----------------------------
-- Table structure for datos_objetos_cercados
-- ----------------------------
DROP TABLE IF EXISTS `datos_objetos_cercados`;
CREATE TABLE `datos_objetos_cercados`  (
  `id` int(11) NOT NULL,
  `mapid` int(11) NOT NULL,
  `cellid` int(11) NOT NULL,
  `resiste` int(11) NOT NULL DEFAULT -1,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for datos_objetos_mercadillo
-- ----------------------------
DROP TABLE IF EXISTS `datos_objetos_mercadillo`;
CREATE TABLE `datos_objetos_mercadillo`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `mapa` int(11) NOT NULL,
  `dueño` int(11) NOT NULL,
  `precio` int(11) NOT NULL,
  `cantidad` int(3) NOT NULL,
  `tiempoventa` varchar(20) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT 'rien',
  `objeto` int(11) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 1 CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for datos_personajes
-- ----------------------------
DROP TABLE IF EXISTS `datos_personajes`;
CREATE TABLE `datos_personajes`  (
  `id` int(11) NOT NULL,
  `nombre` varchar(30) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `sexo` tinyint(4) NOT NULL,
  `clase` smallint(6) NOT NULL,
  `color1` int(11) NOT NULL,
  `color2` int(11) NOT NULL,
  `color3` int(11) NOT NULL,
  `kamas` int(11) NOT NULL,
  `puntoshechizo` int(11) NOT NULL,
  `capital` int(11) NOT NULL,
  `energia` int(11) NOT NULL DEFAULT 10000,
  `nivel` int(11) NOT NULL,
  `experiencia` bigint(32) NOT NULL DEFAULT 0,
  `tamaño` int(11) NOT NULL,
  `gfx` int(11) NOT NULL,
  `alineacion` int(11) NOT NULL DEFAULT 0,
  `honor` int(11) NOT NULL DEFAULT 0,
  `deshonor` int(11) NOT NULL DEFAULT 0,
  `nivelalineacion` int(11) NOT NULL DEFAULT 0 COMMENT 'Niveau alignement',
  `cuenta` int(11) NOT NULL,
  `vitalidad` int(11) NOT NULL DEFAULT 101,
  `fuerza` int(11) NOT NULL DEFAULT 101,
  `sabiduria` int(11) NOT NULL DEFAULT 101,
  `inteligencia` int(11) NOT NULL DEFAULT 101,
  `suerte` int(11) NOT NULL DEFAULT 101,
  `agilidad` int(11) NOT NULL DEFAULT 101,
  `verhechizo` tinyint(4) NOT NULL DEFAULT 0,
  `veramigos` tinyint(4) NOT NULL DEFAULT 1,
  `veralineacion` tinyint(4) NOT NULL DEFAULT 0,
  `vervendedor` tinyint(4) NOT NULL DEFAULT 0,
  `canales` varchar(15) CHARACTER SET latin2 COLLATE latin2_general_ci NOT NULL DEFAULT '',
  `mapa` int(11) NOT NULL DEFAULT 8479,
  `celda` int(11) NOT NULL,
  `puntosdevida` int(11) NOT NULL DEFAULT 100,
  `hechizos` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `objetos` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `objetosmercante` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `puntoguardado` varchar(20) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT '10298,314',
  `zaaps` varchar(250) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT '',
  `oficios` varchar(300) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT '',
  `xpmontura` int(11) NOT NULL DEFAULT 0,
  `montura` int(11) NOT NULL DEFAULT -1,
  `titulo` int(11) NOT NULL DEFAULT 0,
  `esposo` int(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of datos_personajes
-- ----------------------------
INSERT INTO `datos_personajes` VALUES (1, 'Player-xD', 0, 9, -1, -1, -1, 5000838, 110, 5, 10000, 151, 534476095, 100, 90, 0, 0, 0, 0, 24, 14, 3, 8, 283, 13, 9, 0, 1, 0, 0, '*#%!pi$:?', 7412, 396, 100, '161;6;b,162;1;_,163;1;_,164;1;d,165;1;_,166;1;_,167;1;_,168;6;i,169;1;c,170;1;_,171;1;_,172;1;_,173;1;_,174;1;_,175;1;_,176;1;_,177;1;_,178;1;_,179;1;_,180;1;_,414;1;_,415;5;e', '', '', '7411,297', '164,528,844,935,951,1158,1242,1841,2191,3022,3250,4263,4739,5295,6137,6855,6954,7411,8037,8088,8125,8163,8437,8785,9454,10297,10304,10317,10349,10643,11170,11210', '', 0, -1, 0, 0);
INSERT INTO `datos_personajes` VALUES (2, 'testeo', 0, 9, -1, -1, -1, 5000000, 79, 395, 10000, 80, 38945000, 100, 90, 0, 0, 0, 0, 25, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, '*#%!pi$:?', 7412, 440, 100, '161;1;b,162;1;_,163;1;_,164;1;d,165;1;_,166;1;_,167;1;_,168;1;_,169;1;c,170;1;_,171;1;_,172;1;_,173;1;_,174;1;_,175;1;_,176;1;_,177;1;_,178;1;_', '', '', '5,255', '164,528,844,935,951,1158,1242,1841,2191,3022,3250,4263,4739,5295,6137,6855,6954,7411,8037,8088,8125,8163,8437,8785,9454,10297,10304,10317,10349,10643,11170,11210', '', 0, -1, 0, 0);
INSERT INTO `datos_personajes` VALUES (3, 'testico', 0, 11, -1, -1, -1, 5000000, 79, 395, 10000, 80, 38945000, 100, 110, 0, 0, 0, 0, 26, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, '*#%!pi$:?', 7413, 35, 100, '431;1;c,432;1;b,433;1;_,434;1;d,436;1;_,437;1;_,438;1;_,439;1;_,440;1;_,441;1;_,442;1;_,443;1;_,444;1;_,445;1;_,446;1;_,447;1;_,448;1;_,449;1;_', '', '', '5,255', '164,528,844,935,951,1158,1242,1841,2191,3022,3250,4263,4739,5295,6137,6855,6954,7411,8037,8088,8125,8163,8437,8785,9454,10297,10304,10317,10349,10643,11170,11210', '', 0, -1, 0, 0);

-- ----------------------------
-- Table structure for datos_recaudadores
-- ----------------------------
DROP TABLE IF EXISTS `datos_recaudadores`;
CREATE TABLE `datos_recaudadores`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `mapa` int(11) NOT NULL,
  `celda` int(11) NOT NULL,
  `orientacion` int(11) NOT NULL,
  `gremio` int(11) NOT NULL,
  `N1` int(11) NOT NULL,
  `N2` int(11) NOT NULL,
  `objetos` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `kamas` int(11) NOT NULL,
  `experiencia` int(11) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 1 CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for datos_subareas
-- ----------------------------
DROP TABLE IF EXISTS `datos_subareas`;
CREATE TABLE `datos_subareas`  (
  `id` int(11) NOT NULL,
  `area` int(11) NOT NULL,
  `alineacion` int(11) NOT NULL DEFAULT -1,
  `nombre` varchar(200) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  INDEX `id`(`id`) USING BTREE
) ENGINE = MyISAM CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of datos_subareas
-- ----------------------------
INSERT INTO `datos_subareas` VALUES (0, 0, -1, '//Amakna');
INSERT INTO `datos_subareas` VALUES (1, 0, -1, 'Port de Madrestam');
INSERT INTO `datos_subareas` VALUES (2, 0, -1, 'La montagne des Craqueleurs');
INSERT INTO `datos_subareas` VALUES (3, 0, -1, 'Le champ des Ingalsses');
INSERT INTO `datos_subareas` VALUES (4, 0, -1, 'La forêt d\'Amakna');
INSERT INTO `datos_subareas` VALUES (5, 0, -1, 'Le coin des Bouftous');
INSERT INTO `datos_subareas` VALUES (6, 0, -1, 'Le cimetière');
INSERT INTO `datos_subareas` VALUES (7, 0, -1, 'Les cryptes');
INSERT INTO `datos_subareas` VALUES (8, 0, -1, 'Campement des Bworks');
INSERT INTO `datos_subareas` VALUES (9, 0, -1, 'La forêt maléfique');
INSERT INTO `datos_subareas` VALUES (10, 0, -1, 'Le village');
INSERT INTO `datos_subareas` VALUES (11, 0, -1, 'Territoire des Porcos');
INSERT INTO `datos_subareas` VALUES (12, 0, -1, 'La péninsule des gelées');
INSERT INTO `datos_subareas` VALUES (13, 0, -1, 'Le temple Féca');
INSERT INTO `datos_subareas` VALUES (14, 0, -1, 'Le temple Osamodas');
INSERT INTO `datos_subareas` VALUES (15, 0, -1, 'Le temple Enutrof');
INSERT INTO `datos_subareas` VALUES (16, 0, -1, 'Le temple Sram');
INSERT INTO `datos_subareas` VALUES (17, 0, -1, 'Le temple Xélor');
INSERT INTO `datos_subareas` VALUES (18, 0, -1, 'Le temple Ecaflip');
INSERT INTO `datos_subareas` VALUES (19, 0, -1, 'Le temple Iop');
INSERT INTO `datos_subareas` VALUES (20, 0, -1, 'Le temple Crâ ');
INSERT INTO `datos_subareas` VALUES (21, 0, -1, 'Le temple Sadida');
INSERT INTO `datos_subareas` VALUES (22, 0, -1, 'Bord de la fôret maléfique');
INSERT INTO `datos_subareas` VALUES (23, 0, -1, 'La presqu\'île des Dragoeufs');
INSERT INTO `datos_subareas` VALUES (25, 1, -1, 'Sous-terrains des Wabbits');
INSERT INTO `datos_subareas` VALUES (26, 0, -1, 'Le temple Eniripsa');
INSERT INTO `datos_subareas` VALUES (27, 0, -1, 'Côte d\'Asse');
INSERT INTO `datos_subareas` VALUES (28, 0, -1, 'Garnison d\'Amakna');
INSERT INTO `datos_subareas` VALUES (29, 0, -1, 'Souterrain');
INSERT INTO `datos_subareas` VALUES (30, 4, -1, 'Le berceau');
INSERT INTO `datos_subareas` VALUES (31, 0, -1, 'Le marécage');
INSERT INTO `datos_subareas` VALUES (32, 5, -1, '//Sufokia');
INSERT INTO `datos_subareas` VALUES (33, 6, -1, '//Forêt des Abraknydes');
INSERT INTO `datos_subareas` VALUES (34, 3, -1, '//Prison');
INSERT INTO `datos_subareas` VALUES (35, 0, -1, 'Porte de Sufokia');
INSERT INTO `datos_subareas` VALUES (37, 7, -1, '//Bonta');
INSERT INTO `datos_subareas` VALUES (38, 8, -1, '//Plaine de Cania');
INSERT INTO `datos_subareas` VALUES (39, 0, -1, 'Le repaire des Roublards');
INSERT INTO `datos_subareas` VALUES (41, 0, -1, 'Le temple Sacrieur');
INSERT INTO `datos_subareas` VALUES (42, 8, -1, 'Route de Bonta');
INSERT INTO `datos_subareas` VALUES (43, 7, -1, 'Fortification de Bonta');
INSERT INTO `datos_subareas` VALUES (44, 7, -1, 'Quartier des Boulangers');
INSERT INTO `datos_subareas` VALUES (45, 7, -1, 'Quartier de la Milice');
INSERT INTO `datos_subareas` VALUES (46, 7, -1, 'Quartier des Bouchers');
INSERT INTO `datos_subareas` VALUES (47, 7, -1, 'Quartier des Forgerons');
INSERT INTO `datos_subareas` VALUES (48, 7, -1, 'Quartier des Bûcherons');
INSERT INTO `datos_subareas` VALUES (49, 7, -1, 'Quartier des Bricoleurs');
INSERT INTO `datos_subareas` VALUES (50, 7, -1, 'Quartier des Tailleurs');
INSERT INTO `datos_subareas` VALUES (51, 7, -1, 'Quartier des Bijoutiers');
INSERT INTO `datos_subareas` VALUES (53, 11, -1, '//Brâkmar');
INSERT INTO `datos_subareas` VALUES (54, 8, -1, 'Massif de Cania');
INSERT INTO `datos_subareas` VALUES (55, 8, -1, 'Pénates du Corbac');
INSERT INTO `datos_subareas` VALUES (56, 8, -1, 'Forêt de Cania');
INSERT INTO `datos_subareas` VALUES (57, 12, -1, '//Lande de Sidimote');
INSERT INTO `datos_subareas` VALUES (59, 8, -1, 'Cimetière de Bonta');
INSERT INTO `datos_subareas` VALUES (61, 12, -1, 'Cimetière des Torturés');
INSERT INTO `datos_subareas` VALUES (62, 13, -1, '//Village Dopeul');
INSERT INTO `datos_subareas` VALUES (63, 13, -1, 'Prisme des Dopeuls');
INSERT INTO `datos_subareas` VALUES (64, 13, -1, 'Première salle du prisme');
INSERT INTO `datos_subareas` VALUES (65, 13, -1, 'Seconde salle du prisme');
INSERT INTO `datos_subareas` VALUES (66, 13, -1, 'Troisième salle du prisme');
INSERT INTO `datos_subareas` VALUES (67, 13, -1, 'Quatrième salle du prisme');
INSERT INTO `datos_subareas` VALUES (68, 8, -1, 'Les Champs de Cania');
INSERT INTO `datos_subareas` VALUES (69, 8, -1, 'Bois de Litneg');
INSERT INTO `datos_subareas` VALUES (70, 8, -1, 'Plaines Rocheuses');
INSERT INTO `datos_subareas` VALUES (71, 12, -1, 'Gisgoul, le village devasté ');
INSERT INTO `datos_subareas` VALUES (72, 12, -1, 'Façade de Brâkmar');
INSERT INTO `datos_subareas` VALUES (73, 7, -1, 'Egout de Bonta');
INSERT INTO `datos_subareas` VALUES (74, 13, -1, '//Entrainement Dopeuls');
INSERT INTO `datos_subareas` VALUES (75, 11, -1, 'Egout de Brâkmar');
INSERT INTO `datos_subareas` VALUES (76, 14, -1, '//Village Brigandins');
INSERT INTO `datos_subareas` VALUES (77, 0, -1, 'La gelaxième dimension');
INSERT INTO `datos_subareas` VALUES (78, 0, -1, '//La gelaxième dimension (royale)');
INSERT INTO `datos_subareas` VALUES (79, 14, -1, 'Première plate-forme');
INSERT INTO `datos_subareas` VALUES (80, 14, -1, 'Seconde plate-forme');
INSERT INTO `datos_subareas` VALUES (81, 14, -1, 'Prisme Brigandin');
INSERT INTO `datos_subareas` VALUES (82, 4, -1, 'Donjon des Bouftous');
INSERT INTO `datos_subareas` VALUES (83, 17, -1, '//Tutorial');
INSERT INTO `datos_subareas` VALUES (84, 15, -1, '//Foire du Trool');
INSERT INTO `datos_subareas` VALUES (85, 16, -1, '//Jeu du Bouftou');
INSERT INTO `datos_subareas` VALUES (86, 15, -1, 'Gladiatrool');
INSERT INTO `datos_subareas` VALUES (87, 0, -1, '//Amakna Sud');
INSERT INTO `datos_subareas` VALUES (88, 17, -1, '//Tainela');
INSERT INTO `datos_subareas` VALUES (89, 0, -1, 'Tournoi Monde du Jeu');
INSERT INTO `datos_subareas` VALUES (91, 0, -1, 'Souterrain mystérieux');
INSERT INTO `datos_subareas` VALUES (92, 18, -1, 'Contour d\'Astrub');
INSERT INTO `datos_subareas` VALUES (93, 2, -1, 'La plage de Moon');
INSERT INTO `datos_subareas` VALUES (94, 12, -1, 'Donjon du Bworker');
INSERT INTO `datos_subareas` VALUES (95, 18, -1, 'Cité d\'Astrub');
INSERT INTO `datos_subareas` VALUES (96, 18, -1, 'Exploitation minière d\'Astrub');
INSERT INTO `datos_subareas` VALUES (97, 18, -1, 'Forêt d\'Astrub');
INSERT INTO `datos_subareas` VALUES (98, 18, -1, 'Champs d\'Astrub');
INSERT INTO `datos_subareas` VALUES (99, 18, -1, 'Souterrain d\'Astrub');
INSERT INTO `datos_subareas` VALUES (100, 18, -1, 'Souterrain profond d\'Astrub');
INSERT INTO `datos_subareas` VALUES (101, 18, -1, 'Le coin des Tofus');
INSERT INTO `datos_subareas` VALUES (102, 25, -1, 'Le champ du repos');
INSERT INTO `datos_subareas` VALUES (103, 0, -1, 'Territoire des Bandits');
INSERT INTO `datos_subareas` VALUES (105, 19, -1, '//Pandala Neutre');
INSERT INTO `datos_subareas` VALUES (106, 20, -1, 'Bordure d\'Akwadala');
INSERT INTO `datos_subareas` VALUES (107, 22, -1, 'Bordure de Feudala');
INSERT INTO `datos_subareas` VALUES (108, 23, -1, 'Bordure d\'Aerdala');
INSERT INTO `datos_subareas` VALUES (109, 21, -1, 'Bordure de Terrdala');
INSERT INTO `datos_subareas` VALUES (110, 0, -1, '//Amakna passé ');
INSERT INTO `datos_subareas` VALUES (111, 23, -1, 'Porte Aerdala');
INSERT INTO `datos_subareas` VALUES (112, 22, -1, 'Porte Feudala');
INSERT INTO `datos_subareas` VALUES (113, 20, -1, 'Porte Akwadala');
INSERT INTO `datos_subareas` VALUES (114, 21, -1, 'Porte Terrdala');
INSERT INTO `datos_subareas` VALUES (115, 23, -1, 'Village d\'Aerdala');
INSERT INTO `datos_subareas` VALUES (116, 22, -1, 'Village de Feudala');
INSERT INTO `datos_subareas` VALUES (117, 20, -1, 'Village d\'Akwadala');
INSERT INTO `datos_subareas` VALUES (118, 21, -1, 'Village de Terrdala');
INSERT INTO `datos_subareas` VALUES (119, 19, -1, 'Village de Pandala');
INSERT INTO `datos_subareas` VALUES (120, 23, -1, 'Prisme Aerdala');
INSERT INTO `datos_subareas` VALUES (121, 22, -1, 'Prisme Feudala');
INSERT INTO `datos_subareas` VALUES (122, 20, -1, 'Prisme Akwadala');
INSERT INTO `datos_subareas` VALUES (123, 21, -1, 'Prisme Terrdala');
INSERT INTO `datos_subareas` VALUES (124, 24, -1, 'Caverne des Bulbes');
INSERT INTO `datos_subareas` VALUES (125, 24, -1, 'Repaire des Pandikazes - Première plate-forme');
INSERT INTO `datos_subareas` VALUES (126, 24, -1, 'Repaire des Pandikazes - Seconde plate-forme');
INSERT INTO `datos_subareas` VALUES (127, 24, -1, 'Repaire des Pandikazes - Troisième plate-forme');
INSERT INTO `datos_subareas` VALUES (128, 24, -1, 'Repaire des Pandikazes - Quatrième plate-forme');
INSERT INTO `datos_subareas` VALUES (129, 24, -1, 'Repaire des Pandikazes - Cinquième plate-forme');
INSERT INTO `datos_subareas` VALUES (130, 24, -1, 'Repaire des Pandikazes - Sixième plate-forme');
INSERT INTO `datos_subareas` VALUES (131, 24, -1, 'Repaire des Pandikazes - Septième plate-forme');
INSERT INTO `datos_subareas` VALUES (132, 24, -1, 'Repaire des Pandikazes - Huitième plate-forme');
INSERT INTO `datos_subareas` VALUES (133, 24, -1, 'Donjon des Kitsounes - Salle 1');
INSERT INTO `datos_subareas` VALUES (134, 24, -1, 'Donjon des Kitsounes - Salle 2');
INSERT INTO `datos_subareas` VALUES (135, 24, -1, 'Donjon des Kitsounes - Salle 3');
INSERT INTO `datos_subareas` VALUES (136, 24, -1, 'Donjon des Kitsounes - Salle 4');
INSERT INTO `datos_subareas` VALUES (137, 24, -1, 'Donjon des Kitsounes - Salle 4');
INSERT INTO `datos_subareas` VALUES (138, 24, -1, 'Donjon des Kitsounes - Salle 4');
INSERT INTO `datos_subareas` VALUES (139, 24, -1, 'Donjon des Kitsounes - Salle 5');
INSERT INTO `datos_subareas` VALUES (140, 24, -1, 'Donjon des Kitsounes - Salle 6');
INSERT INTO `datos_subareas` VALUES (141, 24, -1, 'Donjon des Kitsounes - Repaire du Tanukouï ');
INSERT INTO `datos_subareas` VALUES (143, 19, -1, 'Pont de Pandala');
INSERT INTO `datos_subareas` VALUES (144, 24, -1, 'Donjon des Firefoux');
INSERT INTO `datos_subareas` VALUES (145, 24, -1, 'Donjon des Firefoux - Salle 1');
INSERT INTO `datos_subareas` VALUES (146, 24, -1, 'Donjon des Firefoux - Salle 2');
INSERT INTO `datos_subareas` VALUES (147, 24, -1, 'Donjon des Firefoux - Salle 3');
INSERT INTO `datos_subareas` VALUES (148, 24, -1, 'Donjon des Firefoux - Salle 4');
INSERT INTO `datos_subareas` VALUES (149, 24, -1, 'Donjon des Firefoux - Salle 5');
INSERT INTO `datos_subareas` VALUES (150, 24, -1, 'Donjon des Firefoux - Salle 6');
INSERT INTO `datos_subareas` VALUES (151, 24, -1, 'Donjon des Firefoux - Salle 7');
INSERT INTO `datos_subareas` VALUES (152, 19, -1, 'L\'île de Grobe');
INSERT INTO `datos_subareas` VALUES (153, 24, -1, 'Repaire des Pandikazes - Plate-forme finale');
INSERT INTO `datos_subareas` VALUES (154, 43, -1, '//Donjon Cochon - Salle 1');
INSERT INTO `datos_subareas` VALUES (155, 43, -1, '//Donjon Cochon - Salle 2');
INSERT INTO `datos_subareas` VALUES (156, 43, -1, '//Donjon Cochon - Salle 3');
INSERT INTO `datos_subareas` VALUES (157, 43, -1, '//Donjon Cochon - Salle 4');
INSERT INTO `datos_subareas` VALUES (158, 43, -1, '//Donjon Cochon - Salle 5');
INSERT INTO `datos_subareas` VALUES (159, 43, -1, '//Donjon Cochon - Salle 6');
INSERT INTO `datos_subareas` VALUES (161, 1, -1, 'Ile des Wabbits');
INSERT INTO `datos_subareas` VALUES (162, 1, -1, 'Ilot des Wabbits');
INSERT INTO `datos_subareas` VALUES (163, 1, -1, 'Ile des Wabbits Squelettes');
INSERT INTO `datos_subareas` VALUES (164, 1, -1, 'Île du château des Wabbits');
INSERT INTO `datos_subareas` VALUES (165, 2, -1, 'La jungle profonde de Moon');
INSERT INTO `datos_subareas` VALUES (166, 2, -1, 'Le chemin vers Moon');
INSERT INTO `datos_subareas` VALUES (167, 2, -1, 'Le bateau pirate');
INSERT INTO `datos_subareas` VALUES (168, 6, -1, 'Forêt des Abraknydes Sombres');
INSERT INTO `datos_subareas` VALUES (169, 6, -1, 'Orée de la forêt des Abraknydes');
INSERT INTO `datos_subareas` VALUES (170, 0, -1, 'Plaine des Scarafeuilles');
INSERT INTO `datos_subareas` VALUES (171, 19, -1, 'Forêt de Pandala');
INSERT INTO `datos_subareas` VALUES (173, 18, -1, 'Prairies d\'Astrub');
INSERT INTO `datos_subareas` VALUES (174, 18, -1, 'Campement des Bandits Manchots');
INSERT INTO `datos_subareas` VALUES (175, 27, -1, '//Donjon Abraknyde - Salle 1');
INSERT INTO `datos_subareas` VALUES (177, 8, -1, 'Prairie des Blops');
INSERT INTO `datos_subareas` VALUES (178, 8, -1, 'Plaine des Porkass');
INSERT INTO `datos_subareas` VALUES (179, 0, -1, 'Le coin des Boos');
INSERT INTO `datos_subareas` VALUES (180, 0, -1, 'Le château d\'Amakna');
INSERT INTO `datos_subareas` VALUES (181, 0, -1, 'Souterrain du Château d\'Amakna');
INSERT INTO `datos_subareas` VALUES (182, 28, -1, 'Village des Eleveurs');
INSERT INTO `datos_subareas` VALUES (200, 26, -1, '//Labyrinthe du Dragon Cochon');
INSERT INTO `datos_subareas` VALUES (201, 29, -1, '//Donjon des Tofus - Salle 1');
INSERT INTO `datos_subareas` VALUES (202, 29, -1, '//Donjon des Tofus - Salle 2');
INSERT INTO `datos_subareas` VALUES (203, 29, -1, '//Donjon des Tofus - Salle 3');
INSERT INTO `datos_subareas` VALUES (204, 29, -1, '//Donjon des Tofus - Salle 4');
INSERT INTO `datos_subareas` VALUES (205, 29, -1, '//Donjon des Tofus - Salle 5');
INSERT INTO `datos_subareas` VALUES (206, 29, -1, '//Donjon des Tofus - Salle 6');
INSERT INTO `datos_subareas` VALUES (207, 29, -1, '//Donjon des Tofus - Salle 7');
INSERT INTO `datos_subareas` VALUES (208, 29, -1, '//Donjon des Tofus - Salle 8');
INSERT INTO `datos_subareas` VALUES (209, 30, -1, 'L\'île du Minotoror');
INSERT INTO `datos_subareas` VALUES (210, 31, -1, 'Le labyrinthe du Minotoror');
INSERT INTO `datos_subareas` VALUES (211, 32, -1, 'La bibliothèque du Maître Corbac');
INSERT INTO `datos_subareas` VALUES (212, 33, -1, '//Donjon des Canidés - Entrée');
INSERT INTO `datos_subareas` VALUES (213, 33, -1, '//Donjon des Canidés - Salle 1');
INSERT INTO `datos_subareas` VALUES (214, 33, -1, '//Donjon des Canidés - Salle 2');
INSERT INTO `datos_subareas` VALUES (215, 33, -1, '//Donjon des Canidés - Salle 3');
INSERT INTO `datos_subareas` VALUES (216, 33, -1, '//Donjon des Canidés - Salle 4');
INSERT INTO `datos_subareas` VALUES (217, 33, -1, '//Donjon des Canidés - Salle 5');
INSERT INTO `datos_subareas` VALUES (218, 33, -1, '//Donjon des Canidés - Salle 6');
INSERT INTO `datos_subareas` VALUES (219, 33, -1, '//Donjon des Canidés - Salle 7');
INSERT INTO `datos_subareas` VALUES (220, 33, -1, '//Donjon des Canidés - Salle 8');
INSERT INTO `datos_subareas` VALUES (221, 33, -1, '//Donjon des Canidés - Salle 9');
INSERT INTO `datos_subareas` VALUES (222, 33, -1, '//Donjon des Canidés - Salle 10');
INSERT INTO `datos_subareas` VALUES (223, 34, -1, '//Caverne du Koulosse - Salle 1');
INSERT INTO `datos_subareas` VALUES (224, 34, -1, '//Caverne du Koulosse - Salle 2');
INSERT INTO `datos_subareas` VALUES (225, 34, -1, '//Caverne du Koulosse - Salle 3');
INSERT INTO `datos_subareas` VALUES (226, 34, -1, '//Caverne du Koulosse - Salle 4');
INSERT INTO `datos_subareas` VALUES (227, 34, -1, '//Caverne du Koulosse - Salle 5');
INSERT INTO `datos_subareas` VALUES (228, 34, -1, '//Caverne du Koulosse - Salle 6');
INSERT INTO `datos_subareas` VALUES (229, 34, -1, '//Caverne du Koulosse - L\'antre');
INSERT INTO `datos_subareas` VALUES (230, 28, -1, 'Cimetière primitif');
INSERT INTO `datos_subareas` VALUES (231, 28, -1, 'Lacs enchantés');
INSERT INTO `datos_subareas` VALUES (232, 28, -1, 'Marécages nauséabonds');
INSERT INTO `datos_subareas` VALUES (233, 28, -1, 'Marécages sans fond');
INSERT INTO `datos_subareas` VALUES (234, 28, -1, 'Forêt de Kaliptus');
INSERT INTO `datos_subareas` VALUES (235, 28, -1, 'Territoire des Dragodindes Sauvages');
INSERT INTO `datos_subareas` VALUES (236, 36, -1, 'Sanctuaire des Familiers');
INSERT INTO `datos_subareas` VALUES (243, 37, -1, '//Donjon des Craqueleurs - Salle 1');
INSERT INTO `datos_subareas` VALUES (244, 37, -1, '//Donjon des Craqueleurs - Salle 2');
INSERT INTO `datos_subareas` VALUES (245, 37, -1, '//Donjon des Craqueleurs - Salle 3');
INSERT INTO `datos_subareas` VALUES (246, 37, -1, '//Donjon des Craqueleurs - Salle 4');
INSERT INTO `datos_subareas` VALUES (247, 37, -1, '//Donjon des Craqueleurs - Salle 5');
INSERT INTO `datos_subareas` VALUES (248, 37, -1, '//Donjon des Craqueleurs - Salle 6');
INSERT INTO `datos_subareas` VALUES (249, 37, -1, '//Donjon des Craqueleurs - Salle 7');
INSERT INTO `datos_subareas` VALUES (250, 37, -1, '//Donjon des Craqueleurs - Salle 8');
INSERT INTO `datos_subareas` VALUES (251, 37, -1, '//Donjon des Craqueleurs - Salle 10');
INSERT INTO `datos_subareas` VALUES (252, 37, -1, '//Donjon des Craqueleurs - Salle 11');
INSERT INTO `datos_subareas` VALUES (253, 28, -1, 'Canyon sauvage');
INSERT INTO `datos_subareas` VALUES (254, 35, -1, '//Repaire de Skeunk - Salle 1');
INSERT INTO `datos_subareas` VALUES (255, 35, -1, '//Repaire de Skeunk - Salle 2');
INSERT INTO `datos_subareas` VALUES (256, 35, -1, '//Repaire de Skeunk - Salle 3');
INSERT INTO `datos_subareas` VALUES (257, 35, -1, '//Repaire de Skeunk - Emeraude');
INSERT INTO `datos_subareas` VALUES (258, 35, -1, '//Repaire de Skeunk - Rubise');
INSERT INTO `datos_subareas` VALUES (259, 35, -1, '//Repaire de Skeunk - Saphira');
INSERT INTO `datos_subareas` VALUES (260, 35, -1, '//Repaire de Skeunk - Diamantine');
INSERT INTO `datos_subareas` VALUES (261, 35, -1, '//Repaire de Skeunk - Antre');
INSERT INTO `datos_subareas` VALUES (266, 35, -1, '//Repaire de Skeunk - Salle 4');
INSERT INTO `datos_subareas` VALUES (267, 35, -1, '//Repaire de Skeunk - Salle 5');
INSERT INTO `datos_subareas` VALUES (268, 29, -1, '//Donjon des Tofus - Salle 9');
INSERT INTO `datos_subareas` VALUES (269, 29, -1, '//Donjon des Tofus - Salle 10');
INSERT INTO `datos_subareas` VALUES (270, 29, -1, '//Donjon des Tofus - Salle 11');
INSERT INTO `datos_subareas` VALUES (271, 29, -1, '//Donjon des Tofus - Salle 12');
INSERT INTO `datos_subareas` VALUES (272, 29, -1, '//Donjon des Tofus - Salle 13');
INSERT INTO `datos_subareas` VALUES (273, 29, -1, '//Donjon des Tofus - Salle 14');
INSERT INTO `datos_subareas` VALUES (274, 29, -1, '//Donjon des Tofus - Salle 15');
INSERT INTO `datos_subareas` VALUES (275, 28, -1, 'Vallée de la Morh\'Kitu');
INSERT INTO `datos_subareas` VALUES (276, 0, -1, 'Campement des Gobelins');
INSERT INTO `datos_subareas` VALUES (277, 0, -1, 'Village des Bworks');
INSERT INTO `datos_subareas` VALUES (278, 18, -1, 'Elevage de Bouftous du Château d\'Amakna');
INSERT INTO `datos_subareas` VALUES (279, 7, -1, 'Bordure de Bonta');
INSERT INTO `datos_subareas` VALUES (280, 11, -1, 'Bordure de Brâkmar');
INSERT INTO `datos_subareas` VALUES (284, 39, -1, '//Donjon des Bworks - Salle 1');
INSERT INTO `datos_subareas` VALUES (285, 39, -1, '//Donjon des Bworks - Salle 2');
INSERT INTO `datos_subareas` VALUES (286, 39, -1, '//Donjon des Bworks - Salle 3');
INSERT INTO `datos_subareas` VALUES (287, 39, -1, '//Donjon des Bworks - Cachot');
INSERT INTO `datos_subareas` VALUES (288, 39, -1, '//Donjon des Bworks - Salle 4');
INSERT INTO `datos_subareas` VALUES (289, 39, -1, '//Donjon des Bworks - Salle 5');
INSERT INTO `datos_subareas` VALUES (290, 39, -1, '//Donjon des Bworks - Salle 6');
INSERT INTO `datos_subareas` VALUES (291, 39, -1, '//Donjon des Bworks - Salle 7');
INSERT INTO `datos_subareas` VALUES (292, 33, -1, '//Donjon des Canidés - Salle 11');
INSERT INTO `datos_subareas` VALUES (293, 33, -1, '//Donjon des Canidés - Salle 12');
INSERT INTO `datos_subareas` VALUES (294, 33, -1, '//Donjon des Canidés - Salle 13');
INSERT INTO `datos_subareas` VALUES (295, 33, -1, '//Donjon des Canidés - Salle 14');
INSERT INTO `datos_subareas` VALUES (296, 33, -1, '//Donjon des Canidés - Salle 15');
INSERT INTO `datos_subareas` VALUES (297, 40, -1, '//Donjon des Scarafeuilles - Salle 1');
INSERT INTO `datos_subareas` VALUES (298, 40, -1, '//Donjon des Scarafeuilles - Salle 2');
INSERT INTO `datos_subareas` VALUES (299, 40, -1, '//Donjon des Scarafeuilles - Salle 3');
INSERT INTO `datos_subareas` VALUES (300, 40, -1, '//Donjon des Scarafeuilles - Salle 4');
INSERT INTO `datos_subareas` VALUES (301, 40, -1, '//Donjon des Scarafeuilles - Salle 5');
INSERT INTO `datos_subareas` VALUES (302, 40, -1, '//Donjon des Scarafeuilles - Salle 6');
INSERT INTO `datos_subareas` VALUES (303, 40, -1, '//Donjon des Scarafeuilles - Salle 7');
INSERT INTO `datos_subareas` VALUES (304, 40, -1, '//Donjon des Scarafeuilles - Salle 8');
INSERT INTO `datos_subareas` VALUES (306, 41, -1, '//Donjon des Champs - Salle 1');
INSERT INTO `datos_subareas` VALUES (307, 41, -1, '//Donjon des Champs - Salle 2');
INSERT INTO `datos_subareas` VALUES (308, 41, -1, '//Donjon des Champs - Salle 3');
INSERT INTO `datos_subareas` VALUES (309, 41, -1, '//Donjon des Champs - Salle 4');
INSERT INTO `datos_subareas` VALUES (310, 41, -1, '//Donjon des Champs - Salle 5');
INSERT INTO `datos_subareas` VALUES (311, 41, -1, '//Donjon des Champs - Salle 6');
INSERT INTO `datos_subareas` VALUES (312, 41, -1, '//Donjon des Champs - Salle 7');
INSERT INTO `datos_subareas` VALUES (313, 29, -1, '//Donjon des Tofus - Salle 16');
INSERT INTO `datos_subareas` VALUES (314, 0, -1, 'Sanctuaire des Dragoeufs');
INSERT INTO `datos_subareas` VALUES (315, 0, -1, 'Village des Dragoeufs');
INSERT INTO `datos_subareas` VALUES (316, 0, -1, 'Sous terrain des Dragoeufs');
INSERT INTO `datos_subareas` VALUES (317, 37, -1, '//Donjon des Craqueleurs - Salle 9');
INSERT INTO `datos_subareas` VALUES (318, 18, -1, 'Alkatraz');
INSERT INTO `datos_subareas` VALUES (319, 31, -1, 'Le labyrinthe du Minotoror');
INSERT INTO `datos_subareas` VALUES (320, 42, -1, 'L\'île de Nowel');
INSERT INTO `datos_subareas` VALUES (321, 42, -1, 'Le donjon de Nowel');
INSERT INTO `datos_subareas` VALUES (322, 43, -1, '//Donjon Cochon - Salle 7');
INSERT INTO `datos_subareas` VALUES (323, 43, -1, '//Donjon Cochon - Salle 8');
INSERT INTO `datos_subareas` VALUES (324, 43, -1, '//Donjon Cochon - Salle 9');
INSERT INTO `datos_subareas` VALUES (325, 44, -1, '//Donjon Dragoeuf - Salle 1');
INSERT INTO `datos_subareas` VALUES (326, 44, -1, '//Donjon Dragoeuf - Salle 2');
INSERT INTO `datos_subareas` VALUES (327, 44, -1, '//Donjon Dragoeuf - Salle 3');
INSERT INTO `datos_subareas` VALUES (328, 44, -1, '//Donjon Dragoeuf - Salle 4');
INSERT INTO `datos_subareas` VALUES (329, 44, -1, '//Donjon Dragoeuf - Salle 5');
INSERT INTO `datos_subareas` VALUES (330, 44, -1, '//Donjon Dragoeuf - Salle 6');
INSERT INTO `datos_subareas` VALUES (331, 44, -1, '//Donjon Dragoeuf - Salle 7');
INSERT INTO `datos_subareas` VALUES (332, 44, -1, '//Donjon Dragoeuf - Salle 8');
INSERT INTO `datos_subareas` VALUES (333, 44, -1, '//Donjon Dragoeuf - Salle 9');
INSERT INTO `datos_subareas` VALUES (334, 8, -1, 'Baie de Cania');
INSERT INTO `datos_subareas` VALUES (335, 18, -1, 'Calanques d\'Astrub');
INSERT INTO `datos_subareas` VALUES (336, 18, -1, 'Donjon Ensablé ');
INSERT INTO `datos_subareas` VALUES (337, 7, -1, 'Donjon des Rats de Bonta');
INSERT INTO `datos_subareas` VALUES (338, 11, -1, 'Donjon des Rats de Brâkmar');
INSERT INTO `datos_subareas` VALUES (339, 0, -1, 'Donjon des Rats du Château d\'Amakna');
INSERT INTO `datos_subareas` VALUES (440, 45, -1, 'Pitons rocheux');
INSERT INTO `datos_subareas` VALUES (441, 45, -1, 'Clairière');
INSERT INTO `datos_subareas` VALUES (442, 45, -1, 'Lac');
INSERT INTO `datos_subareas` VALUES (443, 45, -1, 'Forêt');
INSERT INTO `datos_subareas` VALUES (444, 45, -1, 'Champs');
INSERT INTO `datos_subareas` VALUES (445, 45, -1, 'Prairie');
INSERT INTO `datos_subareas` VALUES (446, 45, -1, 'Temple');
INSERT INTO `datos_subareas` VALUES (447, 45, -1, 'Donjon');
INSERT INTO `datos_subareas` VALUES (448, 45, -1, '//Divers');
INSERT INTO `datos_subareas` VALUES (449, 45, -1, 'Cimetière');
INSERT INTO `datos_subareas` VALUES (450, 45, -1, '//Sortie du temple');
INSERT INTO `datos_subareas` VALUES (451, 46, -1, 'Ile des naufragés');
INSERT INTO `datos_subareas` VALUES (452, 46, -1, 'Mer');
INSERT INTO `datos_subareas` VALUES (453, 46, -1, 'Plage de Corail');
INSERT INTO `datos_subareas` VALUES (454, 46, -1, 'Plaines herbeuses');
INSERT INTO `datos_subareas` VALUES (455, 46, -1, 'Jungle obscure');
INSERT INTO `datos_subareas` VALUES (457, 46, -1, 'Tourbière sans fond');
INSERT INTO `datos_subareas` VALUES (459, 46, -1, 'Canopée du Kimbo');
INSERT INTO `datos_subareas` VALUES (460, 46, -1, 'Grotte Hesque');
INSERT INTO `datos_subareas` VALUES (461, 46, -1, 'L\'arche d\'Otomaï ');
INSERT INTO `datos_subareas` VALUES (462, 46, -1, 'La clairière de Floribonde');
INSERT INTO `datos_subareas` VALUES (463, 46, -1, 'Le laboratoire du Tynril');
INSERT INTO `datos_subareas` VALUES (464, 46, -1, 'Tronc de l\'arbre Hakam');
INSERT INTO `datos_subareas` VALUES (465, 46, -1, 'Le village des éleveurs');
INSERT INTO `datos_subareas` VALUES (466, 46, -1, 'Le village côtier');
INSERT INTO `datos_subareas` VALUES (467, 46, -1, 'Cimetière de l\'île d\'Otomaï ');
INSERT INTO `datos_subareas` VALUES (468, 47, -1, '//Village des Zoths');
INSERT INTO `datos_subareas` VALUES (469, 46, -1, 'Village de la Canopée');
INSERT INTO `datos_subareas` VALUES (470, 46, -1, 'Goulet du Rasboul');
INSERT INTO `datos_subareas` VALUES (471, 46, -1, 'Tourbière nauséabonde');
INSERT INTO `datos_subareas` VALUES (472, 46, -1, 'Feuillage de L\'arbre Hakam');
INSERT INTO `datos_subareas` VALUES (473, 46, -1, 'Le laboratoire caché ');
INSERT INTO `datos_subareas` VALUES (474, 46, -1, 'Cale de l\'arche d\'Otomaï ');
INSERT INTO `datos_subareas` VALUES (476, 19, -1, 'Pont de Grobe');
INSERT INTO `datos_subareas` VALUES (477, 47, -1, 'Prisme Zothier');
INSERT INTO `datos_subareas` VALUES (478, 47, -1, '//Gardiens de la porte des Zoths');
INSERT INTO `datos_subareas` VALUES (479, 0, -1, 'Rivière Kawaii');
INSERT INTO `datos_subareas` VALUES (480, 0, -1, 'Montagne basse des Craqueleurs');
INSERT INTO `datos_subareas` VALUES (481, 0, -1, 'Clairière de Brouce Boulgour');
INSERT INTO `datos_subareas` VALUES (482, 0, -1, 'La Millifutaie ');
INSERT INTO `datos_subareas` VALUES (483, 0, -1, 'Le chemin de fer abandonné ');
INSERT INTO `datos_subareas` VALUES (484, 0, -1, 'Orée de la Millifutaie');
INSERT INTO `datos_subareas` VALUES (485, 0, -1, 'La campagne');
INSERT INTO `datos_subareas` VALUES (486, 0, -1, 'Le Bosquet du petit talus');
INSERT INTO `datos_subareas` VALUES (487, 19, -1, 'Grenier-Cachot de Pandala Air');
INSERT INTO `datos_subareas` VALUES (488, 0, -1, 'Souterrains d\'Amakna');
INSERT INTO `datos_subareas` VALUES (490, 0, -1, 'Rivage du golfe sufokien');
INSERT INTO `datos_subareas` VALUES (491, 0, -1, 'Donjon des Larves');
INSERT INTO `datos_subareas` VALUES (492, 0, -1, 'Passage vers Brâkmar');
INSERT INTO `datos_subareas` VALUES (493, 8, -1, 'Donjon des Blops');
INSERT INTO `datos_subareas` VALUES (494, 12, -1, 'Donjon Fungus');
INSERT INTO `datos_subareas` VALUES (495, 12, -1, 'Caverne des Fungus');
INSERT INTO `datos_subareas` VALUES (496, 12, -1, 'Donjon de Ku\'tan');
INSERT INTO `datos_subareas` VALUES (497, 8, -1, 'Donjon d\'Ilyzaelle');
INSERT INTO `datos_subareas` VALUES (498, 8, -1, 'Sanctuaire Hotomani');
INSERT INTO `datos_subareas` VALUES (499, 30, -1, 'Cimetière de l\'île du Minotoror');
INSERT INTO `datos_subareas` VALUES (500, 47, -1, 'Cimetière du village des Zoths.');
INSERT INTO `datos_subareas` VALUES (501, 0, -1, 'L\'îlot Estitch');
INSERT INTO `datos_subareas` VALUES (502, 11, -1, 'Quartier des bûcherons');
INSERT INTO `datos_subareas` VALUES (503, 11, -1, 'Quartier des bouchers');
INSERT INTO `datos_subareas` VALUES (504, 11, -1, 'Quartier de la milice');
INSERT INTO `datos_subareas` VALUES (505, 11, -1, 'Quartier des boulangers');
INSERT INTO `datos_subareas` VALUES (506, 11, -1, 'Quartier des bijoutiers');
INSERT INTO `datos_subareas` VALUES (507, 11, -1, 'Quartier des tailleurs');
INSERT INTO `datos_subareas` VALUES (508, 11, -1, 'Quartier des forgerons');
INSERT INTO `datos_subareas` VALUES (509, 11, -1, 'Quartier des bricoleurs');
INSERT INTO `datos_subareas` VALUES (510, 11, -1, 'Arène');
INSERT INTO `datos_subareas` VALUES (511, 11, -1, 'Centre-ville');
INSERT INTO `datos_subareas` VALUES (512, 7, -1, 'Arène');
INSERT INTO `datos_subareas` VALUES (513, 7, -1, 'Centre-ville');
INSERT INTO `datos_subareas` VALUES (514, 7, -1, 'Tour des ordres');
INSERT INTO `datos_subareas` VALUES (515, 11, -1, 'Tour des ordres');
INSERT INTO `datos_subareas` VALUES (536, 0, -1, 'Goultarminator');

SET FOREIGN_KEY_CHECKS = 1;
