select dense_rank() over (order by at.nm)                                             as            group_nm,
       row_number() over (order by at.nm)                                           rn,
       at.nm as                                                                     access_tool_name,
       r.check_unit_value,
       r.end_date,
       coalesce(blocktype,'default') blocktype,
       case when r.result='FORBIDDEN_CONTENT_DETECTED' then 'Обнаружен запрещенный контент'
            when r.result='DOUBTFUL' then 'Сомнительный контент'
            when r.result='COMPLETED' then 'Нет нарушения'
            when r.result ilike '%ERROR%' then 'Ошибка'
            else 'Прочее' end result,
            r.result result_code,
            msr_prd.caption
from results.results r
         join portal.arrangements a on r.arrangement_id = a.id
             join sor.content_history ch on r.content_id=ch.content_id and r.start_date >=ch.st_dt and r.start_date<=ch.end_dt
         join sor.content_info ci on ch.content_id = ci.content_id and ch.content_version_id=ci.content_version_id
         join config.robots on robots.name = a.access_tool
         join dm.access_tool at
              on robots.orig_id = at.orig_id and a.creation_date >= at.eff_dttm and a.creation_date < at.end_dttm
                  and ((robots.type = 'PS' and at.access_tool_tp_id = 1))
         left join sor.addon on addon.content_id = ch.content_id and addon.addon_version_id=ch.addon_version_id
         join dm.msr_prd on msr_prd.msr_prd_tp_id = 1 and r.end_date between msr_prd.eff_dttm and msr_prd.end_dttm
where msr_prd.eff_dttm::date = '2019-11-07'
  and access_tool_tp_id = 1
