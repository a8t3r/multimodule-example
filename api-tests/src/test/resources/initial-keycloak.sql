CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DELETE FROM public.user_entity WHERE email like '%@nowhere.com' or email like '%@noreply.phasetwo.io';
TRUNCATE TABLE public.organization cascade;
TRUNCATE TABLE public.organization_domain cascade;
TRUNCATE TABLE public.organization_member cascade;
TRUNCATE TABLE public.organization_role cascade;
TRUNCATE TABLE public.user_organization_role_mapping cascade;

INSERT INTO public.user_entity VALUES
    ('cb082bb6-d7f3-4052-b43a-fff7547dc3b4', 'admin@nowhere.com', '8cba2359-0c92-4148-b42a-81cdff0483dd', false, true, NULL, NULL, NULL, '75c23205-f1ea-4ce2-9f85-521bdd68e51d', 'organization-admin', 1705962214771, NULL, 0),
    ('edf5c94f-8da8-4cc8-97e6-f7625d199186', 'org-admin-70b3b9e8-1c63-4768-98f1-29ae087de907@noreply.phasetwo.io', 'org-admin-70b3b9e8-1c63-4768-98f1-29ae087de907@noreply.phasetwo.io', true, true, NULL, NULL, NULL, '75c23205-f1ea-4ce2-9f85-521bdd68e51d', 'org-admin-70b3b9e8-1c63-4768-98f1-29ae087de907', 1705962692068, NULL, 0),
    ('94c3c352-215e-47cb-b580-649c887b1fa3', 'org-admin-c55e2b54-a0f9-425b-bd2e-e64f9e441eb8@noreply.phasetwo.io', 'org-admin-c55e2b54-a0f9-425b-bd2e-e64f9e441eb8@noreply.phasetwo.io', true, true, NULL, NULL, NULL, '75c23205-f1ea-4ce2-9f85-521bdd68e51d', 'org-admin-c55e2b54-a0f9-425b-bd2e-e64f9e441eb8', 1706043395033, NULL, 0),
    ('84cad478-66db-40ba-ad1a-4be516ff14d0', 'org-admin-d11e0aee-be2c-413a-9001-0856430a8d71@noreply.phasetwo.io', 'org-admin-d11e0aee-be2c-413a-9001-0856430a8d71@noreply.phasetwo.io', true, true, NULL, NULL, NULL, '75c23205-f1ea-4ce2-9f85-521bdd68e51d', 'org-admin-d11e0aee-be2c-413a-9001-0856430a8d71', 1706043406609, NULL, 0),
    ('bd918ec6-7b7d-49a6-93fa-824191ba261f', 'developer1@nowhere.com', 'eda4b88c-b35e-4523-9856-2a129270025f', false, true, NULL, '', '', '75c23205-f1ea-4ce2-9f85-521bdd68e51d', 'developer1', 1706043425311, NULL, 0),
    ('fb51139c-2348-417a-b486-ae5bba6a34cf', 'developer2@nowhere.com', '4b7375ed-90fe-4950-9f10-568480c5bca6', false, true, NULL, '', '', '75c23205-f1ea-4ce2-9f85-521bdd68e51d', 'developer2', 1706043434282, NULL, 0);

INSERT INTO public.organization VALUES
	('70b3b9e8-1c63-4768-98f1-29ae087de907', 'Developers org', '75c23205-f1ea-4ce2-9f85-521bdd68e51d', 'cb082bb6-d7f3-4052-b43a-fff7547dc3b4', 'Developers LTD', ''),
	('c55e2b54-a0f9-425b-bd2e-e64f9e441eb8', 'First Organization', '75c23205-f1ea-4ce2-9f85-521bdd68e51d', 'cb082bb6-d7f3-4052-b43a-fff7547dc3b4', 'The One', ''),
	('d11e0aee-be2c-413a-9001-0856430a8d71', 'Second Organization', '75c23205-f1ea-4ce2-9f85-521bdd68e51d', 'cb082bb6-d7f3-4052-b43a-fff7547dc3b4', 'The two', '');

