alter materialized view if exists sor.content_view rename  to content_view_legacy;
create table if not exists sor.content_view  as (select * from sor.content_view_legacy);
create index if not exists content_view_content_id on sor.content_view(content_id);

create or replace function sor.update_content_view() returns void
    language plpgsql
as
$$
BEGIN

delete from sor.content_view where content_id in (
    select content_id from sor.content_history where content_version_id=(select max(content_version_id) from sor.content_history)
    );
    WITH
         new_content as (
   select content_id,content_version_id, (st_dt=end_dt) is_delete from sor.content_history where content_version_id=(select max(content_version_id) from sor.content_history)
),
         help AS (
        SELECT content.erdi_id,
               content.id      AS content_id,
               max(addon_1.id) AS addon_id,
               history.content_version_id,
               info.irtz_type,
               info.includetime
        FROM (((sor.content content
            join new_content on new_content.content_id=content.id and not is_delete
            JOIN sor.content_history history ON (((content.id = history.content_id) AND
                                                  (history.end_dt = to_date('30000101'::text, 'YYYYMMDD'::text)))))
            LEFT JOIN sor.addon addon_1 ON (((content.id = addon_1.content_id) AND
                                             (history.addon_version_id = addon_1.addon_version_id))))
                 JOIN sor.content_info info
                      ON (((content.id = info.content_id) AND (history.content_version_id = info.content_version_id))))
        GROUP BY content.id, history.content_version_id, info.irtz_type, info.includetime
    )
    insert into sor.content_view(id, includetime, content_id, resource_type, info_type_id, registry_name, category_name, violation_name, decision_org, visitors_cnt_russia, visitors_cnt_world)
    SELECT help.erdi_id                                                        AS id,
           help.includetime,
           help.content_id,
           COALESCE(help.irtz_type, 'Не заполнено'::text)                      AS resource_type,
           COALESCE(subtype.orig_id, 'Не заполнено'::character varying)        AS info_type_id,
           COALESCE(subtype.registry_name, 'Не заполнено'::character varying)  AS registry_name,
           COALESCE(subtype.category_name, 'Не заполнено'::character varying)  AS category_name,
           COALESCE(subtype.violation_name, 'Не заполнено'::character varying) AS violation_name,
           COALESCE(decision.org, 'Не заполнено'::text)                        AS decision_org,
           COALESCE(addon.visitors_cnt_russia, (0)::bigint)                    AS visitors_cnt_russia,
           COALESCE(addon.visitors_cnt_world, (0)::bigint)                     AS visitors_cnt_world
    FROM (((help
        LEFT JOIN sor.addon addon ON ((help.addon_id = addon.id)))
        LEFT JOIN sor.subtype subtype ON ((((addon.info_type_id)::text = (subtype.orig_id)::text) AND
                                           (subtype.eff_dt = to_date('30000101'::text, 'YYYYMMDD'::text)))))
             LEFT JOIN sor.decision decision ON (((help.content_id = decision.content_id) AND
                                                  (help.content_version_id = decision.content_version_id))));

END
$$;


alter materialized view if exists sor.check_units rename  to check_units_legacy;
create table if not exists sor.check_units  as (select * from sor.check_units_legacy);
create index if not exists check_units_content_id on sor.check_units(content_id);

create or replace function sor.update_check_units() returns void
    language plpgsql
as
$$
BEGIN

delete from sor.check_units where content_id in (
    select content_id from sor.content_history where content_version_id=(select max(content_version_id) from sor.content_history)
    );
with new_content as (
   select content_id,content_version_id, (st_dt=end_dt) is_delete from sor.content_history where content_version_id=(select max(content_version_id) from sor.content_history)
)
insert into sor.check_units(check_unit_id, irtz_type, content_id, content_version_id, check_unit_value, check_unit_type) (
    SELECT resources_1.id    AS check_unit_id,
           content_info.irtz_type,
           resources_1.content_id,
           resources_1.content_version_id,
           resources_1.value AS check_unit_value,
           types.dsc         AS check_unit_type
    FROM sor.content
             join new_content on content.id = new_content.content_id and not is_delete
             join sor.content_resources resources_1 on content.id = resources_1.content_id
        and new_content.content_version_id = resources_1.content_version_id
             join sor.content_info on content.id = content_info.content_id and
                                      content_info.content_version_id = new_content.content_version_id
             JOIN sor.irtz_types_resourse_types irtz ON irtz.irtz_type = content_info.irtz_type AND
                                                        irtz.resourse_type_id = resources_1.resource_type_id
             JOIN sor.resource_type types ON resources_1.resource_type_id = types.id
);
END
$$;

