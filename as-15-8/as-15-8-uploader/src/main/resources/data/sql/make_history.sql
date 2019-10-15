
CREATE OR REPLACE FUNCTION sor.make_history()
    RETURNS trigger
    LANGUAGE 'plpgsql'
    COST 1
    VOLATILE NOT LEAKPROOF
AS $$
  begin
    update sor.content_history h set  end_dt=NEW.st_dt
			where h.content_id=NEW.content_id and h.end_dt=to_date('3000-01-01 00:00:00','yyyy-mm-dd HH24:mi:ss');

	IF NEW.end_dt is null THEN
		NEW.end_dt := to_date('3000-01-01 00:00:00','yyyy-mm-dd HH24:mi:ss');
	END IF;

	IF NEW.content_version_id is null THEN
		NEW.content_version_id := (select coalesce(max(h.content_version_id), (select max(id) from sor.content_version)) from sor.content_history h where h.content_id=NEW.content_id);

	END IF;
	IF NEW.addon_version_id is null THEN
		NEW.addon_version_id := (select coalesce(max(h.addon_version_id), (select max(id) from sor.addon_version)) from sor.content_history h where h.content_id=NEW.content_id);
	END IF;

	return NEW;
    end;
$$;