INSERT INTO public.organization_domain VALUES
	('70b3b9e8-1c63-4768-98f1-29ae087de907', 'dev.io', false, 'ed625ccd-a0d2-4f50-b3d3-898bd1a306a6'),
	('c55e2b54-a0f9-425b-bd2e-e64f9e441eb8', '', false, '1661a466-c2bb-4b39-8095-059b02b60fa4'),
	('d11e0aee-be2c-413a-9001-0856430a8d71', '', false, 'a12725ed-1e9c-4b13-be12-a36fae7f73e0');

INSERT INTO public.organization_member VALUES
    ('62d9de06-4240-4b6e-9ace-c4bafd996e80', '2024-01-22 22:31:32.079', 'cb082bb6-d7f3-4052-b43a-fff7547dc3b4', '70b3b9e8-1c63-4768-98f1-29ae087de907'),
    ('2cf705fa-5947-434e-9b0e-b17b637fc3f0', '2024-01-23 20:56:35.046', 'cb082bb6-d7f3-4052-b43a-fff7547dc3b4', 'c55e2b54-a0f9-425b-bd2e-e64f9e441eb8'),
    ('a2e97a20-33ad-4960-a1a0-730d4b2d1c90', '2024-01-23 20:56:46.623', 'cb082bb6-d7f3-4052-b43a-fff7547dc3b4', 'd11e0aee-be2c-413a-9001-0856430a8d71'),
	('62d9de06-4240-4b6e-9ace-c4bafd996e89', '2024-01-22 22:31:32.079', 'edf5c94f-8da8-4cc8-97e6-f7625d199186', '70b3b9e8-1c63-4768-98f1-29ae087de907'),
	('2cf705fa-5947-434e-9b0e-b17b637fc3f4', '2024-01-23 20:56:35.046', '94c3c352-215e-47cb-b580-649c887b1fa3', 'c55e2b54-a0f9-425b-bd2e-e64f9e441eb8'),
	('a2e97a20-33ad-4960-a1a0-730d4b2d1c96', '2024-01-23 20:56:46.623', '84cad478-66db-40ba-ad1a-4be516ff14d0', 'd11e0aee-be2c-413a-9001-0856430a8d71'),
	('7a297fb6-f9c6-434f-a983-a5fc0817a4f6', '2024-01-23 20:57:25.399', 'bd918ec6-7b7d-49a6-93fa-824191ba261f', 'c55e2b54-a0f9-425b-bd2e-e64f9e441eb8'),
	('5be659d4-bd9d-4667-95d2-3e13865ad611', '2024-01-23 20:58:10.155', 'fb51139c-2348-417a-b486-ae5bba6a34cf', 'd11e0aee-be2c-413a-9001-0856430a8d71'),
	('ecf4424f-e736-4b76-a208-cbdd8f4d999a', '2024-01-23 20:58:21.909', 'bd918ec6-7b7d-49a6-93fa-824191ba261f', '70b3b9e8-1c63-4768-98f1-29ae087de907'),
	('8c8765c7-5847-4adb-80c0-71d004eb78e6', '2024-01-23 20:58:21.909', 'fb51139c-2348-417a-b486-ae5bba6a34cf', '70b3b9e8-1c63-4768-98f1-29ae087de907');

