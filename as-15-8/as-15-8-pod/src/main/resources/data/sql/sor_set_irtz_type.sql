create or replace function sor.set_irtz_type() returns void
    language plpgsql
as
$$
BEGIN
update sor.content_info cii set irtz_type=(
    select  case when (blocktype is null or blocktype='default' or blocktype='DEFAULT') then
                                        case when cnt_url<>0 then 'url'
                                             when cnt_domain_mask<>0 then 'domain-mask'
                                             when cnt_domain<>0 then 'domain'
                                             when cnt_ip<>0 then 'ip'
                                             when cnt_subnet<>0 then 'subnet'
                                             else 'unknown' end
            when a.blocktype='ip' then
                    case when cnt_ip<>0 then 'ip'
                                             when cnt_subnet<>0 then 'subnet'
                                             else 'unknown' end
            else blocktype end from
                    (select cii.blocktype from sor.content_info ci where ci.content_id=cii.content_id and ci.content_version_id=cii.content_version_id) a join
                                    (
                    select
       count(*) filter ( where resource_type_id=6 ) cnt_url,
       count(*) filter ( where resource_type_id=1 ) cnt_domain,
       count(*) filter ( where resource_type_id in (2,3)  ) cnt_ip,
       count(*) filter ( where resource_type_id in (4,5) ) cnt_subnet,
       count(*) filter ( where resource_type_id=1 and value like '%*%' ) cnt_domain_mask
from   sor.content_resources cr where cr.content_id=cii.content_id and cr.content_version_id=cii.content_version_id
                                            ) d on 1=1)
where irtz_type is null;
END
$$;