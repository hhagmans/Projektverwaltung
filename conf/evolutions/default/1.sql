# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table employee (
  id                        varchar(255) not null,
  name                      varchar(255),
  constraint pk_employee primary key (id))
;

create table project (
  id                        bigint auto_increment not null,
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
  start_date                datetime,
  end_date                  datetime,
  employeeId                varchar(255),
  projectId                 bigint)
;

alter table project add constraint fk_project_principalConsultant_1 foreign key (principalConsultant) references employee (id) on delete restrict on update restrict;
create index ix_project_principalConsultant_1 on project (principalConsultant);
alter table project_employee add constraint fk_project_employee_employee_2 foreign key (employeeId) references employee (id) on delete restrict on update restrict;
create index ix_project_employee_employee_2 on project_employee (employeeId);
alter table project_employee add constraint fk_project_employee_project_3 foreign key (projectId) references project (id) on delete restrict on update restrict;
create index ix_project_employee_project_3 on project_employee (projectId);



# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table employee;

drop table project;

drop table project_employee;

SET FOREIGN_KEY_CHECKS=1;

