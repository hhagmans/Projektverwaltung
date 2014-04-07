# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table employee (
  id                        varchar(255) not null,
  name                      varchar(255),
  constraint pk_employee primary key (id))
;

create table project (
  id                        integer not null,
  name                      varchar(255),
  description               varchar(255),
  wiki_link                 varchar(255),
  active                    boolean,
  start_date                timestamp,
  end_date                  timestamp,
  principalConsultant       varchar(255),
  constraint pk_project primary key (id))
;

create table project_employee (
  project_employee_id       integer not null,
  employee_id               varchar(255),
  project_id                integer,
  start_date                timestamp,
  end_date                  timestamp,
  constraint pk_project_employee primary key (project_employee_id))
;

create sequence employee_seq;

create sequence project_seq;

create sequence project_employee_seq;

alter table project add constraint fk_project_principalConsultant_1 foreign key (principalConsultant) references employee (id) on delete restrict on update restrict;
create index ix_project_principalConsultant_1 on project (principalConsultant);



# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists employee;

drop table if exists project;

drop table if exists project_employee;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists employee_seq;

drop sequence if exists project_seq;

drop sequence if exists project_employee_seq;

