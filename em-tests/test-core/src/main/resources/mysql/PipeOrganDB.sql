-- MySQL dump 10.13  Distrib 5.5.12, for Linux (x86_64)
--
-- Host: localhost    Database: test
-- ------------------------------------------------------
-- Server version	5.5.12-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `ACCOUNT`
--

DROP TABLE IF EXISTS `ACCOUNT`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ACCOUNT` (
  `userid` varchar(80) NOT NULL,
  `email` varchar(80) NOT NULL,
  `firstname` varchar(80) NOT NULL,
  `lastname` varchar(80) NOT NULL,
  `status` varchar(2) DEFAULT NULL,
  `addr1` varchar(80) NOT NULL,
  `addr2` varchar(40) DEFAULT NULL,
  `city` varchar(80) NOT NULL,
  `state` varchar(80) NOT NULL,
  `zip` varchar(20) NOT NULL,
  `country` varchar(20) NOT NULL,
  `phone` varchar(80) NOT NULL,
  PRIMARY KEY (`userid`)
) ENGINE=InnoDB DEFAULT CHARSET=UTF8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ACCOUNT`
--

-- LOCK TABLES `ACCOUNT` WRITE;
/*!40000 ALTER TABLE `ACCOUNT` DISABLE KEYS */;
INSERT INTO `ACCOUNT` VALUES ('acid','acid@users.com','acid','base','OK','901 San Antonio Rd.','Suite 101','Palo Alto','CA','94303','USA','555-555-5555'),('j2ee','j2ee@users.com','awesome','supers','OK','1 CA Pl','Suite 1010','Island','NY','11665','USA','222-222-2222'),('user1','user1@users.com','user','one','OK','1 User St.','Apt 1','Userville','AL','12345','USA','222-222-2222'),('user10','user10@users.com','user','ten','OK','10 User St.','Apt 10','Userville','ID','87654','USA','876-543-2109'),('user11','user11@users.com','user','eleven','OK','11 User St.','Apt 11','Userville','IL','76543','USA','765-432-1098'),('user12','user12@users.com','user','twelve','OK','12 User St.','Apt 12','Userville','IN','65432','USA','654-321-0987'),('user13','user13@users.com','user','thirteen','OK','13 User St.','Apt 13','Userville','IO','54321','USA','543-210-9876'),('user14','user14@users.com','user','fourteen','OK','14 User St.','Apt 14','Userville','KS','43210','USA','432-109-8765'),('user15','user15@users.com','user','fifteen','OK','15 User St.','Apt 15','Userville','KY','32109','USA','321-098-7654'),('user16','user16@users.com','user','sixteen','OK','16 User St.','Apt 16','Userville','ME','21098','USA','210-987-6543'),('user17','user17@users.com','user','seventeen','OK','17 User St.','Apt 17','Userville','MD','10987','USA','246-801-3579'),('user18','user18@users.com','user','eighteen','OK','18 User St.','Apt 18','Userville','MA','13579','USA','468-135-7924'),('user19','user19@users.com','user','nineteen','OK','19 User St.','Apt 19','Userville','MI','24680','USA','680-357-9135'),('user2','user2@users.com','user','two','OK','2 User St.','Apt 2','Userville','AK','23456','USA','333-333-3333'),('user20','user20@users.com','user','twenty','OK','20 User St.','Apt 20','Userville','MN','10293','USA','802-579-1357'),('user21','user21@users.com','user','twenty-one','OK','21 User St.','Suite 101','Userville','NJ','09828','USA','555-333-1223'),('user3','user3@users.com','user','three','OK','3 User St.','Apt 3','Userville','AR','34567','USA','444-444-4444'),('user4','user4@users.com','user','four','OK','4 User St.','Apt 4','Userville','CA','45678','USA','555-555-5555'),('user5','user5@users.com','user','five','OK','5 User St.','Apt 5','Userville','CO','56789','USA','666-666-6666'),('user6','user6@users.com','user','six','OK','6 User St.','Apt 6','Userville','CT','67890','USA','777-777-7777'),('user7','user7@users.com','user','seven','OK','7 User St.','Apt 7','Userville','DE','01234','USA','888-888-8888'),('user8','user8@users.com','user','eight','OK','8 User St.','Apt 8','Userville','FL','09876','USA','999-999-9999'),('user9','user9@users.com','user','nueve','OK','9 User St.','Apt 9','Userville','HI','98765','USA','987-654-3210');
/*!40000 ALTER TABLE `ACCOUNT` ENABLE KEYS */;
-- UNLOCK TABLES;
