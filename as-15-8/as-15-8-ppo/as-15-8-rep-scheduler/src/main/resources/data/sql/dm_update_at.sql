 create or replace function dm.update_at() returns void
    language plpgsql
as
$$
begin


with d as (
select
       orig_id,
       c_date,
       name,
       hostname,
       ps_id,
       row_number() over (partition by orig_id order by c_date) rn
from sor.ps
order by orig_id),
dd as (select
d.orig_id,
d.c_date,
d.name,
d.hostname,
d.ps_id
from d left join d d_prev on d.orig_id=d_prev.orig_id and d.rn-1=d_prev.rn
where d.name<>d_prev.name or d_prev.name is null)
insert into dm.access_tool(nm, access_tool_tp_id, portal_id, hostname, orig_id,eff_dttm)
(select dd.name, 1, ps_id,hostname, orig_id,c_date from dd where
                    ( dd.orig_id,  dd.c_date) not  in
                    (select coalesce(orig_id,0),eff_dttm from  dm.access_tool where access_tool_tp_id=1));



with d as (
select
       orig_id,
       c_date,
       name,
       hostname,
      pasd_id,
       row_number() over (partition by orig_id order by c_date) rn
from sor.pasd
order by orig_id),
dd as (select
d.orig_id,
d.c_date,
d.name,
d.hostname,d.pasd_id
from d left join d d_prev on d.orig_id=d_prev.orig_id and d.rn-1=d_prev.rn
where d.name<>d_prev.name or d_prev.name is null)
insert into dm.access_tool(nm, access_tool_tp_id, portal_id, hostname, orig_id,eff_dttm)
(select dd.name, 2, pasd_id,hostname, orig_id,c_date from dd where
                    ( dd.orig_id,  dd.c_date) not  in
                    (select coalesce(orig_id,0),eff_dttm from  dm.access_tool where access_tool_tp_id=2));
    end;
$$;