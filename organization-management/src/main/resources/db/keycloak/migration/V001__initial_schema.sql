create table if not exists organization_departments(
    id          uuid primary key,
    name        varchar(255) not null,
    organization_id uuid not null,
    deleted     boolean not null,
    created_at  timestamp without time zone default current_timestamp,
    updated_at  timestamp without time zone,
    version     int not null default 0
);

create table if not exists organization_positions(
    id              uuid primary key,
    name            varchar(255) not null,
    organization_id uuid not null,
    parent_id       uuid,
    deleted         boolean not null,
    created_at      timestamp without time zone default current_timestamp,
    updated_at      timestamp without time zone,
    version         int not null default 0,
    foreign key (parent_id) references organization_positions(id)
);

create unique index if not exists organization_positions_name_parent_id_key on organization_positions(name, parent_id)
    where not deleted;

create table if not exists organization_position_role_mapping(
    position_id uuid not null,
    role_id     varchar(36) not null,
    primary key (position_id, role_id),
    foreign key (position_id) references organization_positions(id),
    foreign key (role_id) references organization_role(id)
);

create table if not exists organization_employees(
    member_id       uuid not null,
    organization_id uuid not null,
    department_id   uuid not null,
    position_id     uuid not null,
    deleted         boolean not null,
    created_at      timestamp without time zone default current_timestamp,
    updated_at      timestamp without time zone,
    version         int not null default 0,
    primary key (member_id, department_id),
    foreign key (position_id) references organization_positions(id),
    foreign key (department_id) references organization_departments(id)
);
