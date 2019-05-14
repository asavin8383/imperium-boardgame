create sequence portal.access_tools_id_seq start 1 increment 1
create sequence portal.arrangement_items_id_seq start 1 increment 1
create sequence portal.arrangements_id_seq start 1 increment 1
create sequence portal.departments_id_seq start 1 increment 1
create sequence portal.formal_tasks_id_seq start 1 increment 1
create sequence portal.informal_tasks_id_seq start 1 increment 1
create sequence portal.job_results_id_seq start 1 increment 1
create sequence portal.user_roles_id_seq start 1 increment 1
create sequence portal.users_id_seq start 1 increment 1

    create table portal.access_tools (
       id int8 not null,
        name varchar(255) not null,
        type varchar(255) not null,
        primary key (id)
    )

    create table portal.arrangement_items (
       id int8 not null,
        arrangement_id int8 not null,
        content_id int8 not null,
        primary key (id)
    )

    create table portal.arrangement_results (
       id int8 not null,
        result varchar(255),
        screenshot bytea,
        url varchar(255),
        content_id int8 not null,
        task_job_id int8 not null,
        primary key (id)
    )

    create table portal.arrangements (
       id int8 not null,
        creation_date timestamp,
        end_date timestamp,
        result varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        access_tool_id int8 not null,
        formal_task_id int8 not null,
        primary key (id)
    )

    create table portal.departments (
       id int8 not null,
        name varchar(255) not null,
        parent_id int8,
        primary key (id)
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

    alter table portal.users 
       add constraint UK_k8d0f2n7n88w1a16yhua64onx unique (user_name)

    create table sa.content (
       id int8 not null,
        primary key (id)
    )

    alter table portal.arrangement_items 
       add constraint FK_arrangement_items_arrangemen_id 
       foreign key (arrangement_id) 
       references portal.arrangements

    alter table portal.arrangement_items 
       add constraint FK_arrangement_items_content_id 
       foreign key (content_id) 
       references sa.content

    alter table portal.arrangement_results 
       add constraint FK_job_results_content_id 
       foreign key (content_id) 
       references sa.content

    alter table portal.arrangement_results 
       add constraint FK_job_results_job_id 
       foreign key (task_job_id) 
       references portal.arrangements

    alter table portal.arrangements 
       add constraint FK_arrangements_access_tool_id 
       foreign key (access_tool_id) 
       references portal.access_tools

    alter table portal.arrangements 
       add constraint FK_arrangements_formal_task_id 
       foreign key (formal_task_id) 
       references portal.formal_tasks

    alter table portal.departments 
       add constraint FK63q917a0aq92i7gcw6h7f1jrv 
       foreign key (parent_id) 
       references portal.departments

    alter table portal.formal_tasks 
       add constraint FK7pc90p1o1ag8pgds0asgeymt8 
       foreign key (author_id) 
       references portal.users

    alter table portal.formal_tasks 
       add constraint FK3xpi0noactbkjx6lmp5jnetda 
       foreign key (informal_task_id) 
       references portal.informal_tasks

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
