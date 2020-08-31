
-- create  materialized view sor.content_view_kl as
    WITH help AS (
        SELECT
        *
--                content.erdi_id,
--                content.id        AS content_id,
--                max(addon_1.id)   AS addon_id,
--                history.content_version_id,
--                info.irtz_type,
--                info.includetime,
--                min(resources.id) AS resource_id
        FROM sor.content content
            JOIN sor.content_history history ON content.id = history.content_id AND
                                                  history.end_dt = to_date('30000101'::text, 'YYYYMMDD'::text)
            LEFT JOIN sor.addon addon_1 ON history.content_id = addon_1.content_id AND
                                            history.addon_version_id = addon_1.addon_version_id
            JOIN sor.content_info info ON history.content_id = info.content_id AND
                                        history.content_version_id = info.content_version_id
                 LEFT JOIN sor.content_resources resources ON history.content_id = resources.content_id AND
                                                              history.content_version_id = info.content_version_id AND
                                                                (
                                                                    (info.blocktype = 'domain'::text and resources.resource_type_id = 1) or
                                                                    (info.blocktype = 'ip'::text and resources.resource_type_id = ANY (ARRAY [2, 3, 4, 5])) or
                                                                    (info.blocktype = 'domain-mask'::text and resources.resource_type_id = 7) or
                                                                    resources.resource_type_id = 6
                                                                    )
        GROUP BY content.id, history.content_version_id, info.irtz_type, info.includetime
    )
    SELECT help.erdi_id                                                        AS id,
           help.includetime,
           help.content_id,
           res.value                                                           AS resource_value,
           COALESCE(help.irtz_type, 'Не заполнено'::text)                      AS resource_type,
           COALESCE(subtype.orig_id, 'Не заполнено'::character varying)        AS info_type_id,
           COALESCE(subtype.registry_name, 'Не заполнено'::character varying)  AS registry_name,
           COALESCE(subtype.category_name, 'Не заполнено'::character varying)  AS category_name,
           COALESCE(subtype.violation_name, 'Не заполнено'::character varying) AS violation_name,
           COALESCE(decision.org, 'Не заполнено'::text)                        AS decision_org,
           COALESCE(addon.visitors_cnt_russia, (0)::bigint)                    AS visitors_cnt_russia,
           COALESCE(addon.visitors_cnt_world, (0)::bigint)                     AS visitors_cnt_world
    FROM (((((help
        LEFT JOIN sor.content_resources res ON ((help.resource_id = res.id)))
        LEFT JOIN sor.resource_type restype ON ((res.resource_type_id = restype.id)))
        LEFT JOIN sor.addon addon ON ((help.addon_id = addon.id)))
        LEFT JOIN sor.subtype subtype ON ((((addon.info_type_id)::text = (subtype.orig_id)::text) AND
                                           (subtype.eff_dt = to_date('30000101'::text, 'YYYYMMDD'::text)))))
             JOIN sor.decision decision ON (((help.content_id = decision.content_id) AND
                                             (help.content_version_id = decision.content_version_id))));

alter materialized view content_view owner to as_user;