INSERT INTO public.organization_role VALUES
	('bfeaa63a-5fe4-427e-9018-724026f6e654', '70b3b9e8-1c63-4768-98f1-29ae087de907', 'view-organization', NULL),
	('79657e86-a7f2-4fb0-ac10-d8adddc9dd60', '70b3b9e8-1c63-4768-98f1-29ae087de907', 'manage-organization', NULL),
	('d71f302d-a0ad-4d33-be4e-89bcb2678d48', '70b3b9e8-1c63-4768-98f1-29ae087de907', 'view-members', NULL),
	('9260756a-022c-479c-9b49-2899c8859580', '70b3b9e8-1c63-4768-98f1-29ae087de907', 'manage-members', NULL),
	('f3979612-8831-455b-8881-d263daa1626c', '70b3b9e8-1c63-4768-98f1-29ae087de907', 'view-roles', NULL),
	('490aa58e-e533-4649-92b1-25fb9334c12c', '70b3b9e8-1c63-4768-98f1-29ae087de907', 'manage-roles', NULL),
	('5d0b0ffd-7fee-452f-afbc-3e959e30b005', '70b3b9e8-1c63-4768-98f1-29ae087de907', 'view-invitations', NULL),
	('019dc068-2133-4c58-bd67-0ba696433871', '70b3b9e8-1c63-4768-98f1-29ae087de907', 'manage-invitations', NULL),
	('2590a0de-b76f-49b7-881f-0b08264d2d7c', '70b3b9e8-1c63-4768-98f1-29ae087de907', 'view-identity-providers', NULL),
	('49cf3521-bf56-4a6b-be3d-783d3aaa3f00', '70b3b9e8-1c63-4768-98f1-29ae087de907', 'manage-identity-providers', NULL),
	('fa32bc3d-22a2-46a7-a79d-567b780417a0', 'c55e2b54-a0f9-425b-bd2e-e64f9e441eb8', 'view-organization', NULL),
	('9a667a99-d1bf-4cb7-a02e-7052b9141fda', 'c55e2b54-a0f9-425b-bd2e-e64f9e441eb8', 'manage-organization', NULL),
	('8cee97ea-9825-4f5e-ba3e-ffe09452b325', 'c55e2b54-a0f9-425b-bd2e-e64f9e441eb8', 'view-members', NULL),
	('0275ea08-c7de-4ecc-a39f-303714c3f0fa', 'c55e2b54-a0f9-425b-bd2e-e64f9e441eb8', 'manage-members', NULL),
	('5406a22e-c3f3-4587-9990-15f1c557d16f', 'c55e2b54-a0f9-425b-bd2e-e64f9e441eb8', 'view-roles', NULL),
	('4004d383-bb51-4216-a874-8e37b20a548a', 'c55e2b54-a0f9-425b-bd2e-e64f9e441eb8', 'manage-roles', NULL),
	('d257f343-cdda-4ac2-b680-48fea1424167', 'c55e2b54-a0f9-425b-bd2e-e64f9e441eb8', 'view-invitations', NULL),
	('0fc427a8-112a-452d-9f4e-33051294dfc3', 'c55e2b54-a0f9-425b-bd2e-e64f9e441eb8', 'manage-invitations', NULL),
	('67da7365-8008-441e-b87c-2150738af6f9', 'c55e2b54-a0f9-425b-bd2e-e64f9e441eb8', 'view-identity-providers', NULL),
	('e42278eb-544b-4186-936c-47fce29a6588', 'c55e2b54-a0f9-425b-bd2e-e64f9e441eb8', 'manage-identity-providers', NULL),
	('fbd1a112-698f-4da1-8760-761f3e996bbe', 'd11e0aee-be2c-413a-9001-0856430a8d71', 'view-organization', NULL),
	('fb000590-e0b6-473a-bc9a-6b6920703c65', 'd11e0aee-be2c-413a-9001-0856430a8d71', 'manage-organization', NULL),
	('63e77cef-8c69-490e-8f02-9f0a0d09efc3', 'd11e0aee-be2c-413a-9001-0856430a8d71', 'view-members', NULL),
	('5263b61d-de44-47a5-b3c8-491c51fff114', 'd11e0aee-be2c-413a-9001-0856430a8d71', 'manage-members', NULL),
	('4e904245-9ebc-4334-87c8-5c1e37c5e483', 'd11e0aee-be2c-413a-9001-0856430a8d71', 'view-roles', NULL),
	('f327d48f-fa7e-401b-b12b-7746948a34e5', 'd11e0aee-be2c-413a-9001-0856430a8d71', 'manage-roles', NULL),
	('c74d4472-bb20-4683-b22a-bafe09070563', 'd11e0aee-be2c-413a-9001-0856430a8d71', 'view-invitations', NULL),
	('d4c0493f-e79a-4bf3-a624-937fbe542985', 'd11e0aee-be2c-413a-9001-0856430a8d71', 'manage-invitations', NULL),
	('961999c8-d916-4e42-86d3-e383267b5e98', 'd11e0aee-be2c-413a-9001-0856430a8d71', 'view-identity-providers', NULL),
	('3e06952e-66fa-4040-b952-7b3723570377', 'd11e0aee-be2c-413a-9001-0856430a8d71', 'manage-identity-providers', NULL);

-- admin has full access to all organizations
INSERT INTO public.user_organization_role_mapping
    SELECT id as role_id, uuid_generate_v4() as id, current_timestamp, 'cb082bb6-d7f3-4052-b43a-fff7547dc3b4' as user_id
    from public.organization_role
;
