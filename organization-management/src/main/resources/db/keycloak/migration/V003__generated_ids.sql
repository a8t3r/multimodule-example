alter table organization add column if not exists uid uuid not null generated always as (id::uuid) stored;

alter table organization_domain
    add column if not exists uid uuid not null generated always as (id::uuid) stored,
    add column if not exists organization_uid uuid not null generated always as (organization_id::uuid) stored;

alter table user_entity add column if not exists uid uuid not null generated always as (id::uuid) stored;
alter table user_attribute add column if not exists user_uid uuid not null generated always as (user_id::uuid) stored;

alter table organization_member
    add column if not exists uid uuid not null generated always as (id::uuid) stored,
    add column if not exists user_uid uuid not null generated always as (user_id::uuid) stored,
    add column if not exists organization_uid uuid not null generated always as (organization_id::uuid) stored;