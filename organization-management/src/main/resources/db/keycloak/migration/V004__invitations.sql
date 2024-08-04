alter table organization_employees alter column department_id drop not null;
alter table organization_employees add column created_by uuid;
update organization_employees set created_by = (select uid from user_entity where email like 'admin@%' limit 1) where true;
alter table organization_employees alter column created_by set not null;

create table if not exists organization_invitations(
    id              uuid primary key,
    email           varchar(255) not null,
    created_by      uuid         not null,
    organization_id uuid         not null,
    deleted         boolean      not null,
    created_at      timestamp without time zone default current_timestamp,
    updated_at      timestamp without time zone,
    version         int          not null       default 0,
    status          int          not null       default 0,
    user_id         uuid,
    department_id   uuid,
    position_id     uuid
);

create unique index if not exists invitations_organization_id_email_key
    on organization_invitations(organization_id, email) where not deleted;
