create table if not exists accession_requests(
    id                      uuid primary key,
    created_by              uuid not null,
    deleted                 boolean      not null default false,
    created_at              timestamp without time zone default current_timestamp,
    updated_at              timestamp without time zone,
    version                 int not null default 0,
    initiated_by_id         uuid not null,
    processed_by_id         uuid,
    vat                     text,
    organization_id         uuid,
    status                  int not null default 0,
    rejection_message       text
);
