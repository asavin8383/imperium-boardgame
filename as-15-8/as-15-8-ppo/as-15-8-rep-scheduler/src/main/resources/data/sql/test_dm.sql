
insert into dm.access_tool(nm, access_tool_tp_id,hostname) (
    select distinct name,1,ps.hostname from sor.ps
);
insert into dm.access_tool(nm, access_tool_tp_id,hostname) (
    select distinct name,2,hostname from sor.pasd
);


select * from dm.mis_at_stat_dm;
select * from dm.msr_prd_tp;


insert into dm.mis_at_stat_dm(msr_prd_id, access_tool_tp_id, access_tool_id, mis_new, mis_all, mis_se, mis_s, mis_e, mis_t) (
    select
    msr_prd.msr_prd_id,
        access_tool.access_tool_tp_id,
           access_tool.id,
           floor(random() * (10-1+1) + 1)::int,
           floor(random() * (10-1+1) + 1)::int,
           floor(random() * (10-1+1) + 1)::int,
           floor(random() * (10-1+1) + 1)::int,
           floor(random() * (10-1+1) + 1)::int,
           floor(random() * (10-1+1) + 1)::int
    from dm.msr_prd join dm.access_tool on msr_prd.msr_prd_tp_id=1
);

insert into dm.mis_at_stat_dm(msr_prd_id, access_tool_tp_id, access_tool_id, mis_new, mis_all, mis_se, mis_s, mis_e, mis_t) (
    select
    week_id, access_tool_tp_id, access_tool_id, sum(mis_new), sum(mis_all), sum(mis_se), sum(mis_s), sum(mis_e), sum(mis_t)
    from dm.mis_at_stat_dm join dm.msr_prd_hier on msr_prd_id=day_id
    group by week_id, access_tool_tp_id, access_tool_id
    );
insert into dm.mis_at_stat_dm(msr_prd_id, access_tool_tp_id, access_tool_id, mis_new, mis_all, mis_se, mis_s, mis_e, mis_t) (
    select
    month_id, access_tool_tp_id, access_tool_id, sum(mis_new), sum(mis_all), sum(mis_se), sum(mis_s), sum(mis_e), sum(mis_t)
    from dm.mis_at_stat_dm join dm.msr_prd_hier on msr_prd_id=day_id
    group by month_id, access_tool_tp_id, access_tool_id
    );
insert into dm.mis_at_stat_dm(msr_prd_id, access_tool_tp_id, access_tool_id, mis_new, mis_all, mis_se, mis_s, mis_e, mis_t) (
    select
    quarter_id, access_tool_tp_id, access_tool_id, sum(mis_new), sum(mis_all), sum(mis_se), sum(mis_s), sum(mis_e), sum(mis_t)
    from dm.mis_at_stat_dm join dm.msr_prd_hier on msr_prd_id=day_id
    group by quarter_id, access_tool_tp_id, access_tool_id
    );
insert into dm.mis_at_stat_dm(msr_prd_id, access_tool_tp_id, access_tool_id, mis_new, mis_all, mis_se, mis_s, mis_e, mis_t) (
    select
    half_id, access_tool_tp_id, access_tool_id, sum(mis_new), sum(mis_all), sum(mis_se), sum(mis_s), sum(mis_e), sum(mis_t)
    from dm.mis_at_stat_dm join dm.msr_prd_hier on msr_prd_id=day_id
    group by half_id, access_tool_tp_id, access_tool_id
    );
insert into dm.mis_at_stat_dm(msr_prd_id, access_tool_tp_id, access_tool_id, mis_new, mis_all, mis_se, mis_s, mis_e, mis_t) (
    select
    year_id, access_tool_tp_id, access_tool_id, sum(mis_new), sum(mis_all), sum(mis_se), sum(mis_s), sum(mis_e), sum(mis_t)
    from dm.mis_at_stat_dm join dm.msr_prd_hier on msr_prd_id=day_id
    group by year_id, access_tool_tp_id, access_tool_id
    );
