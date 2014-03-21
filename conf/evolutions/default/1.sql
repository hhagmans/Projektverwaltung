# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table employee (
  uid                       varchar(255) not null,
  name                      varchar(255),
  constraint pk_employee primary key (uid))
;

create table project (
  id                        varchar(255) not null,
  name                      varchar(255),
  description               varchar(255),
  wiki_link                 varchar(255),
  active                    tinyint(1) default 0,
  constraint pk_project primary key (id))
;




# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table employee;

drop table project;

SET FOREIGN_KEY_CHECKS=1;

