create sequence portal.departments_id_seq start 1 increment 1
create sequence portal.forbidden_resources_id_seq start 1 increment 1
create sequence portal.formal_tasks_id_seq start 1 increment 1
create sequence portal.informal_tasks_id_seq start 1 increment 1
create sequence portal.organizations_id_seq start 1 increment 1
create sequence portal.search_systems_id_seq start 1 increment 1
create sequence portal.tas_jobs_id_seq start 1 increment 1
create sequence portal.user_roles_id_seq start 1 increment 1
create sequence portal.users_id_seq start 1 increment 1
create sequence portal.vpn_list_id_seq start 1 increment 1

    create table portal.departments (
       id int8 not null,
        name varchar(255) not null,
        parent_id int8,
        primary key (id)
    )

    create table portal.forbidden_resources (
       id int8 not null,
        creation_date timestamp,
        modification_date timestamp,
        name varchar(255) not null,
        url varchar(255) not null,
        organization_id int8 not null,
        primary key (id)
    )

    create table portal.forbidden_resources_task_jobs (
       forbidden_resource_id int8 not null,
        task_job_id int8 not null
    )

    create table portal.formal_tasks (
       id int8 not null,
        creation_date timestamp,
        end_date timestamp,
        modification_date timestamp,
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        author_id int8 not null,
        informal_task_id int8,
        primary key (id)
    )

    create table portal.informal_tasks (
       id int8 not null,
        author varchar(255),
        confirmed boolean not null,
        content bytea,
        creation_date timestamp,
        title varchar(255),
        primary key (id)
    )

    create table portal.organizations (
       id int8 not null,
        name varchar(255) not null,
        primary key (id)
    )

    create table portal.search_systems (
       id int8 not null,
        name varchar(255) not null,
        primary key (id)
    )

    create table portal.search_systems_task_jobs (
       search_system_id int8 not null,
        task_job_id int8 not null
    )

    create table portal.task_jobs (
       id int8 not null,
        creation_date timestamp,
        end_date timestamp,
        result varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        formal_task_id int8 not null,
        primary key (id)
    )

    create table portal.user_roles (
       id int8 not null,
        role varchar(255) not null,
        primary key (id)
    )

    create table portal.user_roles_users (
       user_role_id int8 not null,
        user_id int8 not null
    )

    create table portal.users (
       id int8 not null,
        first_name varchar(255),
        second_name varchar(255),
        user_name varchar(255) not null,
        department_id int8,
        primary key (id)
    )

    create table portal.vpn (
       id int8 not null,
        name varchar(255) not null,
        primary key (id)
    )

    create table portal.vpn_task_jobs (
       vpn_id int8 not null,
        task_job_id int8 not null
    )

    alter table portal.users 
       add constraint UK_k8d0f2n7n88w1a16yhua64onx unique (user_name)

    alter table portal.departments 
       add constraint FK63q917a0aq92i7gcw6h7f1jrv 
       foreign key (parent_id) 
       references portal.departments

    alter table portal.forbidden_resources 
       add constraint FK7cf12t0hwhus5gmno0apu6o56 
       foreign key (organization_id) 
       references portal.organizations

    alter table portal.forbidden_resources_task_jobs 
       add constraint FKplu0gq74h6p9yo6yatjk4dtfd 
       foreign key (task_job_id) 
       references portal.task_jobs

    alter table portal.forbidden_resources_task_jobs 
       add constraint FKsos5v3b48ewx8ap120h06o1ea 
       foreign key (forbidden_resource_id) 
       references portal.forbidden_resources

    alter table portal.formal_tasks 
       add constraint FK7pc90p1o1ag8pgds0asgeymt8 
       foreign key (author_id) 
       references portal.users

    alter table portal.formal_tasks 
       add constraint FK3xpi0noactbkjx6lmp5jnetda 
       foreign key (informal_task_id) 
       references portal.informal_tasks

    alter table portal.search_systems_task_jobs 
       add constraint FKt7lhbvp9rc7pomugfcwbowad2 
       foreign key (task_job_id) 
       references portal.task_jobs

    alter table portal.search_systems_task_jobs 
       add constraint FK7fiv14k3dc6l7igdhiht09utx 
       foreign key (search_system_id) 
       references portal.search_systems

    alter table portal.task_jobs 
       add constraint FKq02vl1ddetuu6ahdlict4dr1o 
       foreign key (formal_task_id) 
       references portal.formal_tasks

    alter table portal.user_roles_users 
       add constraint FKq0c5875e1xlfeoxwqkrmq2gla 
       foreign key (user_id) 
       references portal.users

    alter table portal.user_roles_users 
       add constraint FKaa324ufa5d9jcxt9mvb8etkm7 
       foreign key (user_role_id) 
       references portal.user_roles

    alter table portal.users 
       add constraint FKsbg59w8q63i0oo53rlgvlcnjq 
       foreign key (department_id) 
       references portal.departments

    alter table portal.vpn_task_jobs 
       add constraint FKi5jkcq0b8hsyhrhoowr7hl60f 
       foreign key (task_job_id) 
       references portal.task_jobs

    alter table portal.vpn_task_jobs 
       add constraint FKssnfox9rvq4fwetame52kxkvh 
       foreign key (vpn_id) 
       references portal.vpn
