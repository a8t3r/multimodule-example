create table if not exists organization_public_information(
    id                  uuid primary key,
    organization_id     uuid not null,
    created_by          uuid not null,
    deleted             boolean      not null default false,
    created_at          timestamp without time zone default current_timestamp,
    updated_at          timestamp without time zone,
    version             int          not null       default 0,
    name                text,
    inn                 text,
    kpp                 text,
    ogrn                text,
    address             text,
    location_lon        double precision,
    location_lat        double precision
);
