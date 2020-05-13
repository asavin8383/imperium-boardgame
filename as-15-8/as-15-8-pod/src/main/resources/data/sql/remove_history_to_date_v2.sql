create or replace function sor.remove_history_to_date(to_date date) returns void
    language plpgsql
as
$$
BEGIN
	--- remove resources
	with h_exclude as (
		select distinct content_id, content_version_id
		from sor.content_history
		where end_dt = to_date('3000-01-01 00:00:00','yyyy-mm-dd HH24:mi:ss')
	),
	hist as (
		select distinct h.content_id, h.content_version_id
		from sor.content_history h left join h_exclude he on
			h.content_id = he.content_id and
			h.content_version_id = he.content_version_id
		where he.content_id is null and h.end_dt <= to_date
	)
	DELETE FROM sor.content_resources
	WHERE (content_id, content_version_id) in (select content_id, content_version_id from hist);

	--- remove history
	DELETE FROM sor.content_history h
	WHERE h.end_dt <= to_date and
		h.end_dt < to_date('3000-01-01 00:00:00','yyyy-mm-dd HH24:mi:ss');
END
$$;


-- --use
--
-- select sor.remove_history_to_date(to_date('01.01.2020 00:00:00', 'DD.MM.YYYY HH24:MI:SS'));
-- select sor.set_irtz_type();
-- select sor.update_check_units();
-- select sor.update_content_view();
-- commit;
