create table if not exists farm_acl(
    id                          uuid primary key,
    organization_id             uuid not null,
    farm_owner_organization_id   uuid not null,
    farm_id                     uuid not null,
    field_ids                   uuid[],
    role_ids                    int[] not null default '{}',
    deleted                     boolean not null,
    created_at                  timestamp without time zone default current_timestamp,
    updated_at                  timestamp without time zone,
    version                     int not null default 0
);

create unique index if not exists farm_acl_organization_farm_key
    on farm_acl(organization_id, farm_id) where not deleted;

alter table organization_departments add column if not exists global_binding boolean default null;

create table if not exists department_farm_binding(
    id              uuid not null primary key,
    department_id   uuid not null,
    farm_id         uuid not null,
    field_ids       uuid[],
    unique (department_id, farm_id)
);

create table if not exists department_region_binding(
    id              uuid not null primary key,
    department_id   uuid not null,
    region_id       uuid not null,
    unique (department_id, region_id)
);

create function array_intersect (anyarray, anyarray) returns anyarray as $$
    select array_agg(x) from unnest($1) x where x = any($2);
$$ language sql;