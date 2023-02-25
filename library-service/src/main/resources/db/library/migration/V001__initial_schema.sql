create table if not exists library_authors (
    id          uuid primary key,
    first_name  varchar(64) not null,
    last_name   varchar(64)
);

create table if not exists library_books (
    id          uuid primary key,
    name        varchar(64) not null,
    author_ids  uuid[] not null
);