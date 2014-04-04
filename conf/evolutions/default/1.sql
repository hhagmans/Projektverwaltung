# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table employee (
  id                        varchar(255) not null,
  name                      varchar(255),
  constraint pk_employee primary key (id))
;

create table project (
  id                        integer auto_increment not null,
  name                      varchar(255),
  description               varchar(255),
  wiki_link                 varchar(255),
  active                    tinyint(1) default 0,
  start_date                datetime,
  end_date                  datetime,
  principalConsultant       varchar(255),
  constraint pk_project primary key (id))
;

create table project_employee (
  project_employee_id       integer auto_increment not null,
  employee_id               varchar(255),
  project_id                integer,
  start_date                datetime,
  end_date                  datetime,
  employee                  varchar(255),
  project                   integer,
  constraint pk_project_employee primary key (project_employee_id))
;

alter table project add constraint fk_project_principalConsultant_1 foreign key (principalConsultant) references employee (id) on delete restrict on update restrict;
create index ix_project_principalConsultant_1 on project (principalConsultant);
alter table project_employee add constraint fk_project_employee_employee_2 foreign key (employee) references employee (id) on delete restrict on update restrict;
create index ix_project_employee_employee_2 on project_employee (employee);
alter table project_employee add constraint fk_project_employee_project_3 foreign key (project) references project (id) on delete restrict on update restrict;
create index ix_project_employee_project_3 on project_employee (project);



# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table employee;

drop table project;

drop table project_employee;

SET FOREIGN_KEY_CHECKS=1;

