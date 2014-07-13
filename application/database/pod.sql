SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

DROP SCHEMA IF EXISTS `pod` ;
CREATE SCHEMA IF NOT EXISTS `pod` DEFAULT CHARACTER SET utf8 ;
USE `pod` ;

-- -----------------------------------------------------
-- Table `pod`.`activities`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `pod`.`activities` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(45) NOT NULL ,
  `installationScriptLocation` VARCHAR(150) NULL DEFAULT NULL ,
  `status` VARCHAR(45) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  UNIQUE INDEX `name_UNIQUE` (`name` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `pod`.`workers`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `pod`.`workers` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `status` VARCHAR(45) NULL DEFAULT NULL ,
  `public_ip` VARCHAR(45) NULL DEFAULT NULL ,
  `local_ip` VARCHAR(45) NULL DEFAULT NULL ,
  `instance_id` VARCHAR(45) NULL DEFAULT NULL ,
  `is_manager` INT(1) NOT NULL DEFAULT 0 ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB
AUTO_INCREMENT = 433
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Table `pod`.`installations`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `pod`.`installations` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `activityId` INT(11) NOT NULL ,
  `workerId` INT(11) NOT NULL ,
  `status` VARCHAR(45) NULL DEFAULT NULL ,
  `errorDescription` VARCHAR(250) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_installations_1` (`activityId` ASC) ,
  INDEX `fk_installations_2` (`workerId` ASC) ,
  CONSTRAINT `fk_installations_1`
    FOREIGN KEY (`activityId` )
    REFERENCES `pod`.`activities` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_installations_2`
    FOREIGN KEY (`workerId` )
    REFERENCES `pod`.`workers` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `pod`.`policies`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `pod`.`policies` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(45) NOT NULL ,
  `active` INT(1) NOT NULL DEFAULT '0' ,
  `rules` VARCHAR(255) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  UNIQUE INDEX `name_UNIQUE` (`name` ASC) )
ENGINE = InnoDB
AUTO_INCREMENT = 2
DEFAULT CHARACTER SET = utf8;



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
