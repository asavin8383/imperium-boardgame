alter table portal.access_tools drop constraint if exists ck_name;
alter table portal.access_tools add constraint ck_name check ( name in ('GOOGLE','YANDEX', 'GOOGLE_API', 'VPN', 'CAMELEO_XYZ', 'HIDEMYASS', 'HOLA', 'EXPRESS', 'KASPERSKY'));
alter table portal.access_tools drop constraint if exists ck_type;
alter table portal.access_tools add constraint ck_type check ( type in ('SEARCH_SYSTEM','VPN','ANONYMIZER', 'PROXY'));
alter table portal.arrangements drop constraint if exists ck_status;
alter table portal.arrangements add constraint ck_status check ( status in ('NEW', 'FORMED', 'SCHEDULED', 'RUNNING', 'ACTION_REQUIRED', 'FINISHED', 'ERROR'));

alter table portal.arrangement_results drop constraint if exists ck_result;
alter table portal.arrangement_results add constraint ck_result check ( result in (
'FORMED',
'RUNNING',
'COMPLETED',
'DOUBTFUL',
'FORBIDDEN_CONTENT_DETECTED',
'CAPTCHA_DETECTED',
'DNS_ERROR',
'SOCKET_ERROR',
'HTTP_SERVER_SEND_NO_RESPONSE',
'PAGE_NOT_FOUND', 
'INTERNAL_ERROR',
'TIMEOUT_ERROR' ));
alter table portal.arrangement_results drop constraint if exists ck_user_result;
alter table portal.arrangement_results add constraint ck_user_result check ( user_result in (
'COMPLETED',
'DOUBTFUL',
'FORBIDDEN_CONTENT_DETECTED'));

alter table portal.search_system_parameters drop constraint if exists ck_type;
alter table portal.search_system_parameters add constraint ck_type check ( access_tool_type in ('SEARCH_SYSTEM'));
alter table portal.vpn_parameters drop constraint if exists ck_type;
alter table portal.vpn_parameters add constraint ck_type check ( access_tool_type in ('VPN'));
alter table portal.proxy_parameters drop constraint if exists ck_type;
alter table portal.proxy_parameters add constraint ck_type check ( access_tool_type in ('PROXY'));
alter table portal.anonymizer_parameters drop constraint if exists ck_type;
alter table portal.anonymizer_parameters add constraint ck_type check ( access_tool_type in ('ANONYMIZER'));

alter table portal.global_parameters drop constraint if exists ck_key;
alter table portal.global_parameters add constraint ck_key check ( key in ('ETALON_PROXY_HOST', 'ETALON_PROXY_PORT', 'ETALON_PROXY_USERNAME', 'ETALON_PROXY_PASSWORD', 'USE_ETALON', 'TOTAL_WORKERS_COUNT'));

alter table portal.planned_processing_times drop constraint if exists ck_type;
alter table portal.planned_processing_times add constraint ck_type check ( check_method in ('BROWSER', 'NMAP'));

alter table schedule.schedule_period_check_units drop constraint if exists ck_status;
alter table schedule.schedule_period_check_units add constraint ck_status check ( status in ('READY', 'EXPIRED'));