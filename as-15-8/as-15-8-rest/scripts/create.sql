
    create table portal.access_tools (
       id bigserial not null,
        name varchar(255) not null,
        type varchar(255) not null,
        primary key (id)
    );

    create table portal.arrangement_items (
       id bigserial not null,
        arrangement_id bigserial not null,
        content_id int8 not null,
        primary key (id)
    );

    create table portal.arrangement_results (
       id bigserial not null,
        result varchar(255),
        screenshot bytea,
        url varchar(255),
        content_id int8 not null,
        arrangement_id bigserial not null,
        primary key (id)
    );

    create table portal.arrangements (
       id bigserial not null,
        creation_date timestamp,
        end_date timestamp,
        result varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        access_tool_id bigserial not null,
        formal_task_id bigserial not null,
        primary key (id)
    );

    create table portal.departments (
       id bigserial not null,
        name varchar(255) not null,
        parent_id bigserial,
        primary key (id)
    );

    create table portal.formal_tasks (
       id bigserial not null,
        creation_date timestamp,
        end_date timestamp,
        modification_date timestamp,
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        user_id bigserial not null,
        informal_task_id bigserial,
        primary key (id)
    );

    create table portal.informal_tasks (
       id bigserial not null,
        author varchar(255),
        confirmed boolean not null,
        content bytea,
        creation_date timestamp,
        title varchar(255),
        primary key (id)
    );

    create table portal.user_roles (
       id bigserial not null,
        role varchar(255) not null,
        primary key (id)
    );

    create table portal.user_roles_users (
       user_role_id bigserial not null,
        user_id bigserial not null
    );

    create table portal.users (
       id bigserial not null,
        first_name varchar(255),
        second_name varchar(255),
        user_name varchar(255) not null,
        department_id bigserial,
        primary key (id)
    );

    alter table portal.users 
       add constraint UK_k8d0f2n7n88w1a16yhua64onx unique (user_name);

    create table hibernate_sequences (
       sequence_name varchar(255) not null,
        next_val int8,
        primary key (sequence_name)
    );

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    alter table portal.arrangement_items 
       add constraint FK_arrangement_items_arrangement_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangement_results 
       add constraint FK_arrangement_results_job_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangements 
       add constraint FK_arrangements_access_tool_id 
       foreign key (access_tool_id) 
       references portal.access_tools;

    alter table portal.arrangements 
       add constraint FK_arrangements_formal_task_id 
       foreign key (formal_task_id) 
       references portal.formal_tasks;

    alter table portal.departments 
       add constraint FK_departments_parent_id 
       foreign key (parent_id) 
       references portal.departments;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_informal_task_id 
       foreign key (informal_task_id) 
       references portal.informal_tasks;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_role_id 
       foreign key (user_role_id) 
       references portal.user_roles;

    alter table portal.users 
       add constraint FK_users_department_id 
       foreign key (department_id) 
       references portal.departments;

    create table portal.access_tools (
       id bigserial not null,
        name varchar(255) not null,
        type varchar(255) not null,
        primary key (id)
    );

    create table portal.arrangement_items (
       id bigserial not null,
        arrangement_id bigserial not null,
        content_id int8 not null,
        primary key (id)
    );

    create table portal.arrangement_results (
       id bigserial not null,
        result varchar(255),
        screenshot bytea,
        url varchar(255),
        content_id int8 not null,
        arrangement_id bigserial not null,
        primary key (id)
    );

    create table portal.arrangements (
       id bigserial not null,
        creation_date timestamp,
        end_date timestamp,
        result varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        access_tool_id bigserial not null,
        formal_task_id bigserial not null,
        primary key (id)
    );

    create table portal.departments (
       id bigserial not null,
        name varchar(255) not null,
        parent_id bigserial,
        primary key (id)
    );

    create table portal.formal_tasks (
       id bigserial not null,
        creation_date timestamp,
        end_date timestamp,
        modification_date timestamp,
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        user_id bigserial not null,
        informal_task_id bigserial,
        primary key (id)
    );

    create table portal.informal_tasks (
       id bigserial not null,
        author varchar(255),
        confirmed boolean not null,
        content bytea,
        creation_date timestamp,
        title varchar(255),
        primary key (id)
    );

    create table portal.user_roles (
       id bigserial not null,
        role varchar(255) not null,
        primary key (id)
    );

    create table portal.user_roles_users (
       user_role_id bigserial not null,
        user_id bigserial not null
    );

    create table portal.users (
       id bigserial not null,
        first_name varchar(255),
        second_name varchar(255),
        user_name varchar(255) not null,
        department_id bigserial,
        primary key (id)
    );

    alter table portal.users 
       add constraint UK_k8d0f2n7n88w1a16yhua64onx unique (user_name);

    create table hibernate_sequences (
       sequence_name varchar(255) not null,
        next_val int8,
        primary key (sequence_name)
    );

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    alter table portal.arrangement_items 
       add constraint FK_arrangement_items_arrangement_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangement_results 
       add constraint FK_arrangement_results_job_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangements 
       add constraint FK_arrangements_access_tool_id 
       foreign key (access_tool_id) 
       references portal.access_tools;

    alter table portal.arrangements 
       add constraint FK_arrangements_formal_task_id 
       foreign key (formal_task_id) 
       references portal.formal_tasks;

    alter table portal.departments 
       add constraint FK_departments_parent_id 
       foreign key (parent_id) 
       references portal.departments;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_informal_task_id 
       foreign key (informal_task_id) 
       references portal.informal_tasks;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_role_id 
       foreign key (user_role_id) 
       references portal.user_roles;

    alter table portal.users 
       add constraint FK_users_department_id 
       foreign key (department_id) 
       references portal.departments;

    create table portal.access_tools (
       id bigserial not null,
        name varchar(255) not null,
        type varchar(255) not null,
        primary key (id)
    );

    create table portal.arrangement_items (
       id bigserial not null,
        arrangement_id bigserial not null,
        content_id int8 not null,
        primary key (id)
    );

    create table portal.arrangement_results (
       id bigserial not null,
        result varchar(255),
        screenshot bytea,
        url varchar(255),
        content_id int8 not null,
        arrangement_id bigserial not null,
        primary key (id)
    );

    create table portal.arrangements (
       id bigserial not null,
        creation_date timestamp,
        end_date timestamp,
        result varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        access_tool_id bigserial not null,
        formal_task_id bigserial not null,
        primary key (id)
    );

    create table portal.departments (
       id bigserial not null,
        name varchar(255) not null,
        parent_id bigserial,
        primary key (id)
    );

    create table portal.formal_tasks (
       id bigserial not null,
        creation_date timestamp,
        end_date timestamp,
        modification_date timestamp,
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        user_id bigserial not null,
        informal_task_id bigserial,
        primary key (id)
    );

    create table portal.informal_tasks (
       id bigserial not null,
        author varchar(255),
        confirmed boolean not null,
        content bytea,
        creation_date timestamp,
        title varchar(255),
        primary key (id)
    );

    create table portal.user_roles (
       id bigserial not null,
        role varchar(255) not null,
        primary key (id)
    );

    create table portal.user_roles_users (
       user_role_id bigserial not null,
        user_id bigserial not null
    );

    create table portal.users (
       id bigserial not null,
        first_name varchar(255),
        second_name varchar(255),
        user_name varchar(255) not null,
        department_id bigserial,
        primary key (id)
    );

    alter table portal.users 
       add constraint UK_k8d0f2n7n88w1a16yhua64onx unique (user_name);

    create table hibernate_sequences (
       sequence_name varchar(255) not null,
        next_val int8,
        primary key (sequence_name)
    );

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    alter table portal.arrangement_items 
       add constraint FK_arrangement_items_arrangement_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangement_results 
       add constraint FK_arrangement_results_job_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangements 
       add constraint FK_arrangements_access_tool_id 
       foreign key (access_tool_id) 
       references portal.access_tools;

    alter table portal.arrangements 
       add constraint FK_arrangements_formal_task_id 
       foreign key (formal_task_id) 
       references portal.formal_tasks;

    alter table portal.departments 
       add constraint FK_departments_parent_id 
       foreign key (parent_id) 
       references portal.departments;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_informal_task_id 
       foreign key (informal_task_id) 
       references portal.informal_tasks;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_role_id 
       foreign key (user_role_id) 
       references portal.user_roles;

    alter table portal.users 
       add constraint FK_users_department_id 
       foreign key (department_id) 
       references portal.departments;

    create table portal.access_tools (
       id bigserial not null,
        name varchar(255) not null,
        type varchar(255) not null,
        primary key (id)
    );

    create table portal.arrangement_items (
       id bigserial not null,
        arrangement_id bigserial not null,
        content_id int8 not null,
        primary key (id)
    );

    create table portal.arrangement_results (
       id bigserial not null,
        result varchar(255),
        screenshot bytea,
        url varchar(255),
        content_id int8 not null,
        arrangement_id bigserial not null,
        primary key (id)
    );

    create table portal.arrangements (
       id bigserial not null,
        creation_date timestamp,
        end_date timestamp,
        result varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        access_tool_id bigserial not null,
        formal_task_id bigserial not null,
        primary key (id)
    );

    create table portal.departments (
       id bigserial not null,
        name varchar(255) not null,
        parent_id bigserial,
        primary key (id)
    );

    create table portal.formal_tasks (
       id bigserial not null,
        creation_date timestamp,
        end_date timestamp,
        modification_date timestamp,
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        user_id bigserial not null,
        informal_task_id bigserial,
        primary key (id)
    );

    create table portal.informal_tasks (
       id bigserial not null,
        author varchar(255),
        confirmed boolean not null,
        content bytea,
        creation_date timestamp,
        title varchar(255),
        primary key (id)
    );

    create table portal.user_roles (
       id bigserial not null,
        role varchar(255) not null,
        primary key (id)
    );

    create table portal.user_roles_users (
       user_role_id bigserial not null,
        user_id bigserial not null
    );

    create table portal.users (
       id bigserial not null,
        first_name varchar(255),
        second_name varchar(255),
        user_name varchar(255) not null,
        department_id bigserial,
        primary key (id)
    );

    alter table portal.users 
       add constraint UK_k8d0f2n7n88w1a16yhua64onx unique (user_name);

    create table hibernate_sequences (
       sequence_name varchar(255) not null,
        next_val int8,
        primary key (sequence_name)
    );

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    alter table portal.arrangement_items 
       add constraint FK_arrangement_items_arrangement_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangement_results 
       add constraint FK_arrangement_results_job_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangements 
       add constraint FK_arrangements_access_tool_id 
       foreign key (access_tool_id) 
       references portal.access_tools;

    alter table portal.arrangements 
       add constraint FK_arrangements_formal_task_id 
       foreign key (formal_task_id) 
       references portal.formal_tasks;

    alter table portal.departments 
       add constraint FK_departments_parent_id 
       foreign key (parent_id) 
       references portal.departments;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_informal_task_id 
       foreign key (informal_task_id) 
       references portal.informal_tasks;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_role_id 
       foreign key (user_role_id) 
       references portal.user_roles;

    alter table portal.users 
       add constraint FK_users_department_id 
       foreign key (department_id) 
       references portal.departments;

    create table portal.access_tools (
       id bigserial not null,
        name varchar(255) not null,
        type varchar(255) not null,
        primary key (id)
    );

    create table portal.arrangement_items (
       id bigserial not null,
        arrangement_id bigserial not null,
        content_id int8 not null,
        primary key (id)
    );

    create table portal.arrangement_results (
       id bigserial not null,
        result varchar(255),
        screenshot bytea,
        url varchar(255),
        content_id int8 not null,
        arrangement_id bigserial not null,
        primary key (id)
    );

    create table portal.arrangements (
       id bigserial not null,
        creation_date timestamp,
        end_date timestamp,
        result varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        access_tool_id bigserial not null,
        formal_task_id bigserial not null,
        primary key (id)
    );

    create table portal.departments (
       id bigserial not null,
        name varchar(255) not null,
        parent_id bigserial,
        primary key (id)
    );

    create table portal.formal_tasks (
       id bigserial not null,
        agreed boolean not null,
        author varchar(255),
        creation_date timestamp,
        deadline_date timestamp,
        end_date timestamp,
        fgis_id varchar(255),
        modification_date timestamp,
        priority varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        informal_task_id bigserial,
        user_id bigserial not null,
        primary key (id)
    );

    create table portal.informal_tasks (
       id bigserial not null,
        author varchar(255),
        confirmed boolean not null,
        content bytea,
        creation_date timestamp,
        title varchar(255),
        primary key (id)
    );

    create table portal.user_roles (
       id bigserial not null,
        role varchar(255) not null,
        primary key (id)
    );

    create table portal.user_roles_users (
       user_role_id bigserial not null,
        user_id bigserial not null
    );

    create table portal.users (
       id bigserial not null,
        first_name varchar(255),
        second_name varchar(255),
        user_name varchar(255) not null,
        department_id bigserial,
        primary key (id)
    );

    alter table portal.users 
       add constraint UK_k8d0f2n7n88w1a16yhua64onx unique (user_name);

    create table hibernate_sequences (
       sequence_name varchar(255) not null,
        next_val int8,
        primary key (sequence_name)
    );

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    alter table portal.arrangement_items 
       add constraint FK_arrangement_items_arrangement_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangement_results 
       add constraint FK_arrangement_results_job_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangements 
       add constraint FK_arrangements_access_tool_id 
       foreign key (access_tool_id) 
       references portal.access_tools;

    alter table portal.arrangements 
       add constraint FK_arrangements_formal_task_id 
       foreign key (formal_task_id) 
       references portal.formal_tasks;

    alter table portal.departments 
       add constraint FK_departments_parent_id 
       foreign key (parent_id) 
       references portal.departments;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_informal_task_id 
       foreign key (informal_task_id) 
       references portal.informal_tasks;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_role_id 
       foreign key (user_role_id) 
       references portal.user_roles;

    alter table portal.users 
       add constraint FK_users_department_id 
       foreign key (department_id) 
       references portal.departments;

    create table portal.access_tools (
       id bigserial not null,
        name varchar(255) not null,
        type varchar(255) not null,
        primary key (id)
    );

    create table portal.arrangement_items (
       id bigserial not null,
        arrangement_id bigserial not null,
        content_id int8 not null,
        primary key (id)
    );

    create table portal.arrangement_results (
       id bigserial not null,
        result varchar(255),
        screenshot bytea,
        url varchar(255),
        content_id int8 not null,
        arrangement_id bigserial not null,
        primary key (id)
    );

    create table portal.arrangements (
       id bigserial not null,
        creation_date timestamp,
        end_date timestamp,
        result varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        access_tool_id bigserial not null,
        formal_task_id bigserial not null,
        primary key (id)
    );

    create table portal.departments (
       id bigserial not null,
        name varchar(255) not null,
        parent_id bigserial,
        primary key (id)
    );

    create table portal.formal_tasks (
       id bigserial not null,
        agreed boolean not null,
        author varchar(255),
        creation_date timestamp,
        deadline_date timestamp,
        end_date timestamp,
        fgis_id varchar(255),
        modification_date timestamp,
        priority varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        informal_task_id bigserial,
        user_id bigserial not null,
        primary key (id)
    );

    create table portal.informal_tasks (
       id bigserial not null,
        author varchar(255),
        confirmed boolean not null,
        content bytea,
        creation_date timestamp,
        title varchar(255),
        primary key (id)
    );

    create table portal.user_roles (
       id bigserial not null,
        role varchar(255) not null,
        primary key (id)
    );

    create table portal.user_roles_users (
       user_role_id bigserial not null,
        user_id bigserial not null
    );

    create table portal.users (
       id bigserial not null,
        first_name varchar(255),
        second_name varchar(255),
        user_name varchar(255) not null,
        department_id bigserial,
        primary key (id)
    );

    alter table portal.users 
       add constraint UK_k8d0f2n7n88w1a16yhua64onx unique (user_name);

    create table hibernate_sequences (
       sequence_name varchar(255) not null,
        next_val int8,
        primary key (sequence_name)
    );

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    alter table portal.arrangement_items 
       add constraint FK_arrangement_items_arrangement_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangement_results 
       add constraint FK_arrangement_results_job_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangements 
       add constraint FK_arrangements_access_tool_id 
       foreign key (access_tool_id) 
       references portal.access_tools;

    alter table portal.arrangements 
       add constraint FK_arrangements_formal_task_id 
       foreign key (formal_task_id) 
       references portal.formal_tasks;

    alter table portal.departments 
       add constraint FK_departments_parent_id 
       foreign key (parent_id) 
       references portal.departments;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_informal_task_id 
       foreign key (informal_task_id) 
       references portal.informal_tasks;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_role_id 
       foreign key (user_role_id) 
       references portal.user_roles;

    alter table portal.users 
       add constraint FK_users_department_id 
       foreign key (department_id) 
       references portal.departments;

    create table portal.access_tools (
       id bigserial not null,
        name varchar(255) not null,
        type varchar(255) not null,
        primary key (id)
    );

    create table portal.arrangement_items (
       id bigserial not null,
        arrangement_id bigserial not null,
        content_id int8 not null,
        primary key (id)
    );

    create table portal.arrangement_results (
       id bigserial not null,
        result varchar(255),
        screenshot bytea,
        url varchar(255),
        content_id int8 not null,
        arrangement_id bigserial not null,
        primary key (id)
    );

    create table portal.arrangements (
       id bigserial not null,
        creation_date timestamp,
        end_date timestamp,
        result varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        access_tool_id bigserial not null,
        formal_task_id bigserial not null,
        primary key (id)
    );

    create table portal.departments (
       id bigserial not null,
        name varchar(255) not null,
        parent_id bigserial,
        primary key (id)
    );

    create table portal.formal_tasks (
       id bigserial not null,
        agreed boolean not null,
        author varchar(255),
        creation_date timestamp,
        deadline_date timestamp,
        end_date timestamp,
        fgis_id varchar(255),
        modification_date timestamp,
        priority varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        informal_task_id bigserial,
        user_id bigserial not null,
        primary key (id)
    );

    create table portal.informal_tasks (
       id bigserial not null,
        author varchar(255),
        confirmed boolean not null,
        content bytea,
        creation_date timestamp,
        title varchar(255),
        primary key (id)
    );

    create table portal.user_roles (
       id bigserial not null,
        role varchar(255) not null,
        primary key (id)
    );

    create table portal.user_roles_users (
       user_role_id bigserial not null,
        user_id bigserial not null
    );

    create table portal.users (
       id bigserial not null,
        first_name varchar(255),
        second_name varchar(255),
        user_name varchar(255) not null,
        department_id bigserial,
        primary key (id)
    );

    alter table portal.users 
       add constraint UK_k8d0f2n7n88w1a16yhua64onx unique (user_name);

    create table hibernate_sequences (
       sequence_name varchar(255) not null,
        next_val int8,
        primary key (sequence_name)
    );

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    alter table portal.arrangement_items 
       add constraint FK_arrangement_items_arrangement_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangement_results 
       add constraint FK_arrangement_results_job_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangements 
       add constraint FK_arrangements_access_tool_id 
       foreign key (access_tool_id) 
       references portal.access_tools;

    alter table portal.arrangements 
       add constraint FK_arrangements_formal_task_id 
       foreign key (formal_task_id) 
       references portal.formal_tasks;

    alter table portal.departments 
       add constraint FK_departments_parent_id 
       foreign key (parent_id) 
       references portal.departments;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_informal_task_id 
       foreign key (informal_task_id) 
       references portal.informal_tasks;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_role_id 
       foreign key (user_role_id) 
       references portal.user_roles;

    alter table portal.users 
       add constraint FK_users_department_id 
       foreign key (department_id) 
       references portal.departments;

    create table portal.access_tools (
       id bigserial not null,
        name varchar(255) not null,
        type varchar(255) not null,
        primary key (id)
    );

    create table portal.arrangement_items (
       id bigserial not null,
        arrangement_id bigserial not null,
        content_id int8 not null,
        primary key (id)
    );

    create table portal.arrangement_results (
       id bigserial not null,
        result varchar(255),
        screenshot bytea,
        url varchar(255),
        content_id int8 not null,
        arrangement_id bigserial not null,
        primary key (id)
    );

    create table portal.arrangements (
       id bigserial not null,
        creation_date timestamp,
        end_date timestamp,
        result varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        access_tool_id bigserial not null,
        formal_task_id bigserial not null,
        primary key (id)
    );

    create table portal.departments (
       id bigserial not null,
        name varchar(255) not null,
        parent_id bigserial,
        primary key (id)
    );

    create table portal.formal_tasks (
       id bigserial not null,
        agreed boolean not null,
        author varchar(255),
        creation_date timestamp,
        deadline_date timestamp,
        end_date timestamp,
        fgis_id varchar(255),
        modification_date timestamp,
        priority varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        informal_task_id bigserial,
        user_id bigserial not null,
        primary key (id)
    );

    create table portal.informal_tasks (
       id bigserial not null,
        author varchar(255),
        confirmed boolean not null,
        content bytea,
        creation_date timestamp,
        title varchar(255),
        primary key (id)
    );

    create table portal.user_roles (
       id bigserial not null,
        role varchar(255) not null,
        primary key (id)
    );

    create table portal.user_roles_users (
       user_role_id bigserial not null,
        user_id bigserial not null
    );

    create table portal.users (
       id bigserial not null,
        first_name varchar(255),
        second_name varchar(255),
        user_name varchar(255) not null,
        department_id bigserial,
        primary key (id)
    );

    alter table portal.users 
       add constraint UK_k8d0f2n7n88w1a16yhua64onx unique (user_name);

    create table hibernate_sequences (
       sequence_name varchar(255) not null,
        next_val int8,
        primary key (sequence_name)
    );

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    alter table portal.arrangement_items 
       add constraint FK_arrangement_items_arrangement_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangement_results 
       add constraint FK_arrangement_results_job_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangements 
       add constraint FK_arrangements_access_tool_id 
       foreign key (access_tool_id) 
       references portal.access_tools;

    alter table portal.arrangements 
       add constraint FK_arrangements_formal_task_id 
       foreign key (formal_task_id) 
       references portal.formal_tasks;

    alter table portal.departments 
       add constraint FK_departments_parent_id 
       foreign key (parent_id) 
       references portal.departments;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_informal_task_id 
       foreign key (informal_task_id) 
       references portal.informal_tasks;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_role_id 
       foreign key (user_role_id) 
       references portal.user_roles;

    alter table portal.users 
       add constraint FK_users_department_id 
       foreign key (department_id) 
       references portal.departments;

    create table portal.access_tools (
       id bigserial not null,
        name varchar(255) not null,
        type varchar(255) not null,
        primary key (id)
    );

    create table portal.arrangement_items (
       id bigserial not null,
        arrangement_id bigserial not null,
        content_id int8 not null,
        primary key (id)
    );

    create table portal.arrangement_results (
       id bigserial not null,
        result varchar(255),
        screenshot bytea,
        url varchar(255),
        content_id int8 not null,
        arrangement_id bigserial not null,
        primary key (id)
    );

    create table portal.arrangements (
       id bigserial not null,
        creation_date timestamp,
        end_date timestamp,
        result varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        access_tool_id bigserial not null,
        formal_task_id bigserial not null,
        primary key (id)
    );

    create table portal.departments (
       id bigserial not null,
        name varchar(255) not null,
        parent_id bigserial,
        primary key (id)
    );

    create table portal.formal_tasks (
       id bigserial not null,
        agreed boolean not null,
        author varchar(255),
        creation_date timestamp,
        deadline_date timestamp,
        end_date timestamp,
        fgis_id varchar(255),
        modification_date timestamp,
        priority varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        informal_task_id bigserial,
        user_id bigserial not null,
        primary key (id)
    );

    create table portal.informal_tasks (
       id bigserial not null,
        author varchar(255),
        confirmed boolean not null,
        content bytea,
        creation_date timestamp,
        title varchar(255),
        primary key (id)
    );

    create table portal.user_roles (
       id bigserial not null,
        role varchar(255) not null,
        primary key (id)
    );

    create table portal.user_roles_users (
       user_role_id bigserial not null,
        user_id bigserial not null
    );

    create table portal.users (
       id bigserial not null,
        first_name varchar(255),
        second_name varchar(255),
        user_name varchar(255) not null,
        department_id bigserial,
        primary key (id)
    );

    alter table portal.users 
       add constraint UK_k8d0f2n7n88w1a16yhua64onx unique (user_name);

    create table hibernate_sequences (
       sequence_name varchar(255) not null,
        next_val int8,
        primary key (sequence_name)
    );

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    alter table portal.arrangement_items 
       add constraint FK_arrangement_items_arrangement_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangement_results 
       add constraint FK_arrangement_results_job_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangements 
       add constraint FK_arrangements_access_tool_id 
       foreign key (access_tool_id) 
       references portal.access_tools;

    alter table portal.arrangements 
       add constraint FK_arrangements_formal_task_id 
       foreign key (formal_task_id) 
       references portal.formal_tasks;

    alter table portal.departments 
       add constraint FK_departments_parent_id 
       foreign key (parent_id) 
       references portal.departments;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_informal_task_id 
       foreign key (informal_task_id) 
       references portal.informal_tasks;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_role_id 
       foreign key (user_role_id) 
       references portal.user_roles;

    alter table portal.users 
       add constraint FK_users_department_id 
       foreign key (department_id) 
       references portal.departments;

    create table portal.access_tools (
       id bigserial not null,
        name varchar(255) not null,
        type varchar(255) not null,
        primary key (id)
    );

    create table portal.arrangement_items (
       id bigserial not null,
        arrangement_id bigserial not null,
        content_id int8 not null,
        primary key (id)
    );

    create table portal.arrangement_results (
       id bigserial not null,
        result varchar(255),
        screenshot bytea,
        url varchar(255),
        content_id int8 not null,
        arrangement_id bigserial not null,
        primary key (id)
    );

    create table portal.arrangements (
       id bigserial not null,
        creation_date timestamp,
        end_date timestamp,
        result varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        access_tool_id bigserial not null,
        formal_task_id bigserial not null,
        primary key (id)
    );

    create table portal.departments (
       id bigserial not null,
        name varchar(255) not null,
        parent_id bigserial,
        primary key (id)
    );

    create table portal.formal_tasks (
       id bigserial not null,
        agreed boolean not null,
        author varchar(255),
        creation_date timestamp,
        deadline_date timestamp,
        end_date timestamp,
        fgis_id varchar(255),
        modification_date timestamp,
        priority varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        informal_task_id bigserial,
        user_id bigserial not null,
        primary key (id)
    );

    create table portal.informal_tasks (
       id bigserial not null,
        author varchar(255),
        confirmed boolean not null,
        content bytea,
        creation_date timestamp,
        title varchar(255),
        primary key (id)
    );

    create table portal.user_roles (
       id bigserial not null,
        role varchar(255) not null,
        primary key (id)
    );

    create table portal.user_roles_users (
       user_role_id bigserial not null,
        user_id bigserial not null
    );

    create table portal.users (
       id bigserial not null,
        first_name varchar(255),
        second_name varchar(255),
        user_name varchar(255) not null,
        department_id bigserial,
        primary key (id)
    );

    alter table portal.users 
       add constraint UK_k8d0f2n7n88w1a16yhua64onx unique (user_name);

    create table hibernate_sequences (
       sequence_name varchar(255) not null,
        next_val int8,
        primary key (sequence_name)
    );

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    alter table portal.arrangement_items 
       add constraint FK_arrangement_items_arrangement_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangement_results 
       add constraint FK_arrangement_results_job_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangements 
       add constraint FK_arrangements_access_tool_id 
       foreign key (access_tool_id) 
       references portal.access_tools;

    alter table portal.arrangements 
       add constraint FK_arrangements_formal_task_id 
       foreign key (formal_task_id) 
       references portal.formal_tasks;

    alter table portal.departments 
       add constraint FK_departments_parent_id 
       foreign key (parent_id) 
       references portal.departments;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_informal_task_id 
       foreign key (informal_task_id) 
       references portal.informal_tasks;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_role_id 
       foreign key (user_role_id) 
       references portal.user_roles;

    alter table portal.users 
       add constraint FK_users_department_id 
       foreign key (department_id) 
       references portal.departments;

    create table portal.access_tools (
       id bigserial not null,
        name varchar(255) not null,
        type varchar(255) not null,
        primary key (id)
    );

    create table portal.arrangement_items (
       id bigserial not null,
        arrangement_id bigserial not null,
        content_id int8 not null,
        primary key (id)
    );

    create table portal.arrangement_results (
       id bigserial not null,
        result varchar(255),
        screenshot bytea,
        url varchar(255),
        content_id int8 not null,
        arrangement_id bigserial not null,
        primary key (id)
    );

    create table portal.arrangements (
       id bigserial not null,
        creation_date timestamp,
        end_date timestamp,
        result varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        access_tool_id bigserial not null,
        formal_task_id bigserial not null,
        primary key (id)
    );

    create table portal.departments (
       id bigserial not null,
        name varchar(255) not null,
        parent_id bigserial,
        primary key (id)
    );

    create table portal.formal_tasks (
       id bigserial not null,
        agreed boolean not null,
        author varchar(255),
        creation_date timestamp,
        deadline_date timestamp,
        end_date timestamp,
        fgis_id varchar(255),
        modification_date timestamp,
        priority varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        informal_task_id bigserial,
        user_id bigserial not null,
        primary key (id)
    );

    create table portal.informal_tasks (
       id bigserial not null,
        author varchar(255),
        confirmed boolean not null,
        content bytea,
        creation_date timestamp,
        title varchar(255),
        primary key (id)
    );

    create table portal.user_roles (
       id bigserial not null,
        role varchar(255) not null,
        primary key (id)
    );

    create table portal.user_roles_users (
       user_role_id bigserial not null,
        user_id bigserial not null
    );

    create table portal.users (
       id bigserial not null,
        first_name varchar(255),
        second_name varchar(255),
        user_name varchar(255) not null,
        department_id bigserial,
        primary key (id)
    );

    alter table portal.users 
       add constraint UK_k8d0f2n7n88w1a16yhua64onx unique (user_name);

    create table hibernate_sequences (
       sequence_name varchar(255) not null,
        next_val int8,
        primary key (sequence_name)
    );

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    alter table portal.arrangement_items 
       add constraint FK_arrangement_items_arrangement_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangement_results 
       add constraint FK_arrangement_results_job_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangements 
       add constraint FK_arrangements_access_tool_id 
       foreign key (access_tool_id) 
       references portal.access_tools;

    alter table portal.arrangements 
       add constraint FK_arrangements_formal_task_id 
       foreign key (formal_task_id) 
       references portal.formal_tasks;

    alter table portal.departments 
       add constraint FK_departments_parent_id 
       foreign key (parent_id) 
       references portal.departments;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_informal_task_id 
       foreign key (informal_task_id) 
       references portal.informal_tasks;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_role_id 
       foreign key (user_role_id) 
       references portal.user_roles;

    alter table portal.users 
       add constraint FK_users_department_id 
       foreign key (department_id) 
       references portal.departments;

    create table portal.access_tools (
       id bigserial not null,
        name varchar(255) not null,
        type varchar(255) not null,
        primary key (id)
    );

    create table portal.arrangement_items (
       id bigserial not null,
        arrangement_id bigserial not null,
        content_id int8 not null,
        primary key (id)
    );

    create table portal.arrangement_results (
       id bigserial not null,
        result varchar(255),
        screenshot bytea,
        url varchar(255),
        content_id int8 not null,
        arrangement_id bigserial not null,
        primary key (id)
    );

    create table portal.arrangements (
       id bigserial not null,
        creation_date timestamp,
        end_date timestamp,
        result varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        access_tool_id bigserial not null,
        formal_task_id bigserial not null,
        primary key (id)
    );

    create table portal.departments (
       id bigserial not null,
        name varchar(255) not null,
        parent_id bigserial,
        primary key (id)
    );

    create table portal.formal_tasks (
       id bigserial not null,
        agreed boolean not null,
        author varchar(255),
        creation_date timestamp,
        deadline_date timestamp,
        end_date timestamp,
        fgis_id varchar(255),
        modification_date timestamp,
        priority varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        informal_task_id bigserial,
        user_id bigserial not null,
        primary key (id)
    );

    create table portal.informal_tasks (
       id bigserial not null,
        author varchar(255),
        confirmed boolean not null,
        content bytea,
        creation_date timestamp,
        title varchar(255),
        primary key (id)
    );

    create table portal.user_roles (
       id bigserial not null,
        role varchar(255) not null,
        primary key (id)
    );

    create table portal.user_roles_users (
       user_role_id bigserial not null,
        user_id bigserial not null
    );

    create table portal.users (
       id bigserial not null,
        first_name varchar(255),
        second_name varchar(255),
        user_name varchar(255) not null,
        department_id bigserial,
        primary key (id)
    );

    alter table portal.users 
       add constraint UK_k8d0f2n7n88w1a16yhua64onx unique (user_name);

    create table hibernate_sequences (
       sequence_name varchar(255) not null,
        next_val int8,
        primary key (sequence_name)
    );

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    alter table portal.arrangement_items 
       add constraint FK_arrangement_items_arrangement_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangement_results 
       add constraint FK_arrangement_results_job_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangements 
       add constraint FK_arrangements_access_tool_id 
       foreign key (access_tool_id) 
       references portal.access_tools;

    alter table portal.arrangements 
       add constraint FK_arrangements_formal_task_id 
       foreign key (formal_task_id) 
       references portal.formal_tasks;

    alter table portal.departments 
       add constraint FK_departments_parent_id 
       foreign key (parent_id) 
       references portal.departments;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_informal_task_id 
       foreign key (informal_task_id) 
       references portal.informal_tasks;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_role_id 
       foreign key (user_role_id) 
       references portal.user_roles;

    alter table portal.users 
       add constraint FK_users_department_id 
       foreign key (department_id) 
       references portal.departments;

    create table portal.access_tools (
       id bigserial not null,
        name varchar(255) not null,
        type varchar(255) not null,
        primary key (id)
    );

    create table portal.arrangement_items (
       id bigserial not null,
        arrangement_id bigserial not null,
        content_id int8 not null,
        primary key (id)
    );

    create table portal.arrangement_results (
       id bigserial not null,
        result varchar(255),
        screenshot bytea,
        url varchar(255),
        content_id int8 not null,
        arrangement_id bigserial not null,
        primary key (id)
    );

    create table portal.arrangements (
       id bigserial not null,
        creation_date timestamp,
        end_date timestamp,
        result varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        access_tool_id bigserial not null,
        formal_task_id bigserial not null,
        primary key (id)
    );

    create table portal.departments (
       id bigserial not null,
        name varchar(255) not null,
        parent_id bigserial,
        primary key (id)
    );

    create table portal.formal_tasks (
       id bigserial not null,
        agreed boolean not null,
        author varchar(255),
        creation_date timestamp,
        deadline_date timestamp,
        end_date timestamp,
        fgis_id varchar(255),
        modification_date timestamp,
        priority varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        informal_task_id bigserial,
        user_id bigserial not null,
        primary key (id)
    );

    create table portal.informal_tasks (
       id bigserial not null,
        author varchar(255),
        confirmed boolean not null,
        content bytea,
        creation_date timestamp,
        title varchar(255),
        primary key (id)
    );

    create table portal.user_roles (
       id bigserial not null,
        role varchar(255) not null,
        primary key (id)
    );

    create table portal.user_roles_users (
       user_role_id bigserial not null,
        user_id bigserial not null
    );

    create table portal.users (
       id bigserial not null,
        first_name varchar(255),
        second_name varchar(255),
        user_name varchar(255) not null,
        department_id bigserial,
        primary key (id)
    );

    alter table portal.users 
       add constraint UK_k8d0f2n7n88w1a16yhua64onx unique (user_name);

    create table hibernate_sequences (
       sequence_name varchar(255) not null,
        next_val int8,
        primary key (sequence_name)
    );

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    alter table portal.arrangement_items 
       add constraint FK_arrangement_items_arrangement_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangement_results 
       add constraint FK_arrangement_results_job_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangements 
       add constraint FK_arrangements_access_tool_id 
       foreign key (access_tool_id) 
       references portal.access_tools;

    alter table portal.arrangements 
       add constraint FK_arrangements_formal_task_id 
       foreign key (formal_task_id) 
       references portal.formal_tasks;

    alter table portal.departments 
       add constraint FK_departments_parent_id 
       foreign key (parent_id) 
       references portal.departments;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_informal_task_id 
       foreign key (informal_task_id) 
       references portal.informal_tasks;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_role_id 
       foreign key (user_role_id) 
       references portal.user_roles;

    alter table portal.users 
       add constraint FK_users_department_id 
       foreign key (department_id) 
       references portal.departments;

    create table portal.access_tools (
       id bigserial not null,
        name varchar(255) not null,
        type varchar(255) not null,
        primary key (id)
    );

    create table portal.arrangement_items (
       id bigserial not null,
        arrangement_id bigserial not null,
        content_id int8 not null,
        primary key (id)
    );

    create table portal.arrangement_results (
       id bigserial not null,
        result varchar(255),
        screenshot bytea,
        url varchar(255),
        content_id int8 not null,
        arrangement_id bigserial not null,
        primary key (id)
    );

    create table portal.arrangements (
       id bigserial not null,
        creation_date timestamp,
        end_date timestamp,
        result varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        access_tool_id bigserial not null,
        formal_task_id bigserial not null,
        primary key (id)
    );

    create table portal.departments (
       id bigserial not null,
        name varchar(255) not null,
        parent_id bigserial,
        primary key (id)
    );

    create table portal.formal_tasks (
       id bigserial not null,
        agreed boolean not null,
        author varchar(255),
        creation_date timestamp,
        deadline_date timestamp,
        end_date timestamp,
        fgis_id varchar(255),
        modification_date timestamp,
        priority varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        informal_task_id bigserial,
        user_id bigserial not null,
        primary key (id)
    );

    create table portal.informal_tasks (
       id bigserial not null,
        author varchar(255),
        confirmed boolean not null,
        content bytea,
        creation_date timestamp,
        title varchar(255),
        primary key (id)
    );

    create table portal.user_roles (
       id bigserial not null,
        role varchar(255) not null,
        primary key (id)
    );

    create table portal.user_roles_users (
       user_role_id bigserial not null,
        user_id bigserial not null
    );

    create table portal.users (
       id bigserial not null,
        first_name varchar(255),
        second_name varchar(255),
        user_name varchar(255) not null,
        department_id bigserial,
        primary key (id)
    );

    alter table portal.users 
       add constraint UK_k8d0f2n7n88w1a16yhua64onx unique (user_name);

    create table hibernate_sequences (
       sequence_name varchar(255) not null,
        next_val int8,
        primary key (sequence_name)
    );

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    alter table portal.arrangement_items 
       add constraint FK_arrangement_items_arrangement_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangement_results 
       add constraint FK_arrangement_results_job_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangements 
       add constraint FK_arrangements_access_tool_id 
       foreign key (access_tool_id) 
       references portal.access_tools;

    alter table portal.arrangements 
       add constraint FK_arrangements_formal_task_id 
       foreign key (formal_task_id) 
       references portal.formal_tasks;

    alter table portal.departments 
       add constraint FK_departments_parent_id 
       foreign key (parent_id) 
       references portal.departments;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_informal_task_id 
       foreign key (informal_task_id) 
       references portal.informal_tasks;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_role_id 
       foreign key (user_role_id) 
       references portal.user_roles;

    alter table portal.users 
       add constraint FK_users_department_id 
       foreign key (department_id) 
       references portal.departments;

    create table portal.access_tools (
       id bigserial not null,
        name varchar(255) not null,
        type varchar(255) not null,
        primary key (id)
    );

    create table portal.arrangement_items (
       id bigserial not null,
        arrangement_id bigserial not null,
        content_id int8 not null,
        primary key (id)
    );

    create table portal.arrangement_results (
       id bigserial not null,
        result varchar(255),
        screenshot bytea,
        url varchar(255),
        content_id int8 not null,
        arrangement_id bigserial not null,
        primary key (id)
    );

    create table portal.arrangements (
       id bigserial not null,
        creation_date timestamp,
        end_date timestamp,
        result varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        access_tool_id bigserial not null,
        formal_task_id bigserial not null,
        primary key (id)
    );

    create table portal.departments (
       id bigserial not null,
        name varchar(255) not null,
        parent_id bigserial,
        primary key (id)
    );

    create table portal.formal_tasks (
       id bigserial not null,
        agreed boolean not null,
        author varchar(255),
        creation_date timestamp,
        deadline_date timestamp,
        end_date timestamp,
        fgis_id varchar(255),
        modification_date timestamp,
        priority varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        informal_task_id bigserial,
        user_id bigserial not null,
        primary key (id)
    );

    create table portal.informal_tasks (
       id bigserial not null,
        author varchar(255),
        confirmed boolean not null,
        content bytea,
        creation_date timestamp,
        title varchar(255),
        primary key (id)
    );

    create table portal.user_roles (
       id bigserial not null,
        role varchar(255) not null,
        primary key (id)
    );

    create table portal.user_roles_users (
       user_role_id bigserial not null,
        user_id bigserial not null
    );

    create table portal.users (
       id bigserial not null,
        first_name varchar(255),
        second_name varchar(255),
        user_name varchar(255) not null,
        department_id bigserial,
        primary key (id)
    );

    alter table portal.users 
       add constraint UK_k8d0f2n7n88w1a16yhua64onx unique (user_name);

    create table hibernate_sequences (
       sequence_name varchar(255) not null,
        next_val int8,
        primary key (sequence_name)
    );

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    alter table portal.arrangement_items 
       add constraint FK_arrangement_items_arrangement_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangement_results 
       add constraint FK_arrangement_results_job_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangements 
       add constraint FK_arrangements_access_tool_id 
       foreign key (access_tool_id) 
       references portal.access_tools;

    alter table portal.arrangements 
       add constraint FK_arrangements_formal_task_id 
       foreign key (formal_task_id) 
       references portal.formal_tasks;

    alter table portal.departments 
       add constraint FK_departments_parent_id 
       foreign key (parent_id) 
       references portal.departments;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_informal_task_id 
       foreign key (informal_task_id) 
       references portal.informal_tasks;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_role_id 
       foreign key (user_role_id) 
       references portal.user_roles;

    alter table portal.users 
       add constraint FK_users_department_id 
       foreign key (department_id) 
       references portal.departments;

    create table portal.access_tools (
       id bigserial not null,
        name varchar(255) not null,
        type varchar(255) not null,
        primary key (id)
    );

    create table portal.arrangement_items (
       id bigserial not null,
        arrangement_id bigserial not null,
        content_id int8 not null,
        primary key (id)
    );

    create table portal.arrangement_results (
       id bigserial not null,
        result varchar(255),
        screenshot bytea,
        url varchar(255),
        content_id int8 not null,
        arrangement_id bigserial not null,
        primary key (id)
    );

    create table portal.arrangements (
       id bigserial not null,
        creation_date timestamp,
        end_date timestamp,
        result varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        access_tool_id bigserial not null,
        formal_task_id bigserial not null,
        primary key (id)
    );

    create table portal.departments (
       id bigserial not null,
        name varchar(255) not null,
        parent_id bigserial,
        primary key (id)
    );

    create table portal.formal_tasks (
       id bigserial not null,
        agreed boolean not null,
        author varchar(255),
        creation_date timestamp,
        deadline_date timestamp,
        end_date timestamp,
        fgis_id varchar(255),
        modification_date timestamp,
        priority varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        informal_task_id bigserial,
        user_id bigserial not null,
        primary key (id)
    );

    create table portal.informal_tasks (
       id bigserial not null,
        author varchar(255),
        confirmed boolean not null,
        content bytea,
        creation_date timestamp,
        title varchar(255),
        primary key (id)
    );

    create table portal.user_roles (
       id bigserial not null,
        role varchar(255) not null,
        primary key (id)
    );

    create table portal.user_roles_users (
       user_role_id bigserial not null,
        user_id bigserial not null
    );

    create table portal.users (
       id bigserial not null,
        first_name varchar(255),
        second_name varchar(255),
        user_name varchar(255) not null,
        department_id bigserial,
        primary key (id)
    );

    alter table portal.users 
       add constraint UK_k8d0f2n7n88w1a16yhua64onx unique (user_name);

    create table hibernate_sequences (
       sequence_name varchar(255) not null,
        next_val int8,
        primary key (sequence_name)
    );

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    alter table portal.arrangement_items 
       add constraint FK_arrangement_items_arrangement_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangement_results 
       add constraint FK_arrangement_results_job_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangements 
       add constraint FK_arrangements_access_tool_id 
       foreign key (access_tool_id) 
       references portal.access_tools;

    alter table portal.arrangements 
       add constraint FK_arrangements_formal_task_id 
       foreign key (formal_task_id) 
       references portal.formal_tasks;

    alter table portal.departments 
       add constraint FK_departments_parent_id 
       foreign key (parent_id) 
       references portal.departments;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_informal_task_id 
       foreign key (informal_task_id) 
       references portal.informal_tasks;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_role_id 
       foreign key (user_role_id) 
       references portal.user_roles;

    alter table portal.users 
       add constraint FK_users_department_id 
       foreign key (department_id) 
       references portal.departments;

    create table portal.access_tools (
       id bigserial not null,
        name varchar(255) not null,
        type varchar(255) not null,
        primary key (id)
    );

    create table portal.arrangement_items (
       id bigserial not null,
        arrangement_id bigserial not null,
        content_id int8 not null,
        primary key (id)
    );

    create table portal.arrangement_results (
       id bigserial not null,
        result varchar(255),
        screenshot bytea,
        url varchar(255),
        content_id int8 not null,
        arrangement_id bigserial not null,
        primary key (id)
    );

    create table portal.arrangements (
       id bigserial not null,
        creation_date timestamp,
        end_date timestamp,
        result varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        access_tool_id bigserial not null,
        formal_task_id bigserial not null,
        primary key (id)
    );

    create table portal.departments (
       id bigserial not null,
        name varchar(255) not null,
        parent_id bigserial,
        primary key (id)
    );

    create table portal.formal_tasks (
       id bigserial not null,
        agreed boolean not null,
        author varchar(255),
        creation_date timestamp,
        deadline_date timestamp,
        end_date timestamp,
        fgis_id varchar(255),
        modification_date timestamp,
        priority varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        informal_task_id bigserial,
        user_id bigserial not null,
        primary key (id)
    );

    create table portal.informal_tasks (
       id bigserial not null,
        author varchar(255),
        confirmed boolean not null,
        content bytea,
        creation_date timestamp,
        title varchar(255),
        primary key (id)
    );

    create table portal.user_roles (
       id bigserial not null,
        role varchar(255) not null,
        primary key (id)
    );

    create table portal.user_roles_users (
       user_role_id bigserial not null,
        user_id bigserial not null
    );

    create table portal.users (
       id bigserial not null,
        email varchar(255),
        first_name varchar(255),
        second_name varchar(255),
        user_name varchar(255) not null,
        department_id bigserial,
        primary key (id)
    );

    alter table portal.users 
       add constraint UK_k8d0f2n7n88w1a16yhua64onx unique (user_name);

    create table hibernate_sequences (
       sequence_name varchar(255) not null,
        next_val int8,
        primary key (sequence_name)
    );

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    alter table portal.arrangement_items 
       add constraint FK_arrangement_items_arrangement_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangement_results 
       add constraint FK_arrangement_results_job_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangements 
       add constraint FK_arrangements_access_tool_id 
       foreign key (access_tool_id) 
       references portal.access_tools;

    alter table portal.arrangements 
       add constraint FK_arrangements_formal_task_id 
       foreign key (formal_task_id) 
       references portal.formal_tasks;

    alter table portal.departments 
       add constraint FK_departments_parent_id 
       foreign key (parent_id) 
       references portal.departments;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_informal_task_id 
       foreign key (informal_task_id) 
       references portal.informal_tasks;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_role_id 
       foreign key (user_role_id) 
       references portal.user_roles;

    alter table portal.users 
       add constraint FK_users_department_id 
       foreign key (department_id) 
       references portal.departments;

    create table portal.access_tools (
       id bigserial not null,
        name varchar(255) not null,
        type varchar(255) not null,
        primary key (id)
    );

    create table portal.arrangement_items (
       id bigserial not null,
        arrangement_id bigserial not null,
        content_id int8 not null,
        primary key (id)
    );

    create table portal.arrangement_results (
       id bigserial not null,
        result varchar(255),
        screenshot bytea,
        url varchar(255),
        content_id int8 not null,
        arrangement_id bigserial not null,
        primary key (id)
    );

    create table portal.arrangements (
       id bigserial not null,
        creation_date timestamp,
        end_date timestamp,
        result varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        access_tool_id bigserial not null,
        formal_task_id bigserial not null,
        primary key (id)
    );

    create table portal.departments (
       id bigserial not null,
        name varchar(255) not null,
        parent_id bigserial,
        primary key (id)
    );

    create table portal.formal_tasks (
       id bigserial not null,
        agreed boolean not null,
        author varchar(255),
        creation_date timestamp,
        deadline_date timestamp,
        end_date timestamp,
        fgis_id varchar(255),
        modification_date timestamp,
        priority varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        informal_task_id bigserial,
        user_id bigserial not null,
        primary key (id)
    );

    create table portal.informal_tasks (
       id bigserial not null,
        author varchar(255),
        confirmed boolean not null,
        content bytea,
        creation_date timestamp,
        title varchar(255),
        primary key (id)
    );

    create table portal.user_roles (
       id bigserial not null,
        role varchar(255) not null,
        primary key (id)
    );

    create table portal.user_roles_users (
       user_role_id bigserial not null,
        user_id bigserial not null
    );

    create table portal.users (
       id bigserial not null,
        email varchar(255),
        first_name varchar(255),
        second_name varchar(255),
        user_name varchar(255) not null,
        department_id bigserial,
        primary key (id)
    );

    alter table portal.users 
       add constraint UK_k8d0f2n7n88w1a16yhua64onx unique (user_name);

    create table hibernate_sequences (
       sequence_name varchar(255) not null,
        next_val int8,
        primary key (sequence_name)
    );

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    alter table portal.arrangement_items 
       add constraint FK_arrangement_items_arrangement_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangement_results 
       add constraint FK_arrangement_results_job_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangements 
       add constraint FK_arrangements_access_tool_id 
       foreign key (access_tool_id) 
       references portal.access_tools;

    alter table portal.arrangements 
       add constraint FK_arrangements_formal_task_id 
       foreign key (formal_task_id) 
       references portal.formal_tasks;

    alter table portal.departments 
       add constraint FK_departments_parent_id 
       foreign key (parent_id) 
       references portal.departments;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_informal_task_id 
       foreign key (informal_task_id) 
       references portal.informal_tasks;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_role_id 
       foreign key (user_role_id) 
       references portal.user_roles;

    alter table portal.users 
       add constraint FK_users_department_id 
       foreign key (department_id) 
       references portal.departments;

    create table portal.access_tools (
       id bigserial not null,
        name varchar(255) not null,
        type varchar(255) not null,
        primary key (id)
    );

    create table portal.arrangement_items (
       id bigserial not null,
        arrangement_id bigserial not null,
        content_id int8 not null,
        primary key (id)
    );

    create table portal.arrangement_results (
       id bigserial not null,
        result varchar(255),
        screenshot bytea,
        url varchar(255),
        content_id int8 not null,
        arrangement_id bigserial not null,
        primary key (id)
    );

    create table portal.arrangements (
       id bigserial not null,
        creation_date timestamp,
        end_date timestamp,
        result varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        access_tool_id bigserial not null,
        formal_task_id bigserial not null,
        primary key (id)
    );

    create table portal.departments (
       id bigserial not null,
        name varchar(255) not null,
        parent_id bigserial,
        primary key (id)
    );

    create table portal.formal_tasks (
       id bigserial not null,
        agreed boolean not null,
        author varchar(255),
        creation_date timestamp,
        deadline_date timestamp,
        end_date timestamp,
        fgis_id varchar(255),
        modification_date timestamp,
        priority varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        informal_task_id bigserial,
        user_id bigserial not null,
        primary key (id)
    );

    create table portal.informal_tasks (
       id bigserial not null,
        author varchar(255),
        confirmed boolean not null,
        content bytea,
        creation_date timestamp,
        title varchar(255),
        primary key (id)
    );

    create table portal.user_roles (
       id bigserial not null,
        role varchar(255) not null,
        primary key (id)
    );

    create table portal.user_roles_users (
       user_role_id bigserial not null,
        user_id bigserial not null
    );

    create table portal.users (
       id bigserial not null,
        email varchar(255),
        first_name varchar(255),
        second_name varchar(255),
        user_name varchar(255) not null,
        department_id bigserial,
        primary key (id)
    );

    alter table portal.users 
       add constraint UK_k8d0f2n7n88w1a16yhua64onx unique (user_name);

    create table hibernate_sequences (
       sequence_name varchar(255) not null,
        next_val int8,
        primary key (sequence_name)
    );

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    alter table portal.arrangement_items 
       add constraint FK_arrangement_items_arrangement_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangement_results 
       add constraint FK_arrangement_results_job_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangements 
       add constraint FK_arrangements_access_tool_id 
       foreign key (access_tool_id) 
       references portal.access_tools;

    alter table portal.arrangements 
       add constraint FK_arrangements_formal_task_id 
       foreign key (formal_task_id) 
       references portal.formal_tasks;

    alter table portal.departments 
       add constraint FK_departments_parent_id 
       foreign key (parent_id) 
       references portal.departments;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_informal_task_id 
       foreign key (informal_task_id) 
       references portal.informal_tasks;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_role_id 
       foreign key (user_role_id) 
       references portal.user_roles;

    alter table portal.users 
       add constraint FK_users_department_id 
       foreign key (department_id) 
       references portal.departments;

    create table portal.access_tools (
       id bigserial not null,
        name varchar(255) not null,
        type varchar(255) not null,
        primary key (id)
    );

    create table portal.arrangement_items (
       id bigserial not null,
        arrangement_id bigserial not null,
        content_id int8 not null,
        primary key (id)
    );

    create table portal.arrangement_results (
       id bigserial not null,
        result varchar(255),
        screenshot bytea,
        url varchar(255),
        content_id int8 not null,
        arrangement_id bigserial not null,
        primary key (id)
    );

    create table portal.arrangements (
       id bigserial not null,
        creation_date timestamp,
        end_date timestamp,
        result varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        access_tool_id bigserial not null,
        formal_task_id bigserial not null,
        primary key (id)
    );

    create table portal.departments (
       id bigserial not null,
        name varchar(255) not null,
        parent_id bigserial,
        primary key (id)
    );

    create table portal.formal_tasks (
       id bigserial not null,
        agreed boolean not null,
        author varchar(255),
        creation_date timestamp,
        deadline_date timestamp,
        end_date timestamp,
        fgis_id varchar(255),
        modification_date timestamp,
        priority varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        informal_task_id bigserial,
        user_id bigserial not null,
        primary key (id)
    );

    create table portal.informal_tasks (
       id bigserial not null,
        author varchar(255),
        confirmed boolean not null,
        content bytea,
        creation_date timestamp,
        title varchar(255),
        primary key (id)
    );

    create table portal.user_roles (
       id bigserial not null,
        role varchar(255) not null,
        primary key (id)
    );

    create table portal.user_roles_users (
       user_role_id bigserial not null,
        user_id bigserial not null
    );

    create table portal.users (
       id bigserial not null,
        email varchar(255),
        first_name varchar(255),
        second_name varchar(255),
        user_name varchar(255) not null,
        department_id bigserial,
        primary key (id)
    );

    alter table portal.users 
       add constraint UK_k8d0f2n7n88w1a16yhua64onx unique (user_name);

    create table hibernate_sequences (
       sequence_name varchar(255) not null,
        next_val int8,
        primary key (sequence_name)
    );

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    alter table portal.arrangement_items 
       add constraint FK_arrangement_items_arrangement_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangement_results 
       add constraint FK_arrangement_results_job_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangements 
       add constraint FK_arrangements_access_tool_id 
       foreign key (access_tool_id) 
       references portal.access_tools;

    alter table portal.arrangements 
       add constraint FK_arrangements_formal_task_id 
       foreign key (formal_task_id) 
       references portal.formal_tasks;

    alter table portal.departments 
       add constraint FK_departments_parent_id 
       foreign key (parent_id) 
       references portal.departments;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_informal_task_id 
       foreign key (informal_task_id) 
       references portal.informal_tasks;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_role_id 
       foreign key (user_role_id) 
       references portal.user_roles;

    alter table portal.users 
       add constraint FK_users_department_id 
       foreign key (department_id) 
       references portal.departments;

    create table portal.access_tools (
       id bigserial not null,
        name varchar(255) not null,
        type varchar(255) not null,
        primary key (id)
    );

    create table portal.arrangement_items (
       id bigserial not null,
        arrangement_id bigserial not null,
        content_id int8 not null,
        primary key (id)
    );

    create table portal.arrangement_results (
       id bigserial not null,
        result varchar(255),
        screenshot bytea,
        url varchar(255),
        content_id int8 not null,
        arrangement_id bigserial not null,
        primary key (id)
    );

    create table portal.arrangements (
       id bigserial not null,
        creation_date timestamp,
        end_date timestamp,
        result varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        access_tool_id bigserial not null,
        formal_task_id bigserial not null,
        primary key (id)
    );

    create table portal.departments (
       id bigserial not null,
        name varchar(255) not null,
        parent_id bigserial,
        primary key (id)
    );

    create table portal.formal_tasks (
       id bigserial not null,
        agreed boolean not null,
        author varchar(255),
        creation_date timestamp,
        deadline_date timestamp,
        end_date timestamp,
        fgis_id varchar(255),
        modification_date timestamp,
        priority varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        informal_task_id bigserial,
        user_id bigserial not null,
        primary key (id)
    );

    create table portal.informal_tasks (
       id bigserial not null,
        author varchar(255),
        confirmed boolean not null,
        content bytea,
        creation_date timestamp,
        title varchar(255),
        primary key (id)
    );

    create table portal.user_roles (
       id bigserial not null,
        role varchar(255) not null,
        primary key (id)
    );

    create table portal.user_roles_users (
       user_role_id bigserial not null,
        user_id bigserial not null
    );

    create table portal.users (
       id bigserial not null,
        email varchar(255),
        first_name varchar(255),
        second_name varchar(255),
        user_name varchar(255) not null,
        department_id bigserial,
        primary key (id)
    );

    alter table portal.users 
       add constraint UK_k8d0f2n7n88w1a16yhua64onx unique (user_name);

    create table hibernate_sequences (
       sequence_name varchar(255) not null,
        next_val int8,
        primary key (sequence_name)
    );

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    alter table portal.arrangement_items 
       add constraint FK_arrangement_items_arrangement_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangement_results 
       add constraint FK_arrangement_results_job_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangements 
       add constraint FK_arrangements_access_tool_id 
       foreign key (access_tool_id) 
       references portal.access_tools;

    alter table portal.arrangements 
       add constraint FK_arrangements_formal_task_id 
       foreign key (formal_task_id) 
       references portal.formal_tasks;

    alter table portal.departments 
       add constraint FK_departments_parent_id 
       foreign key (parent_id) 
       references portal.departments;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_informal_task_id 
       foreign key (informal_task_id) 
       references portal.informal_tasks;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_role_id 
       foreign key (user_role_id) 
       references portal.user_roles;

    alter table portal.users 
       add constraint FK_users_department_id 
       foreign key (department_id) 
       references portal.departments;

    create table portal.access_tools (
       id bigserial not null,
        name varchar(255) not null,
        type varchar(255) not null,
        primary key (id)
    );

    create table portal.arrangement_items (
       id bigserial not null,
        arrangement_id bigserial not null,
        content_id int8 not null,
        primary key (id)
    );

    create table portal.arrangement_results (
       id bigserial not null,
        result varchar(255),
        screenshot bytea,
        url varchar(255),
        content_id int8 not null,
        arrangement_id bigserial not null,
        primary key (id)
    );

    create table portal.arrangements (
       id bigserial not null,
        creation_date timestamp,
        end_date timestamp,
        result varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        access_tool_id bigserial not null,
        formal_task_id bigserial not null,
        primary key (id)
    );

    create table portal.departments (
       id bigserial not null,
        name varchar(255) not null,
        parent_id bigserial,
        primary key (id)
    );

    create table portal.formal_tasks (
       id bigserial not null,
        agreed boolean not null,
        author varchar(255),
        creation_date timestamp,
        deadline_date timestamp,
        end_date timestamp,
        fgis_id varchar(255),
        modification_date timestamp,
        priority varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        informal_task_id bigserial,
        user_id bigserial not null,
        primary key (id)
    );

    create table portal.informal_tasks (
       id bigserial not null,
        author varchar(255),
        confirmed boolean not null,
        content bytea,
        creation_date timestamp,
        title varchar(255),
        primary key (id)
    );

    create table portal.user_roles (
       id bigserial not null,
        role varchar(255) not null,
        primary key (id)
    );

    create table portal.user_roles_users (
       user_role_id bigserial not null,
        user_id bigserial not null
    );

    create table portal.users (
       id bigserial not null,
        email varchar(255),
        first_name varchar(255),
        second_name varchar(255),
        user_name varchar(255) not null,
        department_id bigserial,
        primary key (id)
    );

    alter table portal.users 
       add constraint UK_k8d0f2n7n88w1a16yhua64onx unique (user_name);

    create table hibernate_sequences (
       sequence_name varchar(255) not null,
        next_val int8,
        primary key (sequence_name)
    );

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    alter table portal.arrangement_items 
       add constraint FK_arrangement_items_arrangement_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangement_results 
       add constraint FK_arrangement_results_job_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangements 
       add constraint FK_arrangements_access_tool_id 
       foreign key (access_tool_id) 
       references portal.access_tools;

    alter table portal.arrangements 
       add constraint FK_arrangements_formal_task_id 
       foreign key (formal_task_id) 
       references portal.formal_tasks;

    alter table portal.departments 
       add constraint FK_departments_parent_id 
       foreign key (parent_id) 
       references portal.departments;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_informal_task_id 
       foreign key (informal_task_id) 
       references portal.informal_tasks;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_role_id 
       foreign key (user_role_id) 
       references portal.user_roles;

    alter table portal.users 
       add constraint FK_users_department_id 
       foreign key (department_id) 
       references portal.departments;

    create table portal.access_tools (
       id bigserial not null,
        name varchar(255) not null,
        type varchar(255) not null,
        primary key (id)
    );

    create table portal.arrangement_items (
       id bigserial not null,
        arrangement_id bigserial not null,
        content_id int8 not null,
        primary key (id)
    );

    create table portal.arrangement_results (
       id bigserial not null,
        result varchar(255),
        screenshot bytea,
        url varchar(255),
        content_id int8 not null,
        arrangement_id bigserial not null,
        primary key (id)
    );

    create table portal.arrangements (
       id bigserial not null,
        creation_date timestamp,
        end_date timestamp,
        result varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        access_tool_id bigserial not null,
        formal_task_id bigserial not null,
        primary key (id)
    );

    create table portal.departments (
       id bigserial not null,
        name varchar(255) not null,
        parent_id bigserial,
        primary key (id)
    );

    create table portal.formal_tasks (
       id bigserial not null,
        agreed boolean not null,
        author varchar(255),
        creation_date timestamp,
        deadline_date timestamp,
        end_date timestamp,
        fgis_id varchar(255),
        modification_date timestamp,
        priority varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        informal_task_id bigserial,
        user_id bigserial not null,
        primary key (id)
    );

    create table portal.informal_tasks (
       id bigserial not null,
        author varchar(255),
        confirmed boolean not null,
        content bytea,
        creation_date timestamp,
        title varchar(255),
        primary key (id)
    );

    create table portal.user_roles (
       id bigserial not null,
        role varchar(255) not null,
        primary key (id)
    );

    create table portal.user_roles_users (
       user_role_id bigserial not null,
        user_id bigserial not null
    );

    create table portal.users (
       id bigserial not null,
        email varchar(255),
        first_name varchar(255),
        second_name varchar(255),
        user_name varchar(255) not null,
        department_id bigserial,
        primary key (id)
    );

    alter table portal.users 
       add constraint UK_k8d0f2n7n88w1a16yhua64onx unique (user_name);

    create table hibernate_sequences (
       sequence_name varchar(255) not null,
        next_val int8,
        primary key (sequence_name)
    );

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    alter table portal.arrangement_items 
       add constraint FK_arrangement_items_arrangement_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangement_results 
       add constraint FK_arrangement_results_job_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangements 
       add constraint FK_arrangements_access_tool_id 
       foreign key (access_tool_id) 
       references portal.access_tools;

    alter table portal.arrangements 
       add constraint FK_arrangements_formal_task_id 
       foreign key (formal_task_id) 
       references portal.formal_tasks;

    alter table portal.departments 
       add constraint FK_departments_parent_id 
       foreign key (parent_id) 
       references portal.departments;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_informal_task_id 
       foreign key (informal_task_id) 
       references portal.informal_tasks;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_role_id 
       foreign key (user_role_id) 
       references portal.user_roles;

    alter table portal.users 
       add constraint FK_users_department_id 
       foreign key (department_id) 
       references portal.departments;

    create table portal.access_tools (
       id bigserial not null,
        name varchar(255) not null,
        type varchar(255) not null,
        primary key (id)
    );

    create table portal.arrangement_items (
       id bigserial not null,
        arrangement_id bigserial not null,
        content_id int8 not null,
        primary key (id)
    );

    create table portal.arrangement_results (
       id bigserial not null,
        result varchar(255),
        screenshot bytea,
        url varchar(255),
        content_id int8 not null,
        arrangement_id bigserial not null,
        primary key (id)
    );

    create table portal.arrangements (
       id bigserial not null,
        creation_date timestamp,
        end_date timestamp,
        result varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        access_tool_id bigserial not null,
        formal_task_id bigserial not null,
        primary key (id)
    );

    create table portal.departments (
       id bigserial not null,
        name varchar(255) not null,
        parent_id bigserial,
        primary key (id)
    );

    create table portal.formal_tasks (
       id bigserial not null,
        agreed boolean not null,
        author varchar(255),
        creation_date timestamp,
        deadline_date timestamp,
        end_date timestamp,
        fgis_id varchar(255),
        modification_date timestamp,
        priority varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        informal_task_id bigserial,
        user_id bigserial not null,
        primary key (id)
    );

    create table portal.informal_tasks (
       id bigserial not null,
        author varchar(255),
        confirmed boolean not null,
        content bytea,
        creation_date timestamp,
        title varchar(255),
        primary key (id)
    );

    create table portal.user_roles (
       id bigserial not null,
        role varchar(255) not null,
        primary key (id)
    );

    create table portal.user_roles_users (
       user_role_id bigserial not null,
        user_id bigserial not null
    );

    create table portal.users (
       id bigserial not null,
        email varchar(255),
        first_name varchar(255),
        second_name varchar(255),
        user_name varchar(255) not null,
        department_id bigserial,
        primary key (id)
    );

    alter table portal.users 
       add constraint UK_k8d0f2n7n88w1a16yhua64onx unique (user_name);

    create table hibernate_sequences (
       sequence_name varchar(255) not null,
        next_val int8,
        primary key (sequence_name)
    );

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    alter table portal.arrangement_items 
       add constraint FK_arrangement_items_arrangement_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangement_results 
       add constraint FK_arrangement_results_job_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangements 
       add constraint FK_arrangements_access_tool_id 
       foreign key (access_tool_id) 
       references portal.access_tools;

    alter table portal.arrangements 
       add constraint FK_arrangements_formal_task_id 
       foreign key (formal_task_id) 
       references portal.formal_tasks;

    alter table portal.departments 
       add constraint FK_departments_parent_id 
       foreign key (parent_id) 
       references portal.departments;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_informal_task_id 
       foreign key (informal_task_id) 
       references portal.informal_tasks;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_role_id 
       foreign key (user_role_id) 
       references portal.user_roles;

    alter table portal.users 
       add constraint FK_users_department_id 
       foreign key (department_id) 
       references portal.departments;

    create table portal.access_tools (
       id bigserial not null,
        name varchar(255) not null,
        type varchar(255) not null,
        primary key (id)
    );

    create table portal.arrangement_items (
       id bigserial not null,
        arrangement_id bigserial not null,
        content_id int8 not null,
        primary key (id)
    );

    create table portal.arrangement_results (
       id bigserial not null,
        result varchar(255),
        screenshot bytea,
        url varchar(255),
        content_id int8 not null,
        arrangement_id bigserial not null,
        primary key (id)
    );

    create table portal.arrangements (
       id bigserial not null,
        creation_date timestamp,
        end_date timestamp,
        result varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        access_tool_id bigserial not null,
        formal_task_id bigserial not null,
        primary key (id)
    );

    create table portal.departments (
       id bigserial not null,
        name varchar(255) not null,
        parent_id bigserial,
        primary key (id)
    );

    create table portal.formal_tasks (
       id bigserial not null,
        agreed boolean not null,
        author varchar(255),
        creation_date timestamp,
        deadline_date timestamp,
        end_date timestamp,
        fgis_id varchar(255),
        modification_date timestamp,
        priority varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        informal_task_id bigserial,
        user_id bigserial not null,
        primary key (id)
    );

    create table portal.informal_tasks (
       id bigserial not null,
        author varchar(255),
        confirmed boolean not null,
        content bytea,
        creation_date timestamp,
        title varchar(255),
        primary key (id)
    );

    create table portal.user_roles (
       id bigserial not null,
        role varchar(255) not null,
        primary key (id)
    );

    create table portal.user_roles_users (
       user_role_id bigserial not null,
        user_id bigserial not null
    );

    create table portal.users (
       id bigserial not null,
        email varchar(255),
        first_name varchar(255),
        second_name varchar(255),
        user_name varchar(255) not null,
        department_id bigserial,
        primary key (id)
    );

    alter table portal.users 
       add constraint UK_k8d0f2n7n88w1a16yhua64onx unique (user_name);

    create table hibernate_sequences (
       sequence_name varchar(255) not null,
        next_val int8,
        primary key (sequence_name)
    );

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    alter table portal.arrangement_items 
       add constraint FK_arrangement_items_arrangement_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangement_results 
       add constraint FK_arrangement_results_job_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangements 
       add constraint FK_arrangements_access_tool_id 
       foreign key (access_tool_id) 
       references portal.access_tools;

    alter table portal.arrangements 
       add constraint FK_arrangements_formal_task_id 
       foreign key (formal_task_id) 
       references portal.formal_tasks;

    alter table portal.departments 
       add constraint FK_departments_parent_id 
       foreign key (parent_id) 
       references portal.departments;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_informal_task_id 
       foreign key (informal_task_id) 
       references portal.informal_tasks;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_role_id 
       foreign key (user_role_id) 
       references portal.user_roles;

    alter table portal.users 
       add constraint FK_users_department_id 
       foreign key (department_id) 
       references portal.departments;

    create table portal.access_tools (
       id bigserial not null,
        name varchar(255) not null,
        type varchar(255) not null,
        primary key (id)
    );

    create table portal.arrangement_items (
       id bigserial not null,
        arrangement_id bigserial not null,
        content_id int8 not null,
        primary key (id)
    );

    create table portal.arrangement_results (
       id bigserial not null,
        result varchar(255),
        screenshot bytea,
        url varchar(255),
        content_id int8 not null,
        arrangement_id bigserial not null,
        primary key (id)
    );

    create table portal.arrangements (
       id bigserial not null,
        creation_date timestamp,
        end_date timestamp,
        result varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        access_tool_id bigserial not null,
        formal_task_id bigserial not null,
        primary key (id)
    );

    create table portal.departments (
       id bigserial not null,
        name varchar(255) not null,
        parent_id bigserial,
        primary key (id)
    );

    create table portal.formal_tasks (
       id bigserial not null,
        agreed boolean not null,
        author varchar(255),
        creation_date timestamp,
        deadline_date timestamp,
        end_date timestamp,
        fgis_id varchar(255),
        modification_date timestamp,
        priority varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        informal_task_id bigserial,
        user_id bigserial not null,
        primary key (id)
    );

    create table portal.informal_tasks (
       id bigserial not null,
        author varchar(255),
        confirmed boolean not null,
        content bytea,
        creation_date timestamp,
        title varchar(255),
        primary key (id)
    );

    create table portal.user_roles (
       id bigserial not null,
        role varchar(255) not null,
        primary key (id)
    );

    create table portal.user_roles_users (
       user_role_id bigserial not null,
        user_id bigserial not null
    );

    create table portal.users (
       id bigserial not null,
        email varchar(255),
        first_name varchar(255),
        second_name varchar(255),
        user_name varchar(255) not null,
        department_id bigserial,
        primary key (id)
    );

    alter table portal.users 
       add constraint UK_k8d0f2n7n88w1a16yhua64onx unique (user_name);

    create table hibernate_sequences (
       sequence_name varchar(255) not null,
        next_val int8,
        primary key (sequence_name)
    );

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    alter table portal.arrangement_items 
       add constraint FK_arrangement_items_arrangement_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangement_results 
       add constraint FK_arrangement_results_job_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangements 
       add constraint FK_arrangements_access_tool_id 
       foreign key (access_tool_id) 
       references portal.access_tools;

    alter table portal.arrangements 
       add constraint FK_arrangements_formal_task_id 
       foreign key (formal_task_id) 
       references portal.formal_tasks;

    alter table portal.departments 
       add constraint FK_departments_parent_id 
       foreign key (parent_id) 
       references portal.departments;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_informal_task_id 
       foreign key (informal_task_id) 
       references portal.informal_tasks;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_role_id 
       foreign key (user_role_id) 
       references portal.user_roles;

    alter table portal.users 
       add constraint FK_users_department_id 
       foreign key (department_id) 
       references portal.departments;

    create table portal.access_tools (
       id bigserial not null,
        name varchar(255) not null,
        type varchar(255) not null,
        primary key (id)
    );

    create table portal.arrangement_items (
       id bigserial not null,
        arrangement_id bigserial not null,
        content_id int8 not null,
        primary key (id)
    );

    create table portal.arrangement_results (
       id bigserial not null,
        result varchar(255),
        screenshot bytea,
        url varchar(255),
        content_id int8 not null,
        arrangement_id bigserial not null,
        primary key (id)
    );

    create table portal.arrangements (
       id bigserial not null,
        creation_date timestamp,
        end_date timestamp,
        result varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        access_tool_id bigserial not null,
        formal_task_id bigserial not null,
        primary key (id)
    );

    create table portal.departments (
       id bigserial not null,
        name varchar(255) not null,
        parent_id bigserial,
        primary key (id)
    );

    create table portal.formal_tasks (
       id bigserial not null,
        agreed boolean not null,
        author varchar(255),
        creation_date timestamp,
        deadline_date timestamp,
        end_date timestamp,
        fgis_id varchar(255),
        modification_date timestamp,
        priority varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        informal_task_id bigserial,
        user_id bigserial not null,
        primary key (id)
    );

    create table portal.informal_tasks (
       id bigserial not null,
        author varchar(255),
        confirmed boolean not null,
        content bytea,
        creation_date timestamp,
        title varchar(255),
        primary key (id)
    );

    create table portal.user_roles (
       id bigserial not null,
        role varchar(255) not null,
        primary key (id)
    );

    create table portal.user_roles_users (
       user_role_id bigserial not null,
        user_id bigserial not null
    );

    create table portal.users (
       id bigserial not null,
        email varchar(255),
        first_name varchar(255),
        second_name varchar(255),
        user_name varchar(255) not null,
        department_id bigserial,
        primary key (id)
    );

    alter table portal.users 
       add constraint UK_k8d0f2n7n88w1a16yhua64onx unique (user_name);

    create table hibernate_sequences (
       sequence_name varchar(255) not null,
        next_val int8,
        primary key (sequence_name)
    );

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    alter table portal.arrangement_items 
       add constraint FK_arrangement_items_arrangement_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangement_results 
       add constraint FK_arrangement_results_job_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangements 
       add constraint FK_arrangements_access_tool_id 
       foreign key (access_tool_id) 
       references portal.access_tools;

    alter table portal.arrangements 
       add constraint FK_arrangements_formal_task_id 
       foreign key (formal_task_id) 
       references portal.formal_tasks;

    alter table portal.departments 
       add constraint FK_departments_parent_id 
       foreign key (parent_id) 
       references portal.departments;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_informal_task_id 
       foreign key (informal_task_id) 
       references portal.informal_tasks;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_role_id 
       foreign key (user_role_id) 
       references portal.user_roles;

    alter table portal.users 
       add constraint FK_users_department_id 
       foreign key (department_id) 
       references portal.departments;

    create table portal.access_tools (
       id bigserial not null,
        name varchar(255) not null,
        type varchar(255) not null,
        primary key (id)
    );

    create table portal.arrangement_items (
       id bigserial not null,
        arrangement_id bigserial not null,
        content_id int8 not null,
        primary key (id)
    );

    create table portal.arrangement_results (
       id bigserial not null,
        result varchar(255),
        screenshot bytea,
        url varchar(255),
        content_id int8 not null,
        arrangement_id bigserial not null,
        primary key (id)
    );

    create table portal.arrangements (
       id bigserial not null,
        creation_date timestamp,
        end_date timestamp,
        result varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        access_tool_id bigserial not null,
        formal_task_id bigserial not null,
        primary key (id)
    );

    create table portal.departments (
       id bigserial not null,
        name varchar(255) not null,
        parent_id bigserial,
        primary key (id)
    );

    create table portal.formal_tasks (
       id bigserial not null,
        agreed boolean not null,
        author varchar(255),
        creation_date timestamp,
        deadline_date timestamp,
        end_date timestamp,
        fgis_id varchar(255),
        modification_date timestamp,
        priority varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        informal_task_id bigserial,
        user_id bigserial not null,
        primary key (id)
    );

    create table portal.informal_tasks (
       id bigserial not null,
        author varchar(255),
        confirmed boolean not null,
        content bytea,
        creation_date timestamp,
        title varchar(255),
        primary key (id)
    );

    create table portal.user_roles (
       id bigserial not null,
        role varchar(255) not null,
        primary key (id)
    );

    create table portal.user_roles_users (
       user_role_id bigserial not null,
        user_id bigserial not null
    );

    create table portal.users (
       id bigserial not null,
        email varchar(255),
        first_name varchar(255),
        second_name varchar(255),
        user_name varchar(255) not null,
        department_id bigserial,
        primary key (id)
    );

    alter table portal.users 
       add constraint UK_k8d0f2n7n88w1a16yhua64onx unique (user_name);

    create table hibernate_sequences (
       sequence_name varchar(255) not null,
        next_val int8,
        primary key (sequence_name)
    );

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    alter table portal.arrangement_items 
       add constraint FK_arrangement_items_arrangement_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangement_results 
       add constraint FK_arrangement_results_job_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangements 
       add constraint FK_arrangements_access_tool_id 
       foreign key (access_tool_id) 
       references portal.access_tools;

    alter table portal.arrangements 
       add constraint FK_arrangements_formal_task_id 
       foreign key (formal_task_id) 
       references portal.formal_tasks;

    alter table portal.departments 
       add constraint FK_departments_parent_id 
       foreign key (parent_id) 
       references portal.departments;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_informal_task_id 
       foreign key (informal_task_id) 
       references portal.informal_tasks;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_role_id 
       foreign key (user_role_id) 
       references portal.user_roles;

    alter table portal.users 
       add constraint FK_users_department_id 
       foreign key (department_id) 
       references portal.departments;

    create table portal.access_tools (
       id bigserial not null,
        name varchar(255) not null,
        type varchar(255) not null,
        primary key (id)
    );

    create table portal.arrangement_items (
       id bigserial not null,
        arrangement_id bigserial not null,
        content_id int8 not null,
        primary key (id)
    );

    create table portal.arrangement_results (
       id bigserial not null,
        result varchar(255),
        screenshot bytea,
        url varchar(255),
        content_id int8 not null,
        arrangement_id bigserial not null,
        primary key (id)
    );

    create table portal.arrangements (
       id bigserial not null,
        creation_date timestamp,
        end_date timestamp,
        result varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        access_tool_id bigserial not null,
        formal_task_id bigserial not null,
        primary key (id)
    );

    create table portal.departments (
       id bigserial not null,
        name varchar(255) not null,
        parent_id bigserial,
        primary key (id)
    );

    create table portal.formal_tasks (
       id bigserial not null,
        agreed boolean not null,
        author varchar(255),
        creation_date timestamp,
        deadline_date timestamp,
        end_date timestamp,
        fgis_id varchar(255),
        modification_date timestamp,
        priority varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        informal_task_id bigserial,
        user_id bigserial not null,
        primary key (id)
    );

    create table portal.informal_tasks (
       id bigserial not null,
        author varchar(255),
        confirmed boolean not null,
        content bytea,
        creation_date timestamp,
        title varchar(255),
        primary key (id)
    );

    create table portal.user_roles (
       id bigserial not null,
        role varchar(255) not null,
        primary key (id)
    );

    create table portal.user_roles_users (
       user_role_id bigserial not null,
        user_id bigserial not null
    );

    create table portal.users (
       id bigserial not null,
        email varchar(255),
        first_name varchar(255),
        second_name varchar(255),
        user_name varchar(255) not null,
        department_id bigserial,
        primary key (id)
    );

    alter table portal.users 
       add constraint UK_k8d0f2n7n88w1a16yhua64onx unique (user_name);

    create table hibernate_sequences (
       sequence_name varchar(255) not null,
        next_val int8,
        primary key (sequence_name)
    );

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    alter table portal.arrangement_items 
       add constraint FK_arrangement_items_arrangement_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangement_results 
       add constraint FK_arrangement_results_job_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangements 
       add constraint FK_arrangements_access_tool_id 
       foreign key (access_tool_id) 
       references portal.access_tools;

    alter table portal.arrangements 
       add constraint FK_arrangements_formal_task_id 
       foreign key (formal_task_id) 
       references portal.formal_tasks;

    alter table portal.departments 
       add constraint FK_departments_parent_id 
       foreign key (parent_id) 
       references portal.departments;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_informal_task_id 
       foreign key (informal_task_id) 
       references portal.informal_tasks;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_role_id 
       foreign key (user_role_id) 
       references portal.user_roles;

    alter table portal.users 
       add constraint FK_users_department_id 
       foreign key (department_id) 
       references portal.departments;

    create table portal.access_tools (
       id bigserial not null,
        name varchar(255) not null,
        type varchar(255) not null,
        primary key (id)
    );

    create table portal.arrangement_items (
       id bigserial not null,
        arrangement_id bigserial not null,
        content_id int8 not null,
        primary key (id)
    );

    create table portal.arrangement_results (
       id bigserial not null,
        result varchar(255),
        screenshot bytea,
        url varchar(255),
        content_id int8 not null,
        arrangement_id bigserial not null,
        primary key (id)
    );

    create table portal.arrangements (
       id bigserial not null,
        creation_date timestamp,
        end_date timestamp,
        result varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        access_tool_id bigserial not null,
        formal_task_id bigserial not null,
        primary key (id)
    );

    create table portal.departments (
       id bigserial not null,
        name varchar(255) not null,
        parent_id bigserial,
        primary key (id)
    );

    create table portal.formal_tasks (
       id bigserial not null,
        agreed boolean not null,
        author varchar(255),
        creation_date timestamp,
        deadline_date timestamp,
        end_date timestamp,
        fgis_id varchar(255),
        modification_date timestamp,
        priority varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        informal_task_id bigserial,
        user_id bigserial not null,
        primary key (id)
    );

    create table portal.informal_tasks (
       id bigserial not null,
        author varchar(255),
        confirmed boolean not null,
        content bytea,
        creation_date timestamp,
        title varchar(255),
        primary key (id)
    );

    create table portal.user_roles (
       id bigserial not null,
        role varchar(255) not null,
        primary key (id)
    );

    create table portal.user_roles_users (
       user_role_id bigserial not null,
        user_id bigserial not null
    );

    create table portal.users (
       id bigserial not null,
        email varchar(255),
        first_name varchar(255),
        second_name varchar(255),
        user_name varchar(255) not null,
        department_id bigserial,
        primary key (id)
    );

    alter table portal.users 
       add constraint UK_k8d0f2n7n88w1a16yhua64onx unique (user_name);

    create table hibernate_sequences (
       sequence_name varchar(255) not null,
        next_val int8,
        primary key (sequence_name)
    );

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    alter table portal.arrangement_items 
       add constraint FK_arrangement_items_arrangement_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangement_results 
       add constraint FK_arrangement_results_job_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangements 
       add constraint FK_arrangements_access_tool_id 
       foreign key (access_tool_id) 
       references portal.access_tools;

    alter table portal.arrangements 
       add constraint FK_arrangements_formal_task_id 
       foreign key (formal_task_id) 
       references portal.formal_tasks;

    alter table portal.departments 
       add constraint FK_departments_parent_id 
       foreign key (parent_id) 
       references portal.departments;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_informal_task_id 
       foreign key (informal_task_id) 
       references portal.informal_tasks;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_role_id 
       foreign key (user_role_id) 
       references portal.user_roles;

    alter table portal.users 
       add constraint FK_users_department_id 
       foreign key (department_id) 
       references portal.departments;

    create table portal.access_tools (
       id bigserial not null,
        name varchar(255) not null,
        type varchar(255) not null,
        primary key (id)
    );

    create table portal.arrangement_items (
       id bigserial not null,
        arrangement_id bigserial not null,
        content_id int8 not null,
        primary key (id)
    );

    create table portal.arrangement_results (
       id bigserial not null,
        result varchar(255),
        screenshot bytea,
        url varchar(255),
        content_id int8 not null,
        arrangement_id bigserial not null,
        primary key (id)
    );

    create table portal.arrangements (
       id bigserial not null,
        creation_date timestamp,
        end_date timestamp,
        result varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        access_tool_id bigserial not null,
        formal_task_id bigserial not null,
        primary key (id)
    );

    create table portal.departments (
       id bigserial not null,
        name varchar(255) not null,
        parent_id bigserial,
        primary key (id)
    );

    create table portal.formal_tasks (
       id bigserial not null,
        agreed boolean not null,
        author varchar(255),
        creation_date timestamp,
        deadline_date timestamp,
        end_date timestamp,
        fgis_id varchar(255),
        modification_date timestamp,
        priority varchar(255),
        start_date timestamp,
        status varchar(255) not null,
        title varchar(255) not null,
        informal_task_id bigserial,
        user_id bigserial not null,
        primary key (id)
    );

    create table portal.informal_tasks (
       id bigserial not null,
        author varchar(255),
        confirmed boolean not null,
        content bytea,
        creation_date timestamp,
        title varchar(255),
        primary key (id)
    );

    create table portal.user_roles (
       id bigserial not null,
        role varchar(255) not null,
        primary key (id)
    );

    create table portal.user_roles_users (
       user_role_id bigserial not null,
        user_id bigserial not null
    );

    create table portal.users (
       id bigserial not null,
        email varchar(255),
        first_name varchar(255),
        second_name varchar(255),
        user_name varchar(255) not null,
        department_id bigserial,
        primary key (id)
    );

    alter table portal.users 
       add constraint UK_k8d0f2n7n88w1a16yhua64onx unique (user_name);

    create table hibernate_sequences (
       sequence_name varchar(255) not null,
        next_val int8,
        primary key (sequence_name)
    );

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    insert into hibernate_sequences(sequence_name, next_val) values ('default',0);

    alter table portal.arrangement_items 
       add constraint FK_arrangement_items_arrangement_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangement_results 
       add constraint FK_arrangement_results_job_id 
       foreign key (arrangement_id) 
       references portal.arrangements;

    alter table portal.arrangements 
       add constraint FK_arrangements_access_tool_id 
       foreign key (access_tool_id) 
       references portal.access_tools;

    alter table portal.arrangements 
       add constraint FK_arrangements_formal_task_id 
       foreign key (formal_task_id) 
       references portal.formal_tasks;

    alter table portal.departments 
       add constraint FK_departments_parent_id 
       foreign key (parent_id) 
       references portal.departments;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_informal_task_id 
       foreign key (informal_task_id) 
       references portal.informal_tasks;

    alter table portal.formal_tasks 
       add constraint FK_formal_tasks_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_id 
       foreign key (user_id) 
       references portal.users;

    alter table portal.user_roles_users 
       add constraint FK_user_roles_users_user_role_id 
       foreign key (user_role_id) 
       references portal.user_roles;

    alter table portal.users 
       add constraint FK_users_department_id 
       foreign key (department_id) 
       references portal.departments;
