create table if not exists organization_departments(
    id          uuid primary key,
    name        varchar(255) not null,
    organization_id uuid not null,
    deleted     boolean not null,
    created_at  timestamp without time zone default current_timestamp,
    updated_at  timestamp without time zone,
    version     int not null default 0
);

create unique index if not exists organization_departments_name_organization_key
    on organization_departments(organization_id, name) where not deleted;

create table if not exists organization_positions(
    id              uuid primary key,
    name            varchar(255) not null,
    organization_id uuid not null,
    parent_id       uuid,
    role_ids        int[] not null,
    deleted         boolean not null,
    created_at      timestamp without time zone default current_timestamp,
    updated_at      timestamp without time zone,
    version         int not null default 0,
    foreign key (parent_id) references organization_positions(id)
);

create unique index if not exists organization_positions_name_organization_key
    on organization_positions(organization_id, name, parent_id) where not deleted;

create table if not exists organization_employees(
    id              uuid not null primary key,
    member_id       uuid not null,
    user_id         uuid not null,
    organization_id uuid not null,
    department_id   uuid not null,
    position_id     uuid not null,
    deleted         boolean not null default false,
    created_at      timestamp without time zone default current_timestamp,
    updated_at      timestamp without time zone,
    version         int not null default 0,
    foreign key (position_id) references organization_positions(id),
    foreign key (department_id) references organization_departments(id)
);

create unique index if not exists organization_employees_member_department_key
    on organization_employees(member_id, department_id) where not deleted;