delete from dm.mis_overall_stat_dm;
insert into dm.mis_overall_stat_dm(msr_prd_id, ind_tp_id, mis_new, mis_all, mis_se, mis_s, mis_e, mis_t) (
    select
    msr_prd_id, 1, sum(mis_new), sum(mis_all), sum(mis_se), sum(mis_s), sum(mis_e), sum(mis_t)
    from dm.mis_at_stat_dm
    group by  msr_prd_id
);
insert into dm.mis_overall_stat_dm(msr_prd_id, ind_tp_id, mis_new, mis_all, mis_se, mis_s, mis_e, mis_t) (
    select
    msr_prd_id, 2, sum(mis_new)+34, sum(mis_all)-34, sum(mis_se)-4, sum(mis_s)+7, sum(mis_e)-6, sum(mis_t)+9
    from dm.mis_at_stat_dm
    group by  msr_prd_id
);

insert into dm.mis_overall_stat_dm(msr_prd_id, ind_tp_id, mis_new, mis_all, mis_se, mis_s, mis_e, mis_t) (
    select
    msr_prd_id, 3, sum(mis_new)+4, sum(mis_all)-3, sum(mis_se)+12, sum(mis_s)+17, sum(mis_e)+123, sum(mis_t)+91
    from dm.mis_at_stat_dm
    group by  msr_prd_id
);



select * from dm.mis_overall_stat_dm;









insert into dm.ev_at_stat_dm(msr_prd_id, access_tool_tp_id, access_tool_id, ev_new, ev_all, ev_se, ev_s, ev_e, ev_t)  (
    select
        msr_prd.msr_prd_id,
        access_tool.access_tool_tp_id,
        access_tool.id,
        floor(random() * (1000-100+1) + 100)::int,
        floor(random() * (1000-100+1) + 100)::int,
        floor(random() * (1000-100+1) + 100)::int,
        floor(random() * (1000-100+1) + 100)::int,
        floor(random() * (1000-100+1) + 100)::int,
        floor(random() * (1000-100+1) + 100)::int
    from dm.msr_prd join dm.access_tool on msr_prd.msr_prd_tp_id=1
);


insert into dm.ev_at_stat_dm(msr_prd_id, access_tool_tp_id, access_tool_id, ev_new, ev_all, ev_se, ev_s, ev_e, ev_t) (
    select
    week_id, access_tool_tp_id, access_tool_id, sum(ev_new), sum(ev_all), sum(ev_se), sum(ev_s), sum(ev_e), sum(ev_t)
    from dm.ev_at_stat_dm join dm.msr_prd_hier on msr_prd_id=day_id
    group by week_id, access_tool_tp_id, access_tool_id
    );
insert into dm.ev_at_stat_dm(msr_prd_id, access_tool_tp_id, access_tool_id, ev_new, ev_all, ev_se, ev_s, ev_e, ev_t) (
    select
    month_id, access_tool_tp_id, access_tool_id, sum(ev_new), sum(ev_all), sum(ev_se), sum(ev_s), sum(ev_e), sum(ev_t)
    from dm.ev_at_stat_dm join dm.msr_prd_hier on msr_prd_id=day_id
    group by month_id, access_tool_tp_id, access_tool_id
    );
insert into dm.ev_at_stat_dm(msr_prd_id, access_tool_tp_id, access_tool_id, ev_new, ev_all, ev_se, ev_s, ev_e, ev_t) (
    select
    quarter_id, access_tool_tp_id, access_tool_id, sum(ev_new), sum(ev_all), sum(ev_se), sum(ev_s), sum(ev_e), sum(ev_t)
    from dm.ev_at_stat_dm join dm.msr_prd_hier on msr_prd_id=day_id
    group by quarter_id, access_tool_tp_id, access_tool_id
    );
insert into dm.ev_at_stat_dm(msr_prd_id, access_tool_tp_id, access_tool_id, ev_new, ev_all, ev_se, ev_s, ev_e, ev_t) (
    select
    half_id, access_tool_tp_id, access_tool_id, sum(ev_new), sum(ev_all), sum(ev_se), sum(ev_s), sum(ev_e), sum(ev_t)
    from dm.ev_at_stat_dm join dm.msr_prd_hier on msr_prd_id=day_id
    group by half_id, access_tool_tp_id, access_tool_id
    );
