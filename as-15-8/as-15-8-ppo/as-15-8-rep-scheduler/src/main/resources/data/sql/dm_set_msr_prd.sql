create or replace function dm.set_msr_prd() returns trigger
     language plpgsql
    AS
$$
BEGIN
   UPDATE DM.param_reports set
    (msr_prd_id,msr_prd_tp,msr_prd_end_dttm,msr_prd_caption)=
       (
           select
                  msr_prd.msr_prd_id,
                  msr_prd_tp.nm,
                  msr_prd.end_dttm,
                  msr_prd.caption
           from
                dm.msr_prd join dm.msr_prd_tp
                    on msr_prd.msr_prd_tp_id = msr_prd_tp.msr_prd_tp_id
           where msr_prd.msr_prd_tp_id=NEW.msr_prd_tp_id and msr_prd.eff_dttm=NEW.msr_prd_st_dttm)
        where rep_id=NEW.rep_id;
   RETURN NEW;
END;
$$;
