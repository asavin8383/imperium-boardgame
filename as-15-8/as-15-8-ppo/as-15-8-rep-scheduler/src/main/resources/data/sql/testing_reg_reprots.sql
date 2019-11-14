delete from dm.reg_reports where dm.reg_reports.msr_prd_end_dttm::date <'2019-11-05';

select * from dm.reg_reports where reg_reports.msr_prd_id=310;
update dm.reg_reports set status='NEW' where rep_id in (929,3551,6173);
select * from dm.reg_reports where rep_id in (929,3551,6173);
select * from dm.ev_overall_stat_dm where msr_prd_id=310;