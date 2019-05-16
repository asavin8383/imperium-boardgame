
    alter table portal.arrangement_items 
       drop constraint if exists FK_arrangement_items_arrangement_id;

    alter table portal.arrangement_results 
       drop constraint if exists FK_arrangement_results_job_id;

    alter table portal.arrangements 
       drop constraint if exists FK_arrangements_access_tool_id;

    alter table portal.arrangements 
       drop constraint if exists FK_arrangements_formal_task_id;

    alter table portal.departments 
       drop constraint if exists FK_departments_parent_id;

    alter table portal.formal_tasks 
       drop constraint if exists FK_formal_tasks_user_id;

    alter table portal.formal_tasks 
       drop constraint if exists FK_formal_tasks_informal_task_id;

    alter table portal.user_roles_users 
       drop constraint if exists FK_user_roles_users_user_id;

    alter table portal.user_roles_users 
       drop constraint if exists FK_user_roles_users_user_role_id;

    alter table portal.users 
       drop constraint if exists FK_users_department_id;

    drop table if exists portal.access_tools cascade;

    drop table if exists portal.arrangement_items cascade;

    drop table if exists portal.arrangement_results cascade;

    drop table if exists portal.arrangements cascade;

    drop table if exists portal.departments cascade;

    drop table if exists portal.formal_tasks cascade;

    drop table if exists portal.informal_tasks cascade;

    drop table if exists portal.user_roles cascade;

    drop table if exists portal.user_roles_users cascade;

    drop table if exists portal.users cascade;

    drop table if exists hibernate_sequences cascade;

    alter table portal.arrangement_items 
       drop constraint if exists FK_arrangement_items_arrangement_id;

    alter table portal.arrangement_results 
       drop constraint if exists FK_arrangement_results_job_id;

    alter table portal.arrangements 
       drop constraint if exists FK_arrangements_access_tool_id;

    alter table portal.arrangements 
       drop constraint if exists FK_arrangements_formal_task_id;

    alter table portal.departments 
       drop constraint if exists FK_departments_parent_id;

    alter table portal.formal_tasks 
       drop constraint if exists FK_formal_tasks_user_id;

    alter table portal.formal_tasks 
       drop constraint if exists FK_formal_tasks_informal_task_id;

    alter table portal.user_roles_users 
       drop constraint if exists FK_user_roles_users_user_id;

    alter table portal.user_roles_users 
       drop constraint if exists FK_user_roles_users_user_role_id;

    alter table portal.users 
       drop constraint if exists FK_users_department_id;

    drop table if exists portal.access_tools cascade;

    drop table if exists portal.arrangement_items cascade;

    drop table if exists portal.arrangement_results cascade;

    drop table if exists portal.arrangements cascade;

    drop table if exists portal.departments cascade;

    drop table if exists portal.formal_tasks cascade;

    drop table if exists portal.informal_tasks cascade;

    drop table if exists portal.user_roles cascade;

    drop table if exists portal.user_roles_users cascade;

    drop table if exists portal.users cascade;

    drop table if exists hibernate_sequences cascade;

    alter table portal.arrangement_items 
       drop constraint if exists FK_arrangement_items_arrangement_id;

    alter table portal.arrangement_results 
       drop constraint if exists FK_arrangement_results_job_id;

    alter table portal.arrangements 
       drop constraint if exists FK_arrangements_access_tool_id;

    alter table portal.arrangements 
       drop constraint if exists FK_arrangements_formal_task_id;

    alter table portal.departments 
       drop constraint if exists FK_departments_parent_id;

    alter table portal.formal_tasks 
       drop constraint if exists FK_formal_tasks_user_id;

    alter table portal.formal_tasks 
       drop constraint if exists FK_formal_tasks_informal_task_id;

    alter table portal.user_roles_users 
       drop constraint if exists FK_user_roles_users_user_id;

    alter table portal.user_roles_users 
       drop constraint if exists FK_user_roles_users_user_role_id;

    alter table portal.users 
       drop constraint if exists FK_users_department_id;

    drop table if exists portal.access_tools cascade;

    drop table if exists portal.arrangement_items cascade;

    drop table if exists portal.arrangement_results cascade;

    drop table if exists portal.arrangements cascade;

    drop table if exists portal.departments cascade;

    drop table if exists portal.formal_tasks cascade;

    drop table if exists portal.informal_tasks cascade;

    drop table if exists portal.user_roles cascade;

    drop table if exists portal.user_roles_users cascade;

    drop table if exists portal.users cascade;

    drop table if exists hibernate_sequences cascade;

    alter table portal.arrangement_items 
       drop constraint if exists FK_arrangement_items_arrangement_id;

    alter table portal.arrangement_results 
       drop constraint if exists FK_arrangement_results_job_id;

    alter table portal.arrangements 
       drop constraint if exists FK_arrangements_access_tool_id;

    alter table portal.arrangements 
       drop constraint if exists FK_arrangements_formal_task_id;

    alter table portal.departments 
       drop constraint if exists FK_departments_parent_id;

    alter table portal.formal_tasks 
       drop constraint if exists FK_formal_tasks_user_id;

    alter table portal.formal_tasks 
       drop constraint if exists FK_formal_tasks_informal_task_id;

    alter table portal.user_roles_users 
       drop constraint if exists FK_user_roles_users_user_id;

    alter table portal.user_roles_users 
       drop constraint if exists FK_user_roles_users_user_role_id;

    alter table portal.users 
       drop constraint if exists FK_users_department_id;

    drop table if exists portal.access_tools cascade;

    drop table if exists portal.arrangement_items cascade;

    drop table if exists portal.arrangement_results cascade;

    drop table if exists portal.arrangements cascade;

    drop table if exists portal.departments cascade;

    drop table if exists portal.formal_tasks cascade;

    drop table if exists portal.informal_tasks cascade;

    drop table if exists portal.user_roles cascade;

    drop table if exists portal.user_roles_users cascade;

    drop table if exists portal.users cascade;

    drop table if exists hibernate_sequences cascade;
