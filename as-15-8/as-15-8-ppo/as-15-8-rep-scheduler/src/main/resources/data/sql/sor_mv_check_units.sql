-- create or replace materialized view check_units as
    WITH actual_content AS (
        SELECT history.content_id,
               history.content_version_id,
               content_info.irtz_type
        FROM (sor.content_history history
                 JOIN sor.content_info content_info ON (((history.content_id = content_info.content_id) AND
                                                         (history.content_version_id = content_info.content_version_id) AND
                                                         (history.end_dt = to_date('30000101'::text, 'YYYYMMDD'::text)))))
    ),
         resources AS (
             SELECT resources_1.id    AS check_unit_id,
                    actual_content.irtz_type,
                    resources_1.content_id,
                    resources_1.content_version_id,
                    resources_1.value AS check_unit_value,
                    types.dsc         AS check_unit_type
             FROM (((sor.content_resources resources_1
                 JOIN sor.resource_type types ON ((resources_1.resource_type_id = types.id)))
                 JOIN actual_content ON (((actual_content.content_id = resources_1.content_id) AND
                                          (actual_content.content_version_id = resources_1.content_version_id))))
                      JOIN sor.irtz_types_resourse_types irtz ON (((irtz.irtz_type = actual_content.irtz_type) AND
                                                                   (irtz.resourse_type_id = resources_1.resource_type_id))))
         )
    SELECT resources.check_unit_id,
           resources.irtz_type,
           resources.content_id,
           resources.content_version_id,
           resources.check_unit_value,
           resources.check_unit_type
    FROM resources;

alter materialized view check_units owner to as_user;

create index check_unit_id_idx
    on check_units (check_unit_id);

