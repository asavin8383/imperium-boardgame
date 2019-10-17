
CREATE OR REPLACE FUNCTION sor.remove_last_content_version()
RETURNS void
LANGUAGE plpgsql
AS $$
DECLARE
	LAST_VERSION INTEGER := 0;
BEGIN
	LAST_VERSION := (select max(id) from sor.content_version);
	RAISE NOTICE 'LAST_VERSION = %', LAST_VERSION;
	
	if LAST_VERSION is NULL
	THEN RETURN;
	END IF;

  update sor.content_history HIST
	set end_dt = to_date('30000101', 'YYYYMMDD')
	from
	(
		select CH.id, CH.end_dt as end_dt_prev, CH_LAST.st_dt as st_dt_last from
		sor.content_history CH
		join
		(select distinct content_id, st_dt from sor.content_history
		where content_version_id = LAST_VERSION) CH_LAST
		on CH.content_id = CH_LAST.content_id
		join
		(select content_id, max(id) as id_prev from sor.content_history
		where content_version_id < LAST_VERSION GROUP BY content_id) CH_PREV
		on CH.content_id = CH_PREV.content_id
		where CH.id = CH_PREV.id_prev and CH.end_dt = CH_LAST.st_dt
	) HIST_UPD
	where HIST.id = HIST_UPD.id;

	delete FROM sor.content_history where content_version_id = LAST_VERSION;
	delete FROM sor.content_info where content_version_id = LAST_VERSION;
	delete FROM sor.content_resources where content_version_id = LAST_VERSION;
	delete FROM sor.decision where content_version_id = LAST_VERSION;
	delete FROM sor.content where init_content_version_id = LAST_VERSION;
	delete FROM sor.content_version where id = LAST_VERSION;
END $$;