create sequence portal.departments_id_seq start 1 increment 1
create sequence portal.forbidden_resources_id_seq start 1 increment 1
create sequence portal.formal_tasks_id_seq start 1 increment 1
create sequence portal.informal_tasks_id_seq start 1 increment 1
create sequence portal.job_items_id_seq start 1 increment 1
create sequence portal.organizations_id_seq start 1 increment 1
create sequence portal.search_systems_id_seq start 1 increment 1
create sequence portal.tas_jobs_id_seq start 1 increment 1
create sequence portal.user_roles_id_seq start 1 increment 1
create sequence portal.users_id_seq start 1 increment 1
create sequence portal.vpn_list_id_seq start 1 increment 1

    create table portal.departments (
       id int8 not null,
        name varchar(255) not null,
        parent_id int8,
        primary key (id)
    )

    create table portal.forbidden_resources (
       id int8 not null,
        creation_date timestamp,
        modification_date timestamp,
        name varchar(255) not null,
        url varchar(255) not null,
        organization_id int8 not null,
        primary key (id)
    )

    create table portal.forbidden_resources_task_jobs (
       forbidden_resource_id int8 not null,
        task_job_id int8 not null
    )

    create table portal.formal_tasks (
       id int8 not null,
        creation_date timestamp,
        end_date timestamp,
        modification_date timestamp,
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        author_id int8 not null,
        informal_task_id int8,
        primary key (id)
    )

    create table portal.informal_tasks (
       id int8 not null,
        author varchar(255),
        confirmed boolean not null,
        content bytea,
        creation_date timestamp,
        title varchar(255),
        primary key (id)
    )

    create table portal.job_items (
       id int8 not null,
        content_id int8 not null,
        task_job_id int8 not null,
        primary key (id)
    )

    create table portal.organizations (
       id int8 not null,
        name varchar(255) not null,
        primary key (id)
    )

    create table portal.search_systems (
       id int8 not null,
        name varchar(255) not null,
        primary key (id)
    )

    create table portal.search_systems_task_jobs (
       search_system_id int8 not null,
        task_job_id int8 not null
    )

    create table portal.task_jobs (
       id int8 not null,
        creation_date timestamp,
        end_date timestamp,
        result varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        formal_task_id int8 not null,
        primary key (id)
    )

    create table portal.user_roles (
       id int8 not null,
        role varchar(255) not null,
        primary key (id)
    )

    create table portal.user_roles_users (
       user_role_id int8 not null,
        user_id int8 not null
    )

    create table portal.users (
       id int8 not null,
        first_name varchar(255),
        second_name varchar(255),
        user_name varchar(255) not null,
        department_id int8,
        primary key (id)
    )

    create table portal.vpn (
       id int8 not null,
        name varchar(255) not null,
        primary key (id)
    )

    create table portal.vpn_task_jobs (
       vpn_id int8 not null,
        task_job_id int8 not null
    )

    alter table portal.users 
       add constraint UK_k8d0f2n7n88w1a16yhua64onx unique (user_name)

    create table sa.content (
       id int8 not null,
        primary key (id)
    )

    alter table portal.departments 
       add constraint FK63q917a0aq92i7gcw6h7f1jrv 
       foreign key (parent_id) 
       references portal.departments

    alter table portal.forbidden_resources 
       add constraint FK7cf12t0hwhus5gmno0apu6o56 
       foreign key (organization_id) 
       references portal.organizations

    alter table portal.forbidden_resources_task_jobs 
       add constraint FK_forbidden_resources_task_jobs_task_job_id 
       foreign key (task_job_id) 
       references portal.task_jobs

    alter table portal.forbidden_resources_task_jobs 
       add constraint FK_forbidden_resources_task_jobs_resource_id 
       foreign key (forbidden_resource_id) 
       references portal.forbidden_resources

    alter table portal.formal_tasks 
       add constraint FK7pc90p1o1ag8pgds0asgeymt8 
       foreign key (author_id) 
       references portal.users

    alter table portal.formal_tasks 
       add constraint FK3xpi0noactbkjx6lmp5jnetda 
       foreign key (informal_task_id) 
       references portal.informal_tasks

    alter table portal.job_items 
       add constraint FK_job_items_content_id 
       foreign key (content_id) 
       references sa.content

    alter table portal.job_items 
       add constraint FK_job_items_job_id 
       foreign key (task_job_id) 
       references portal.task_jobs

    alter table portal.search_systems_task_jobs 
       add constraint FKt7lhbvp9rc7pomugfcwbowad2 
       foreign key (task_job_id) 
       references portal.task_jobs

    alter table portal.search_systems_task_jobs 
       add constraint FK7fiv14k3dc6l7igdhiht09utx 
       foreign key (search_system_id) 
       references portal.search_systems

    alter table portal.task_jobs 
       add constraint FKq02vl1ddetuu6ahdlict4dr1o 
       foreign key (formal_task_id) 
       references portal.formal_tasks

    alter table portal.user_roles_users 
       add constraint FKq0c5875e1xlfeoxwqkrmq2gla 
       foreign key (user_id) 
       references portal.users

    alter table portal.user_roles_users 
       add constraint FKaa324ufa5d9jcxt9mvb8etkm7 
       foreign key (user_role_id) 
       references portal.user_roles

    alter table portal.users 
       add constraint FKsbg59w8q63i0oo53rlgvlcnjq 
       foreign key (department_id) 
       references portal.departments

    alter table portal.vpn_task_jobs 
       add constraint FKi5jkcq0b8hsyhrhoowr7hl60f 
       foreign key (task_job_id) 
       references portal.task_jobs

    alter table portal.vpn_task_jobs 
       add constraint FKssnfox9rvq4fwetame52kxkvh 
       foreign key (vpn_id) 
       references portal.vpn
