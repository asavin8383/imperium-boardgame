 create or replace function dm.make_at_history() returns trigger
    language plpgsql
as
$$
begin
    update dm.access_tool h set  end_dttm=NEW.eff_dttm
			where h.orig_id=NEW.orig_id and h.end_dttm=to_date('3000-01-01 00:00:00','yyyy-mm-dd HH24:mi:ss');

	IF NEW.end_dttm is null THEN
		NEW.end_dttm := to_date('3000-01-01 00:00:00','yyyy-mm-dd HH24:mi:ss');
	END IF;
	return NEW;
    end;
$$;