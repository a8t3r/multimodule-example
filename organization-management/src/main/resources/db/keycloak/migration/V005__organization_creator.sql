alter table organization add column if not exists created_by uuid not null generated always as (created_by_user_id::uuid) stored;

alter table organization_positions add column created_by uuid;
update organization_positions set created_by = (select uid from user_entity where email like 'admin@%' limit 1) where true;
alter table organization_positions alter column created_by set not null;

alter table organization_departments add column created_by uuid;
update organization_departments set created_by = (select uid from user_entity where email like 'admin@%' limit 1) where true;
alter table organization_departments alter column created_by set not null;

alter table farm_acl add column created_by uuid;
update farm_acl set created_by = (select uid from user_entity where email like 'admin@%' limit 1) where true;
alter table farm_acl alter column created_by set not null;