create sequence portal.departments_id_seq start 1 increment 1
create sequence portal.forbidden_resources_id_seq start 1 increment 1
create sequence portal.formal_tasks_id_seq start 1 increment 1
create sequence portal.informal_tasks_id_seq start 1 increment 1
create sequence portal.job_items_id_seq start 1 increment 1
create sequence portal.organizations_id_seq start 1 increment 1
create sequence portal.search_systems_id_seq start 1 increment 1
create sequence portal.tas_jobs_id_seq start 1 increment 1
create sequence portal.user_roles_id_seq start 1 increment 1
create sequence portal.users_id_seq start 1 increment 1
create sequence portal.vpn_list_id_seq start 1 increment 1

    create table portal.departments (
       id int8 not null,
        name varchar(255) not null,
        parent_id int8,
        primary key (id)
    )

    create table portal.forbidden_resources (
       id int8 not null,
        creation_date timestamp,
        modification_date timestamp,
        name varchar(255) not null,
        url varchar(255) not null,
        organization_id int8 not null,
        primary key (id)
    )

    create table portal.forbidden_resources_task_jobs (
       forbidden_resource_id int8 not null,
        task_job_id int8 not null
    )

    create table portal.formal_tasks (
       id int8 not null,
        creation_date timestamp,
        end_date timestamp,
        modification_date timestamp,
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        author_id int8 not null,
        informal_task_id int8,
        primary key (id)
    )

    create table portal.informal_tasks (
       id int8 not null,
        author varchar(255),
        confirmed boolean not null,
        content bytea,
        creation_date timestamp,
        title varchar(255),
        primary key (id)
    )

    create table portal.job_items (
       id int8 not null,
        content_id varchar(255) not null,
        task_job_id int8 not null,
        primary key (id)
    )

    create table portal.organizations (
       id int8 not null,
        name varchar(255) not null,
        primary key (id)
    )

    create table portal.search_systems (
       id int8 not null,
        name varchar(255) not null,
        primary key (id)
    )

    create table portal.search_systems_task_jobs (
       search_system_id int8 not null,
        task_job_id int8 not null
    )

    create table portal.task_jobs (
       id int8 not null,
        creation_date timestamp,
        end_date timestamp,
        result varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        formal_task_id int8 not null,
        primary key (id)
    )

    create table portal.user_roles (
       id int8 not null,
        role varchar(255) not null,
        primary key (id)
    )

    create table portal.user_roles_users (
       user_role_id int8 not null,
        user_id int8 not null
    )

    create table portal.users (
       id int8 not null,
        first_name varchar(255),
        second_name varchar(255),
        user_name varchar(255) not null,
        department_id int8,
        primary key (id)
    )

    create table portal.vpn (
       id int8 not null,
        name varchar(255) not null,
        primary key (id)
    )

    create table portal.vpn_task_jobs (
       vpn_id int8 not null,
        task_job_id int8 not null
    )

    alter table portal.users 
       add constraint UK_k8d0f2n7n88w1a16yhua64onx unique (user_name)

    create table sa.content (
       id varchar(255) not null,
        primary key (id)
    )

    alter table portal.departments 
       add constraint FK63q917a0aq92i7gcw6h7f1jrv 
       foreign key (parent_id) 
       references portal.departments

    alter table portal.forbidden_resources 
       add constraint FK7cf12t0hwhus5gmno0apu6o56 
       foreign key (organization_id) 
       references portal.organizations

    alter table portal.forbidden_resources_task_jobs 
       add constraint FK_forbidden_resources_task_jobs_task_job_id 
       foreign key (task_job_id) 
       references portal.task_jobs

    alter table portal.forbidden_resources_task_jobs 
       add constraint FK_forbidden_resources_task_jobs_resource_id 
       foreign key (forbidden_resource_id) 
       references portal.forbidden_resources

    alter table portal.formal_tasks 
       add constraint FK7pc90p1o1ag8pgds0asgeymt8 
       foreign key (author_id) 
       references portal.users

    alter table portal.formal_tasks 
       add constraint FK3xpi0noactbkjx6lmp5jnetda 
       foreign key (informal_task_id) 
       references portal.informal_tasks

    alter table portal.job_items 
       add constraint FK_job_items_content_id 
       foreign key (content_id) 
       references sa.content

    alter table portal.job_items 
       add constraint FK_job_items_job_id 
       foreign key (task_job_id) 
       references portal.task_jobs

    alter table portal.search_systems_task_jobs 
       add constraint FKt7lhbvp9rc7pomugfcwbowad2 
       foreign key (task_job_id) 
       references portal.task_jobs

    alter table portal.search_systems_task_jobs 
       add constraint FK7fiv14k3dc6l7igdhiht09utx 
       foreign key (search_system_id) 
       references portal.search_systems

    alter table portal.task_jobs 
       add constraint FKq02vl1ddetuu6ahdlict4dr1o 
       foreign key (formal_task_id) 
       references portal.formal_tasks

    alter table portal.user_roles_users 
       add constraint FKq0c5875e1xlfeoxwqkrmq2gla 
       foreign key (user_id) 
       references portal.users

    alter table portal.user_roles_users 
       add constraint FKaa324ufa5d9jcxt9mvb8etkm7 
       foreign key (user_role_id) 
       references portal.user_roles

    alter table portal.users 
       add constraint FKsbg59w8q63i0oo53rlgvlcnjq 
       foreign key (department_id) 
       references portal.departments

    alter table portal.vpn_task_jobs 
       add constraint FKi5jkcq0b8hsyhrhoowr7hl60f 
       foreign key (task_job_id) 
       references portal.task_jobs

    alter table portal.vpn_task_jobs 
       add constraint FKssnfox9rvq4fwetame52kxkvh 
       foreign key (vpn_id) 
       references portal.vpn
