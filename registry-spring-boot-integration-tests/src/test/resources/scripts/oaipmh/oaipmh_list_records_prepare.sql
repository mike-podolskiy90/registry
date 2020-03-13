INSERT INTO public.node (key, gbif_region, continent, title, country, created_by, modified_by, created, modified, deleted, fulltext_search, type, participation_status)
VALUES ('5c09dc92-68b0-40e0-aa8a-a98e9126eeea', 'EUROPE', 'EUROPE', 'The UK National Node 1', 'GB', 'WS TEST', 'WS TEST', '2020-02-22 09:54:09.835039', '2020-02-22 09:54:09.835039', null, '''countri'':5 ''europ'':7,8 ''gb'':9 ''nation'':3 ''node'':4 ''uk'':2 ''vote'':6', 'COUNTRY', 'VOTING');
INSERT INTO public.node (key, gbif_region, continent, title, country, created_by, modified_by, created, modified, deleted, fulltext_search, type, participation_status)
VALUES ('e4064af7-8656-4d10-8373-56f902c99ca1', 'EUROPE', 'EUROPE', 'The UK National Node 2', 'GB', 'WS TEST', 'WS TEST', '2020-02-22 09:54:09.835039', '2020-02-22 09:54:09.835039', null, '''countri'':5 ''europ'':7,8 ''gb'':9 ''nation'':3 ''node'':4 ''uk'':2 ''vote'':6', 'COUNTRY', 'VOTING');


INSERT INTO public.organization (key, endorsing_node_key, endorsement_approved, password, title, abbreviation, description, language, logo_url, city, province, country, postal_code, latitude, longitude, created_by, modified_by, created, modified, deleted, fulltext_search, email, phone, homepage, address, challenge_code_key)
VALUES ('353bebfa-801f-4586-9474-2c6a1ec32352', '5c09dc92-68b0-40e0-aa8a-a98e9126eeea', false, 'password', 'The BGBM Iceland', 'BGBM', 'The Berlin Botanical...', 'de', 'http://www.example.org', 'BERLIN', 'BERLIN', 'IS', '1408', null, null, 'WS TEST', 'WS TEST', '2020-03-06 18:56:21.565977', '2020-03-06 18:56:21.565977', null, '''1408'':15 ''2920202'':9 ''a@b.com'':8 ''berlin'':5,12,13,14 ''bgbm'':2,3 ''botan'':6 ''de'':7 ''www.example.org'':10,11', '{a@b.com}', '{2920202}', '{http://www.example.org}', '{Berliner}', null);
INSERT INTO public.organization (key, endorsing_node_key, endorsement_approved, password, title, abbreviation, description, language, logo_url, city, province, country, postal_code, latitude, longitude, created_by, modified_by, created, modified, deleted, fulltext_search, email, phone, homepage, address, challenge_code_key)
VALUES ('8bf7ae57-bbed-4186-aa22-640882198b67', 'e4064af7-8656-4d10-8373-56f902c99ca1', false, 'password', 'The BGBM New Zealand', 'BGBM', 'The Berlin Botanical...', 'de', 'http://www.example.org', 'BERLIN', 'BERLIN', 'NZ', '1408', null, null, 'WS TEST', 'WS TEST', '2020-03-06 18:56:23.507347', '2020-03-06 18:56:23.507347', null, '''1408'':16 ''2920202'':9 ''a@b.com'':8 ''berlin'':5,12,13,14 ''bgbm'':2,3 ''botan'':6 ''de'':7 ''nz'':15 ''www.example.org'':10,11', '{a@b.com}', '{2920202}', '{http://www.example.org}', '{Berliner}', null);


INSERT INTO public.installation (key, organization_key, type, title, description, created_by, modified_by, created, modified, deleted, fulltext_search, password, disabled)
VALUES ('4546be6b-2164-4b5f-be0d-7f7e334e6d02', '353bebfa-801f-4586-9474-2c6a1ec32352', 'IPT_INSTALLATION', 'The BGBM BIOCASE INSTALLATION 1', 'The Berlin Botanical...', 'WS TEST', 'WS TEST', '2020-03-06 18:56:21.634862', '2020-03-06 18:56:21.634862', null, '''berlin'':8 ''bgbm'':2 ''biocas'':3 ''botan'':9 ''instal'':4,6 ''ipt'':5', null, false);
INSERT INTO public.installation (key, organization_key, type, title, description, created_by, modified_by, created, modified, deleted, fulltext_search, password, disabled)
VALUES ('88768fae-f12a-4cf5-b666-1a433685fcea', '353bebfa-801f-4586-9474-2c6a1ec32352', 'IPT_INSTALLATION', 'The BGBM BIOCASE INSTALLATION 2', 'The Berlin Botanical...', 'WS TEST', 'WS TEST', '2020-03-06 18:56:23.368069', '2020-03-06 18:56:23.368069', null, '''berlin'':8 ''bgbm'':2 ''biocas'':3 ''botan'':9 ''instal'':4,6 ''ipt'':5', null, false);
INSERT INTO public.installation (key, organization_key, type, title, description, created_by, modified_by, created, modified, deleted, fulltext_search, password, disabled)
VALUES ('59f5ed3e-0f74-4a77-86ee-f2cd8e3f8a2f', '8bf7ae57-bbed-4186-aa22-640882198b67', 'IPT_INSTALLATION', 'The BGBM BIOCASE INSTALLATION 3', 'The Berlin Botanical...', 'WS TEST', 'WS TEST', '2020-03-06 18:56:23.549362', '2020-03-06 18:56:23.549362', null, '''berlin'':8 ''bgbm'':2 ''biocas'':3 ''botan'':9 ''instal'':4,6 ''ipt'':5', null, false);


INSERT INTO public.dataset (key, parent_dataset_key, duplicate_of_dataset_key, installation_key, publishing_organization_key, external, type, sub_type, title, alias, abbreviation, description, language, homepage, logo_url, citation, citation_identifier, rights, locked_for_auto_update, created_by, modified_by, created, modified, deleted, fulltext_search, doi, license, maintenance_update_frequency, version)
VALUES ('e367cb07-3c32-4d44-a3c7-8f1da93d3929', null, null, '4546be6b-2164-4b5f-be0d-7f7e334e6d02', '353bebfa-801f-4586-9474-2c6a1ec32352', false, 'CHECKLIST', null, 'Pontaurus needs more than 255 characters for it''s title. It is a very, very, very, very long title in the German language. Word by word and character by character it''s exact title is: "Vegetationskundliche Untersuchungen in der Hochgebirgsregion der Bolkar Daglari & Aladaglari, Türkei"', 'BGBM', 'BGBM', 'The Berlin Botanical...', 'da', 'http://www.example.org', 'http://www.example.org', 'This is a citation text', 'ABC', 'The rights', false, 'WS TEST', 'WS TEST', '2020-03-06 18:56:21.731929', '2015-10-15 22:00:00.000000', null, '''255'':5 ''aladaglari'':44 ''berlin'':50 ''bgbm'':47,48 ''bolkar'':42 ''botan'':51 ''charact'':6,28,30 ''checklist'':46 ''citat'':56 ''daglari'':43 ''der'':39,41 ''exact'':33 ''german'':22 ''hochgebirgsregion'':40 ''languag'':23 ''long'':18 ''need'':2 ''pontaurus'':1 ''text'':57 ''titl'':10,19,34 ''turkei'':45 ''untersuchungen'':37 ''vegetationskundlich'':36 ''word'':24,26 ''www.example.org'':52', '10.21373/gbif.2014.xsd123', 'CC_BY_NC_4_0', null, null);
INSERT INTO public.dataset (key, parent_dataset_key, duplicate_of_dataset_key, installation_key, publishing_organization_key, external, type, sub_type, title, alias, abbreviation, description, language, homepage, logo_url, citation, citation_identifier, rights, locked_for_auto_update, created_by, modified_by, created, modified, deleted, fulltext_search, doi, license, maintenance_update_frequency, version)
VALUES ('3c967323-db26-4223-9be3-ed744e45e1f9', null, null, '88768fae-f12a-4cf5-b666-1a433685fcea', '353bebfa-801f-4586-9474-2c6a1ec32352', false, 'OCCURRENCE', null, 'Pontaurus needs more than 255 characters for it''s title. It is a very, very, very, very long title in the German language. Word by word and character by character it''s exact title is: "Vegetationskundliche Untersuchungen in der Hochgebirgsregion der Bolkar Daglari & Aladaglari, Türkei"', 'BGBM', 'BGBM', 'The Berlin Botanical...', 'da', 'http://www.example.org', 'http://www.example.org', 'This is a citation text', 'ABC', 'The rights', false, 'WS TEST', 'WS TEST', '2020-03-06 18:56:23.411305', '2020-03-05 23:00:00.000000', null, '''255'':5 ''aladaglari'':44 ''berlin'':50 ''bgbm'':47,48 ''bolkar'':42 ''botan'':51 ''charact'':6,28,30 ''citat'':56 ''daglari'':43 ''der'':39,41 ''exact'':33 ''german'':22 ''hochgebirgsregion'':40 ''languag'':23 ''long'':18 ''need'':2 ''occurr'':46 ''pontaurus'':1 ''text'':57 ''titl'':10,19,34 ''turkei'':45 ''untersuchungen'':37 ''vegetationskundlich'':36 ''word'':24,26 ''www.example.org'':52', '10.21373/gbif.2014.xsd123', 'CC_BY_NC_4_0', null, null);
INSERT INTO public.dataset (key, parent_dataset_key, duplicate_of_dataset_key, installation_key, publishing_organization_key, external, type, sub_type, title, alias, abbreviation, description, language, homepage, logo_url, citation, citation_identifier, rights, locked_for_auto_update, created_by, modified_by, created, modified, deleted, fulltext_search, doi, license, maintenance_update_frequency, version)
VALUES ('503298df-448a-4b77-b29b-0497ff4778c7', null, null, '59f5ed3e-0f74-4a77-86ee-f2cd8e3f8a2f', '8bf7ae57-bbed-4186-aa22-640882198b67', false, 'CHECKLIST', null, 'Pontaurus needs more than 255 characters for it''s title. It is a very, very, very, very long title in the German language. Word by word and character by character it''s exact title is: "Vegetationskundliche Untersuchungen in der Hochgebirgsregion der Bolkar Daglari & Aladaglari, Türkei"', 'BGBM', 'BGBM', 'The Berlin Botanical...', 'da', 'http://www.example.org', 'http://www.example.org', 'This is a citation text', 'ABC', 'The rights', false, 'WS TEST', 'WS TEST', '2020-03-06 18:56:23.571007', '2020-03-05 23:00:00.000000', null, '''255'':5 ''aladaglari'':44 ''berlin'':50 ''bgbm'':47,48 ''bolkar'':42 ''botan'':51 ''charact'':6,28,30 ''checklist'':46 ''citat'':56 ''daglari'':43 ''der'':39,41 ''exact'':33 ''german'':22 ''hochgebirgsregion'':40 ''languag'':23 ''long'':18 ''need'':2 ''pontaurus'':1 ''text'':57 ''titl'':10,19,34 ''turkei'':45 ''untersuchungen'':37 ''vegetationskundlich'':36 ''word'':24,26 ''www.example.org'':52', '10.21373/gbif.2014.xsd123', 'CC_BY_NC_4_0', null, null);

