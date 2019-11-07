create or replace function dm.update_reports_data(rep_id bigint) returns void
    language plpgsql
as
$$
DECLARE
    m_msr_prd_id     integer;
    m_msr_prd_eff_dt timestamp;
    m_msr_prd_end_dt timestamp;
    m_msr_prd_tp_id  smallint;
BEGIN

    select msr_prd_id into m_msr_prd_id from dm.reg_reports where reg_reports.rep_id = update_reports_data.rep_id;
    select msr_prd_st_dttm
    into m_msr_prd_eff_dt
    from dm.reg_reports
    where reg_reports.rep_id = update_reports_data.rep_id;
    select msr_prd_end_dttm
    into m_msr_prd_end_dt
    from dm.reg_reports
    where reg_reports.rep_id = update_reports_data.rep_id;
    select msr_prd_tp_id into m_msr_prd_tp_id from dm.reg_reports where reg_reports.rep_id = update_reports_data.rep_id;
    if not exists(select 1 from dm.mis_overall_stat_dm where msr_prd_id = m_msr_prd_id limit 1) then
        insert into dm.mis_overall_stat_dm(msr_prd_id, ind_tp_id, mis_new, mis_all, mis_se, mis_s, mis_e, mis_t)
            (select m_msr_prd_id                                                                                 msr_prd_id,
                    case
                        when at.access_tool_tp_id = 1 then 1
                        when at.access_tool_tp_id = 2 then 2
                        else 3 end                                                                               ind_tp_id,
                    count(*) filter ( where create_msr_prd.msr_prd_id = m_msr_prd_id)                            mis_new,
                    count(*) filter ( where ft.start_date < m_msr_prd_eff_dt and ft.status <> 'FINISHED' or
                                            create_msr_prd.msr_prd_id = m_msr_prd_id )                           mis_all,
                    count(*) filter ( where end_msr_prd.msr_prd_id = m_msr_prd_id and ft.status = 'FINISHED')    mis_se,
                    count(*) filter ( where start_msr_prd.msr_prd_id = m_msr_prd_id and ft.status <> 'FINISHED') mis_s,
                    count(*) filter ( where ft.start_date < m_msr_prd_eff_dt and ft.status = 'FINISHED')         mis_e,
                    count(*) filter ( where ft.start_date < m_msr_prd_eff_dt and ft.status = 'FINISHED')         mis_t

             from portal.formal_tasks ft
                      join portal.arrangements on ft.id = arrangements.formal_task_id
                      join dm.msr_prd create_msr_prd on ft.creation_date >= create_msr_prd.eff_dttm and
                                                        ft.creation_date < create_msr_prd.end_dttm and
                                                        create_msr_prd.msr_prd_tp_id = m_msr_prd_tp_id
                      join dm.msr_prd start_msr_prd on ft.start_date >= start_msr_prd.eff_dttm and
                                                       ft.start_date < start_msr_prd.end_dttm and
                                                       start_msr_prd.msr_prd_tp_id = m_msr_prd_tp_id
                      join dm.msr_prd end_msr_prd
                           on ft.end_date >= end_msr_prd.eff_dttm and ft.end_date < end_msr_prd.end_dttm and
                              end_msr_prd.msr_prd_tp_id = m_msr_prd_tp_id
                      join config.robots on arrangements.access_tool = robots.name
                      join dm.access_tool at on robots.orig_id = at.orig_id and ft.creation_date >= at.eff_dttm and
                                                ft.creation_date < at.end_dttm and
                                                ((robots.type = 'PS' and at.access_tool_tp_id = 1) or
                                                 (robots.type = 'PASD' and at.access_tool_tp_id = 2))
             where create_msr_prd.msr_prd_id = m_msr_prd_id
                or start_msr_prd.msr_prd_id = m_msr_prd_id
                or end_msr_prd.msr_prd_id = m_msr_prd_id
                or (ft.start_date < m_msr_prd_eff_dt and ft.status <> 'FINISHED')
             group by rollup (at.access_tool_tp_id));
    end if;
    if not exists(select 1 from dm.mis_at_stat_dm where msr_prd_id = m_msr_prd_id limit 1) then
        insert into dm.mis_at_stat_dm(msr_prd_id, access_tool_tp_id, access_tool_id, mis_new, mis_all, mis_se, mis_s,
                                      mis_e, mis_t)
            (select m_msr_prd_id                                                                                 msr_prd_id,
                    at.access_tool_tp_id,
                    at.id,
                    count(*) filter ( where create_msr_prd.msr_prd_id = m_msr_prd_id)                            mis_new,
                    count(*) filter ( where ft.start_date < m_msr_prd_eff_dt and ft.status <> 'FINISHED' or
                                            create_msr_prd.msr_prd_id = m_msr_prd_id )                           mis_all,
                    count(*) filter ( where end_msr_prd.msr_prd_id = m_msr_prd_id and ft.status = 'FINISHED')    mis_se,
                    count(*) filter ( where start_msr_prd.msr_prd_id = m_msr_prd_id and ft.status <> 'FINISHED') mis_s,
                    count(*) filter ( where ft.start_date < m_msr_prd_eff_dt and ft.status = 'FINISHED')         mis_e,
                    count(*) filter ( where ft.start_date < m_msr_prd_eff_dt and ft.status = 'FINISHED')         mis_t

             from portal.formal_tasks ft
                      join portal.arrangements on ft.id = arrangements.formal_task_id
                      join dm.msr_prd create_msr_prd on ft.creation_date >= create_msr_prd.eff_dttm and
                                                        ft.creation_date < create_msr_prd.end_dttm and
                                                        create_msr_prd.msr_prd_tp_id = m_msr_prd_tp_id
                      join dm.msr_prd start_msr_prd on ft.start_date >= start_msr_prd.eff_dttm and
                                                       ft.start_date < start_msr_prd.end_dttm and
                                                       start_msr_prd.msr_prd_tp_id = m_msr_prd_tp_id
                      join dm.msr_prd end_msr_prd
                           on ft.end_date >= end_msr_prd.eff_dttm and ft.end_date < end_msr_prd.end_dttm and
                              end_msr_prd.msr_prd_tp_id = m_msr_prd_tp_id
                      join config.robots on arrangements.access_tool = robots.name
                      join dm.access_tool at on robots.orig_id = at.orig_id and ft.creation_date >= at.eff_dttm and
                                                ft.creation_date < at.end_dttm and
                                                ((robots.type = 'PS' and at.access_tool_tp_id = 1) or
                                                 (robots.type = 'PASD' and at.access_tool_tp_id = 2))
             where create_msr_prd.msr_prd_id = m_msr_prd_id
                or start_msr_prd.msr_prd_id = m_msr_prd_id
                or end_msr_prd.msr_prd_id = m_msr_prd_id
                or (ft.start_date < m_msr_prd_eff_dt and ft.status <> 'FINISHED')
             group by at.access_tool_tp_id, at.id);
    end if;
    if not exists(select 1 from dm.ev_overall_stat_dm where msr_prd_id = m_msr_prd_id limit 1) then
        insert into dm.ev_overall_stat_dm(msr_prd_id, ind_tp_id, ev_new, ev_all, ev_se, ev_s, ev_e, ev_t)
        select m_msr_prd_id,
               case when at.access_tool_tp_id = 1 then 1 when at.access_tool_tp_id = 2 then 2 else 3 end         ind_tp_id,
               count(*) filter ( where create_msr_prd.msr_prd_id = m_msr_prd_id)                                 ev_new,
               count(*) filter ( where ar_data.start_date < m_msr_prd_eff_dt and ar_data.status <> 'FINISHED' or
                                       create_msr_prd.msr_prd_id = m_msr_prd_id )                                ev_all,
               count(*) filter ( where end_msr_prd.msr_prd_id = m_msr_prd_id and ar_data.status = 'FINISHED')    ev_se,
               count(*) filter ( where start_msr_prd.msr_prd_id = m_msr_prd_id and ar_data.status <> 'FINISHED') ev_s,
               count(*) filter ( where ar_data.start_date < m_msr_prd_eff_dt and ar_data.status = 'FINISHED')    ev_e,
               count(*) filter ( where ar_data.start_date < m_msr_prd_eff_dt and ar_data.status = 'FINISHED')    ev_t
        from (
                 select arrangements.id,
                        arrangements.status,
                        arrangements.creation_date,
                        arrangements.access_tool,
                        min(ar.start_date) start_date,
                        max(ar.end_date)   end_date
                 from portal.arrangements
                          join results.results ar on arrangements.id = ar.arrangement_id
                 where ar.start_date between m_msr_prd_eff_dt and m_msr_prd_end_dt
                    or ar.end_date between m_msr_prd_eff_dt and m_msr_prd_end_dt
                    or ar.start_date < m_msr_prd_eff_dt and status <> 'FINISHED'
                 group by arrangements.id, arrangements.creation_date, arrangements.status,
                          arrangements.access_tool) ar_data
                 join config.robots on ar_data.access_tool = robots.name
                 join dm.access_tool at on robots.orig_id = at.orig_id and ar_data.creation_date >= at.eff_dttm and
                                           ar_data.creation_date < at.end_dttm and
                                           ((robots.type = 'PS' and at.access_tool_tp_id = 1) or
                                            (robots.type = 'PASD' and at.access_tool_tp_id = 2))
                 join dm.msr_prd create_msr_prd on ar_data.creation_date >= create_msr_prd.eff_dttm and
                                                   ar_data.creation_date < create_msr_prd.end_dttm and
                                                   create_msr_prd.msr_prd_tp_id = m_msr_prd_tp_id
                 join dm.msr_prd start_msr_prd on ar_data.start_date >= start_msr_prd.eff_dttm and
                                                  ar_data.start_date < start_msr_prd.end_dttm and
                                                  start_msr_prd.msr_prd_tp_id = m_msr_prd_tp_id
                 join dm.msr_prd end_msr_prd
                      on ar_data.end_date >= end_msr_prd.eff_dttm and ar_data.end_date < end_msr_prd.end_dttm and
                         end_msr_prd.msr_prd_tp_id = m_msr_prd_tp_id
        group by rollup (at.access_tool_tp_id);
    end if;

    if not exists(select 1 from dm.ev_at_stat_dm where msr_prd_id = m_msr_prd_id limit 1) then
        insert into dm.ev_at_stat_dm(msr_prd_id, access_tool_tp_id, access_tool_id, ev_new, ev_all, ev_se, ev_s, ev_e,
                                     ev_t) (
            select m_msr_prd_id,
                   at.access_tool_tp_id,
                   at.id,
                   count(*) filter ( where create_msr_prd.msr_prd_id = m_msr_prd_id)                              ev_new,
                   count(*) filter ( where ar_data.start_date < m_msr_prd_eff_dt and ar_data.status <> 'FINISHED' or
                                           create_msr_prd.msr_prd_id = m_msr_prd_id )                             ev_all,
                   count(*) filter ( where end_msr_prd.msr_prd_id = m_msr_prd_id and ar_data.status = 'FINISHED') ev_se,
                   count(*)
                   filter ( where start_msr_prd.msr_prd_id = m_msr_prd_id and ar_data.status <> 'FINISHED')       ev_s,
                   count(*) filter ( where ar_data.start_date < m_msr_prd_eff_dt and ar_data.status = 'FINISHED') ev_e,
                   count(*) filter ( where ar_data.start_date < m_msr_prd_eff_dt and ar_data.status = 'FINISHED') ev_t
            from (
                     select arrangements.id,
                            arrangements.status,
                            arrangements.creation_date,
                            arrangements.access_tool,
                            min(ar.start_date) start_date,
                            max(ar.end_date)   end_date
                     from portal.arrangements
                              join results.results ar on arrangements.id = ar.arrangement_id
                     where ar.start_date between m_msr_prd_eff_dt and m_msr_prd_end_dt
                        or ar.end_date between m_msr_prd_eff_dt and m_msr_prd_end_dt
                        or ar.start_date < m_msr_prd_eff_dt and status <> 'FINISHED'
                     group by arrangements.id, arrangements.creation_date, arrangements.status,
                              arrangements.access_tool) ar_data
                     join config.robots on ar_data.access_tool = robots.name
                     join dm.access_tool at on robots.orig_id = at.orig_id and ar_data.creation_date >= at.eff_dttm and
                                               ar_data.creation_date < at.end_dttm and
                                               ((robots.type = 'PS' and at.access_tool_tp_id = 1) or
                                                (robots.type = 'PASD' and at.access_tool_tp_id = 2))
                     join dm.msr_prd create_msr_prd on ar_data.creation_date >= create_msr_prd.eff_dttm and
                                                       ar_data.creation_date < create_msr_prd.end_dttm and
                                                       create_msr_prd.msr_prd_tp_id = m_msr_prd_tp_id
                     join dm.msr_prd start_msr_prd on ar_data.start_date >= start_msr_prd.eff_dttm and
                                                      ar_data.start_date < start_msr_prd.end_dttm and
                                                      start_msr_prd.msr_prd_tp_id = m_msr_prd_tp_id
                     join dm.msr_prd end_msr_prd
                          on ar_data.end_date >= end_msr_prd.eff_dttm and ar_data.end_date < end_msr_prd.end_dttm and
                             end_msr_prd.msr_prd_tp_id = m_msr_prd_tp_id
            group by at.access_tool_tp_id,
                     at.id);
    end if;
    if not exists(select 1 from dm.test_overall_stat_dm where msr_prd_id = m_msr_prd_id limit 1) then
        insert into dm.test_overall_stat_dm(msr_prd_id, ind_tp_id, test_cnt_all, test_cnt_url, test_cnt_d, test_cnt_dm,
                                            test_cnt_ip, test_cnt_sn)
            (select m_msr_prd_id,
                    case when at.access_tool_tp_id = 1 then 1 when at.access_tool_tp_id = 2 then 2 else 3 end     ind_tp_id,
                    count(*)                                                                                      test_cnt_all,
                    count(*) filter ( where ar.check_unit_type = 'URL')                                        as test_cnt_url,
                    count(*) filter ( where ar.check_unit_type = 'DOMAIN')                                     as test_cnt_d,
                    count(*) filter ( where ar.check_unit_type = 'DOMAIN_MASK')                                as test_cnt_dm,
                    count(*)
                    filter ( where ar.check_unit_type = 'IP_V4' or ar.check_unit_type = 'IP_V6')               as test_cnt_ip,
                    count(*) filter ( where ar.check_unit_type = 'IP_V4_SUBNET' or
                                            ar.check_unit_type = 'IP_V6_SUBNET')                               as test_cnt_sn
             from results.results ar
                      join portal.arrangements a on ar.arrangement_id = a.id
                      join dm.msr_prd on ar.start_date >= msr_prd.eff_dttm and
                                         ar.start_date < msr_prd.end_dttm and
                                         msr_prd.msr_prd_tp_id = m_msr_prd_tp_id
                      join config.robots on a.access_tool = robots.name
                      join dm.access_tool at on robots.orig_id = at.orig_id and a.creation_date >= at.eff_dttm and
                                                a.creation_date < at.end_dttm
             where msr_prd_id = m_msr_prd_id
             group by rollup ( at.access_tool_tp_id));
    end if;
    if not exists(select 1 from dm.test_at_stat_dm where msr_prd_id = m_msr_prd_id limit 1) then
        insert into dm.test_at_stat_dm(msr_prd_id, access_tool_tp_id, access_tool_id, test_cnt_all, test_cnt_url,
                                       test_cnt_d, test_cnt_dm, test_cnt_ip, test_cnt_sn)
            (select m_msr_prd_id,
                    at.access_tool_tp_id,
                    at.id,
                    count(*)                                                                                      test_cnt_all,
                    count(*) filter ( where ar.check_unit_type = 'URL')                                        as test_cnt_url,
                    count(*) filter ( where ar.check_unit_type = 'DOMAIN')                                     as test_cnt_d,
                    count(*) filter ( where ar.check_unit_type = 'DOMAIN_MASK')                                as test_cnt_dm,
                    count(*)
                    filter ( where ar.check_unit_type = 'IP_V4' or ar.check_unit_type = 'IP_V6')               as test_cnt_ip,
                    count(*) filter ( where ar.check_unit_type = 'IP_V4_SUBNET' or
                                            ar.check_unit_type = 'IP_V6_SUBNET')                               as test_cnt_sn
             from results.results ar
                      join portal.arrangements a on ar.arrangement_id = a.id
                      join dm.msr_prd on ar.start_date >= msr_prd.eff_dttm and
                                         ar.start_date < msr_prd.end_dttm and
                                         msr_prd.msr_prd_tp_id = m_msr_prd_tp_id
                      join config.robots on a.access_tool = robots.name
                      join dm.access_tool at on robots.orig_id = at.orig_id and a.creation_date >= at.eff_dttm and
                                                a.creation_date < at.end_dttm
             where msr_prd_id = m_msr_prd_id
             group by at.access_tool_tp_id, at.id);
    end if;
END;
$$;
