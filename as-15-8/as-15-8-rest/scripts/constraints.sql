alter table portal.access_tools drop constraint if exists ck_name;
alter table portal.access_tools add constraint ck_name check ( name in ('GOOGLE','YANDEX','CAMELEO_XYZ','HIDEMYASS'));
alter table portal.access_tools drop constraint if exists ck_type;
alter table portal.access_tools add constraint ck_type check ( type in ('SEARCH_SYSTEM','VPN','ANONYMIZER'));
alter table portal.arrangements drop constraint if exists ck_status;
alter table portal.arrangements add constraint ck_status check ( status in ('NEW', 'PLANNED', 'RUNNING', 'ACTION_REQUIRED', 'FINISHED'));
alter table portal.arrangement_results drop constraint if exists ck_result;
alter table portal.arrangement_results add constraint ck_result check ( result in ('COMPLETED', 'FORBIDDEN_CONTENT_DETECTED', 'CAPTCHA_DETECTED', 'DNS_ERROR', 'SOCKET_ERROR', 'HTTP_SERVER_SEND_NO_RESPONSE', 'PAGE_NOT_FOUND', 'INTERNAL_ERROR', 'TIMEOUT_ERROR' ));