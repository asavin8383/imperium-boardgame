create or replace function sor.set_irtz_type() returns void
    language plpgsql
as
$$
BEGIN
with d as  (
select
       cr.content_id,
       cr.content_version_id,
       count(*) filter ( where resource_type_id=6) cnt_url,
       count(*) filter ( where resource_type_id=1 ) cnt_domain,
       count(*) filter ( where resource_type_id in (2,3)  ) cnt_ip,
       count(*) filter ( where resource_type_id in (4,5) ) cnt_subnet,
       count(*) filter ( where resource_type_id=7) cnt_domain_mask
        from
             sor.content_resources cr
                join sor.content_info ci on ci.content_id=cr.content_id and ci.content_version_id=cr.content_version_id
        where ci.irtz_type is null
group by cr.content_id, cr.content_version_id)
update sor.content_info cii set irtz_type=
      case when (cii.blocktype is null or cii.blocktype='default' or cii.blocktype='DEFAULT') then
                                        case when cnt_url<>0 then 'url'
                                             when cnt_domain_mask<>0 then 'domain-mask'
                                             when cnt_domain<>0 then 'domain'
                                             when cnt_ip<>0 then 'ip'
                                             when cnt_subnet<>0 then 'subnet'
                                             else 'unknown' end
            when cii.blocktype='ip' then
                    case when cnt_ip<>0 then 'ip'
                                             when cnt_subnet<>0 then 'subnet'
                                             else 'unknown' end
            else cii.blocktype end
            from d where cii.content_id=d.content_id and
                         cii.content_version_id=d.content_version_id
                        and irtz_type is null;
END
$$;