create sequence portal.departments_id_seq start 1 increment 1
create sequence portal.forbidden_resources_id_seq start 1 increment 1
create sequence portal.formal_tasks_id_seq start 1 increment 1
create sequence portal.informal_tasks_id_seq start 1 increment 1
create sequence portal.job_items_id_seq start 1 increment 1
create sequence portal.organizations_id_seq start 1 increment 1
create sequence portal.search_systems_id_seq start 1 increment 1
create sequence portal.tas_jobs_id_seq start 1 increment 1
create sequence portal.user_roles_id_seq start 1 increment 1
create sequence portal.users_id_seq start 1 increment 1
create sequence portal.vpn_list_id_seq start 1 increment 1

    create table portal.departments (
       id int8 not null,
        name varchar(255) not null,
        parent_id int8,
        primary key (id)
    )

    create table portal.forbidden_resources (
       id int8 not null,
        creation_date timestamp,
        modification_date timestamp,
        name varchar(255) not null,
        url varchar(255) not null,
        organization_id int8 not null,
        primary key (id)
    )

    create table portal.forbidden_resources_task_jobs (
       forbidden_resource_id int8 not null,
        task_job_id int8 not null
    )

    create table portal.formal_tasks (
       id int8 not null,
        creation_date timestamp,
        end_date timestamp,
        modification_date timestamp,
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        author_id int8 not null,
        informal_task_id int8,
        primary key (id)
    )

    create table portal.informal_tasks (
       id int8 not null,
        author varchar(255),
        confirmed boolean not null,
        content bytea,
        creation_date timestamp,
        title varchar(255),
        primary key (id)
    )

    create table portal.job_items (
       id int8 not null,
        content_id varchar(255) not null,
        task_job_id int8 not null,
        primary key (id)
    )

    create table portal.organizations (
       id int8 not null,
        name varchar(255) not null,
        primary key (id)
    )

    create table portal.search_systems (
       id int8 not null,
        name varchar(255) not null,
        primary key (id)
    )

    create table portal.search_systems_task_jobs (
       search_system_id int8 not null,
        task_job_id int8 not null
    )

    create table portal.task_jobs (
       id int8 not null,
        creation_date timestamp,
        end_date timestamp,
        result varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        formal_task_id int8 not null,
        primary key (id)
    )

    create table portal.user_roles (
       id int8 not null,
        role varchar(255) not null,
        primary key (id)
    )

    create table portal.user_roles_users (
       user_role_id int8 not null,
        user_id int8 not null
    )

    create table portal.users (
       id int8 not null,
        first_name varchar(255),
        second_name varchar(255),
        user_name varchar(255) not null,
        department_id int8,
        primary key (id)
    )

    create table portal.vpn (
       id int8 not null,
        name varchar(255) not null,
        primary key (id)
    )

    create table portal.vpn_task_jobs (
       vpn_id int8 not null,
        task_job_id int8 not null
    )

    alter table portal.users 
       add constraint UK_k8d0f2n7n88w1a16yhua64onx unique (user_name)

    create table sa.content (
       id varchar(255) not null,
        primary key (id)
    )

    alter table portal.departments 
       add constraint FK63q917a0aq92i7gcw6h7f1jrv 
       foreign key (parent_id) 
       references portal.departments

    alter table portal.forbidden_resources 
       add constraint FK7cf12t0hwhus5gmno0apu6o56 
       foreign key (organization_id) 
       references portal.organizations

    alter table portal.forbidden_resources_task_jobs 
       add constraint FK_forbidden_resources_task_jobs_task_job_id 
       foreign key (task_job_id) 
       references portal.task_jobs

    alter table portal.forbidden_resources_task_jobs 
       add constraint FK_forbidden_resources_task_jobs_resource_id 
       foreign key (forbidden_resource_id) 
       references portal.forbidden_resources

    alter table portal.formal_tasks 
       add constraint FK7pc90p1o1ag8pgds0asgeymt8 
       foreign key (author_id) 
       references portal.users

    alter table portal.formal_tasks 
       add constraint FK3xpi0noactbkjx6lmp5jnetda 
       foreign key (informal_task_id) 
       references portal.informal_tasks

    alter table portal.job_items 
       add constraint FK_job_items_content_id 
       foreign key (content_id) 
       references sa.content

    alter table portal.job_items 
       add constraint FK_job_items_job_id 
       foreign key (task_job_id) 
       references portal.task_jobs

    alter table portal.search_systems_task_jobs 
       add constraint FKt7lhbvp9rc7pomugfcwbowad2 
       foreign key (task_job_id) 
       references portal.task_jobs

    alter table portal.search_systems_task_jobs 
       add constraint FK7fiv14k3dc6l7igdhiht09utx 
       foreign key (search_system_id) 
       references portal.search_systems

    alter table portal.task_jobs 
       add constraint FKq02vl1ddetuu6ahdlict4dr1o 
       foreign key (formal_task_id) 
       references portal.formal_tasks

    alter table portal.user_roles_users 
       add constraint FKq0c5875e1xlfeoxwqkrmq2gla 
       foreign key (user_id) 
       references portal.users

    alter table portal.user_roles_users 
       add constraint FKaa324ufa5d9jcxt9mvb8etkm7 
       foreign key (user_role_id) 
       references portal.user_roles

    alter table portal.users 
       add constraint FKsbg59w8q63i0oo53rlgvlcnjq 
       foreign key (department_id) 
       references portal.departments

    alter table portal.vpn_task_jobs 
       add constraint FKi5jkcq0b8hsyhrhoowr7hl60f 
       foreign key (task_job_id) 
       references portal.task_jobs

    alter table portal.vpn_task_jobs 
       add constraint FKssnfox9rvq4fwetame52kxkvh 
       foreign key (vpn_id) 
       references portal.vpn
