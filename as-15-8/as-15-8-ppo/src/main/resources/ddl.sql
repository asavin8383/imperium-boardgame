-- хранилище отчетов

CREATE SEQUENCE trash.reports_rep_id_seq INCREMENT 1 START 1;

CREATE TABLE trash.reports
(
    rep_id int DEFAULT nextval('trash.reports_rep_id_seq'::regclass) NOT NULL,
    rep_name text,
    data bytea, -- тело отчета
    start timestamp,
    finish timestamp, -- период
    type varchar, -- название периода, для простоты
    mime varchar, -- для простоты, тип отчета
    created timestamp DEFAULT CURRENT_TIMESTAMP -- Дата создания
);

