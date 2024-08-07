alter table department_region_binding
    alter column region_id type bigint using (0::bigint);

create table if not exists farm_regions(
    farm_id    uuid primary key,
    region_ids bigint[] not null default '{}'::bigint[]
);

create or replace view department_bindings as (
with preset as (
    select
           d.id as department_id,
           fa.organization_id,
           fa.farm_owner_organization_id,
           coalesce(fr.region_ids, '{}') as farm_region_ids,
           case
               when (d.global_binding is false) then null
               else fa.role_ids
               end
           as role_ids,
           case
               when (d.global_binding is true) then fa.farm_id
               when (d.global_binding is false) then null
               when (fb.farm_id is not null) then fb.farm_id
               when (rb.region_id is not null) then fa.farm_id
               end
           as farm_id,
           case
               when (d.global_binding is true) then fa.field_ids
               when (d.global_binding is false) then null
               when (fb.field_ids is not null and fa.field_ids is null) then fb.field_ids
               when (fb.field_ids is not null and fa.field_ids is not null)
                   then array_intersect(fa.field_ids, fb.field_ids)
               when (fb.department_id is not null) then fa.field_ids
               when (rb.department_id is not null) then fa.field_ids
               end
           as field_ids
    from farm_acl fa
             left join farm_regions fr on fr.farm_id = fa.farm_id
             join organization_departments d on d.organization_id = fa.organization_id
             left join department_farm_binding fb
                       on fb.department_id = d.id and fa.farm_id = fb.farm_id
             left join department_region_binding rb
                       on rb.department_id = rb.id and rb.region_id = ANY (fr.region_ids)
    where not fa.deleted and not d.deleted
)
select *
from preset
where farm_id is not null);
