SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

DROP SCHEMA IF EXISTS `kaerus_core` ;
CREATE SCHEMA IF NOT EXISTS `kaerus_core` DEFAULT CHARACTER SET utf8 ;
USE `kaerus_core` ;

-- -----------------------------------------------------
-- Table `kaerus_core`.`activities`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `kaerus_core`.`activities` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(45) NULL DEFAULT NULL ,
  `workGroupId` INT(11) NULL DEFAULT NULL ,
  `repoURL` VARCHAR(255) NULL DEFAULT NULL ,
  `nextActivityId` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  UNIQUE INDEX `name_UNIQUE` (`name` ASC) ,
  INDEX `fk_task_definitions_1` (`workGroupId` ASC) )
ENGINE = InnoDB
AUTO_INCREMENT = 2
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `kaerus_core`.`executions`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `kaerus_core`.`executions` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `input` TEXT NULL DEFAULT NULL ,
  `output` TEXT NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `kaerus_core`.`execution_events`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `kaerus_core`.`execution_events` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `executionId` INT(11) NULL DEFAULT NULL ,
  `message` VARCHAR(45) NULL DEFAULT NULL ,
  `date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  `metadata` TEXT NULL DEFAULT NULL ,
  `metadataType` VARCHAR(45) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `executions_table` (`executionId` ASC) ,
  INDEX `fk_execution_events_1` (`date` ASC) ,
  CONSTRAINT `fk_execution_events_1`
    FOREIGN KEY (`executionId` )
    REFERENCES `kaerus_core`.`executions` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `kaerus_core`.`task_events`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `kaerus_core`.`task_events` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `activityId` INT(11) NULL DEFAULT NULL ,
  `activityName` VARCHAR(45) NULL DEFAULT NULL ,
  `workerId` INT(11) NULL DEFAULT NULL ,
  `executionId` INT(11) NULL DEFAULT NULL ,
  `message` VARCHAR(45) NULL DEFAULT NULL ,
  `metadataType` VARCHAR(45) NULL DEFAULT NULL ,
  `metadata` TEXT NULL DEFAULT NULL ,
  `date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `kaerus_core`.`tasks`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `kaerus_core`.`tasks` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `activityId` INT(11) NULL DEFAULT NULL ,
  `executionId` INT(11) NULL DEFAULT NULL ,
  `input` TEXT NULL DEFAULT NULL ,
  `output` TEXT NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `work_groups` (`activityId` ASC) ,
  INDEX `executions` (`executionId` ASC) ,
  INDEX `fk_task_instances_1` (`activityId` ASC) ,
  CONSTRAINT `executions`
    FOREIGN KEY (`executionId` )
    REFERENCES `kaerus_core`.`executions` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_task_instances_1`
    FOREIGN KEY (`activityId` )
    REFERENCES `kaerus_core`.`activities` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `kaerus_core`.`work_groups`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `kaerus_core`.`work_groups` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(45) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  UNIQUE INDEX `name_UNIQUE` (`name` ASC) )
ENGINE = InnoDB
AUTO_INCREMENT = 3
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `kaerus_core`.`worker_events`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `kaerus_core`.`worker_events` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `workerId` INT(11) NULL DEFAULT NULL ,
  `activityId` INT(11) NULL DEFAULT NULL ,
  `activityName` VARCHAR(45) NULL DEFAULT NULL ,
  `message` VARCHAR(45) NULL DEFAULT NULL ,
  `date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `kaerus_core`.`workers`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `kaerus_core`.`workers` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `state` VARCHAR(45) NULL DEFAULT NULL ,
  `status` VARCHAR(45) NULL DEFAULT NULL ,
  `instanceId` VARCHAR(45) NOT NULL ,
  `workGroupId` INT(11) NULL DEFAULT NULL ,
  `taskId` INT(11) NULL DEFAULT NULL ,
  `lastUpdate` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  `launchedAt` TIMESTAMP NULL DEFAULT NULL ,
  `deployedAt` TIMESTAMP NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `work_group_table` (`workGroupId` ASC) ,
  CONSTRAINT `work_group_table`
    FOREIGN KEY (`workGroupId` )
    REFERENCES `kaerus_core`.`work_groups` (`id` ))
ENGINE = InnoDB
AUTO_INCREMENT = 7
DEFAULT CHARACTER SET = utf8;



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