insert into dm.ev_at_stat_dm(msr_prd_id, access_tool_tp_id, access_tool_id, ev_new, ev_all, ev_se, ev_s, ev_e, ev_t) (
    select
    year_id, access_tool_tp_id, access_tool_id, sum(ev_new), sum(ev_all), sum(ev_se), sum(ev_s), sum(ev_e), sum(ev_t)
    from dm.ev_at_stat_dm join dm.msr_prd_hier on msr_prd_id=day_id
    group by year_id, access_tool_tp_id, access_tool_id
    );
delete from dm.mis_overall_stat_dm;
insert into dm.ev_overall_stat_dm(msr_prd_id, ind_tp_id, ev_new, ev_all, ev_se, ev_s, ev_e, ev_t) (
    select
    msr_prd_id, 1, sum(ev_new), sum(ev_all), sum(ev_se), sum(ev_s), sum(ev_e), sum(ev_t)
    from dm.ev_at_stat_dm
    group by  msr_prd_id
);
insert into dm.ev_overall_stat_dm(msr_prd_id, ind_tp_id, ev_new, ev_all, ev_se, ev_s, ev_e, ev_t) (
    select
    msr_prd_id, 2, sum(ev_new)+34, sum(ev_all)-34, sum(ev_se)-4, sum(ev_s)+7, sum(ev_e)-6, sum(ev_t)+9
    from dm.ev_at_stat_dm
    group by  msr_prd_id
);

insert into dm.ev_overall_stat_dm(msr_prd_id, ind_tp_id, ev_new, ev_all, ev_se, ev_s, ev_e, ev_t) (
    select
    msr_prd_id, 3, sum(ev_new)+4, sum(ev_all)-3, sum(ev_se)+12, sum(ev_s)+17, sum(ev_e)+123, sum(ev_t)+91
    from dm.ev_at_stat_dm
    group by  msr_prd_id
);







select  * from dm.mis_overall_stat_dm;
select  * from dm.ev_overall_stat_dm;
select  * from dm.ev_at_stat_dm;




insert into dm.test_at_stat_dm (msr_prd_id, access_tool_tp_id, access_tool_id, test_cnt_all, test_cnt_url, test_cnt_d, test_cnt_dm, test_cnt_ip, test_cnt_sn)   (
    select
        msr_prd.msr_prd_id,
        access_tool.access_tool_tp_id,
        access_tool.id,
        floor(random() * (100000-10000+1) + 10000)::int,
        floor(random() * (100000-10000+1) + 10000)::int,
        floor(random() * (100000-10000+1) + 10000)::int,
        floor(random() * (100000-10000+1) + 10000)::int,
        floor(random() * (100000-10000+1) + 10000)::int,
        floor(random() * (100000-10000+1) + 10000)::int
    from dm.msr_prd join dm.access_tool on msr_prd.msr_prd_tp_id=1
);
select * from dm.test_at_stat_dm;



insert into dm.test_at_stat_dm(msr_prd_id, access_tool_tp_id, access_tool_id, test_cnt_all, test_cnt_url, test_cnt_d, test_cnt_dm, test_cnt_ip, test_cnt_sn) (
    select
    week_id, access_tool_tp_id, access_tool_id,  sum(test_cnt_all), sum(test_cnt_url), sum(test_cnt_d), sum(test_cnt_dm), sum(test_cnt_ip), sum(test_cnt_sn)
    from dm.test_at_stat_dm join dm.msr_prd_hier on msr_prd_id=day_id
    group by week_id, access_tool_tp_id, access_tool_id
    );
