-- Базовый запрос

select
       at.orig_id as ss,
       r.end_date,
       at.access_tool_tp_id,
       a.id as ev,
       at.nm as access_tool_name,
       r.check_unit_value,
       r.result status,
       coalesce(ci.blocktype,'default') as irtz_blocktype,
       ci.entrytype_id irtz_regtype,
       addon.visitors_cnt_russia IRTZ_TRF_RU,
       addon.visitors_cnt_world IRTZ_TRF_W,
       addon.orig_id as IRTZ_IRZTYPE,
       ci.includetime as IRTZ_REG_TS,
       msr_prd.eff_dttm::date as RPFD
       from
              results.results r
                  join portal.arrangements a on r.arrangement_id=a.id
                  join sor.content c  on r.content_id=c.id
                  join sor.content_info ci on c.id = ci.content_id
                   join config.robots on robots.name=a.access_tool
                  join dm.access_tool at on robots.orig_id=at.orig_id and a.creation_date>=at.eff_dttm and a.creation_date<at.end_dttm
and ((robots.type='PS' and at.access_tool_tp_id=1) or (robots.type='PASD' and at.access_tool_tp_id=2))
left join sor.addon on addon.content_id=c.id
join dm.msr_prd on msr_prd.msr_prd_tp_id=1 and r.end_date between msr_prd.eff_dttm and msr_prd.end_dttm
where
msr_prd.eff_dttm::date='2019-11-07'
and access_tool_tp_id=1;
-- Раздел 2 Общие показатели исполнения ОПС обязанности по исключению из поисковой выдачи сведений об ИРТЗ за отчётный период:
select
       row_number() over (order by at.nm) rn,
       at.nm as access_tool_name,
       count(*) IRTZ_CHCK_SS_ALL,
       count(distinct r.check_unit_value) IRTZ_SS_ALL,
       count(distinct r.check_unit_value) filter ( where r.result in ('FORBIDDEN_CONTENT_DETECTED','DOUBTFUL') ) IRTZ_SS_OFF_ALL
       from
              results.results r
                  join portal.arrangements a on r.arrangement_id=a.id
                  join sor.content c  on r.content_id=c.id
                  join sor.content_info ci on c.id = ci.content_id
                   join config.robots on robots.name=a.access_tool
                  join dm.access_tool at on robots.orig_id=at.orig_id and a.creation_date>=at.eff_dttm and a.creation_date<at.end_dttm
and ((robots.type='PS' and at.access_tool_tp_id=1))
left join sor.addon on addon.content_id=c.id
join dm.msr_prd on msr_prd.msr_prd_tp_id=1 and r.end_date between msr_prd.eff_dttm and msr_prd.end_dttm
where
msr_prd.eff_dttm::date='2019-11-07'
and access_tool_tp_id=1
group by  at.nm order by at.nm
;
-- Раздел 3
select
       dense_rank() over(order by at.nm) as group_nm,
       msr_prd.caption,
       at.nm as access_tool_name,
       row_number() over (PARTITION BY at.nm order by coalesce(ci.blocktype,'default') nulls first ) rn,
       case when  coalesce(ci.blocktype,'default')='default' then       '     URL'
            when  coalesce(ci.blocktype,'default')='domain' then        '     Доменных имен'
            when  coalesce(ci.blocktype,'default')='domain-mask' then   '     Доменных имен по маске'
           when coalesce(ci.blocktype,'default') is null then 'Общее количесто ИРТЗ'
           else 'Прочее' end
           as blocktype,
       count(distinct r.check_unit_value) IRTZ_SS_ALL,
       count(distinct r.check_unit_value) filter ( where r.result in ('FORBIDDEN_CONTENT_DETECTED','DOUBTFUL') ) IRTZ_SS_OFF_ALL
       from
              results.results r
                  join portal.arrangements a on r.arrangement_id=a.id
                  join sor.content c  on r.content_id=c.id
                  join sor.content_info ci on c.id = ci.content_id
                   join config.robots on robots.name=a.access_tool
                  join dm.access_tool at on robots.orig_id=at.orig_id and a.creation_date>=at.eff_dttm and a.creation_date<at.end_dttm
and ((robots.type='PS' and at.access_tool_tp_id=1))
left join sor.addon on addon.content_id=c.id
join dm.msr_prd on msr_prd.msr_prd_tp_id=1 and r.end_date between msr_prd.eff_dttm and msr_prd.end_dttm
where
msr_prd.eff_dttm::date='2019-11-07'
and access_tool_tp_id=1
group by grouping sets ((  msr_prd.caption,at.nm),(  msr_prd.caption,at.nm,coalesce(ci.blocktype,'default')) ) order by at.nm,coalesce(ci.blocktype,'default') nulls first
;
select * from sor.content;
select * from sor.blocktype order by blocktype;
select * from results.results;

select
       row_number() over (order by at.nm) rn,
       at.nm as access_tool_name,
       count(*) IRTZ_CHCK_SS_ALL,
       count(distinct r.check_unit_value) IRTZ_SS_ALL,
       count(distinct r.check_unit_value) filter ( where r.result in ('FORBIDDEN_CONTENT_DETECTED','DOUBTFUL') ) IRTZ_SS_OFF_ALL
       from
              results.results r
                  join portal.arrangements a on r.arrangement_id=a.id
                  join sor.content c  on r.content_id=c.id
                  join sor.content_info ci on c.id = ci.content_id
                   join config.robots on robots.name=a.access_tool
                  join dm.access_tool at on robots.orig_id=at.orig_id and a.creation_date>=at.eff_dttm and a.creation_date<at.end_dttm
and ((robots.type='PS' and at.access_tool_tp_id=1))
left join sor.addon on addon.content_id=c.id
join dm.msr_prd on msr_prd.msr_prd_tp_id=1 and r.end_date between msr_prd.eff_dttm and msr_prd.end_dttm
where
msr_prd.eff_dttm::date='2019-11-07'
and access_tool_tp_id=1
group by at.nm;

select * from dm.msr_prd where msr_prd_tp_id=2