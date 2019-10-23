CREATE OR REPLACE VIEW sa.ERDI as
WITH content_info AS (
  SELECT
    content.id id,
    content.erdi_id erdi_id,
    content.includetime include_time,
    coalesce(upper(content.blocktype), 'URL') blocktype,
    urgency.dsc urgency,
    decision.org org,
    decision.number decision_number,
    decision.date decision_date
  FROM sa.content content
    JOIN sa.decision decision ON content.id = decision.content_id
    JOIN sa.urgencytype urgency ON coalesce(content.urgencytype, '0') = urgency.id
) SELECT id, erdi_id, include_time, urgency, blocktype, url.url check_unit, org, decision_number, decision_date
  from content_info
    JOIN sa.url url ON content_info.id = url.content_id AND content_info.blocktype = 'URL'
  UNION
  SELECT id, erdi_id, include_time, urgency, blocktype, domain.domain check_unit, org, decision_number, decision_date
  from content_info
    JOIN sa.domain domain ON content_info.id = domain.content_id AND content_info.blocktype like 'DOMAIN%'
  UNION
  SELECT id, erdi_id, include_time, urgency, blocktype, ip.ip check_unit, org, decision_number, decision_date
  from content_info
    JOIN sa.ip ip ON content_info.id = ip.content_id AND content_info.blocktype = 'IP';