insert into dm.test_at_stat_dm(msr_prd_id, access_tool_tp_id, access_tool_id, test_cnt_all, test_cnt_url, test_cnt_d, test_cnt_dm, test_cnt_ip, test_cnt_sn) (
    select
    month_id, access_tool_tp_id, access_tool_id,  sum(test_cnt_all), sum(test_cnt_url), sum(test_cnt_d), sum(test_cnt_dm), sum(test_cnt_ip), sum(test_cnt_sn)
    from dm.test_at_stat_dm join dm.msr_prd_hier on msr_prd_id=day_id
    group by month_id, access_tool_tp_id, access_tool_id
    );
insert into dm.test_at_stat_dm(msr_prd_id, access_tool_tp_id, access_tool_id, test_cnt_all, test_cnt_url, test_cnt_d, test_cnt_dm, test_cnt_ip, test_cnt_sn) (
    select
    quarter_id, access_tool_tp_id, access_tool_id,  sum(test_cnt_all), sum(test_cnt_url), sum(test_cnt_d), sum(test_cnt_dm), sum(test_cnt_ip), sum(test_cnt_sn)
    from dm.test_at_stat_dm join dm.msr_prd_hier on msr_prd_id=day_id
    group by quarter_id, access_tool_tp_id, access_tool_id
    );
insert into dm.test_at_stat_dm(msr_prd_id, access_tool_tp_id, access_tool_id, test_cnt_all, test_cnt_url, test_cnt_d, test_cnt_dm, test_cnt_ip, test_cnt_sn) (
    select
    half_id, access_tool_tp_id, access_tool_id,  sum(test_cnt_all), sum(test_cnt_url), sum(test_cnt_d), sum(test_cnt_dm), sum(test_cnt_ip), sum(test_cnt_sn)
    from dm.test_at_stat_dm join dm.msr_prd_hier on msr_prd_id=day_id
    group by half_id, access_tool_tp_id, access_tool_id
    );
insert into dm.test_at_stat_dm(msr_prd_id, access_tool_tp_id, access_tool_id, test_cnt_all, test_cnt_url, test_cnt_d, test_cnt_dm, test_cnt_ip, test_cnt_sn) (
    select
    year_id, access_tool_tp_id, access_tool_id,  sum(test_cnt_all), sum(test_cnt_url), sum(test_cnt_d), sum(test_cnt_dm), sum(test_cnt_ip), sum(test_cnt_sn)
    from dm.test_at_stat_dm join dm.msr_prd_hier on msr_prd_id=day_id
    group by year_id, access_tool_tp_id, access_tool_id
    );
delete from dm.mis_overall_stat_dm;
insert into dm.test_overall_stat_dm(msr_prd_id, ind_tp_id, test_cnt_all, test_cnt_url, test_cnt_d, test_cnt_dm, test_cnt_ip, test_cnt_sn) (
    select
    msr_prd_id, 1, sum(test_cnt_all), sum(test_cnt_url), sum(test_cnt_d), sum(test_cnt_dm), sum(test_cnt_ip), sum(test_cnt_sn)
    from dm.test_at_stat_dm
    group by  msr_prd_id
);
insert into dm.test_overall_stat_dm(msr_prd_id, ind_tp_id, test_cnt_all, test_cnt_url, test_cnt_d, test_cnt_dm, test_cnt_ip, test_cnt_sn) (
    select
    msr_prd_id, 2, sum(test_cnt_all)+34, sum(test_cnt_url)-34, sum(test_cnt_d)-4, sum(test_cnt_dm)+7, sum(test_cnt_ip)-6, sum(test_cnt_sn)+9
    from dm.test_at_stat_dm
    group by  msr_prd_id
);

insert into dm.test_overall_stat_dm(msr_prd_id, ind_tp_id, test_cnt_all, test_cnt_url, test_cnt_d, test_cnt_dm, test_cnt_ip, test_cnt_sn) (
    select
    msr_prd_id, 3, sum(test_cnt_all)+4, sum(test_cnt_url)-3, sum(test_cnt_d)+12, sum(test_cnt_dm)+17, sum(test_cnt_ip)+123, sum(test_cnt_sn)+91
    from dm.test_at_stat_dm
    group by  msr_prd_id
);

select * from dm.test_overall_stat_dm;



