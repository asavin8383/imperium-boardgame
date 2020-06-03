create or replace function sor.remove_history_to_date(
		toDate date, removeLastVersion boolean DEFAULT true) returns void
    language plpgsql
as
$$
BEGIN
	---------- REMOVE RESOURCES
	with h_exclude as (
		select content_id || '-' || max(content_version_id) || '-' || max(addon_version_id) as uniq_id
		from sor.content_history
		where NOT removeLastVersion
		GROUP BY content_id
	),
	hist as (
		select content_id || '-' || content_version_id || '-' || addon_version_id as uniq_id,
			id, content_id, content_version_id, addon_version_id, end_dt
		from sor.content_history
		where end_dt <=toDate
	),
	h as (
		select hist.content_id, hist.content_version_id
		from hist left join h_exclude on hist.uniq_id = h_exclude.uniq_id
		where h_exclude.uniq_id is null
		group by hist.content_id, hist.content_version_id
	)
	DELETE FROM sor.content_resources
	WHERE (content_id, content_version_id) in (select content_id, content_version_id from h);

	----------- REMOVE HISTORY
	with h_exclude as (
		select content_id || '-' || max(content_version_id) || '-' || max(addon_version_id) as uniq_id
		from sor.content_history
		where NOT removeLastVersion
		GROUP BY content_id
	),
	hist as (
		select content_id || '-' || content_version_id || '-' || addon_version_id as uniq_id,
			id, content_id, content_version_id, addon_version_id, end_dt
		from sor.content_history
		where end_dt <= toDate
	),
	h as (
		select hist.id
		from hist left join h_exclude on hist.uniq_id = h_exclude.uniq_id
		where h_exclude.uniq_id is null
	)
	DELETE FROM sor.content_history
	WHERE id in (select id from h);
END
$$;

-- --use
-- select sor.remove_history_to_date(to_date('01.01.2020 00:00:00', 'DD.MM.YYYY HH24:MI:SS'));
-- select sor.set_irtz_type();
-- select sor.update_check_units();
-- select sor.update_content_view();
