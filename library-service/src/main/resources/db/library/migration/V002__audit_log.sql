alter table library_authors
    add column if not exists created_at timestamp without time zone not null default current_timestamp,
    add column if not exists updated_at timestamp without time zone not null default current_timestamp,
    add column if not exists deleted bool default false,
    add column if not exists version bigint not null default 0
;

alter table library_books
    add column if not exists created_at timestamp without time zone not null default current_timestamp,
    add column if not exists updated_at timestamp without time zone not null default current_timestamp,
    add column if not exists deleted bool default false,
    add column if not exists version bigint not null default 0
;
