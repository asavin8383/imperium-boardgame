alter table portal.access_tools drop constraint if exists ck_name;
alter table portal.access_tools add constraint ck_name check ( name in ('GOOGLE','YANDEX','CAMELEO_XYZ','HIDEMYASS'));
alter table portal.access_tools drop constraint if exists ck_type;
alter table portal.access_tools add constraint ck_type check ( type in ('SEARCH_SYSTEM','VPN','ANONYMIZER'));