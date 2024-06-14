-- mocking for tests
create table if not exists planet_osm_rels (
    id      bigint primary key,
    members jsonb not null,
    tags    jsonb not null
);

update planet_osm_rels set members = members || '[{"ref": 3795586, "role": "subarea", "type": "R"}]' where id = 1059500;

create materialized view regions_tree as with recursive regions_tree(parent_id, id, name, depth, child_id, country) as (
    select null::bigint as parent_id, id, tags ->> 'name' as name, 0  as depth,
           (m ->> 'ref')::bigint as child_id, tags->>'ISO3166-1' as country
        from planet_osm_rels, jsonb_array_elements(members) m
        where (tags ->> 'admin_level')::int = 2 and tags->>'ISO3166-1' is not null and tags->>'boundary' = 'administrative'
    union all
    select r.id as parent_id, o.id, tags ->> 'name' as name, r.depth + 1 as depth,
           (m ->> 'ref')::bigint as child_id, r.country
        from planet_osm_rels o, regions_tree r, jsonb_array_elements(members) m
        where o.id = r.child_id and r.depth < 4
)
select distinct id, id as relation_id, name, country, parent_id, depth from regions_tree;